package com.parkinglot.testsupport;

import com.parkinglot.domains.ParkingTicket;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public final class ParkingTicketTestSupport {

    private ParkingTicketTestSupport() {
    }

    public static ParkingTicket withTimes(ParkingTicket ticket,
                                          LocalDateTime entryTime,
                                          LocalDateTime exitTime) {
        setField(ticket, "entryTime", entryTime);
        setField(ticket, "exitTime", exitTime);
        return ticket;
    }

    private static void setField(ParkingTicket ticket, String fieldName, Object value) {
        try {
            Field field = ParkingTicket.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(ticket, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to set ParkingTicket." + fieldName, e);
        }
    }
}
