import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

public class RegistryFactory {
    public static void main(String[] args) throws RemoteException {
        LocateRegistry.createRegistry(0);
    }
}
