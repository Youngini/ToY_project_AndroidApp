package com.example.sample01.DataBase;

public class KoreaGPSData {
    private String big;
    private String middle;
    private String small;
    private double x;
    private double y;

    public void KoreaGPSData(String big, String middle,String small,double x, double y){
        this.big = big;
        this.middle = middle;
        this.small = small;
        this.x = x;
        this.y = y;
    }



    public String getBig(){return this.big;}
    public String getMiddle(){return this.middle;}
    public String getSmall(){return this.small;}
    public double getX(){return this.x;}
    public double getY(){return this.y;}


}
