import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ReservationSystem
 */
public class ReservationSystem implements Cinema {

    // Fields
    int seatsNum;
    long timeForConfirmation;

    Set<String> cuckedUsers;
    Set<String> expiredUsers;
    Set<String> confirmedUsers;
    Set<Integer> unreservedSeats;
    ConcurrentSkipListMap<Integer, String> seatsMap;

    ScheduledExecutorService executor;

    // Methods
    ReservationSystem() {

        // RMI init
        try {
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

        System.out.println("");
        System.out.println("");

        executor = Executors.newScheduledThreadPool(8);

        // Concurrent set workaround
        unreservedSeats = ConcurrentHashMap.newKeySet();
        confirmedUsers = ConcurrentHashMap.newKeySet();
        expiredUsers = ConcurrentHashMap.newKeySet();
        cuckedUsers = ConcurrentHashMap.newKeySet();
        seatsMap = new ConcurrentSkipListMap<>();

        this.seatsNum = seats;
        this.timeForConfirmation = timeForConfirmation;

        for (int i = 0; i < this.seatsNum; i++) {

            seatsMap.put(i, "");
            unreservedSeats.add(i);

        }

    }

    @Override
    public synchronized Set<Integer> notReservedSeats() {

        System.out.println("\t\t[notReservedSeats]: " + unreservedSeats);
        return unreservedSeats;

    }

    @Override
    public synchronized boolean reservation(String user, Set<Integer> seats) {

        System.out.println("[RES] unreservedSeats: " + unreservedSeats);

        if ( confirmedUsers.contains(user) )
            return false;

        List<String> whoToCuck = new ArrayList<>();

        for (Integer seat : seats) {

            if ( !unreservedSeats.contains(seat) )
                return false;

            if ( seatsMap.get(seat) != "" ) {
                if ( expiredUsers.contains(seatsMap.get(seat)) )
                    whoToCuck.add(seatsMap.get(seat));
            }

        }

        if ( confirmedUsers.contains(user) )
            return false;

        // Mark the seats as reserved under the requesting user's username
        cuckedUsers.remove(user);
        cuckedUsers.addAll(whoToCuck);
        expiredUsers.remove(user);
        unreservedSeats.removeAll(seats);
        seats.forEach( (seat) -> { seatsMap.replace(seat, user); });

        // Mark the reserved seats as available after the delay
        executor.schedule(new Runnable() {

            @Override
            public synchronized void run() {

                if ( confirmedUsers.contains(user) )
                    return;

                expiredUsers.add(user);
                unreservedSeats.addAll(seats);

                System.out.println("-- EXPIRED -- user: " + user);

                // for (int seat : seats)
                //     seatsMap.replace(seat, "");

            };

        }, timeForConfirmation, TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public synchronized boolean confirmation(String user) {

        System.out.println("");
        System.out.println("\t[CON] unreservedSeats: " + unreservedSeats);
        System.out.println("\t[CON] " + user + "@seatsMap.containsValue: " + seatsMap.containsValue(user));
        System.out.println("\t[CON] confirmedUsers: " + confirmedUsers);
        System.out.println("\t[CON] cuckedUsers: " + cuckedUsers);

        if ( confirmedUsers.contains(user) )
            return false;

        // if (expiredUsers.contains(user) && !seatsMap.containsValue(user) ) {
        //     return false;
        // }

        if (cuckedUsers.contains(user)) {
            return false;
        }

        if ( seatsMap.containsValue(user) ) {

            System.out.println("\tSuccessfull confirmation! user: " + user);

            confirmedUsers.add(user);
            expiredUsers.remove(user);

            var seatsToFreeUp = List.of();

            for (int i = 0; i < seatsMap.size(); i++) {
                if (seatsMap.get(i) == user) {
                    seatsToFreeUp.add(i);
                }
            }

            unreservedSeats.removeAll(seatsToFreeUp);

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

        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException re) {
            System.err.println(re);
        }

        ReservationSystem server = new ReservationSystem();
        server.configuration(10, (long) 1000);

    }

    public <K, V> K getKey(Map<K, V> map, V value) {

        for (Entry<K, V> entry : map.entrySet()) {

            if (entry.getValue().equals(value)) {

                return entry.getKey();

            }
        }
        return null;
    }
}