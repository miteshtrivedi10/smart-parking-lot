package com.parkinglot.pricing;

import com.parkinglot.domains.ParkingTicket;
import com.parkinglot.domains.enums.VehicleType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.parkinglot.domains.AppConstant.STANDARD_PRICING_POLICY;

public class StandardRatePolicy implements PricingStrategy {

    private static final double FIRST_HOUR_RATE = 5.0;
    private static final double SECOND_HOUR_RATE = 3.0;
    private static final double ADDITIONAL_HOUR_RATE = 2.0;
    private static final Double SURCHARGE_MULTIPLIER = 1.5;
    private static final Map<String, List<Pair<LocalTime, LocalTime>>> surchargeTimings = new HashMap<>();

    public StandardRatePolicy() {
        surchargeTimings.put(DayOfWeek.MONDAY.name(), Arrays.asList(
                Pair.of(LocalTime.of(7,0,0), LocalTime.of(10,0,0)),
                Pair.of(LocalTime.of(16,0,0), LocalTime.of(19,0,0))));

        surchargeTimings.put(DayOfWeek.TUESDAY.name(), Arrays.asList(
                Pair.of(LocalTime.of(7,0,0), LocalTime.of(10,0,0)),
                Pair.of(LocalTime.of(16,0,0), LocalTime.of(19,0,0))));

        surchargeTimings.put(DayOfWeek.WEDNESDAY.name(), Arrays.asList(
                Pair.of(LocalTime.of(7,0,0), LocalTime.of(10,0,0)),
                Pair.of(LocalTime.of(16,0,0), LocalTime.of(19,0,0))));

        surchargeTimings.put(DayOfWeek.THURSDAY.name(), Arrays.asList(
                Pair.of(LocalTime.of(7,0,0), LocalTime.of(10,0,0)),
                Pair.of(LocalTime.of(16,0,0), LocalTime.of(19,0,0))));

        surchargeTimings.put(DayOfWeek.FRIDAY.name(), Arrays.asList(
                Pair.of(LocalTime.of(7,0,0), LocalTime.of(10,0,0)),
                Pair.of(LocalTime.of(16,0,0), LocalTime.of(19,0,0))));

    }

    private long calculateTotalNumberOfHours(ParkingTicket parkingTicket) {
        Duration duration = Duration.between(parkingTicket.getEntryTime(), parkingTicket.getExitTime());
        return duration.toSeconds() % 3600 == 0 ? duration.toHours() : duration.toHours() + 1;
    }

    @Override
    public boolean isPolicyApplicable(ParkingTicket parkingTicket) {
        return true;
    }

    @Override
    public String getPolicyName() {
        return STANDARD_PRICING_POLICY;
    }

    @Override
    public double calculate(ParkingTicket parkingTicket) {
        double totalBill = 0.0;
        long billableHours = calculateTotalNumberOfHours(parkingTicket);

        for (long hour = 0; hour < billableHours; hour++) {
            LocalDateTime segmentStart = parkingTicket.getEntryTime().plusHours(hour);
            LocalDateTime segmentEnd = segmentStart.plusHours(1);
            double hourlyRate = getProgressiveHourlyRate(hour + 1) * VehicleType.getRateMultiplier(parkingTicket.getVehicleType());

            totalBill += overlapsPeakWindow(segmentStart, segmentEnd)
                    ? hourlyRate * SURCHARGE_MULTIPLIER
                    : hourlyRate;
        }
        return totalBill;
    }

    private double getProgressiveHourlyRate(long hourNumber) {
        if (hourNumber == 1) {
            return FIRST_HOUR_RATE;
        }
        if (hourNumber == 2) {
            return SECOND_HOUR_RATE;
        }
        return ADDITIONAL_HOUR_RATE;
    }

    private boolean overlapsPeakWindow(LocalDateTime segmentStart, LocalDateTime segmentEnd) {
        for(LocalDate dt = segmentStart.toLocalDate(); !dt.isAfter(segmentEnd.toLocalDate()); dt = dt.plusDays(1)) {
            for (Pair<LocalTime, LocalTime> peakWindow : surchargeTimings.getOrDefault(dt.getDayOfWeek().name(), Collections.emptyList())) {
                LocalDateTime peakStart = dt.atTime(peakWindow.getLeft());
                LocalDateTime peakEnd = dt.atTime(peakWindow.getRight());

                if (segmentStart.isBefore(peakEnd) && segmentEnd.isAfter(peakStart)) {
                    return true;
                }
            }
        }
        return false;
    }
}
