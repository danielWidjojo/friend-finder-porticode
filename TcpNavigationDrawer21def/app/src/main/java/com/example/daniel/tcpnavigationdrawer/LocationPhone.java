package com.example.daniel.tcpnavigationdrawer;

/**
 * Created by Daniel on 10/28/2017.
 */

public class LocationPhone {
    String deviceId;
    String latitude;
    String longtitude;
    public LocationPhone(){

    }

    public LocationPhone(String deviceId, String latitude, String longtitude) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getdeviceId() {
        return deviceId;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }
}
