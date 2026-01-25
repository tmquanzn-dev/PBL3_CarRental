package com.example.rentalcar.models;

public class Penalties {
    private int id_penalty;
    private Contracts id_contract;
    private PenaltyType penalty_type;
    private double amount;

    public Penalties() {};

    public Penalties(int id_penalty, Contracts id_contract,
                     PenaltyType penalty_type, double amount) {
        this.id_penalty = id_penalty;
        this.id_contract = id_contract;
        this.penalty_type = penalty_type;
        this.amount = amount;
    }

    public int getId_penalty() {
        return id_penalty;
    }

    public void setId_penalty(int id_penalty) {
        this.id_penalty = id_penalty;
    }

    public Contracts getId_contract() {
        return id_contract;
    }

    public void setId_contract(Contracts id_contract) {
        this.id_contract = id_contract;
    }

    public PenaltyType getPenalty_type() {
        return penalty_type;
    }

    public void setPenalty_type(PenaltyType penalty_type) {
        this.penalty_type = penalty_type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
