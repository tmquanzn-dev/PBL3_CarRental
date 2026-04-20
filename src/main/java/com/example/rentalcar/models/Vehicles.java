package com.example.rentalcar.models;

import com.example.rentalcar.models.StatusVehicle;

public class Vehicles {
    private int id_vehicle;
    private String code_vehicle; // Biển số xe
    private String brand;
    private String model;
    private String color;
    private int year_of_manufacture;
    private double price_day;
    private double price_hour;
    private int fuel_capacity;
    private int current_km;

    private String vehicle_type;  // Tay ga, Xe số, Xe côn (Dùng để map giá phụ tùng)
    private int maintenance_km;   // Mốc km cần bảo dưỡng
    private String image_url;     // Đường dẫn ảnh hiển thị trên Card

    private StatusVehicle status;
    private double purchase_price; // Giá trị xe

    public Vehicles() {}

    public Vehicles(int id_vehicle, String code_vehicle, String brand, String model, String vehicle_type,
                    String color, int year_of_manufacture, double price_day, double price_hour,
                    int fuel_capacity, int current_km, int maintenance_km, String image_url,
                    StatusVehicle status, double purchase_price) {
        this.id_vehicle = id_vehicle;
        this.code_vehicle = code_vehicle;
        this.brand = brand;
        this.model = model;
        this.vehicle_type = vehicle_type;
        this.color = color;
        this.year_of_manufacture = year_of_manufacture;
        this.price_day = price_day;
        this.price_hour = price_hour;
        this.fuel_capacity = fuel_capacity;
        this.current_km = current_km;
        this.maintenance_km = maintenance_km;
        this.image_url = image_url;
        this.status = status;
        this.purchase_price = purchase_price;
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

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public int getMaintenance_km() {
        return maintenance_km;
    }

    public void setMaintenance_km(int maintenance_km) {
        this.maintenance_km = maintenance_km;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public StatusVehicle getStatus() {
        return status;
    }

    public void setStatus(StatusVehicle status) {
        this.status = status;
    }

    public double getPurchase_price() {
        return purchase_price;
    }

    public void setPurchase_price(double total_price) {
        this.purchase_price = total_price;
    }
}