import java.rmi.*;

interface MessengerService extends Remote {
    String sendMessage(String clientMessage) throws RemoteException;
}