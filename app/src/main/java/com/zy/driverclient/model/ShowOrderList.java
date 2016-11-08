package com.zy.driverclient.model;



/**
 * Created by blurryFace on 2016/8/20.
 */
public class ShowOrderList {
    private String address;
    private String startDate;
    private String startTime;
    private String phone;
    private String state;
    private int id;

    @Override
    public String toString() {
        return "Order{" +
                "address='" + address + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}