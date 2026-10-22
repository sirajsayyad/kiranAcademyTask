# Task 02: Cab Booking Chatbot

A beginner-friendly Java console application for the Kiran Academy task dated 16 October 2026. It uses `Scanner`, conditional statements, fixed route distances and cab rates, and a `while (true)` main menu. Only the Java standard library is needed.

## Run

Install a JDK (Java 8 or later), open a terminal in this folder, and run:

```powershell
javac CabBookingChatbot.java
java CabBookingChatbot
```

## Booking flow

1. Choose **1. Book Ride** from the main menu.
2. Select Auto, Sedan, or SUV.
3. Select one of the three fixed routes.
4. Review the cab, source, destination, distance, rate, and total fare.
5. Choose **1. Confirm & Share OTP** or **2. Cancel Ride**.
6. Confirmation generates and shares a four-digit OTP (1000-9999). The ride is then locked against cancellation. Cancellation before confirmation shares no OTP.
7. The main menu appears again. Choose **2. Exit** to quit.

Invalid input, including text, blank input, and out-of-range numbers, shows an error and repeats the current menu. Ending input also exits cleanly. `Rs.` denotes Indian rupees and keeps the output compatible with Windows terminals.

## Routes and fares

| Route | Source | Destination | Distance |
| --- | --- | --- | --- |
| 1 | Pune Station | Shivajinagar | 5 km |
| 2 | Swargate | Kothrud | 8 km |
| 3 | Hinjawadi | Wakad | 10 km |

Fare = distance multiplied by the selected cab's rate.

| Cab | Rate per km | Route 1 | Route 2 | Route 3 |
| --- | --- | --- | --- | --- |
| Auto | Rs. 12 | Rs. 60 | Rs. 96 | Rs. 120 |
| Sedan | Rs. 18 | Rs. 90 | Rs. 144 | Rs. 180 |
| SUV | Rs. 25 | Rs. 125 | Rs. 200 | Rs. 250 |

## Sample scenario

Enter these choices on separate lines:

```text
1
2
3
1
2
```

This books a Sedan from Hinjawadi to Wakad: `10 x 18 = Rs. 180`. The app shares a four-digit OTP after confirmation, displays the cancellation lock, returns to the main menu, and exits. The OTP changes between bookings and may occasionally repeat.

## Automated verification

The dependency-free Java test runner compiles the application into a temporary directory inside this folder, exercises its console input and output, and removes its generated files when done.

```powershell
javac VerifyCabBookingChatbot.java
java VerifyCabBookingChatbot
```

Checks cover all nine fares, the sample scenario, cancellation before OTP sharing, invalid input at every menu, repeated bookings, exit behavior, and input ending at each prompt. See `TEST_RESULTS.md` for the recorded verification.

## Submission details from the assignment

Upload the Java source to your GitHub repository. The assignment requests an email containing your full name and repository URL, with no source attachments.

- Recipient: `9011@thekiranacademy.com`
- Subject: `Task 02 – Cab Booking Chatbot – 16/10/2026`

The local implementation does not upload code or send email.
