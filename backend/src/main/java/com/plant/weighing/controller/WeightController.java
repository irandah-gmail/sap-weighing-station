package com.plant.weighing.controller;

import com.plant.weighing.dto.WeightReading;
import com.plant.weighing.service.ScaleReaderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeightController {

    private final ScaleReaderService scaleReaderService;

    public WeightController(ScaleReaderService scaleReaderService) {
        this.scaleReaderService = scaleReaderService;
    }

    /** Angular polls this to show a live weight reading to the operator. */
    @GetMapping("/api/weight")
    public WeightReading getCurrentWeight() {
        return scaleReaderService.readCurrentWeight();
    }
}
