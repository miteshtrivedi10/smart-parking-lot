package com.parkinglot.domains;

import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public final class Invoice {
    private final String invoiceId = UUID.randomUUID().toString();
    private final String ticketId;
    private final VehicleType  vehicleType;
    private final String vehicleNumber;
    private final LocalDateTime inTime;
    private final LocalDateTime outTime;
    private final LoyaltyTier loyaltyTier;
    private final String pricingPolicyApplied;
    private final double amount;
    private final LocalDateTime generatedOn;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

