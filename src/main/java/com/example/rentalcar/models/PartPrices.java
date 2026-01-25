package com.example.rentalcar.models;

public class PartPrices {
    private int id_part_price;
    private String part_name;
    private String vehicle;
    private double price;

    public PartPrices() {};

    public PartPrices(int id_part_price, String part_name, String vehicle, double price) {
        this.id_part_price = id_part_price;
        this.part_name = part_name;
        this.vehicle = vehicle;
        this.price = price;
    }

    public int getId_part_price() {
        return id_part_price;
    }

    public void setId_part_price(int id_part_price) {
        this.id_part_price = id_part_price;
    }

    public String getPart_name() {
        return part_name;
    }

    public void setPart_name(String part_name) {
        this.part_name = part_name;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
