import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class MessengerServiceImpl implements MessengerService {

    @Override
    public String sendMessage(String clientMessage) {
        return "Client Message".equals(clientMessage) ? "Server Message" : null;
    }

    public String unexposedMethod() {
        return "Fuck off";
    }

    public static void main(String[] args) throws RemoteException {

        MessengerService server = new MessengerServiceImpl();
        MessengerService stub = (MessengerService) UnicastRemoteObject.exportObject(
            (MessengerService) server, 0
        );
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("MessengerService", stub);
    }
}