import java.util.concurrent.ConcurrentHashMap;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;
import java.util.Set;

class CinemaClient {
    public static void main(String[] args) throws NotBoundException, RemoteException {

        int cinemaSize = 19;
        Registry registry = LocateRegistry.getRegistry();
        Set<Integer> seats = ConcurrentHashMap.newKeySet();
        Cinema server = (Cinema) registry.lookup(Cinema.SERVICE_NAME);

        List<Integer> list0 = List.of(1, 2, 3, 4);
        seats.clear();
        seats.addAll(list0);
        System.out.println("Adding test@" + list0 + "..." + server.reservation("test", seats));

        List<Integer> list1 = List.of(cinemaSize);
        seats.clear();
        seats.addAll(list1);
        System.out.println("Adding batwings@" + list1 + "..." + server.reservation("batw1ngs", seats));

        List<Integer> list2 = List.of(
            new Random().nextInt(0, cinemaSize),
            new Random().nextInt(0, cinemaSize),
            new Random().nextInt(0, cinemaSize)
        );
        seats.clear();
        seats.addAll(list2);
        System.out.println("Adding krokosz@" + list2 + "..." + server.reservation("krokosz", seats));

        System.out.println("Confirming test..." + server.confirmation("test"));
        try {
            Thread.sleep(5000);
            System.out.println("Waiting...");
        } catch (Exception e) {
            System.out.println("LOL, maybe not!");
        }
        System.out.println("Confirming batw1ngs..." + server.confirmation("batw1ngs"));

        System.out.println("whoHasSeat nr1..." + server.whoHasReservation(1));
        System.out.println("whoHasSeat nr19..." + server.whoHasReservation(19));
    }
}
