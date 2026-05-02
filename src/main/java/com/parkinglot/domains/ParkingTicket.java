package com.parkinglot.domains;

import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ParkingTicket {
    private final String ticketId;
    private final VehicleType vehicleType;
    private final String vehicleNumber;
    private final String buildingName;
    private final String parkingComplexSite;
    private final ParkingSlot parkingSlot;
    private final LoyaltyTier loyaltyTier;
    private final LocalDateTime entryTime;

    private LocalDateTime exitTime;
    private boolean isCompleted;

    public ParkingTicket(VehicleType vehicleType,
                         String vehicleNumber,
                         String buildingName,
                         String parkingComplexSite,
                         ParkingSlot parkingSlot,
                         LoyaltyTier loyaltyTier) {
        this.ticketId = UUID.randomUUID().toString();
        this.entryTime = LocalDateTime.now();
        this.vehicleType = vehicleType;
        this.parkingComplexSite = parkingComplexSite;
        this.buildingName = buildingName;
        this.vehicleNumber = vehicleNumber;
        this.parkingSlot = parkingSlot;
        this.loyaltyTier = loyaltyTier;
        this.isCompleted = false;
    }

    public void updateForExit() {
        this.exitTime = LocalDateTime.now();
        this.isCompleted = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        ParkingTicket parkingTicket = (ParkingTicket) obj;
        return this.ticketId.equalsIgnoreCase(parkingTicket.ticketId);
    }

    @Override
    public int hashCode() {
        return this.ticketId.hashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
