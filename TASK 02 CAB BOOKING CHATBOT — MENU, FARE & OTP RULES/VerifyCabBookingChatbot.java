import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/** Dependency-free console integration tests. Run from this project folder. */
public class VerifyCabBookingChatbot {
    private static final Pattern SHARED_OTP =
            Pattern.compile("OTP shared with you: ([1-9][0-9]{3})(?:\\r?\\n)");
    private static final String LOCK =
            "The OTP has been shared. This ride cannot be cancelled.";
    private static int passed;
    private static int runNumber;
    private static Path buildDirectory;

    public static void main(String[] args) throws Exception {
        Path workspace = Paths.get(".").toAbsolutePath().normalize();
        buildDirectory = Files.createTempDirectory(workspace, "test-build-");
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            check(compiler != null, "A JDK is required to run verification.");
            int status = compiler.run(null, null, null, "-encoding", "UTF-8",
                    "-Xlint:all", "-d", buildDirectory.toString(),
                    workspace.resolve("CabBookingChatbot.java").toString());
            check(status == 0, "Application compilation failed.");

            verifyAllFares();
            verifyAllCancellations();
            verifyInvalidInput();
            verifyRepeatedBookings();
            verifyClosedInput();
            verifyWhitespace();
            verifyExit();
            System.out.println("PASS: " + passed + " console scenarios passed.");
        } finally {
            // Delete only the temporary child directory created by this test run.
            check(buildDirectory.getParent().equals(workspace)
                    && buildDirectory.getFileName().toString().startsWith("test-build-"),
                    "Refusing to clean up outside the project test directory.");
            try (Stream<Path> paths = Files.walk(buildDirectory)) {
                List<Path> generated = paths.sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
                for (Path path : generated) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void verifyAllFares() throws Exception {
        String[] cabs = {"Auto", "Sedan", "SUV"};
        int[] rates = {12, 18, 25};
        String[] sources = {"Pune Station", "Swargate", "Hinjawadi"};
        String[] destinations = {"Shivajinagar", "Kothrud", "Wakad"};
        int[] distances = {5, 8, 10};
        int[][] expectedFares = {{60, 96, 120}, {90, 144, 180}, {125, 200, 250}};

        for (int cab = 0; cab < 3; cab++) {
            for (int route = 0; route < 3; route++) {
                String output = run("1\n" + (cab + 1) + "\n" + (route + 1)
                        + "\n1\n2\n");
                contains(output, "Cab type: " + cabs[cab]);
                contains(output, "Source: " + sources[route]);
                contains(output, "Destination: " + destinations[route]);
                contains(output, "Distance: " + distances[route] + " km");
                contains(output, "Rate per km: Rs. " + rates[cab]);
                contains(output, "Total fare: Rs. " + expectedFares[cab][route]);
                check(count(output, "=== FARE SUMMARY ===") == 1,
                        "Each booking must show one fare summary.");
                check(count(output, "OTP shared with you:") == 1,
                        "A confirmation must share exactly one OTP.");
                Matcher otp = SHARED_OTP.matcher(output);
                check(otp.find(), "The shared OTP must have four digits.");
                int otpPosition = otp.start();
                check(output.indexOf("=== CONFIRM BOOKING ===") < otpPosition,
                        "OTP must follow the confirmation prompt.");
                contains(output, "Booking confirmed!");
                contains(output, LOCK);
                int nextMainMenu = output.indexOf("=== MAIN MENU ===", otpPosition);
                check(nextMainMenu > otpPosition, "Return to the main menu after confirmation.");
                String lockedRide = output.substring(otpPosition, nextMainMenu);
                check(!lockedRide.contains("2. Cancel Ride"),
                        "A confirmed ride must offer no cancellation action.");
                check(!output.contains("Ride cancelled."),
                        "Exit after confirmation must not cancel the ride.");
                contains(output, "Goodbye!");
                passed++;
            }
        }
        System.out.println("PASS: all 9 fares, including the Sedan 10 km / Rs. 180 sample.");
    }

    private static void verifyAllCancellations() throws Exception {
        for (int cab = 1; cab <= 3; cab++) {
            for (int route = 1; route <= 3; route++) {
                String output = run("1\n" + cab + "\n" + route + "\n2\n2\n");
                contains(output, "Ride cancelled. No OTP was shared.");
                check(!output.contains("OTP shared with you:"),
                        "Cancelled rides must never share an OTP.");
                check(!output.contains("Booking confirmed!"),
                        "Cancelled rides must not be confirmed.");
                check(count(output, "=== MAIN MENU ===") == 2,
                        "Cancellation must return to the main menu.");
                passed++;
            }
        }
        System.out.println("PASS: cancellation before OTP for all 9 cab/route combinations.");
    }

    private static void verifyInvalidInput() throws Exception {
        String invalid = "\nhello\n0\n-1\n4\n1.5\n999999999999999999999999999999\n";
        String[] prefixes = {"", "1\n", "1\n2\n", "1\n2\n3\n"};
        String[] suffixes = {"1\n2\n3\n1\n2\n", "2\n3\n1\n2\n",
                "3\n1\n2\n", "1\n2\n"};
        String[] menus = {"=== MAIN MENU ===", "=== CHOOSE CAB TYPE ===",
                "=== CHOOSE ROUTE ===", "=== CONFIRM BOOKING ==="};
        for (int stage = 0; stage < prefixes.length; stage++) {
            String output = run(prefixes[stage] + invalid + suffixes[stage]);
            check(count(output, "Invalid choice.") == 7,
                    "Every invalid entry must be rejected at menu " + stage + ".");
            int expectedMenus = stage == 0 ? 9 : 8;
            check(count(output, menus[stage]) == expectedMenus,
                    "Invalid input must repeat the same menu.");
            contains(output, "Cab type: Sedan");
            contains(output, "Total fare: Rs. 180");
            check(count(output, "OTP shared with you:") == 1,
                    "Invalid input must not trigger an extra booking.");
            passed++;
        }
        System.out.println("PASS: blank, text, decimal, negative, huge and out-of-range input at every menu.");
    }

    private static void verifyRepeatedBookings() throws Exception {
        String output = run("1\n1\n1\n2\n"   // Cancel an Auto.
                + "1\n2\n3\n1\n"            // Confirm a Sedan.
                + "1\n3\n2\n2\n"            // Cancel a new SUV booking.
                + "1\n3\n3\n1\n2\n");       // Confirm another SUV, then exit.
        check(count(output, "=== FARE SUMMARY ===") == 4, "Expected four bookings.");
        check(count(output, "Ride cancelled.") == 2, "Expected two cancellations.");
        check(count(output, "OTP shared with you:") == 2, "Only confirmed rides share OTPs.");
        check(count(output, LOCK) == 2, "Each confirmed ride must be locked.");
        check(count(output, "=== MAIN MENU ===") == 5, "Main menu must keep repeating.");
        String[] summaries = output.split("=== FARE SUMMARY ===");
        int[] expectedFares = {60, 180, 200, 250};
        for (int index = 0; index < expectedFares.length; index++) {
            contains(summaries[index + 1], "Total fare: Rs. " + expectedFares[index]);
        }
        passed++;
        System.out.println("PASS: repeated confirmations and cancellations in one session.");
    }

    private static void verifyClosedInput() throws Exception {
        String[] unfinished = {"", "1\n", "1\n2\n", "1\n2\n3\n", "invalid\n"};
        for (String input : unfinished) {
            String output = run(input);
            contains(output, "Input closed.");
            contains(output, "Goodbye!");
            check(!output.contains("OTP shared with you:"),
                    "Input ending before confirmation must not share an OTP.");
            passed++;
        }
        System.out.println("PASS: closed input at every prompt exits without looping or crashing.");
    }

    private static void verifyWhitespace() throws Exception {
        String output = run(" 1 \n 2 \n 3 \n 1 \n 2 \n");
        contains(output, "Total fare: Rs. 180");
        contains(output, "Booking confirmed!");
        check(!output.contains("Invalid choice."), "Surrounding whitespace should be accepted.");
        passed++;
    }

    private static void verifyExit() throws Exception {
        String output = run("3\n2\n1\n1\n1\n1\n");
        contains(output, "Invalid choice.");
        contains(output, "Goodbye!");
        check(!output.contains("=== FARE SUMMARY ==="), "Exit must stop reading new bookings.");
        passed++;
    }

    private static String run(String input) throws Exception {
        String id = Integer.toString(++runNumber);
        Path inputFile = buildDirectory.resolve("input-" + id + ".txt");
        Path outputFile = buildDirectory.resolve("output-" + id + ".txt");
        Files.write(inputFile, input.getBytes(StandardCharsets.UTF_8));
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        Path java = Paths.get(System.getProperty("java.home"), "bin",
                windows ? "java.exe" : "java");
        Process process = new ProcessBuilder(Arrays.asList(java.toString(), "-cp",
                buildDirectory.toString(), "CabBookingChatbot"))
                .redirectInput(inputFile.toFile())
                .redirectOutput(outputFile.toFile())
                .redirectErrorStream(true)
                .start();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new AssertionError("Application did not terminate within 10 seconds.");
        }
        String output = new String(Files.readAllBytes(outputFile), StandardCharsets.UTF_8);
        check(process.exitValue() == 0, "Application crashed:\n" + output);
        return output;
    }

    private static int count(String text, String part) {
        int result = 0;
        int position = 0;
        while ((position = text.indexOf(part, position)) != -1) {
            result++;
            position += part.length();
        }
        return result;
    }

    private static void contains(String output, String expected) {
        check(output.contains(expected), "Missing output: " + expected + "\n" + output);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
