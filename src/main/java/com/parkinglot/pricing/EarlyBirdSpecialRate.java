package com.parkinglot.pricing;

import com.parkinglot.domains.enums.VehicleType;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.parkinglot.domains.AppConstant.EARLY_BIRD_SPECIAL_POLICY;

public class EarlyBirdSpecialRate extends SpecialRatePolicy {

    private static final double BASE_RATE = 15.00;
    private static final long DURATION_CONSTRAINT = 15;

    @Override
    protected boolean isWindowTimingsApplicable(LocalDateTime entryTime, LocalDateTime exitTime) {

        //If the entry and exit is not on same day - NOT APPLICABLE
        if (!entryTime.toLocalDate().isEqual(exitTime.toLocalDate())) {
            return false;
        }

        LocalTime inTime1 = LocalTime.of(6,0,0);
        LocalTime inTime2 = LocalTime.of(9,0,0);

        LocalTime outTime1 = LocalTime.of(15,30,0);
        LocalTime outTime2 = LocalTime.of(19,0,0);

        boolean incomingWindow = !entryTime.toLocalTime().isBefore(inTime1) && !entryTime.toLocalTime().isAfter(inTime2);
        boolean outgoingWindow = !exitTime.toLocalTime().isBefore(outTime1) && !exitTime.toLocalTime().isAfter(outTime2);

        return incomingWindow && outgoingWindow;
    }

    @Override
    protected long durationConstraint() {
        return DURATION_CONSTRAINT;
    }

    @Override
    protected double getVehicleBasedPrice(VehicleType vehicleType) {
        return BASE_RATE * VehicleType.getRateMultiplier(vehicleType);
    }

    @Override
    public String getPolicyName() {
        return EARLY_BIRD_SPECIAL_POLICY;
    }
}
