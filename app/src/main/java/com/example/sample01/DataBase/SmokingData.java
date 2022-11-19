package com.example.sample01.DataBase;

public class SmokingData {
    private String name;
    private String address;
    private double X;
    private double Y;

    public void SmokingData(String name, String address,double X, double Y){
        this.name = name;
        this.address = address;
        this.X = X;
        this.Y = Y;
    }

    public String getName(){
        return this.name;
    }

    public String getAddress(){
        return this.address;
    }

    public double getX(){return this.X;}

    public double getY(){return this.Y;}



}
