# Verification results

Verified on 22 October 2026 using the installed Java 17 JDK.

```powershell
javac -encoding UTF-8 -Xlint:all CabBookingChatbot.java VerifyCabBookingChatbot.java
java VerifyCabBookingChatbot
```

Compilation completed without warnings or errors. All **30 console scenarios passed**.

| Scenarios | Count | Result |
| --- | --- | --- |
| Every cab/route fare, four-digit OTP, confirmation order, cancellation lock, return to menu | 9 | Passed |
| Cancel every cab/route combination before OTP; no OTP or booking confirmation emitted | 9 | Passed |
| Blank, text, decimal, negative, huge and out-of-range input at each of the four menus | 4 | Passed |
| Four bookings in one session, mixing confirmation and cancellation | 1 | Passed |
| Input ends at each prompt, or after an invalid entry; no crash or infinite loop | 5 | Passed |
| Valid choices with surrounding spaces | 1 | Passed |
| Invalid main menu choice, then Exit; later input is not processed | 1 | Passed |

The assignment's sample booking was verified: Sedan, Hinjawadi to Wakad, 10 km at Rs. 18/km, total Rs. 180. A four-digit OTP is shared only after confirmation, followed by the cancellation lock message.

The tests run the actual application in separate Java processes with redirected console input. A ten-second timeout detects a stalled menu. Generated test files are removed after verification.

Actual test output:

```text
PASS: all 9 fares, including the Sedan 10 km / Rs. 180 sample.
PASS: cancellation before OTP for all 9 cab/route combinations.
PASS: blank, text, decimal, negative, huge and out-of-range input at every menu.
PASS: repeated confirmations and cancellations in one session.
PASS: closed input at every prompt exits without looping or crashing.
PASS: 30 console scenarios passed.
```
