package com.plant.weighing.service;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.plant.weighing.config.ScaleProperties;
import com.plant.weighing.dto.WeightReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Reads weight from a scale indicator/PLC that exposes it over Modbus TCP as
 * a holding register. Adjust the register address/scaling factor to match
 * your indicator's Modbus map (check its Modbus documentation — some report
 * raw integer counts that need a scale factor, e.g. divide by 100 or 1000
 * to get kg).
 *
 * Activate with: scale.mode=modbus
 */
@Service
@ConditionalOnProperty(name = "scale.mode", havingValue = "modbus")
public class ModbusScaleReaderService implements ScaleReaderService {

    private static final Logger log = LoggerFactory.getLogger(ModbusScaleReaderService.class);

    private final ScaleProperties props;

    public ModbusScaleReaderService(ScaleProperties props) {
        this.props = props;
    }

    @Override
    public WeightReading readCurrentWeight() {
        ModbusTCPMaster master = new ModbusTCPMaster(props.getModbusHost(), props.getModbusPort());
        try {
            master.connect();
            // Reads a single holding register; adjust count/address/scaling for your device.
            Register[] registers = master.readMultipleRegisters(props.getModbusRegister(), 1);
            double rawValue = registers[0].getValue();

            // TODO adjust this scale factor to match your indicator's Modbus map
            double scaleFactor = 100.0;
            double actualWeight = rawValue / scaleFactor;

            double net = Math.max(0, actualWeight - props.getTareWeight());
            return new WeightReading(Math.round(net * 100.0) / 100.0, props.getUnit(), true);

        } catch (Exception e) {
            log.error("Error reading from Modbus scale at {}:{}", props.getModbusHost(), props.getModbusPort(), e);
            throw new IllegalStateException("Failed to read Modbus scale: " + e.getMessage(), e);
        } finally {
            master.disconnect();
        }
    }
}
