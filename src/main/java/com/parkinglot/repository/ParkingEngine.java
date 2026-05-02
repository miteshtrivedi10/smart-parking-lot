package com.parkinglot.repository;

import com.parkinglot.domains.Invoice;
import com.parkinglot.domains.ParkingTicket;
import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;

public interface ParkingEngine {
    ParkingTicket parkVehicle(VehicleType vehicleType, String vehicleNumber, LoyaltyTier loyaltyTier);
    void unParkVehicle(ParkingTicket parkingTicket);
    Invoice generateInvoice(ParkingTicket parkingTicket);
}
