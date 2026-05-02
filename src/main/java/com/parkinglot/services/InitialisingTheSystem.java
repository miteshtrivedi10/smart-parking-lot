package com.parkinglot.services;

import com.parkinglot.domains.ParkingBuilding;
import com.parkinglot.domains.ParkingComplexSite;
import com.parkinglot.domains.ParkingSlot;
import com.parkinglot.domains.enums.VehicleType;
import com.parkinglot.repository.InitialiseSystem;

import java.util.HashMap;
import java.util.Map;

import static com.parkinglot.domains.AppConstant.BUILDING_1;
import static com.parkinglot.domains.AppConstant.COMPLEX_SITE_1;

public class InitialisingTheSystem implements InitialiseSystem {

    private final Map<String, ParkingComplexSite> parkingComplexSites = new  HashMap<>();

    @Override
    public ParkingComplexSite setupParkingComplexSite() {
        ParkingComplexSite parkingComplexSite = new ParkingComplexSite(COMPLEX_SITE_1, true);
        parkingComplexSite.addParkingBuilding(setupBuilding1());
        return parkingComplexSite;
    }

    private ParkingBuilding setupBuilding1() {
        ParkingBuilding parkingBuilding1 = new ParkingBuilding(BUILDING_1);
        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(0, VehicleType.MotorCycle));
        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(0, VehicleType.MotorCycle));
        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(0, VehicleType.MotorCycle));
        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(0, VehicleType.MotorCycle));

        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(1, VehicleType.Car));
        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(1, VehicleType.Car));

        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(2, VehicleType.Bus));
        parkingBuilding1.addParkingSlotBasedOnSize(new ParkingSlot(2, VehicleType.Bus));

        return parkingBuilding1;
    }
}
