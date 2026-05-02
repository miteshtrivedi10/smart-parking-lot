package com.parkinglot.domains;

import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.SpotType;
import com.parkinglot.domains.enums.VehicleType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ParkingDomainTest {

    @Test
    void vehicleTypeRateMultipliersMatchConfiguredRates() {
        assertEquals(0.8, VehicleType.getRateMultiplier(VehicleType.MotorCycle));
        assertEquals(1.0, VehicleType.getRateMultiplier(VehicleType.Car));
        assertEquals(2.0, VehicleType.getRateMultiplier(VehicleType.Bus));
    }

    @Test
    void loyaltyTierDiscountsMatchConfiguredRates() {
        assertEquals(0.0, LoyaltyTier.getDiscountedRates(LoyaltyTier.NONE));
        assertEquals(10.0, LoyaltyTier.getDiscountedRates(LoyaltyTier.SILVER));
        assertEquals(20.0, LoyaltyTier.getDiscountedRates(LoyaltyTier.GOLD));
        assertEquals(30.0, LoyaltyTier.getDiscountedRates(LoyaltyTier.PLATINUM));
    }

    @Test
    void spotTypesExposeSupportedVehicleTypes() {
        assertEquals(VehicleType.MotorCycle, SpotType.COMPACT.supportedVehicleTypes(SpotType.COMPACT).getFirst());
        assertEquals(VehicleType.Car, SpotType.NORMAL.supportedVehicleTypes(SpotType.NORMAL).getFirst());
        assertEquals(VehicleType.Bus, SpotType.LARGE.supportedVehicleTypes(SpotType.LARGE).getFirst());
    }

    @Test
    void parkingSlotTracksOccupancy() {
        ParkingSlot slot = new ParkingSlot(2, VehicleType.Bus);

        assertEquals(2, slot.getLevel());
        assertEquals(VehicleType.Bus, slot.getVehicleType());
        assertTrue(slot.isCanPark());
        assertTrue(slot.isSlotEmpty());

        slot.engageSlot();
        assertFalse(slot.isSlotEmpty());

        slot.disengageSlot();
        assertTrue(slot.isSlotEmpty());
    }

    @Test
    void parkingBuildingReturnsOnlyAvailableSlotForVehicleType() {
        ParkingBuilding building = new ParkingBuilding("north");
        ParkingSlot carSlot = new ParkingSlot(1, VehicleType.Car);
        ParkingSlot secondCarSlot = new ParkingSlot(1, VehicleType.Car);
        ParkingSlot busSlot = new ParkingSlot(2, VehicleType.Bus);

        building.addParkingSlotBasedOnSize(null);
        building.addParkingSlotBasedOnSize(carSlot);
        building.addParkingSlotBasedOnSize(secondCarSlot);
        building.addParkingSlotBasedOnSize(busSlot);

        assertEquals(Optional.of(carSlot), building.getAvailableParkingSlotBasedOnVehicleType(VehicleType.Car));

        carSlot.engageSlot();
        assertEquals(Optional.of(secondCarSlot), building.getAvailableParkingSlotBasedOnVehicleType(VehicleType.Car));
        assertEquals(Optional.of(busSlot), building.getAvailableParkingSlotBasedOnVehicleType(VehicleType.Bus));
        assertTrue(building.getAvailableParkingSlotBasedOnVehicleType(VehicleType.MotorCycle).isEmpty());
    }

    @Test
    void parkingComplexStoresBuildingsByName() {
        ParkingComplexSite site = new ParkingComplexSite("site-a", true);
        ParkingBuilding building = new ParkingBuilding("building-a");

        site.addParkingBuilding(building);

        assertEquals("site-a", site.getParkingComplexName());
        assertTrue(site.isEnabled());
        assertSame(building, site.getMapperBuildings().get("building-a"));
    }

    @Test
    void ticketExitMarksCompletionAndInvoiceKeepsValues() {
        ParkingSlot slot = new ParkingSlot(0, VehicleType.MotorCycle);
        ParkingTicket ticket = new ParkingTicket(
                VehicleType.MotorCycle, "KA-01-AA-0001", "b1", "site", slot, LoyaltyTier.NONE);

        assertFalse(ticket.isCompleted());
        assertNull(ticket.getExitTime());
        assertNotNull(ticket.toString());

        ticket.updateForExit();

        assertTrue(ticket.isCompleted());
        assertNotNull(ticket.getExitTime());

        Invoice invoice = new Invoice(ticket.getTicketId(), ticket.getVehicleType(), ticket.getVehicleNumber(),
                ticket.getEntryTime(), ticket.getExitTime(), ticket.getLoyaltyTier(), "policy", 10.0,
                ticket.getExitTime());

        assertNotNull(invoice.getInvoiceId());
        assertEquals(ticket.getTicketId(), invoice.getTicketId());
        assertEquals("policy", invoice.getPricingPolicyApplied());
        assertEquals(10.0, invoice.getAmount());
    }

    @Test
    void ticketEqualsUsesTicketIdAndRejectsNull() {
        ParkingSlot slot = new ParkingSlot(0, VehicleType.Car);
        ParkingTicket ticket = new ParkingTicket(VehicleType.Car, "KA-01-AA-0002", "b1", "site", slot, LoyaltyTier.NONE);
        ParkingTicket otherTicket = new ParkingTicket(VehicleType.Car, "KA-01-AA-0003", "b1", "site", slot, LoyaltyTier.NONE);

        assertEquals(ticket, ticket);
        assertNotEquals(ticket, otherTicket);
        assertNotEquals(null, ticket);
        assertThrows(ClassCastException.class, () -> ticket.equals("not-a-ticket"));
        assertEquals(ticket.getTicketId().hashCode(), ticket.hashCode());
    }
}
