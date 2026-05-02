package com.parkinglot.domains.enums;

public enum VehicleType {
    MotorCycle,
    Car,
    Bus;

    public static double getRateMultiplier(VehicleType vehicleType) {
        return switch (vehicleType) {
            case Car -> 1.0;
            case MotorCycle -> 0.8;
            case Bus -> 2.0;
        };
    }
}
