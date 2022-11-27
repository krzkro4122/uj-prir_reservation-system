import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * Interfejs serwisu rezerwacji miesc
 */
public interface Cinema extends Remote {

	public static String SERVICE_NAME = "CINEMA";

	/**
	 * Metoda konfigurujÄca parametry systemu.
	 *
	 * @param seats               liczba miejsc
	 * @param timeForConfirmation czas w milisekundach na potwierdzenie rezerwacji
	 * @throws RemoteException wyjÄtek wymagany przez RMI
	 */
	public void configuration(int seats, long timeForConfirmation) throws RemoteException;

	/**
	 * Miejsca, ktĂłre moĹźna zarezerwowaÄ. ZbiĂłr nie zawiera miejsc, ktĂłrych
	 * rezerwacja zostaĹa juĹź potwierdzona i tych, ktĂłrych czas oczekiwania na
	 * potwierdzenie rezerwacji jeszcze nie minÄĹ.
	 *
	 * @return zbiĂłr numerĂłw niezarezerwowanych miejsc
	 * @throws RemoteException wyjÄtek wymagany przez RMI
	 */
	public Set<Integer> notReservedSeats() throws RemoteException;

	/**
	 * ZgĹoszenie rezerwacji przez uĹźytkownika o podanej nazwie.
	 *
	 * @param user  nazwa uĹźytkownika serwisu
	 * @param seats zbiĂłr miejsc, ktĂłre uĹźytkownik serwisu chce zarejestrowaÄ.
	 * @return true - miejsca o podanych numerach mogÄ zostaÄ zarezerwowane. false -
	 *         rezerwacja nie moĹźe byÄ zrealizowana, bo nie wszystkie wymienione
	 *         miesca sÄ dostÄpne
	 * @throws RemoteException wyjÄtek wymagany przez RMI
	 */
	public boolean reservation(String user, Set<Integer> seats) throws RemoteException;

	/**
	 * Po uzyskaniu potwierdzenia, Ĺźe miejsca mogÄ byÄ zarezerwowane uĹźytkownik musi
	 * rezerwacjÄ jeszcze potwierdziÄ. Rezerwacja wykonana w czasie
	 * timeForConfirmation od uzyskania informacji o dostÄpnoĹci musi zostaÄ przez
	 * system zaakceptowana i zarezerwowane miejsca nie mogÄ byÄ przez ten czas
	 * nikomu oferowane. JeĹli potwierdzenie pojawi siÄ pĂłĹşniej, nie ma gwaracji, Ĺźe
	 * miejsca sÄ jeszcze dostÄpne.
	 *
	 * @param user nazwa uĹźytkownika serwisu
	 * @return true - rezerwacja miesc potwierdzona, false - miejsca nie sÄ juĹź
	 *         dostÄpne (tylko w przypadku spoĹşnionego potwierdzenia i rezerwacji
	 *         (nawet niepotwierdzonej) miejsca przez kogoĹ innego)
	 * @throws RemoteException wyjÄtek wymagany przez RMI
	 */
	public boolean confirmation(String user) throws RemoteException;

	/**
	 * Informacja o uĹźytkowniku, ktĂłry dokonaĹ potwierdzonej rezerwacji miejsca. Do
	 * chwili zaakceptowania potwierdzenia metoda zwraca null.
	 *
	 * @param seat numer miejsca
	 * @return nazwa uĹźytkownika, ktĂłry z sukcesem przeprowadziĹ proces rejestracji
	 * @throws RemoteException wyjÄtek wymagany przez RMI
	 */
	public String whoHasReservation(int seat) throws RemoteException;
}