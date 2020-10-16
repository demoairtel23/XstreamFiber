package com.airtel.xstreamfiber.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectedDeviceData implements Parcelable {

    private String SignalStrength;
    private String Active;
    private String HostName;
    private String InterfaceType;
    private String frequencyBand;
    private String MACAddress;

    public ConnectedDeviceData(String signalStrength, String active, String hostName, String interfaceType, String frequencyBand, String MACAddress) {
        SignalStrength = signalStrength;
        Active = active;
        HostName = hostName;
        InterfaceType = interfaceType;
        this.frequencyBand = frequencyBand;
        this.MACAddress = MACAddress;
    }

    protected ConnectedDeviceData(Parcel in) {
        SignalStrength = in.readString();
        Active = in.readString();
        HostName = in.readString();
        InterfaceType = in.readString();
        frequencyBand = in.readString();
        MACAddress = in.readString();
    }

    public static final Creator<ConnectedDeviceData> CREATOR = new Creator<ConnectedDeviceData>() {
        @Override
        public ConnectedDeviceData createFromParcel(Parcel in) {
            return new ConnectedDeviceData(in);
        }

        @Override
        public ConnectedDeviceData[] newArray(int size) {
            return new ConnectedDeviceData[size];
        }
    };

    public String getSignalStrength() {
        return SignalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        SignalStrength = signalStrength;
    }

    public String getActive() {
        return Active;
    }

    public void setActive(String active) {
        Active = active;
    }

    public String getHostName() {
        return HostName;
    }

    public void setHostName(String hostName) {
        HostName = hostName;
    }

    public String getInterfaceType() {
        return InterfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        InterfaceType = interfaceType;
    }

    public String getFrequencyBand() {
        return frequencyBand;
    }

    public void setFrequencyBand(String frequencyBand) {
        this.frequencyBand = frequencyBand;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SignalStrength);
        dest.writeString(Active);
        dest.writeString(HostName);
        dest.writeString(InterfaceType);
        dest.writeString(frequencyBand);
        dest.writeString(MACAddress);
    }
}
