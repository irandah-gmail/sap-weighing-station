package com.plant.weighing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bound from the `scale:` section of application.yml.
 * mode: "simulated" | "serial" | "modbus"
 */
@Component
@ConfigurationProperties(prefix = "scale")
public class ScaleProperties {

    private String mode = "simulated";

    // Serial settings
    private String comPort = "COM3";
    private int baudRate = 9600;

    // Modbus settings
    private String modbusHost = "192.168.1.50";
    private int modbusPort = 502;
    private int modbusRegister = 0;

    // Weight parsing
    private double tareWeight = 0.0;
    private String unit = "KG";

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getComPort() { return comPort; }
    public void setComPort(String comPort) { this.comPort = comPort; }

    public int getBaudRate() { return baudRate; }
    public void setBaudRate(int baudRate) { this.baudRate = baudRate; }

    public String getModbusHost() { return modbusHost; }
    public void setModbusHost(String modbusHost) { this.modbusHost = modbusHost; }

    public int getModbusPort() { return modbusPort; }
    public void setModbusPort(int modbusPort) { this.modbusPort = modbusPort; }

    public int getModbusRegister() { return modbusRegister; }
    public void setModbusRegister(int modbusRegister) { this.modbusRegister = modbusRegister; }

    public double getTareWeight() { return tareWeight; }
    public void setTareWeight(double tareWeight) { this.tareWeight = tareWeight; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
