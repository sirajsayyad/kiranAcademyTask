import java.util.Random;
import java.util.Scanner;

/**
 * Kiran Academy - Task 02: Cab Booking Chatbot.
 * Uses fixed routes and fares, with cancellation allowed only before OTP sharing.
 */
public class CabBookingChatbot {
    private static final int AUTO_RATE = 12;
    private static final int SEDAN_RATE = 18;
    private static final int SUV_RATE = 25;

    private static final int PUNE_STATION_DISTANCE = 5;
    private static final int SWARGATE_DISTANCE = 8;
    private static final int HINJAWADI_DISTANCE = 10;

    private static final String MAIN_MENU =
            "\n=== MAIN MENU ===\n"
            + "1. Book Ride\n"
            + "2. Exit";

    private static final String CAB_MENU =
            "\n=== CHOOSE CAB TYPE ===\n"
            + "1. Auto  - Rs. " + AUTO_RATE + "/km\n"
            + "2. Sedan - Rs. " + SEDAN_RATE + "/km\n"
            + "3. SUV   - Rs. " + SUV_RATE + "/km";

    private static final String ROUTE_MENU =
            "\n=== CHOOSE ROUTE ===\n"
            + "1. Pune Station to Shivajinagar - " + PUNE_STATION_DISTANCE + " km\n"
            + "2. Swargate to Kothrud - " + SWARGATE_DISTANCE + " km\n"
            + "3. Hinjawadi to Wakad - " + HINJAWADI_DISTANCE + " km";

    private static final String CONFIRMATION_MENU =
            "\n=== CONFIRM BOOKING ===\n"
            + "1. Confirm & Share OTP\n"
            + "2. Cancel Ride";

    public static void main(String[] args) {
        Random random = new Random();
        System.out.println("Welcome to the Cab Booking Chatbot!");

        try (Scanner scanner = new Scanner(System.in)) {
            // Return here after every completed or cancelled booking.
            while (true) {
                int mainChoice = readChoice(scanner, MAIN_MENU, 2);
                if (mainChoice == 2 || mainChoice == -1) {
                    break;
                }

                int cabChoice = readChoice(scanner, CAB_MENU, 3);
                if (cabChoice == -1) {
                    break;
                }

                String cabType;
                int ratePerKm;
                if (cabChoice == 1) {
                    cabType = "Auto";
                    ratePerKm = AUTO_RATE;
                } else if (cabChoice == 2) {
                    cabType = "Sedan";
                    ratePerKm = SEDAN_RATE;
                } else {
                    cabType = "SUV";
                    ratePerKm = SUV_RATE;
                }

                int routeChoice = readChoice(scanner, ROUTE_MENU, 3);
                if (routeChoice == -1) {
                    break;
                }

                String source;
                String destination;
                int distance;
                if (routeChoice == 1) {
                    source = "Pune Station";
                    destination = "Shivajinagar";
                    distance = PUNE_STATION_DISTANCE;
                } else if (routeChoice == 2) {
                    source = "Swargate";
                    destination = "Kothrud";
                    distance = SWARGATE_DISTANCE;
                } else {
                    source = "Hinjawadi";
                    destination = "Wakad";
                    distance = HINJAWADI_DISTANCE;
                }

                int totalFare = ratePerKm * distance;
                System.out.println("\n=== FARE SUMMARY ===");
                System.out.println("Cab type: " + cabType);
                System.out.println("Source: " + source);
                System.out.println("Destination: " + destination);
                System.out.println("Distance: " + distance + " km");
                System.out.println("Rate per km: Rs. " + ratePerKm);
                System.out.println("Total fare: Rs. " + totalFare);
                System.out.println("Calculation: " + distance + " x " + ratePerKm
                        + " = Rs. " + totalFare);
                System.out.println("You may cancel now, before the OTP is shared.");

                int confirmation = readChoice(scanner, CONFIRMATION_MENU, 2);
                if (confirmation == -1) {
                    break;
                }
                if (confirmation == 2) {
                    System.out.println("Ride cancelled. No OTP was shared.");
                    continue;
                }

                // Generate and share an OTP only after explicit confirmation.
                int otp = 1000 + random.nextInt(9000);
                System.out.println("OTP shared with you: " + otp);
                System.out.println("Booking confirmed!");
                System.out.println("The OTP has been shared. This ride cannot be cancelled.");
                // There is no cancellation action for a confirmed ride.
            }
        }

        System.out.println("\nGoodbye! Thank you for using the Cab Booking Chatbot.");
    }

    /**
     * Repeats the current menu until a valid choice is entered.
     * Returns -1 if input ends, so redirected input also exits cleanly.
     */
    private static int readChoice(Scanner scanner, String menu, int maximumChoice) {
        while (true) {
            System.out.println(menu);
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) {
                System.out.println("\nInput closed.");
                return -1;
            }

            String input = scanner.nextLine().trim();
            for (int choice = 1; choice <= maximumChoice; choice++) {
                if (input.equals(Integer.toString(choice))) {
                    return choice;
                }
            }

            System.out.println("Invalid choice. Please enter a number from 1 to "
                    + maximumChoice + ".");
        }
    }
}
