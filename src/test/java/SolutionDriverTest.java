import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SolutionDriverTest {

    @Test
    void mainPrintsRequirementUseCases() {
        new SolutionDriver();
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            SolutionDriver.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String consoleOutput = output.toString(StandardCharsets.UTF_8);
        assertTrue(consoleOutput.contains("1. Standard hourly rate - car, weekday, no peak overlap"));
        assertTrue(consoleOutput.contains("8. Best-value policy selection"));
        assertTrue(consoleOutput.contains("10. Parking lifecycle"));
        assertTrue(consoleOutput.contains("ParkingTicket:"));
        assertTrue(consoleOutput.contains("Invoice:"));
    }
}
