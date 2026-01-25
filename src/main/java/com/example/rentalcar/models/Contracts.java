package com.example.rentalcar.models;

import java.time.LocalDateTime;

public class Contracts {
    private int id_contract;
    private String code_contract;
    private LocalDateTime start_datetime;
    private LocalDateTime end_datetime;
    private LocalDateTime return_datetime;
    private int km_start;
    private int km_end;
    private int fuel_start;
    private int fuel_end;
    private DepositType deposit_type;
    private double deposit_amount;
    private double base_price;
    private double discount_amount;
    private double total_price;
    private PaymentStatus payment_status;
    private StatusContracts status;
    private Users id_user;
    private Vehicles id_vehicle;
    private Customers id_customer;
    private Vouchers id_voucher;

    public Contracts() {};

    public Contracts(int id_contract, String code_contract, LocalDateTime start_datetime, LocalDateTime end_datetime, LocalDateTime return_datetime,
                     int km_start, int km_end, int fuel_start, int fuel_end, DepositType deposit_type, double deposit_amount, double base_price,
                     double discount_amount, double total_price, PaymentStatus payment_status, StatusContracts status, Users id_user, Vehicles id_vehicle, Customers id_customer, Vouchers id_voucher) {
        this.id_contract = id_contract;
        this.code_contract = code_contract;
        this.start_datetime = start_datetime;
        this.end_datetime = end_datetime;
        this.return_datetime = return_datetime;
        this.km_start = km_start;
        this.km_end = km_end;
        this.fuel_start = fuel_start;
        this.fuel_end = fuel_end;
        this.deposit_type = deposit_type;
        this.deposit_amount = deposit_amount;
        this.base_price = base_price;
        this.discount_amount = discount_amount;
        this.total_price = total_price;
        this.payment_status = payment_status;
        this.status = status;
        this.id_user = id_user;
        this.id_vehicle = id_vehicle;
        this.id_customer = id_customer;
        this.id_voucher = id_voucher;
    }

    public int getId_contract() {
        return id_contract;
    }

    public void setId_contract(int id_contract) {
        this.id_contract = id_contract;
    }

    public String getCode_contract() {
        return code_contract;
    }

    public void setCode_contract(String code_contract) {
        this.code_contract = code_contract;
    }

    public LocalDateTime getStart_datetime() {
        return start_datetime;
    }

    public void setStart_datetime(LocalDateTime start_datetime) {
        this.start_datetime = start_datetime;
    }

    public LocalDateTime getEnd_datetime() {
        return end_datetime;
    }

    public void setEnd_datetime(LocalDateTime end_datetime) {
        this.end_datetime = end_datetime;
    }

    public LocalDateTime getReturn_datetime() {
        return return_datetime;
    }

    public void setReturn_datetime(LocalDateTime return_datetime) {
        this.return_datetime = return_datetime;
    }

    public int getKm_start() {
        return km_start;
    }

    public void setKm_start(int km_start) {
        this.km_start = km_start;
    }

    public int getKm_end() {
        return km_end;
    }

    public void setKm_end(int km_end) {
        this.km_end = km_end;
    }

    public int getFuel_start() {
        return fuel_start;
    }

    public void setFuel_start(int fuel_start) {
        this.fuel_start = fuel_start;
    }

    public int getFuel_end() {
        return fuel_end;
    }

    public void setFuel_end(int fuel_end) {
        this.fuel_end = fuel_end;
    }

    public DepositType getDeposit_type() {
        return deposit_type;
    }

    public void setDeposit_type(DepositType deposit_type) {
        this.deposit_type = deposit_type;
    }

    public double getDeposit_amount() {
        return deposit_amount;
    }

    public void setDeposit_amount(double deposit_amount) {
        this.deposit_amount = deposit_amount;
    }

    public double getBase_price() {
        return base_price;
    }

    public void setBase_price(double base_price) {
        this.base_price = base_price;
    }

    public double getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(double discount_amount) {
        this.discount_amount = discount_amount;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }

    public PaymentStatus getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(PaymentStatus payment_status) {
        this.payment_status = payment_status;
    }

    public StatusContracts getStatus() {
        return status;
    }

    public void setStatus(StatusContracts status) {
        this.status = status;
    }

    public Users getId_user() {
        return id_user;
    }

    public void setId_user(Users id_user) {
        this.id_user = id_user;
    }

    public Vehicles getId_vehicle() {
        return id_vehicle;
    }

    public void setId_vehicle(Vehicles id_vehicle) {
        this.id_vehicle = id_vehicle;
    }

    public Customers getId_customer() {
        return id_customer;
    }

    public void setId_customer(Customers id_customer) {
        this.id_customer = id_customer;
    }

    public Vouchers getId_voucher() {
        return id_voucher;
    }

    public void setId_voucher(Vouchers id_voucher) {
        this.id_voucher = id_voucher;
    }
}
