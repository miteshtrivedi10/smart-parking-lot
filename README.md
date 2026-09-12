# smart-lot (This is part of take home exercise for [SAHAJ SOFTWARE](https://sahaj.ai/))

`smart-lot` is a Java 21 Maven project that models a parking-lot engine with slot allocation, ticketing, unparking, and invoice generation using multiple pricing policies.

## Current Scope

The project currently supports:

- Parking-complex initialization with one enabled site and one building.
- Vehicle-specific parking slots for motorcycles, cars, and buses.
- Ticket creation when a vehicle is parked.
- Duplicate active-vehicle detection by vehicle type and vehicle number.
- Slot release when a vehicle is unparked.
- Invoice generation from the cheapest applicable pricing strategy.
- Standard, early-bird, and night-owl pricing policies.
- Loyalty-tier and vehicle-type multipliers.

## Tech Stack

- Java 21
- Maven
- Lombok
- Apache Commons Lang
- JUnit 5
- JaCoCo for test coverage reports

## Project Structure

```text
src/main/java
├── SolutionDriver.java
└── com/parkinglot
    ├── domains
    │   ├── AppConstant.java
    │   ├── Invoice.java
    │   ├── ParkingBuilding.java
    │   ├── ParkingComplexSite.java
    │   ├── ParkingSlot.java
    │   ├── ParkingTicket.java
    │   ├── PricingResult.java
    │   └── enums
    ├── pricing
    │   ├── EarlyBirdSpecialRate.java
    │   ├── NightOwlSpecialRate.java
    │   ├── PricingStrategy.java
    │   ├── SpecialRatePolicy.java
    │   └── StandardRatePolicy.java
    ├── repository
    │   ├── InitialiseSystem.java
    │   └── ParkingEngine.java
    └── services
        ├── InitialisingTheSystem.java
        └── ParkingEngineImpl.java
```

## Domain Model

`ParkingComplexSite` is the root aggregate for a parking site. It has a site name, an enabled flag, and a map of buildings keyed by building name.

`ParkingBuilding` owns parking slots grouped by `VehicleType`. It returns the first available slot matching the requested vehicle type.

`ParkingSlot` represents a concrete slot. It has a generated parking ID, level, supported vehicle type, and occupancy state.

`ParkingTicket` represents an active or completed parking session. It captures vehicle details, assigned site/building/slot, loyalty tier, entry time, exit time, and completion state.

`Invoice` represents the generated billing output for a ticket.

## Vehicle And Loyalty Configuration

Vehicle rate multipliers:

- `MotorCycle`: `0.8`
- `Car`: `1.0`
- `Bus`: `2.0`

Loyalty discounts:

- `NONE`: `0.0`
- `SILVER`: `10.0`
- `GOLD`: `20.0`
- `PLATINUM`: `30.0`

These values are represented as whole-number percentages in code.

Spot support:

- `COMPACT`: motorcycle
- `NORMAL`: car
- `LARGE`: bus

## Initialization

`InitialisingTheSystem` creates:

- Site: `parking-complex-A`
- Building: `bldg-1`
- Motorcycle slots: 4
- Car slots: 2
- Bus slots: 2

## Parking Flow

1. `ParkingEngineImpl.parkVehicle(...)` checks whether the same vehicle type and vehicle number are already parked.
2. It scans buildings for the first available slot that supports the requested vehicle type.
3. If a slot exists, the slot is marked occupied and a `ParkingTicket` is returned.
4. If no slot exists, or the vehicle is already parked, `null` is returned.

## Unparking Flow

1. `ParkingEngineImpl.unParkVehicle(...)` verifies that the ticket is currently active.
2. The ticket is marked completed and receives an exit timestamp.
3. The slot is released.
4. The ticket is removed from the active ticket set and stored in the archive list.

Unknown tickets are ignored.

## Pricing Policies

`ParkingEngineImpl.generateInvoice(...)` evaluates all applicable pricing strategies and selects the lowest amount.

### Standard Rate

- Progressive car rates: first hour `$5.00`, second hour `$3.00`, every later hour `$2.00`.
- Vehicle multiplier is applied to each hourly segment.
- Weekday peak windows:
  - `07:00` to `10:00`
  - `16:00` to `19:00`
- Peak surcharge multiplier: `1.5`
- Parking duration is rounded up to the nearest full hour.
- Each rounded hourly segment is checked independently for any peak-window overlap.

### Early Bird Special

- Base rate: `15.0`
- Entry must be between `06:00` and `09:00`.
- Exit must be between `15:30` and `19:00`.
- Entry and exit must be on the same calendar day.
- Duration must not exceed 15 hours.
- Current implementation returns the loyalty discount amount for this flat rate, not the final discounted payable amount.

### Night Owl Special

- Base rate: `8.0`
- Entry must be between `18:00` and `23:59`.
- Exit must be between `05:00` and `10:00`.
- Exit must be exactly one calendar day after entry.
- Duration must not exceed 18 hours.
- Current implementation returns the loyalty discount amount for this flat rate, not the final discounted payable amount.

## Solution Driver Use Cases

`SolutionDriver` runs deterministic example scenarios and prints the `ParkingTicket` and `Invoice` after each scenario:

- Standard hourly pricing for a car with no peak overlap.
- Standard hourly pricing for a motorcycle with partial peak overlap.
- Standard hourly pricing for a bus on a weekend.
- Early Bird Special for a car with `SILVER` loyalty.
- Early Bird Special for a bus with `GOLD` loyalty and vehicle multiplier.
- Night Owl Special for a motorcycle with `PLATINUM` loyalty.
- Night Owl Special for a car with no loyalty.
- Best-value policy selection when Standard and Early Bird are both applicable.
- Extended stay over 24 hours, where special rates are invalid and Standard applies.
- Parking lifecycle behavior: duplicate rejection, capacity exhaustion, unpark, and slot reuse.

The driver sets ticket timestamps with reflection so each pricing use case is repeatable.

## Running The Project

Compile:

```bash
mvn compile
```

Run tests:

```bash
mvn test
```

Run the sample driver:

```bash
mvn -q exec:java -Dexec.mainClass=SolutionDriver
```

Generate test coverage:

```bash
mvn test
```

JaCoCo writes the HTML report to:

```text
target/site/jacoco/index.html
```

## Test Coverage

The unit test suite covers:

- Enum rate and discount mappings.
- Slot occupancy state transitions.
- Building slot allocation and filtering.
- Site building registration.
- Ticket completion and invoice field mapping.
- System initialization and configured capacity.
- Parking duplicate detection.
- Unparking and slot reuse.
- Invoice policy selection.
- Standard, early-bird, and night-owl pricing behavior.

## Current Implementation Notes

The README reflects the current code behavior. A few areas should be reviewed before treating the engine as production-ready:

- `ParkingEngineImpl.parkVehicle(...)` returns `null` for duplicate vehicles or full capacity instead of a structured result or exception.
- `ParkingTicket` captures timestamps internally with `LocalDateTime.now()`, which makes deterministic pricing tests harder without reflection or a clock abstraction.
- `SpecialRatePolicy.calculate(...)` currently returns only the loyalty discount amount. Per the PDF requirement, it should subtract the discount from the vehicle-adjusted flat rate.
- `ParkingTicket.equals(...)` casts without checking the object type.
- The package name `repository` currently contains interfaces, not persistence implementations.
