package com.plant.weighing.dto;

public class WeightReading {
    private double weight;
    private String unit;
    private boolean stable; // whether the scale reading has settled

    public WeightReading() {}

    public WeightReading(double weight, String unit, boolean stable) {
        this.weight = weight;
        this.unit = unit;
        this.stable = stable;
    }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isStable() { return stable; }
    public void setStable(boolean stable) { this.stable = stable; }
}
