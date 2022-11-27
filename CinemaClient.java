import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Adler32;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

class CinemaClient {
    public static void main(String[] args) throws NotBoundException, RemoteException {

        Registry registry = LocateRegistry.getRegistry();
        Cinema server = (Cinema) registry.lookup(Cinema.SERVICE_NAME);

        Set<Integer> seats = ConcurrentHashMap.newKeySet();

        seats.addAll(List.of(1, 2, 3, 4));
        server.reservation("test", seats);

        seats.forEach(e -> e += 1);
        server.reservation("null", seats);


        server.confirmation("test");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            System.out.println("LOL!");
        }
        server.confirmation("null");

        server.whoHasReservation(1);
    }
}
