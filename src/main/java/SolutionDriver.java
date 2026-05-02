import com.parkinglot.domains.Invoice;
import com.parkinglot.domains.ParkingComplexSite;
import com.parkinglot.domains.ParkingTicket;
import com.parkinglot.domains.enums.LoyaltyTier;
import com.parkinglot.domains.enums.VehicleType;
import com.parkinglot.repository.InitialiseSystem;
import com.parkinglot.repository.ParkingEngine;
import com.parkinglot.services.InitialisingTheSystem;
import com.parkinglot.services.ParkingEngineImpl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class SolutionDriver {

    public static void main(String[] args) {
        runInvoiceUseCase(
                "1. Standard hourly rate - car, weekday, no peak overlap",
                VehicleType.Car,
                "STD-CAR-001",
                LoyaltyTier.NONE,
                LocalDateTime.of(2026, 5, 4, 11, 0),
                LocalDateTime.of(2026, 5, 4, 13, 0)
        );

        runInvoiceUseCase(
                "2. Standard hourly rate - motorcycle, partial peak overlap",
                VehicleType.MotorCycle,
                "STD-MC-001",
                LoyaltyTier.NONE,
                LocalDateTime.of(2026, 5, 4, 6, 30),
                LocalDateTime.of(2026, 5, 4, 8, 10)
        );

        runInvoiceUseCase(
                "3. Standard hourly rate - bus, weekend has no peak surcharge",
                VehicleType.Bus,
                "STD-BUS-001",
                LoyaltyTier.NONE,
                LocalDateTime.of(2026, 5, 3, 9, 0),
                LocalDateTime.of(2026, 5, 3, 12, 15)
        );

        runInvoiceUseCase(
                "4. Early Bird Special - car with SILVER loyalty",
                VehicleType.Car,
                "EB-CAR-001",
                LoyaltyTier.SILVER,
                LocalDateTime.of(2026, 5, 4, 7, 0),
                LocalDateTime.of(2026, 5, 4, 16, 0)
        );

        runInvoiceUseCase(
                "5. Early Bird Special - bus with GOLD loyalty and vehicle multiplier",
                VehicleType.Bus,
                "EB-BUS-001",
                LoyaltyTier.GOLD,
                LocalDateTime.of(2026, 5, 4, 8, 30),
                LocalDateTime.of(2026, 5, 4, 18, 30)
        );

        runInvoiceUseCase(
                "6. Night Owl Special - motorcycle with PLATINUM loyalty",
                VehicleType.MotorCycle,
                "NO-MC-001",
                LoyaltyTier.PLATINUM,
                LocalDateTime.of(2026, 5, 4, 22, 0),
                LocalDateTime.of(2026, 5, 5, 6, 0)
        );

        runInvoiceUseCase(
                "7. Night Owl Special - car with no loyalty",
                VehicleType.Car,
                "NO-CAR-001",
                LoyaltyTier.NONE,
                LocalDateTime.of(2026, 5, 4, 18, 0),
                LocalDateTime.of(2026, 5, 5, 10, 0)
        );

        runInvoiceUseCase(
                "8. Best-value policy selection - Standard and Early Bird both valid, Early Bird wins",
                VehicleType.Car,
                "BEST-CAR-001",
                LoyaltyTier.NONE,
                LocalDateTime.of(2026, 5, 4, 6, 30),
                LocalDateTime.of(2026, 5, 4, 16, 0)
        );

        runInvoiceUseCase(
                "9. Extended stay over 24 hours - special rates invalid, Standard applies",
                VehicleType.Car,
                "LONG-CAR-001",
                LoyaltyTier.PLATINUM,
                LocalDateTime.of(2026, 5, 4, 7, 0),
                LocalDateTime.of(2026, 5, 5, 8, 30)
        );

        runParkingLifecycleUseCases();
    }

    private static void runInvoiceUseCase(String title,
                                          VehicleType vehicleType,
                                          String vehicleNumber,
                                          LoyaltyTier loyaltyTier,
                                          LocalDateTime entryTime,
                                          LocalDateTime exitTime) {
        ParkingEngine parkingEngine = new ParkingEngineImpl(setupParkingComplexSite());
        ParkingTicket parkingTicket = parkingEngine.parkVehicle(vehicleType, vehicleNumber, loyaltyTier);

        if (parkingTicket == null) {
            printScenario(title, null, null, "Vehicle could not be parked.");
            return;
        }

        setTicketTimes(parkingTicket, entryTime, exitTime);
        Invoice invoice = parkingEngine.generateInvoice(parkingTicket);
        printScenario(title, parkingTicket, invoice, null);
    }

    private static void runParkingLifecycleUseCases() {
        String title = "10. Parking lifecycle - duplicate rejection, capacity exhaustion, unpark, and slot reuse";
        ParkingEngine parkingEngine = new ParkingEngineImpl(setupParkingComplexSite());

        ParkingTicket firstCar = parkingEngine.parkVehicle(VehicleType.Car, "LIFE-CAR-001", LoyaltyTier.NONE);
        ParkingTicket duplicateCar = parkingEngine.parkVehicle(VehicleType.Car, "life-car-001", LoyaltyTier.GOLD);
        ParkingTicket secondCar = parkingEngine.parkVehicle(VehicleType.Car, "LIFE-CAR-002", LoyaltyTier.NONE);
        ParkingTicket thirdCarWhenFull = parkingEngine.parkVehicle(VehicleType.Car, "LIFE-CAR-003", LoyaltyTier.NONE);

        parkingEngine.unParkVehicle(firstCar);
        setTicketTimes(firstCar,
                LocalDateTime.of(2026, 5, 4, 10, 0),
                LocalDateTime.of(2026, 5, 4, 11, 0));
        Invoice firstCarInvoice = parkingEngine.generateInvoice(firstCar);
        ParkingTicket reusedSlotTicket = parkingEngine.parkVehicle(VehicleType.Car, "LIFE-CAR-004", LoyaltyTier.SILVER);

        System.out.println("\n============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("First ParkingTicket:");
        System.out.println(firstCar);
        System.out.println("First Invoice:");
        System.out.println(firstCarInvoice);
        System.out.println("Duplicate ParkingTicket:");
        System.out.println(duplicateCar);
        System.out.println("Second ParkingTicket:");
        System.out.println(secondCar);
        System.out.println("Third car when capacity is full:");
        System.out.println(thirdCarWhenFull);
        System.out.println("ParkingTicket after slot reuse:");
        System.out.println(reusedSlotTicket);
    }

    private static ParkingComplexSite setupParkingComplexSite() {
        InitialiseSystem initialiseSystem = new InitialisingTheSystem();
        return initialiseSystem.setupParkingComplexSite();
    }

    private static void printScenario(String title, ParkingTicket parkingTicket, Invoice invoice, String note) {
        System.out.println("\n============================================================");
        System.out.println(title);
        System.out.println("============================================================");

        if (note != null) {
            System.out.println(note);
        }

        System.out.println("ParkingTicket:");
        System.out.println(parkingTicket);
        System.out.println("Invoice:");
        System.out.println(invoice);
    }

    private static void setTicketTimes(ParkingTicket ticket, LocalDateTime entryTime, LocalDateTime exitTime) {
        setField(ticket, "entryTime", entryTime);
        setField(ticket, "exitTime", exitTime);
    }

    private static void setField(ParkingTicket ticket, String fieldName, Object value) {
        try {
            Field field = ParkingTicket.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(ticket, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set ParkingTicket." + fieldName, e);
        }
    }
}
