package com.parkinglot.domains.enums;

import java.util.Collections;
import java.util.List;

public enum SpotType {
    COMPACT,
    NORMAL,
    LARGE;

    public List<VehicleType> supportedVehicleTypes(SpotType spotType) {
        switch (spotType) {
            case COMPACT: return Collections.singletonList(VehicleType.MotorCycle);
            case NORMAL: return Collections.singletonList(VehicleType.Car);
            case LARGE: return Collections.singletonList(VehicleType.Bus);
            default: throw new IllegalArgumentException();
        }
    }
}
