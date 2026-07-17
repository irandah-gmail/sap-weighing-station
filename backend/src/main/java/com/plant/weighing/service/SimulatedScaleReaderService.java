package com.plant.weighing.service;

import com.plant.weighing.config.ScaleProperties;
import com.plant.weighing.dto.WeightReading;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates plausible weight readings so the full flow (UI -> transaction ->
 * SAP call) can be tested before the physical scale is wired up.
 * Activate with: scale.mode=simulated (this is also the default).
 */
@Service
@ConditionalOnProperty(name = "scale.mode", havingValue = "simulated", matchIfMissing = true)
public class SimulatedScaleReaderService implements ScaleReaderService {

    private final ScaleProperties props;

    public SimulatedScaleReaderService(ScaleProperties props) {
        this.props = props;
    }

    @Override
    public WeightReading readCurrentWeight() {
        double simulated = ThreadLocalRandom.current().nextDouble(45.0, 120.0);
        double net = Math.max(0, simulated - props.getTareWeight());
        return new WeightReading(Math.round(net * 100.0) / 100.0, props.getUnit(), true);
    }
}
