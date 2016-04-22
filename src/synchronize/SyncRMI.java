
package synchronize;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * Класс определяет интерфейс RMIInterface для работы с RMI
 * @author Svetlana
 */
public class SyncRMI extends UnicastRemoteObject implements RMIInterface{

    TreeSet<FileInfo> toDeleteServer, toAddServer, toCopyServer, toDeleteClient, toAddClient, toCopyClient;
    Directory serverDir;
    Server server;

    public SyncRMI() throws RemoteException{
        toDeleteServer = new TreeSet<FileInfo>();
        toAddServer = new TreeSet<FileInfo>();
        toCopyServer = new TreeSet<FileInfo>();
        toDeleteClient = new TreeSet<FileInfo>();
        toAddClient = new TreeSet<FileInfo>();
        toCopyClient = new TreeSet<FileInfo>();
        server = null;
        serverDir = null;
    }
    
    @Override
    public boolean authorization(Users_TP user) throws RemoteException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SynchronizePU");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query queryResult = em.createQuery("from Users_TP");
        List<Users_TP> users = queryResult.getResultList();
        em.getTransaction().commit();
        em.close();
        emf.close();
        return users.contains(user);
    }

    @Override
    public void sync(Directory clientDir) throws RemoteException {
        serverDir.putNewStateInSet();
        serverDir.letsSinchronize(clientDir, toDeleteServer, toAddServer, toCopyServer, toDeleteClient, toAddClient, toCopyClient);
        server.resume();
    }

    @Override
    public TreeSet<FileInfo> getToAddClient() throws RemoteException {
        return toAddClient;
    }

    @Override
    public void setToAddClient(TreeSet<FileInfo> toAddClient) throws RemoteException {
        this.toAddClient = toAddClient;
    }

    @Override
    public TreeSet<FileInfo> getToDeleteClient() throws RemoteException {
         return toDeleteClient;
    }

    @Override
    public void setToDeleteClient(TreeSet<FileInfo> toDeleteClient) throws RemoteException {
        this.toDeleteClient = toDeleteClient;
    }

    @Override
    public TreeSet<FileInfo> getToCopyClient() throws RemoteException {
        return toCopyClient;
    }

    @Override
    public void setToCopyClient(TreeSet<FileInfo> toCopyClient) throws RemoteException {
        this.toCopyClient = toCopyClient;
    }

    @Override
    public TreeSet<FileInfo> getToAddServer() throws RemoteException {
        return toAddServer;
    }

    @Override
    public void setToAddServer(TreeSet<FileInfo> toAddServer) throws RemoteException {
        this.toAddServer = toAddServer;
    }

    @Override
    public TreeSet<FileInfo> getToDeleteServer() throws RemoteException {
         return toDeleteServer;    
    }

    @Override
    public void setToDeleteServer(TreeSet<FileInfo> toDeleteServer) throws RemoteException {
        this.toDeleteServer = toDeleteServer;
    }

    @Override
    public TreeSet<FileInfo> getToCopyServer() throws RemoteException {
        return toCopyServer;
    }

    @Override
    public void setToCopyServer(TreeSet<FileInfo> toCopyServer) throws RemoteException {
        this.toCopyServer = toCopyServer;
    }

    @Override
    public void setServerDir(Directory serverDir) throws RemoteException {
        this.serverDir = serverDir;
    }

    @Override
    public Directory getServerDir() throws RemoteException {
        return serverDir;
    }

    @Override
    public void setServer(Server server) throws RemoteException {
        this.server = server;
    }

    @Override
    public Server getServer() throws RemoteException {
        return server;
    }

    
}
