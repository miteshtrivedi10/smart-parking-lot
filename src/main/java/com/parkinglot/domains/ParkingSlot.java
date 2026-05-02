package com.parkinglot.domains;

import com.parkinglot.domains.enums.VehicleType;
import lombok.Getter;

import java.util.UUID;

public final class ParkingSlot {
    @Getter
    private String parkingId;

    @Getter
    private int level;

    @Getter
    private VehicleType vehicleType;

    @Getter
    private boolean canPark;

    @Getter
    private boolean isSlotEmpty;

    public ParkingSlot(int level, VehicleType vehicleType) {
        this.parkingId = UUID.randomUUID().toString();
        this.level = level;
        this.vehicleType = vehicleType;
        this.canPark = true;
        this.isSlotEmpty = true;
    }

    public void engageSlot() {
        this.isSlotEmpty = false;
    }

    public void disengageSlot() {
        this.isSlotEmpty = true;
    }
}
