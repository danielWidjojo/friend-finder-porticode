package com.example.daniel.tcpnavigationdrawer;

/**
 * Created by Daniel on 10/28/2017.
 */

public class LocationMeetingPoint {
    String deviceId;
    Boolean set;
    String latitude;
    String longtitude;
    public LocationMeetingPoint(){

    }

    public LocationMeetingPoint(String deviceId, Boolean set,String latitude, String longtitude) {
        this.deviceId = deviceId;
        this.set=set;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getdeviceId() {
        return deviceId;
    }
    public Boolean getSet() {
        return set;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }
}
