package com.example.rentalcar.models;


public class Vehicles {
    private int id_vehicle;
    private String code_vehicle;
    private String brand;
    private String model;
    private String color;
    private int year_of_manufacture;
    private double price_day;
    private double price_hour;
    private int fuel_capacity;
    private int current_km;
    private StatusVehicle status;
    private double total_price;

    public Vehicles() {};

    public Vehicles(int id_vehicle, String code_vehicle, String brand, String model, String color, int year_of_manufacture, double price_day, double price_hour, int fuel_capacity, int current_km, StatusVehicle status, double total_price) {
        this.id_vehicle = id_vehicle;
        this.code_vehicle = code_vehicle;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.year_of_manufacture = year_of_manufacture;
        this.price_day = price_day;
        this.price_hour = price_hour;
        this.fuel_capacity = fuel_capacity;
        this.current_km = current_km;
        this.status = status;
        this.total_price = total_price;
    }

    public int getId_vehicle() {
        return id_vehicle;
    }

    public void setId_vehicle(int id_vehicle) {
        this.id_vehicle = id_vehicle;
    }

    public String getCode_vehicle() {
        return code_vehicle;
    }

    public void setCode_vehicle(String code_vehicle) {
        this.code_vehicle = code_vehicle;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYear_of_manufacture() {
        return year_of_manufacture;
    }

    public void setYear_of_manufacture(int year_of_manufacture) {
        this.year_of_manufacture = year_of_manufacture;
    }

    public double getPrice_day() {
        return price_day;
    }

    public void setPrice_day(double price_day) {
        this.price_day = price_day;
    }

    public double getPrice_hour() {
        return price_hour;
    }

    public void setPrice_hour(double price_hour) {
        this.price_hour = price_hour;
    }

    public int getFuel_capacity() {
        return fuel_capacity;
    }

    public void setFuel_capacity(int fuel_capacity) {
        this.fuel_capacity = fuel_capacity;
    }

    public int getCurrent_km() {
        return current_km;
    }

    public void setCurrent_km(int current_km) {
        this.current_km = current_km;
    }

    public StatusVehicle getStatus() {
        return status;
    }

    public void setStatus(StatusVehicle status) {
        this.status = status;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }
}
