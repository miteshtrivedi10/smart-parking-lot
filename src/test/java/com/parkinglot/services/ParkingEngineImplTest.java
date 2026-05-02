package com.parkinglot.services;

import com.parkinglot.domains.Invoice;
import com.parkinglot.domains.ParkingComplexSite;
import com.parkinglot.domains.ParkingTicket;
import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.parkinglot.domains.AppConstant.*;
import static com.parkinglot.testsupport.ParkingTicketTestSupport.withTimes;
import static org.junit.jupiter.api.Assertions.*;

class ParkingEngineImplTest {

    @Test
    void initialisingSystemCreatesConfiguredSiteAndBuildingCapacity() {
        ParkingComplexSite site = new InitialisingTheSystem().setupParkingComplexSite();
        ParkingEngineImpl engine = new ParkingEngineImpl(site);

        assertEquals(COMPLEX_SITE_1, site.getParkingComplexName());
        assertTrue(site.isEnabled());
        assertTrue(site.getMapperBuildings().containsKey(BUILDING_1));

        assertNotNull(engine.parkVehicle(VehicleType.MotorCycle, "MC-1", LoyaltyTier.NONE));
        assertNotNull(engine.parkVehicle(VehicleType.MotorCycle, "MC-2", LoyaltyTier.NONE));
        assertNotNull(engine.parkVehicle(VehicleType.MotorCycle, "MC-3", LoyaltyTier.NONE));
        assertNotNull(engine.parkVehicle(VehicleType.MotorCycle, "MC-4", LoyaltyTier.NONE));
        assertNull(engine.parkVehicle(VehicleType.MotorCycle, "MC-5", LoyaltyTier.NONE));

        assertNotNull(engine.parkVehicle(VehicleType.Car, "CAR-1", LoyaltyTier.NONE));
        assertNotNull(engine.parkVehicle(VehicleType.Car, "CAR-2", LoyaltyTier.NONE));
        assertNull(engine.parkVehicle(VehicleType.Car, "CAR-3", LoyaltyTier.NONE));

        assertNotNull(engine.parkVehicle(VehicleType.Bus, "BUS-1", LoyaltyTier.NONE));
        assertNotNull(engine.parkVehicle(VehicleType.Bus, "BUS-2", LoyaltyTier.NONE));
        assertNull(engine.parkVehicle(VehicleType.Bus, "BUS-3", LoyaltyTier.NONE));
    }

    @Test
    void parkVehicleRejectsDuplicateVehicleNumberForSameVehicleType() {
        ParkingEngineImpl engine = new ParkingEngineImpl(new InitialisingTheSystem().setupParkingComplexSite());

        ParkingTicket firstTicket = engine.parkVehicle(VehicleType.Car, "KA-01-AA-0001", LoyaltyTier.SILVER);
        ParkingTicket duplicateWithDifferentCase = engine.parkVehicle(VehicleType.Car, "ka-01-aa-0001", LoyaltyTier.GOLD);
        ParkingTicket sameNumberDifferentVehicleType = engine.parkVehicle(VehicleType.MotorCycle, "ka-01-aa-0001", LoyaltyTier.GOLD);

        assertNotNull(firstTicket);
        assertNull(duplicateWithDifferentCase);
        assertNotNull(sameNumberDifferentVehicleType);
        assertFalse(firstTicket.getParkingSlot().isSlotEmpty());
    }

    @Test
    void unParkVehicleCompletesTicketAndFreesSlotForReuse() {
        ParkingEngineImpl engine = new ParkingEngineImpl(new InitialisingTheSystem().setupParkingComplexSite());
        ParkingTicket parked = engine.parkVehicle(VehicleType.Car, "KA-01-AA-0002", LoyaltyTier.NONE);

        engine.unParkVehicle(parked);

        assertTrue(parked.isCompleted());
        assertNotNull(parked.getExitTime());
        assertTrue(parked.getParkingSlot().isSlotEmpty());

        ParkingTicket replacement = engine.parkVehicle(VehicleType.Car, "KA-01-AA-0003", LoyaltyTier.NONE);

        assertNotNull(replacement);
        assertSame(parked.getParkingSlot(), replacement.getParkingSlot());
    }

    @Test
    void unParkVehicleIgnoresUnknownTicket() {
        ParkingEngineImpl engine = new ParkingEngineImpl(new InitialisingTheSystem().setupParkingComplexSite());
        ParkingTicket unknownTicket = new ParkingTicket(
                VehicleType.Car, "KA-01-AA-0004", "external", "site", null, LoyaltyTier.NONE);

        engine.unParkVehicle(unknownTicket);

        assertFalse(unknownTicket.isCompleted());
        assertNull(unknownTicket.getExitTime());
    }

    @Test
    void generateInvoiceSelectsCheapestApplicablePricingPolicy() {
        ParkingEngineImpl engine = new ParkingEngineImpl(new InitialisingTheSystem().setupParkingComplexSite());
        ParkingTicket ticket = engine.parkVehicle(VehicleType.Car, "KA-01-AA-0005", LoyaltyTier.SILVER);
        withTimes(ticket,
                LocalDateTime.of(2026, 5, 4, 7, 0),
                LocalDateTime.of(2026, 5, 4, 16, 0));

        Invoice invoice = engine.generateInvoice(ticket);

        assertNotNull(invoice.getInvoiceId());
        assertEquals(ticket.getTicketId(), invoice.getTicketId());
        assertEquals(ticket.getVehicleNumber(), invoice.getVehicleNumber());
        assertEquals(ticket.getEntryTime(), invoice.getInTime());
        assertEquals(ticket.getExitTime(), invoice.getOutTime());
        assertEquals(EARLY_BIRD_SPECIAL_POLICY, invoice.getPricingPolicyApplied());
        assertEquals(1.5, invoice.getAmount());
        assertNotNull(invoice.getGeneratedOn());
    }

    @Test
    void generateInvoiceFallsBackToStandardPolicyWhenSpecialRatesDoNotApply() {
        ParkingEngineImpl engine = new ParkingEngineImpl(new InitialisingTheSystem().setupParkingComplexSite());
        ParkingTicket ticket = engine.parkVehicle(VehicleType.Bus, "KA-01-AA-0006", LoyaltyTier.NONE);
        withTimes(ticket,
                LocalDateTime.of(2026, 5, 4, 11, 0),
                LocalDateTime.of(2026, 5, 4, 12, 0));

        Invoice invoice = engine.generateInvoice(ticket);

        assertEquals(STANDARD_PRICING_POLICY, invoice.getPricingPolicyApplied());
        assertEquals(10.0, invoice.getAmount());
    }
}
