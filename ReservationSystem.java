import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import java.rmi.server.UnicastRemoteObject;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Set;


public class ReservationSystem implements Cinema {

    // F I E L D S

    private ScheduledExecutorService executor;
    private Set<Integer> unreservedSeats;
    private long timeForConfirmation;
    private ArrayList<Order> orders;

    // M E M B E R   C L A S S E S

    private class Order {

        public String username;
        public Set<Integer> wantedSeats;

        public boolean afterExpiry = false;
        public boolean afterConfirmation = false;

        public Order(Set<Integer> wantedSeats, String username) {

            this.wantedSeats = wantedSeats;
            this.username = username;

            Order concreteOrder = this;

            // Set the order's timeout
            executor.schedule(new Runnable() {

                @Override
                public synchronized void run() {

                    if ( concreteOrder.afterConfirmation ) {

                        return;
                    }

                    unreservedSeats.addAll(concreteOrder.wantedSeats);
                    concreteOrder.afterExpiry = true;
                }

            }, timeForConfirmation, TimeUnit.MILLISECONDS);

        }

    }

    // M E T H O D S

    ReservationSystem() {

        // RMI init
        try {

            Cinema stub = (Cinema) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(SERVICE_NAME, stub);

        } catch ( RemoteException e ) {

            System.err.println("RMI-related reservation system exception:");
            e.printStackTrace();

        }

    }

    @Override
    public void configuration(int seats, long timeForConfirmation) {

        this.timeForConfirmation = timeForConfirmation;

        orders = new ArrayList<Order>(seats);

        // Concurrent set workaround
        unreservedSeats = ConcurrentHashMap.newKeySet();

        for ( int i = 0; i < seats; i++ ) {

            unreservedSeats.add(i);

        }

        executor = new ScheduledThreadPoolExecutor(8);
    }

    @Override
    public synchronized Set<Integer> notReservedSeats() {

        return unreservedSeats;

    }

    @Override
    public synchronized boolean reservation(String user, Set<Integer> seats) {

        for ( int seat : seats ) {

            if ( unreservedSeats.contains(seat) ) {

                continue;

            }

            return false;

        }

        orders.add(new Order(seats, user));
        unreservedSeats.removeAll(seats);

        return true;
    }

    @Override
    public synchronized boolean confirmation(String user) {

        for ( Order order : orders ) {

            if ( order.username.equals(user) ) {

                if ( order.afterExpiry ) {

                    for ( int seat : order.wantedSeats ) {

                        if ( ! unreservedSeats.contains(seat) ) {

                            orders.remove(order);

                            return false;

                        }

                    }

                }

                order.afterConfirmation = true;
                unreservedSeats.removeAll(order.wantedSeats);

                return true;

            }

        }

        return false;
    }

    @Override
    public synchronized String whoHasReservation(int seat) {

        for ( Order order : orders ) {

            if ( order.wantedSeats.contains(seat) ) {

                if ( order.afterConfirmation ) {

                    return order.username;

                }

            }

        }

        return null;
    }

    // M A I N

    public static void main(String[] args) {

        try {

            LocateRegistry.createRegistry(1099);

        } catch ( RemoteException re ) {

            System.err.println(re);

        }

        ReservationSystem server = new ReservationSystem();
        server.configuration(10, (long) 1000);
    }
}