import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import java.util.List;

class CinemaClient {
    private static class Customer {
        public Customer(String username, List<Integer> userSeats) {
            this.userSeats = userSeats;
            this.username = username;
        }

        String username;
        List<Integer> userSeats;
    }

    private static void Test2(Cinema reservationSystem) {
        try {
            System.out.println("TEST 2:");

            Random random = new Random();
            reservationSystem.configuration(10, 1000);

            String user1 = "1";
            List<Integer> user1Seats = List.of(1, 2);

            String user2 = "2";
            List<Integer> user2Seats = List.of(2, 3);

            String user3 = "3";
            List<Integer> user3Seats = List.of(3, 4);

            String user4 = "4";
            List<Integer> user4Seats = List.of(4, 5);

            String user5 = "5";
            List<Integer> user5Seats = List.of(1, 3, 5);

            List<Customer> userList = new ArrayList<>();
            userList.add(new Customer(user1, user1Seats));
            userList.add(new Customer(user2, user2Seats));
            userList.add(new Customer(user3, user3Seats));
            userList.add(new Customer(user4, user4Seats));
            userList.add(new Customer(user5, user5Seats));

            userList.parallelStream().forEach((customer) -> {
                try {
                    if(UserCreatesSubscription(customer.username, customer.userSeats, reservationSystem)){
                        Thread.sleep(random.nextInt(890,1100));
                        UserConfirmsSubscription(customer.username, reservationSystem);
                    }
                    else{
                        Thread.sleep(1000);
                        UserCreatesSubscription(customer.username, customer.userSeats, reservationSystem);
                        UserConfirmsSubscription(customer.username, reservationSystem);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.print("[END] {\n");
        try {
            List.of(1, 2, 3, 4, 5).forEach((element) -> {
                try {
                    System.out.print("\t" + element + ": " + reservationSystem.whoHasReservation(element) + ",\n");
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            System.out.print("} - ");
            System.out.println("unreservedSeats: " + reservationSystem.notReservedSeats());
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };
        System.out.println("\n");
    }

    private static boolean UserCreatesSubscription(String userName, List<Integer> seats, Cinema reservationSystem) {
        try {
            if (reservationSystem.reservation(userName, new HashSet<>(seats))) {
                System.out.println("[Reservation] [created] successfully for user: " + userName);
                return true;
            } else {
                System.out.println("[Reservation] [not created] for user: " + userName);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private static boolean UserConfirmsSubscription(String userName, Cinema reservationSystem) {
        try {
            if (reservationSystem.confirmation(userName)) {
                System.out.println("[Reservation] [CONFIRMED] successfully for user: " + userName);
                return true;
            } else {
                System.out.println("[Reservation] [NOT CONFIRMED] for user: " + userName);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) throws NotBoundException, RemoteException {

        Registry registry = LocateRegistry.getRegistry();
        Cinema server = (Cinema) registry.lookup(Cinema.SERVICE_NAME);

        Test2(server);

        // Set<Integer> seats = ConcurrentHashMap.newKeySet();
        // List<Integer> list0 = List.of(1, 2, 3, 4);
        // seats.clear();
        // seats.addAll(list0);
        // System.out.println("Adding test@" + list0 + "..." + server.reservation("test", seats));

        // List<Integer> list1 = List.of(cinemaSize);
        // seats.clear();
        // seats.addAll(list1);
        // System.out.println("Adding batwings@" + list1 + "..." + server.reservation("batw1ngs", seats));

        // List<Integer> list2 = List.of(
        //     new Random().nextInt(0, cinemaSize),
        //     new Random().nextInt(0, cinemaSize),
        //     new Random().nextInt(0, cinemaSize)
        // );
        // seats.clear();
        // seats.addAll(list2);
        // System.out.println("Adding krokosz@" + list2 + "..." + server.reservation("krokosz", seats));

        // System.out.println("Confirming test..." + server.confirmation("test"));
        // try {
        //     Thread.sleep(2000);
        //     System.out.println("Waiting...");
        // } catch (Exception e) { System.out.println("LOL, maybe not!"); }
        // System.out.println("Confirming batw1ngs..." + server.confirmation("batw1ngs"));

        // System.out.println("whoHasSeat nr1..." + server.whoHasReservation(1));
        // System.out.println("whoHasSeat nr19..." + server.whoHasReservation(19));
    }
}
