package com.plant.weighing.service;

import com.fazecast.jSerialComm.SerialPort;
import com.plant.weighing.config.ScaleProperties;
import com.plant.weighing.dto.WeightReading;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads weight from a scale indicator connected via RS-232/USB-serial.
 *
 * NOTE: The exact string format a scale indicator sends varies by
 * manufacturer (e.g. "ST,GS,+00012.34,kg" for many Mettler Toledo/CAS/A&D
 * style protocols, others differ). Adjust WEIGHT_PATTERN and the parsing
 * logic below to match your scale's actual output format — check the
 * indicator's communication manual.
 *
 * Activate with: scale.mode=serial
 */
@Service
@ConditionalOnProperty(name = "scale.mode", havingValue = "serial")
public class SerialScaleReaderService implements ScaleReaderService {

    private static final Logger log = LoggerFactory.getLogger(SerialScaleReaderService.class);

    // Matches a signed decimal number in the scale's output line, e.g. "+00012.34"
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("([-+]?\\d+\\.?\\d*)");

    private final ScaleProperties props;
    private SerialPort serialPort;

    public SerialScaleReaderService(ScaleProperties props) {
        this.props = props;
    }

    private SerialPort getOrOpenPort() {
        if (serialPort != null && serialPort.isOpen()) {
            return serialPort;
        }
        serialPort = SerialPort.getCommPort(props.getComPort());
        serialPort.setComPortParameters(props.getBaudRate(), 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);
        if (!serialPort.openPort()) {
            throw new IllegalStateException("Unable to open serial port " + props.getComPort());
        }
        return serialPort;
    }

    @Override
    public WeightReading readCurrentWeight() {
        SerialPort port = getOrOpenPort();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(port.getInputStream(), StandardCharsets.US_ASCII))) {

            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                throw new IllegalStateException("No data received from scale on " + props.getComPort());
            }

            Matcher matcher = WEIGHT_PATTERN.matcher(line);
            if (!matcher.find()) {
                throw new IllegalStateException("Could not parse weight from scale output: '" + line + "'");
            }

            double raw = Double.parseDouble(matcher.group(1));
            double net = Math.max(0, raw - props.getTareWeight());
            boolean stable = !line.toUpperCase().contains("US"); // many protocols send "US" for unstable

            return new WeightReading(Math.round(net * 100.0) / 100.0, props.getUnit(), stable);

        } catch (IOException e) {
            log.error("Error reading from scale on {}", props.getComPort(), e);
            throw new IllegalStateException("Failed to read scale: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void closePort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }
}
