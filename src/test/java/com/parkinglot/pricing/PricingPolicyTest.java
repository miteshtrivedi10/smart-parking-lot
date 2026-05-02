package com.parkinglot.pricing;

import com.parkinglot.domains.AppConstant;
import com.parkinglot.domains.ParkingSlot;
import com.parkinglot.domains.ParkingTicket;
import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static com.parkinglot.testsupport.ParkingTicketTestSupport.withTimes;
import static org.junit.jupiter.api.Assertions.*;

class PricingPolicyTest {

    @Test
    void standardPolicyIsAlwaysApplicableAndPricesByVehicleType() {
        StandardRatePolicy policy = new StandardRatePolicy();
        ParkingTicket ticket = ticket(VehicleType.Car, LoyaltyTier.NONE);

        assertTrue(policy.isPolicyApplicable(ticket));
        assertEquals(AppConstant.STANDARD_PRICING_POLICY, policy.getPolicyName());
    }

    @Test
    void standardPolicyAppliesPeakSurchargeForWeekdayMorningWindow() {
        StandardRatePolicy policy = new StandardRatePolicy();
        ParkingTicket ticket = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 8, 0),
                LocalDateTime.of(2026, 5, 4, 9, 0));

        assertEquals(7.5, policy.calculate(ticket));
    }

    @Test
    void standardPolicyUsesBaseRateOutsidePeakWindow() {
        StandardRatePolicy policy = new StandardRatePolicy();
        ParkingTicket ticket = withTimes(ticket(VehicleType.MotorCycle, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 11, 0),
                LocalDateTime.of(2026, 5, 4, 12, 0));

        assertEquals(4.0, policy.calculate(ticket));
    }

    @Test
    void standardPolicyPrivateHourCalculationCoversExactAndPartialHours() throws Exception {
        StandardRatePolicy policy = new StandardRatePolicy();
        Method method = StandardRatePolicy.class.getDeclaredMethod("calculateTotalNumberOfHours", ParkingTicket.class);
        method.setAccessible(true);

        ParkingTicket exactHour = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 11, 0),
                LocalDateTime.of(2026, 5, 4, 12, 0));
        ParkingTicket partialHour = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 11, 0),
                LocalDateTime.of(2026, 5, 4, 12, 1));

        assertEquals(1L, method.invoke(policy, exactHour));
        assertEquals(2L, method.invoke(policy, partialHour));
    }

    @Test
    void standardPolicyDoesNotApplyPeakSurchargeOnWeekends() {
        StandardRatePolicy policy = new StandardRatePolicy();
        ParkingTicket ticket = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 3, 11, 0),
                LocalDateTime.of(2026, 5, 3, 12, 0));

        assertEquals(5.0, policy.calculate(ticket));
    }

    @Test
    void earlyBirdPolicyAppliesOnlyForSameDayEarlyEntryAndEveningExitWithinDuration() {
        EarlyBirdSpecialRate policy = new EarlyBirdSpecialRate();
        ParkingTicket applicable = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 7, 0),
                LocalDateTime.of(2026, 5, 4, 16, 0));
        ParkingTicket wrongEntry = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 5, 59),
                LocalDateTime.of(2026, 5, 4, 16, 0));
        ParkingTicket wrongExit = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 7, 0),
                LocalDateTime.of(2026, 5, 4, 15, 30));
        ParkingTicket overnight = withTimes(ticket(VehicleType.Car, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 7, 0),
                LocalDateTime.of(2026, 5, 5, 16, 0));

        assertTrue(policy.isPolicyApplicable(applicable));
        assertFalse(policy.isPolicyApplicable(wrongEntry));
        assertTrue(policy.isPolicyApplicable(wrongExit));
        assertFalse(policy.isPolicyApplicable(overnight));
        assertEquals(AppConstant.EARLY_BIRD_SPECIAL_POLICY, policy.getPolicyName());
    }

    @Test
    void earlyBirdWindowRejectsDifferentCalendarDayWhenCheckedDirectly() {
        EarlyBirdSpecialRate policy = new EarlyBirdSpecialRate();

        assertFalse(policy.isWindowTimingsApplicable(
                LocalDateTime.of(2026, 5, 4, 7, 0),
                LocalDateTime.of(2026, 5, 5, 16, 0)));
    }

    @Test
    void nightOwlPolicyAppliesOnlyForOvernightWindowWithinDuration() {
        NightOwlSpecialRate policy = new NightOwlSpecialRate();
        ParkingTicket applicable = withTimes(ticket(VehicleType.Bus, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 19, 0),
                LocalDateTime.of(2026, 5, 5, 6, 0));
        ParkingTicket sameDay = withTimes(ticket(VehicleType.Bus, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 19, 0),
                LocalDateTime.of(2026, 5, 4, 23, 0));
        ParkingTicket wrongEntry = withTimes(ticket(VehicleType.Bus, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 17, 59),
                LocalDateTime.of(2026, 5, 5, 6, 0));
        ParkingTicket wrongExit = withTimes(ticket(VehicleType.Bus, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 19, 0),
                LocalDateTime.of(2026, 5, 5, 4, 59));
        ParkingTicket tooLong = withTimes(ticket(VehicleType.Bus, LoyaltyTier.NONE),
                LocalDateTime.of(2026, 5, 4, 19, 0),
                LocalDateTime.of(2026, 5, 5, 14, 0));

        assertTrue(policy.isPolicyApplicable(applicable));
        assertFalse(policy.isPolicyApplicable(sameDay));
        assertFalse(policy.isPolicyApplicable(wrongEntry));
        assertFalse(policy.isPolicyApplicable(wrongExit));
        assertFalse(policy.isPolicyApplicable(tooLong));
        assertEquals(AppConstant.NIGHT_OWL_SPECIAL_POLICY, policy.getPolicyName());
    }

    @Test
    void specialRatesCalculateVehiclePriceAndCurrentLoyaltyAmountBehavior() {
        EarlyBirdSpecialRate earlyBird = new EarlyBirdSpecialRate();
        NightOwlSpecialRate nightOwl = new NightOwlSpecialRate();

        assertEquals(12.0, earlyBird.getVehicleBasedPrice(VehicleType.MotorCycle));
        assertEquals(15.0, earlyBird.getVehicleBasedPrice(VehicleType.Car));
        assertEquals(30.0, earlyBird.getVehicleBasedPrice(VehicleType.Bus));
        assertEquals(6.4, nightOwl.getVehicleBasedPrice(VehicleType.MotorCycle));
        assertEquals(8.0, nightOwl.getVehicleBasedPrice(VehicleType.Car));
        assertEquals(16.0, nightOwl.getVehicleBasedPrice(VehicleType.Bus));

        ParkingTicket noDiscount = ticket(VehicleType.Car, LoyaltyTier.NONE);
        ParkingTicket gold = ticket(VehicleType.Car, LoyaltyTier.GOLD);

        assertEquals(0.0, earlyBird.calculate(noDiscount));
        assertEquals(3.0, earlyBird.calculate(gold));
    }

    private static ParkingTicket ticket(VehicleType vehicleType, LoyaltyTier loyaltyTier) {
        return new ParkingTicket(vehicleType, "KA-01-AA-0001", "b1", "site",
                new ParkingSlot(0, vehicleType), loyaltyTier);
    }
}
