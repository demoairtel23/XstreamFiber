package com.airtel.xstreamfiber.model;

import android.os.Parcel;
import android.os.Parcelable;


public class UnblockedDevice implements Parcelable {

    private String signalStrength, active, hostName, interfaceType, frequencyBand, macAddress;

    public UnblockedDevice(String signalStrength, String active, String hostName, String interfaceType, String macAddress) {
        this.signalStrength = signalStrength;
        this.active = active;
        this.hostName = hostName;
        this.interfaceType = interfaceType;
      //  this.frequencyBand = frequencyBand;
        this.macAddress = macAddress;
    }

    protected UnblockedDevice(Parcel in) {
        signalStrength = in.readString();
        active = in.readString();
        hostName = in.readString();
        interfaceType = in.readString();
        frequencyBand = in.readString();
        macAddress = in.readString();
    }

    public static final Creator<UnblockedDevice> CREATOR = new Creator<UnblockedDevice>() {
        @Override
        public UnblockedDevice createFromParcel(Parcel in) {
            return new UnblockedDevice(in);
        }

        @Override
        public UnblockedDevice[] newArray(int size) {
            return new UnblockedDevice[size];
        }
    };

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getFrequencyBand() {
        return frequencyBand;
    }

    public void setFrequencyBand(String frequencyBand) {
        this.frequencyBand = frequencyBand;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(signalStrength);
        dest.writeString(active);
        dest.writeString(hostName);
        dest.writeString(interfaceType);
        dest.writeString(frequencyBand);
        dest.writeString(macAddress);
    }
}
