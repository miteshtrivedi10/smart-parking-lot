package com.parkinglot.domains;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public final class ParkingComplexSite {
    @Getter
    private final String parkingComplexName;
    @Getter
    private final boolean enabled;
    @Getter
    private final Map<String, ParkingBuilding> mapperBuildings = new HashMap<>();

    public ParkingComplexSite(String parkingComplexName,
                              boolean enabled) {
        this.parkingComplexName = parkingComplexName;
        this.enabled = enabled;
    }

    public void addParkingBuilding(ParkingBuilding parkingBuilding) {
        mapperBuildings.put(parkingBuilding.getBuildingName(), parkingBuilding);
    }
}
