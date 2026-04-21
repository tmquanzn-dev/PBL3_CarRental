package com.example.rentalcar.models;

import javafx.beans.property.*;

public class VehicleReportRow {
    private final SimpleIntegerProperty rank;
    private final SimpleStringProperty  vehicleName;
    private final SimpleStringProperty  plateNumber;
    private final SimpleIntegerProperty rentalCount;
    private final SimpleDoubleProperty  revenue;

    public VehicleReportRow(int rank, String vehicleName, String plateNumber,
                            int rentalCount, double revenue) {
        this.rank        = new SimpleIntegerProperty(rank);
        this.vehicleName = new SimpleStringProperty(vehicleName);
        this.plateNumber = new SimpleStringProperty(plateNumber);
        this.rentalCount = new SimpleIntegerProperty(rentalCount);
        this.revenue     = new SimpleDoubleProperty(revenue);
    }

    public int    getRank()        { return rank.get(); }
    public String getVehicleName() { return vehicleName.get(); }
    public String getPlateNumber() { return plateNumber.get(); }
    public int    getRentalCount() { return rentalCount.get(); }
    public double getRevenue()     { return revenue.get(); }

    public IntegerProperty rankProperty()        { return rank; }
    public StringProperty  vehicleNameProperty() { return vehicleName; }
    public StringProperty  plateNumberProperty() { return plateNumber; }
    public IntegerProperty rentalCountProperty() { return rentalCount; }
    public DoubleProperty  revenueProperty()     { return revenue; }
}