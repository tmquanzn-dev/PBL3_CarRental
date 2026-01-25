package com.example.rentalcar.models;

public class Payments {
    private int id_payment;
    private double amount;
    private PaymentType payment_type;
    private PaymentMethod payment_method;
    private Users id_user;
    private Contracts id_contract;

    public Payments() {};

    public Payments(int id_payment, double amount, PaymentType payment_type,
                    PaymentMethod payment_method, Users id_user, Contracts id_contract) {
        this.id_payment = id_payment;
        this.amount = amount;
        this.payment_type = payment_type;
        this.payment_method = payment_method;
        this.id_user = id_user;
        this.id_contract = id_contract;
    }

    public int getId_payment() {
        return id_payment;
    }

    public void setId_payment(int id_payment) {
        this.id_payment = id_payment;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentType getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(PaymentType payment_type) {
        this.payment_type = payment_type;
    }

    public PaymentMethod getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(PaymentMethod payment_method) {
        this.payment_method = payment_method;
    }

    public Users getId_user() {
        return id_user;
    }

    public void setId_user(Users id_user) {
        this.id_user = id_user;
    }

    public Contracts getId_contract() {
        return id_contract;
    }

    public void setId_contract(Contracts id_contract) {
        this.id_contract = id_contract;
    }
}
