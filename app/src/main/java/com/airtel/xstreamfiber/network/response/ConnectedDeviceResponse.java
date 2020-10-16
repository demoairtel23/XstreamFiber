package com.airtel.xstreamfiber.network.response;


import com.google.gson.JsonObject;

public class ConnectedDeviceResponse {


    /**
     * Message : success
     * StatusCode : 200
     * data : [{"SignalStrength":"Not Available","Active":"0","HostName":"LTB0091151-02-Ethernet","InterfaceType":"Ethernet","frequencyBand":"2.4GHz","MACAddress":"C8:5B:76:C6:B2:62","state":"Active"},{"SignalStrength":"Not Available","Active":"0","HostName":"RedmiNote4-Redmi-Wireless","InterfaceType":"Wireless","frequencyBand":"2.4GHz","MACAddress":"C4:0B:CB:5B:47:85","state":"Active"}]
     */

    private String Message;
    private int StatusCode;
    private JsonObject data;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String Message) {
        this.Message = Message;
    }

    public int getStatusCode() {
        return StatusCode;
    }

    public void setStatusCode(int StatusCode) {
        this.StatusCode = StatusCode;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }





}
