
package synchronize;


import java.rmi.*;
import java.util.*;
import java.rmi.server.*;
/**
 * Реализация интерфейса для работы с RMI
 * @author Svetlana
 */
public interface RMIInterface extends Remote{
    /**
     * Метод осуществляет авторизацию пользователя и возвращает результат авторизации
     * @param user пользователь
     * @return результат авторизации
     * @throws RemoteException 
     */
    public boolean authorization(Users_TP user) throws RemoteException;
   
    /**
     * Метод находит необходимые для синхронизации множества файлов
     * @param clientDir директория клиента 
     * @throws RemoteException
     */
    public void sync(Directory clientDir) throws RemoteException;

    public TreeSet<FileInfo> getToAddClient() throws RemoteException;

    public void setToAddClient(TreeSet<FileInfo> toAddClient) throws RemoteException;

    public TreeSet<FileInfo> getToDeleteClient() throws RemoteException;

    public void setToDeleteClient(TreeSet<FileInfo> toDeleteClient) throws RemoteException;

    public TreeSet<FileInfo> getToCopyClient() throws RemoteException;

    public void setToCopyClient(TreeSet<FileInfo> toCopyClient) throws RemoteException;

    public TreeSet<FileInfo> getToAddServer() throws RemoteException;

    public void setToAddServer(TreeSet<FileInfo> toAddServer) throws RemoteException;

    public TreeSet<FileInfo> getToDeleteServer() throws RemoteException;

    public void setToDeleteServer(TreeSet<FileInfo> toDeleteServer) throws RemoteException;

    public TreeSet<FileInfo> getToCopyServer() throws RemoteException;

    public void setToCopyServer(TreeSet<FileInfo> toCopyServer) throws RemoteException;
    
    public void setServerDir(Directory serverDir) throws RemoteException;
    
    public Directory getServerDir() throws RemoteException;
    
    public void setServer(Server server) throws RemoteException ;
    
    public Server getServer() throws RemoteException ;
}
