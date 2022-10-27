package com.test.usb.bean;



public class DeviceInfo {
    public int VID;
    public int PID;
    public String DeviceName;
    public String ProductName;

    public DeviceInfo() {
    }

    public int getVID() {
        return VID;
    }

    public void setVID(int VID) {
        this.VID = VID;
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public void setProductName(String ProductName) {
        this.ProductName = ProductName;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setDeviceName(String DeviceName) {
        this.DeviceName = DeviceName;
    }

    public String getDeviceName() {
        return DeviceName;
    }
}
