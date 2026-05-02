package com.parkinglot.domains.enums;

public enum LoyaltyTier {
    NONE,
    SILVER,
    GOLD,
    PLATINUM;

    public static double getDiscountedRates(LoyaltyTier loyaltyTier) {
        return switch (loyaltyTier) {
            case NONE -> 0.0;
            case SILVER -> 10.0;
            case GOLD -> 20.0;
            case PLATINUM -> 30.0;
        };
    }
}
