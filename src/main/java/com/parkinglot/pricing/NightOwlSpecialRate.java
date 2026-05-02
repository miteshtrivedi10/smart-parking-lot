package com.parkinglot.pricing;

import com.parkinglot.domains.enums.VehicleType;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.parkinglot.domains.AppConstant.NIGHT_OWL_SPECIAL_POLICY;

public class NightOwlSpecialRate extends SpecialRatePolicy {

    private final static long DURATION_CONSTRAINT = 18;
    private final static double BASE_RATE = 8.0;

    @Override
    protected boolean isWindowTimingsApplicable(LocalDateTime entryTime, LocalDateTime exitTime) {

        //Entry and Exit should have exactly ONE calendar day difference
        if (!entryTime.toLocalDate().plusDays(1).isEqual(exitTime.toLocalDate())) {
            return false;
        }

        LocalTime inTime1 = LocalTime.of(18,0,0);
        LocalTime inTime2 = LocalTime.of(23,59,0);

        LocalTime outTime1 = LocalTime.of(5,0,0);
        LocalTime outTime2 = LocalTime.of(10,0,0);

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
        return NIGHT_OWL_SPECIAL_POLICY;
    }
}
