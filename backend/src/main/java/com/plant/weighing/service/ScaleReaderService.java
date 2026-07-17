package com.plant.weighing.service;

import com.plant.weighing.dto.WeightReading;

public interface ScaleReaderService {
    WeightReading readCurrentWeight();
}
