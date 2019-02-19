The assignment is to design and write a Ticket Service that provides the following functions:
 - Find the number of seats available within the venue.
   Note: available seats are seats that are neither held nor reserved
 - Find and hold the best available seats on behalf of a customer .
   Note: each ticket hold should expire within a set number of seconds. 
 - Reserve and commit a specific group of held seats for a customer.

The solution comes up with a 2D array for fast index access and disk-based storage.
PriorityQueue data structures are used to wrapper for the best available seats and normal seats. 
A thread safe is ensured for data access to these structures.
The best seats are defined as best rows in between. For example for 10 rows in venue,
the best rows are from 2 to 6.
A seat hold is temporarily stored in a map and removed from the map for a reservation or 
an expiration. If the seat hold comes to a reservation, its seat states are set to Reserved.
A timer thread is created to count down and check if the seat hold is expired. When this 
occurs, expired held seats are set their states back to Available.

All seat info (available, held, reserved) is periodically stored in a file system. 
When the system starts, it restores the seat info into the repo. Restored Held states 
are set to Available after the system restarts.

Another solution came up for the first thought, using different Maps to store different states 
available, held, reserved. With this solution, keys are designed as row num and column num 
e.g. 0105 means seat at row 01 and col 05. This design might be faster then the above one. 
But the data structures are more complex.

The zip file contains TicketService and TicketClient. TicketService also includes the test cases exectued when built. TicketClient is used to send restful requests to test TicketService. Unzip the file before building and running.

Steps to build and run TicketService:
1. Change directory to TicketService:
   cd TicketService
2. Build TicketService with running unit tests:
   mvn install
   (or Build TicketService without running unit tests:
   mvn install -DskipTests or mvn install -Dmaven.test.skip=true
3. Change directory to Target:
   cd target
4. Run below command to start TicketService:
   java -jar TicketSerivce-0.0.1-SNAPSHOT.jar

Steps to build and run TicketClient:
1. Change directory to TicketClient:
   cd TicketClient
2. Build TicketClient:
   mvn install
3. Change directory to Target:
   cd target
4. Run below command to start TicketClient:
   java -jar TicketClient-0.0.1-SNAPSHOT.jar
5. The console shows one of the following functions:
1-numSeatsAvailable: to get the number of seats available
2-findAndHoldSeats: allows to input number of requested seat and customer email to find and hold seats.
  The seat hold will be expired in 20s by default or it is reserved by below function.
3-reserveSeats: allows to reserve the previous seat hold (at function 2).
4-printSeatMap: addtional function to print the seat info at server (easily to know detailed seat info).
5-forceToStoreData: addtional fucntion to force to store seat info to file (for testing purpose only).
6-forceToRestoreData: addtional fucntion to force to restore seat info from file (for testing purpose only).
0-exit: exit TicketClient.


Tests:
Below are basic test cases included when TicketService built:
1) test the number of seats available (cmd 1)
2) test the findAndHoldSeats (cmd 2) and reserveSeats (cmd 3) successfully.
3) test the findAndHoldSeats (cmd 2) and the seat hold is timeout. The reserveSeats (cmd 3) is failed.
   The Held states of the hold is reset back to Available.
4) test invalid inputs for findAndHoldSeats: negative or zero of num of seats, invalid email, ...
5) additional tests: force to save the seat map info to file (cmd 5), and force to restore save data back to the system (cmd 6) and combine with print seat info map (cmd 4) and get number of available seats (cmd 1). 


