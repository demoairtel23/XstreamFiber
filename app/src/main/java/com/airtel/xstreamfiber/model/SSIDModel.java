package com.airtel.xstreamfiber.model;

public class SSIDModel {

    private  String SSID;
    private String Instance;
    private String active;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getInstance() {
        return Instance;
    }

    public void setInstance(String instance) {
        Instance = instance;
    }
}
