package com.example.sample01.DataBase;

public class SmokingData {
    private String name;
    private String address;
    private double X;
    private double Y;
    private double dist;

    public void SmokingData(String name, String address,double X, double Y,double dist){
        this.name = name;
        this.address = address;
        this.X = X;
        this.Y = Y;
        this.dist = dist;
    }

    public String getName(){
        return this.name;
    }

    public String getAddress(){
        return this.address;
    }

    public double getX(){return this.X;}

    public double getY(){return this.Y;}

    public double getDist(){return this.dist;}



}
