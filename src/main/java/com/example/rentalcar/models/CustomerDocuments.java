package com.example.rentalcar.models;

public class CustomerDocuments {
    private int id_document;
    private Customers customers;
    private DocumentType document_type;
    private String document_number;

    public CustomerDocuments() {};

    public CustomerDocuments(int id_document, Customers customers, DocumentType document_type, String document_number) {
        this.id_document = id_document;
        this.customers = customers;
        this.document_type = document_type;
        this.document_number = document_number;
    }

    public int getId_document() {
        return id_document;
    }

    public void setId_document(int id_document) {
        this.id_document = id_document;
    }

    public Customers getCustomers() {
        return customers;
    }

    public void setCustomers(Customers customers) {
        this.customers = customers;
    }

    public DocumentType getDocument_type() {
        return document_type;
    }

    public void setDocument_type(DocumentType document_type) {
        this.document_type = document_type;
    }

    public String getDocument_number() {
        return document_number;
    }

    public void setDocument_number(String document_number) {
        this.document_number = document_number;
    }
}
