package com.plant.weighing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class WeighingSubmitRequest {

    @NotBlank(message = "Production Order ID is required")
    private String productionOrderId;

    @NotBlank(message = "Batch ID is required")
    private String batchId;

    @NotBlank(message = "User ID is required")
    private String userId;

    /** One or more individual weighments captured for this batch (e.g. multiple
     *  containers/pallets). The backend sums these into the total. If the
     *  operator only takes one reading, send a list with a single value. */
    @NotEmpty(message = "At least one weight reading is required")
    private List<Double> weightReadings;

    private String unit = "KG";

    public String getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(String productionOrderId) { this.productionOrderId = productionOrderId; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Double> getWeightReadings() { return weightReadings; }
    public void setWeightReadings(List<Double> weightReadings) { this.weightReadings = weightReadings; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
