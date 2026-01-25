package com.example.rentalcar.models;

public class Inspections {
    private int id_inspection;
    private Contracts id_contract;
    private Users id_user;
    private InspectionType inspection_type;

    public Inspections() {};

    public Inspections(int id_inspection, Contracts id_contract, Users id_user, InspectionType inspection_type) {
        this.id_inspection = id_inspection;
        this.id_contract = id_contract;
        this.id_user = id_user;
        this.inspection_type = inspection_type;
    }

    public int getId_inspection() {
        return id_inspection;
    }

    public void setId_inspection(int id_inspection) {
        this.id_inspection = id_inspection;
    }

    public Contracts getId_contract() {
        return id_contract;
    }

    public void setId_contract(Contracts id_contract) {
        this.id_contract = id_contract;
    }

    public Users getId_user() {
        return id_user;
    }

    public void setId_user(Users id_user) {
        this.id_user = id_user;
    }

    public InspectionType getInspection_type() {
        return inspection_type;
    }

    public void setInspection_type(InspectionType inspection_type) {
        this.inspection_type = inspection_type;
    }
}
