package com.parkinglot.pricing;

import com.parkinglot.domains.ParkingTicket;
import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class SpecialRatePolicy implements PricingStrategy {

    protected abstract boolean isWindowTimingsApplicable(LocalDateTime entryTime, LocalDateTime exitTime);
    protected abstract long durationConstraint();
    protected abstract double getVehicleBasedPrice(VehicleType vehicleType);

    @Override
    public boolean isPolicyApplicable(ParkingTicket parkingTicket) {
        Duration duration = Duration.between(parkingTicket.getEntryTime(), parkingTicket.getExitTime());
        long numberOfHours =  duration.toSeconds() % 3600 == 0 ?  duration.toHours() : duration.toHours() + 1;

        // If does not satisfy number of hours constraint
        if (numberOfHours > durationConstraint()) {
            return false;
        }
        return isWindowTimingsApplicable(parkingTicket.getEntryTime(), parkingTicket.getExitTime());
    }

    @Override
    public double calculate(ParkingTicket parkingTicket) {
        double discount = LoyaltyTier.getDiscountedRates(parkingTicket.getLoyaltyTier()) / 100;
        double vehicleBasedPrice = getVehicleBasedPrice(parkingTicket.getVehicleType());
        return vehicleBasedPrice * discount;
    }
}
