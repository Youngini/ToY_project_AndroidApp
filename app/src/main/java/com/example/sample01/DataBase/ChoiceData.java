package com.example.sample01.DataBase;

public class ChoiceData {
    private int id;
    private String address;
    private String reason;
    private double latitude;
    private double longitude;


    public ChoiceData (String address, double latitude, double longitude,String reason){
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reason = reason;
    }

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }

    public void setAddress(String address){
        this.address = address;
    }
    public String getAddress(){
        return this.address;
    }

    public void setReason(String reason){
        this.reason = reason;
    }
    public String getReason(){
        return this.reason;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public double getLatitude(){
        return this.latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public double getLongitude() {
        return this.longitude;
    }
}
