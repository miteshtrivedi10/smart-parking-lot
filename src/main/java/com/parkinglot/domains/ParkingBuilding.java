package com.parkinglot.domains;

import com.parkinglot.domains.enums.VehicleType;
import lombok.Getter;

import java.util.*;

public final class ParkingBuilding {

    @Getter
    private final String buildingName;
    private final Map<VehicleType, List<ParkingSlot>> sizeWithSlots = new  HashMap<>();

    public ParkingBuilding(String buildingName) {
        this.buildingName = buildingName;
    }

    public void addParkingSlotBasedOnSize(ParkingSlot parkingSlot) {
        if (parkingSlot == null) {
            return;
        }

        if (sizeWithSlots.containsKey(parkingSlot.getVehicleType())) {
            sizeWithSlots.get(parkingSlot.getVehicleType()).add(parkingSlot);
        } else {
            List<ParkingSlot> list = new ArrayList<>();
            list.add(parkingSlot);
            sizeWithSlots.put(parkingSlot.getVehicleType(), list);
        }

    }

    public Optional<ParkingSlot> getAvailableParkingSlotBasedOnVehicleType(VehicleType vehicleType) {
        List<ParkingSlot> parkingSlots = sizeWithSlots.get(vehicleType);

        if (parkingSlots == null) {
            return Optional.empty();
        }

        return parkingSlots.stream()
                .filter(ParkingSlot::isCanPark)
                .filter(ParkingSlot::isSlotEmpty)
                .findFirst();
    }
}
