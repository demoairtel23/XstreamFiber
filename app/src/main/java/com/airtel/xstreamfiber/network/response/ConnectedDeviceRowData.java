package com.airtel.xstreamfiber.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class ConnectedDeviceRowData implements Parcelable {

    /**
     * SignalStrength : Not Available
     * Active : 0
     * HostName : LTB0091151-02-Ethernet
     * InterfaceType : Ethernet
     * frequencyBand : 2.4GHz
     * MACAddress : C8:5B:76:C6:B2:62
     */

    private String SignalStrength;
    private String Active;
    private String HostName;
    private String InterfaceType;
    private String frequencyBand;
    private String MACAddress;

    public String getSignalStrength() {
        return SignalStrength;
    }

    public void setSignalStrength(String SignalStrength) {
        this.SignalStrength = SignalStrength;
    }

    public String getActive() {
        return Active;
    }

    public void setActive(String Active) {
        this.Active = Active;
    }

    public String getHostName() {
        return HostName;
    }

    public void setHostName(String HostName) {
        this.HostName = HostName;
    }

    public String getInterfaceType() {
        return InterfaceType;
    }

    public void setInterfaceType(String InterfaceType) {
        this.InterfaceType = InterfaceType;
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
        dest.writeString(this.SignalStrength);
        dest.writeString(this.Active);
        dest.writeString(this.HostName);
        dest.writeString(this.InterfaceType);
        dest.writeString(this.frequencyBand);
        dest.writeString(this.MACAddress);
    }

    public ConnectedDeviceRowData() {
    }

    protected ConnectedDeviceRowData(Parcel in) {
        this.SignalStrength = in.readString();
        this.Active = in.readString();
        this.HostName = in.readString();
        this.InterfaceType = in.readString();
        this.frequencyBand = in.readString();
        this.MACAddress = in.readString();
    }

    public static final Parcelable.Creator<ConnectedDeviceRowData> CREATOR = new Parcelable.Creator<ConnectedDeviceRowData>() {
        @Override
        public ConnectedDeviceRowData createFromParcel(Parcel source) {
            return new ConnectedDeviceRowData(source);
        }

        @Override
        public ConnectedDeviceRowData[] newArray(int size) {
            return new ConnectedDeviceRowData[size];
        }
    };
}
