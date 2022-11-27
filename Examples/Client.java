import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Client {
    public static void main(String[] args) throws NotBoundException, RemoteException {

        Registry registry = LocateRegistry.getRegistry();
        MessengerService server = (MessengerService) registry.lookup("MessengerService");
        String responseMessage = server.sendMessage("Client Message");
        String expectedMessage = "Server Message";

        System.out.println("Server: "   + responseMessage);
        System.out.println("Expected: " + expectedMessage);
    }
}
