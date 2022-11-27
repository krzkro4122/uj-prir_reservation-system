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
    int seats;
    long timeForConfirmation;

    Set<Integer> unreservedSeats;
    ScheduledExecutorService executor;
    ConcurrentSkipListMap<Integer, String> seatsMap;

    // Methods
    ReservationSystem() {
        // RMI init
        try {
            LocateRegistry.createRegistry(1099); // TODO - create registry???
            Cinema stub =  (Cinema) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(SERVICE_NAME, stub);
        } catch (RemoteException e) {
            System.err.println("RMI-related reservation system exception:");
            e.printStackTrace();
        }
        // Concurrent set workaround
        unreservedSeats = ConcurrentHashMap.newKeySet();

        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configuration(int seats, long timeForConfirmation) {

        this.seats = seats;
        this.timeForConfirmation = timeForConfirmation;

        seatsMap = new ConcurrentSkipListMap<>();
        for (int i = 0; i < this.seats; i++)
            seatsMap.put(i, null);
    }

    @Override
    public Set<Integer> notReservedSeats() {
        return unreservedSeats;
    }

    @Override
    public boolean reservation(String user, Set<Integer> seats) { // TODO - Co jak tylko jedno miejsce sie nie zgadza?

        for (Integer seat : seats) {
            if ( !unreservedSeats.contains(seat) ) {
                return false;
            }
        }

        // Mark the seats as reserved under the requesting user's username
        unreservedSeats.removeAll(seats);
        seats.forEach( (seat) -> { seatsMap.replace(seat, user); });

        // Mark the reserved seats as available after the delay
        executor.schedule(() -> {
            unreservedSeats.addAll(seats);
        }, timeForConfirmation, TimeUnit.MILLISECONDS);
        executor.shutdown();

        return true;
    }

    @Override
    public boolean confirmation(String user) { // TODO - Co potem sie dzieje z miejscem???
        if ( seatsMap.containsValue(user) )
            return true;
        return false;
    }

    @Override
    public String whoHasReservation(int seat) {
        return seatsMap.get(seat);
    }

    public static void main(String[] args) {
        ReservationSystem server = new ReservationSystem();
        server.configuration(5000, 100);
    }
}