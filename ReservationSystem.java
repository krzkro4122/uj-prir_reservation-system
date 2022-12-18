import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * ReservationSystem
 */
public class ReservationSystem implements Cinema {

    // Fields
    int seatsNum;
    long timeForConfirmation;

    Set<String> confirmedUsers;
    Set<Integer> unreservedSeats;
    ScheduledExecutorService executor;
    ConcurrentSkipListMap<Integer, String> seatsMap;

    // Methods
    ReservationSystem() {
        // RMI init
        try {
            // LocateRegistry.createRegistry(1099);
            Cinema stub = (Cinema) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(SERVICE_NAME, stub);
        } catch (RemoteException e) {
            System.err.println("RMI-related reservation system exception:");
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void configuration(int seats, long timeForConfirmation) {

        // Concurrent set workaround
        unreservedSeats = ConcurrentHashMap.newKeySet();
        confirmedUsers = ConcurrentHashMap.newKeySet();
        executor = Executors.newScheduledThreadPool(8);

        this.seatsNum = seats;
        this.timeForConfirmation = timeForConfirmation;

        System.out.println("[configuration] seatsMap(0): " + seatsMap);
        seatsMap = new ConcurrentSkipListMap<>();
        for (int i = 0; i < this.seatsNum; i++) {
            seatsMap.put(i, "");
            unreservedSeats.add(i);
        }
        System.out.println("[configuration] seatsMap:(1) " + seatsMap);
    }

    @Override
    public synchronized Set<Integer> notReservedSeats() {

        System.out.println("[notReservedSeats] unreservedSeats: " + unreservedSeats);
        return unreservedSeats;

    }

    @Override
    public synchronized boolean reservation(String user, Set<Integer> seats) {

        if ( confirmedUsers.contains(user) )
            return false;

        System.out.println("[reservation]1 seats: " + seats);
        System.out.println("[reservation]2 unreservedSeats: " + unreservedSeats);
        System.out.println("[reservation]3 seatsMap: " + seatsMap);
        System.out.println("[reservation]4 user: " + user);

        for (Integer seat : seats) {
            if ( !unreservedSeats.contains(seat) )
                return false;
            if ( seatsMap.containsValue(user) )
                return false;
        }

        // Mark the seats as reserved under the requesting user's username
        unreservedSeats.removeAll(seats);
        seats.forEach( (seat) -> { seatsMap.replace(seat, user); });

        // Mark the reserved seats as available after the delay
        executor.schedule(new Runnable() {
            @Override
            public synchronized void run() {

                if ( confirmedUsers.contains(user) )
                    return;

                System.out.println("-- EXPIRED -- user: " + user);
                System.out.println("[" + Thread.currentThread().getId() + "] confirmedUsers: " + confirmedUsers);
                System.out.println("[" + Thread.currentThread().getId() + "] unreservedSeats: " + unreservedSeats + ", seats: " + seats);
                unreservedSeats.addAll(seats);

                for (int seat : seats)
                    seatsMap.replace(seat, "");

            };
        }, timeForConfirmation, TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public synchronized boolean confirmation(String user) {
        System.out.println("\t[confirmation] " + user + "@seatsMap.containsValue: " + seatsMap.containsValue(user));

        if ( confirmedUsers.contains(user))
            return false;

        if ( seatsMap.containsValue(user) ){
            System.out.println("\tSuccessfull confirmation! user: " + user);
            confirmedUsers.add(user);
            return true;
        }
        return false;
    }

    @Override
    public synchronized String whoHasReservation(int seat) {
        System.out.println("[whoHasReservation] seatsMap: " + seatsMap);
        String name = seatsMap.get(seat);

        if ( confirmedUsers.contains(name) )
            return name;

        return null;
    }

    public static void main(String[] args) {
        ReservationSystem server = new ReservationSystem();
        server.configuration(10, (long) 1000);
    }
}