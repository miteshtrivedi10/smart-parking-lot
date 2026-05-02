package com.parkinglot.pricing;

import com.parkinglot.domains.ParkingTicket;
import com.parkinglot.domains.enums.VehicleType;

public interface PricingStrategy {
    boolean isPolicyApplicable(ParkingTicket parkingTicket);
    String getPolicyName();
//    double getVehicleBasedPrice(VehicleType  vehicleType);
    double calculate(ParkingTicket parkingTicket);
}
