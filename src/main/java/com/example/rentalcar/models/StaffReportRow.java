package com.example.rentalcar.models;

import javafx.beans.property.*;

public class StaffReportRow {
    private final SimpleIntegerProperty rank;
    private final SimpleStringProperty  fullName;
    private final SimpleIntegerProperty contractCount;
    private final SimpleDoubleProperty  revenue;

    public StaffReportRow(int rank, String fullName, int contractCount, double revenue) {
        this.rank          = new SimpleIntegerProperty(rank);
        this.fullName      = new SimpleStringProperty(fullName);
        this.contractCount = new SimpleIntegerProperty(contractCount);
        this.revenue       = new SimpleDoubleProperty(revenue);
    }

    public int    getRank()          { return rank.get(); }
    public String getFullName()      { return fullName.get(); }
    public int    getContractCount() { return contractCount.get(); }
    public double getRevenue()       { return revenue.get(); }

    public IntegerProperty rankProperty()          { return rank; }
    public StringProperty  fullNameProperty()      { return fullName; }
    public IntegerProperty contractCountProperty() { return contractCount; }
    public DoubleProperty  revenueProperty()       { return revenue; }
}