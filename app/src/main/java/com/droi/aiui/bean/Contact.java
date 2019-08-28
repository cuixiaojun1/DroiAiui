package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 17-12-22.
 */

public class Contact {
    private String name;
    private String phoneNumber;
    private String carrier;
    private String location;

    public Contact() {
    }

    public Contact(String name,String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public Contact(String name, String phoneNumber, String carrier, String location) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.carrier = carrier;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", carrier='" + carrier + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}