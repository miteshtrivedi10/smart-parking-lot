package com.parkinglot.services;

import com.parkinglot.domains.*;
import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;
import com.parkinglot.pricing.EarlyBirdSpecialRate;
import com.parkinglot.pricing.NightOwlSpecialRate;
import com.parkinglot.pricing.PricingStrategy;
import com.parkinglot.pricing.StandardRatePolicy;
import com.parkinglot.repository.ParkingEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.*;

public class ParkingEngineImpl implements ParkingEngine {

    private final ParkingComplexSite parkingComplexSite;
    private final List<ParkingTicket> archivedTickets = new ArrayList<>();
    private final Set<ParkingTicket> parkingTickets = new HashSet<>();
    private final List<PricingStrategy> pricingStrategies = Arrays.asList(
            new StandardRatePolicy(),
            new EarlyBirdSpecialRate(),
            new NightOwlSpecialRate()
    );

    public  ParkingEngineImpl(ParkingComplexSite parkingComplexSite) {
        this.parkingComplexSite = parkingComplexSite;
    }

    @Override
    public ParkingTicket parkVehicle(VehicleType vehicleType, String vehicleNumber, LoyaltyTier loyaltyTier) {

        //Is vehicle already parked
        if (isVehicleAlreadyParked(vehicleType, vehicleNumber)) {return null;}

        Pair<String, Optional<ParkingSlot>> pairSlot = findAvailableParkingSlot(vehicleType);
        if (pairSlot.getRight().isPresent()) {
            ParkingTicket parkingTicket = new ParkingTicket(vehicleType,
                    vehicleNumber,pairSlot.getLeft(),parkingComplexSite.getParkingComplexName(),
                    pairSlot.getRight().get(), loyaltyTier);
            parkingTickets.add(parkingTicket);
            return  parkingTicket;
        }
        return null;
    }

    @Override
    public void unParkVehicle(ParkingTicket parkingTicket) {
        if (!parkingTickets.contains(parkingTicket)) {
            return;
        }
        parkingTicket.updateForExit();
        parkingTicket.getParkingSlot().disengageSlot();
        parkingTickets.remove(parkingTicket);
        archivedTickets.add(parkingTicket);
    }

    private Pair<String,Optional<ParkingSlot>> findAvailableParkingSlot(VehicleType vehicleType) {
        for(ParkingBuilding building : parkingComplexSite.getMapperBuildings().values()) {
            Optional<ParkingSlot> parkingSlot = building.getAvailableParkingSlotBasedOnVehicleType(vehicleType);
            if (parkingSlot.isPresent()) {
                parkingSlot.get().engageSlot();
                return Pair.of(building.getBuildingName(),  parkingSlot);
            }
        }
        return Pair.of(StringUtils.EMPTY, Optional.empty());
    }

    @Override
    public Invoice generateInvoice(ParkingTicket parkingTicket) {
        PricingResult pricingResult = pricingStrategies.stream()
                .filter(strategy -> strategy.isPolicyApplicable(parkingTicket))
                .map(x -> new PricingResult(x.getPolicyName(), x.calculate(parkingTicket)))
                .min(Comparator.comparingDouble(PricingResult::amount)).orElseThrow();


        return new Invoice(parkingTicket.getTicketId(), parkingTicket.getVehicleType(),parkingTicket.getVehicleNumber(),
                parkingTicket.getEntryTime(), parkingTicket.getExitTime(), parkingTicket.getLoyaltyTier(),
                pricingResult.strategyApplied(), pricingResult.amount(), LocalDateTime.now());
    }

    private boolean isVehicleAlreadyParked(VehicleType vehicleType, String vehicleNumber) {
        return parkingTickets
                .stream()
                .anyMatch(parkingTicket ->
                        parkingTicket.getVehicleType() == vehicleType &&
                        parkingTicket.getVehicleNumber().equalsIgnoreCase(vehicleNumber));
    }
}
