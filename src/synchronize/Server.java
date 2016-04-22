
package synchronize;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;

/**
 *
 * @author Svetlana
 */

/**
 * Класс реализует Server Socket.
 * Класс создан на основе класса Thread и для работы с ним необходимо перегрузить
 * метод run(), который выполняется при запуске потока.
 */
public class Server extends Thread implements Serializable {
    
    /*
    * Порт сервера
    */
    private int port;
    /*
    * Конфигурация синхронизации
    */
     public static Config Config;
     Directory serverDir;
     
    public Server(Config Config) {
        this.port = Integer.valueOf(Config.getProperty("port"));
        this.Config = Config;
        serverDir = new Directory(Config.getProperty("secondpath"));
        serverDir.takePrevState(new File(Config.getProperty("secondprevstate")));
        serverDir.putNewStateInSet();
    }
    
    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out= null;
        ServerSocket server = null;
        Socket fromclient = null;
        ObjectInputStream objectIn = null;
        ObjectOutputStream objectOut = null;
        try {
            
            System.out.println("SERVER:Authorizing...");
            String serviceName = "RMIService";
            Registry reg = null;
            try {
                reg = LocateRegistry.createRegistry(Integer.valueOf(Config.getProperty("port")));
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        while (true) {
            TreeSet<FileInfo> toAddServer = null;
            TreeSet<FileInfo> toDeleteServer = null; 
            TreeSet<FileInfo> toCopyServer = null;
            TreeSet<FileInfo> toAddClient = null;
            TreeSet<FileInfo> toDeleteClient = null; 
            TreeSet<FileInfo> toCopyClient = null;
            RMIInterface serverService = null;

            try {                   
                serverService = new SyncRMI();
                serverService.setServer(this);
                serverService.setServerDir(serverDir);
                reg.rebind(serviceName, serverService);
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Сервер ждет, пока SyncRMI не выполнит метод поиска изменений и не "включит" сервер
            suspend();  
            try {
                toAddClient = serverService.getToAddClient();
                toDeleteClient = serverService.getToDeleteClient();
                toCopyClient = serverService.getToCopyClient();
                toAddServer = serverService.getToAddServer();
                toDeleteServer = serverService.getToDeleteServer();
                toCopyServer = serverService.getToCopyServer();
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            server = new ServerSocket(port+5);
            fromclient = server.accept();
            in = new BufferedReader(new InputStreamReader(fromclient.getInputStream()));
            out = new PrintWriter(fromclient.getOutputStream(), true);
            objectIn = new ObjectInputStream(fromclient.getInputStream());
            objectOut = new ObjectOutputStream(fromclient.getOutputStream());
                System.out.println("SERVER:Got the changes!");

                System.out.println("SERVER:Deleting files from server...");
                for(FileInfo a:toDeleteServer){
                    Files.delete(Paths.get((String)serverDir.getPath()+ File.separator+(String)a.getPath()));
                }
                System.out.println("SERVER:Deleted successfully!");

                System.out.println("SERVER:Sending client set of files to be deleted...");
                objectOut.writeObject(toDeleteClient);

                objectOut.writeObject(toAddServer);
                DataInputStream obj = new DataInputStream(fromclient.getInputStream());
                for(FileInfo fi:toAddServer){
                    getFile(obj,serverDir, fi);
                } 
                System.out.println("SERVER:Files added");
                DataOutputStream fileOut = new DataOutputStream(fromclient.getOutputStream());
                System.out.println("SERVER:Sending client set of files to be added...");
                objectOut.writeObject(toAddClient);

                System.out.println("SERVER:Sending files to client to be added");

                for(FileInfo a:toAddClient){
                    //objectOut.writeObject(new File((String) a.getPath()));
                    sendFile(fileOut,serverDir,a);
                }

                objectOut.writeObject(toCopyServer);



                for(FileInfo fi:toCopyServer){
                    getFile(obj,serverDir, fi);
                } 
                System.out.println("SERVER:Files copied");

                System.out.println("SERVER:Sending client set of files to be copied...");
                objectOut.writeObject(toCopyClient);

                System.out.println("SERVER:Sending files to client to be copied");

                for(FileInfo a:toCopyClient){
                    sendFile(fileOut,serverDir,a);
                }

                serverDir.putNewStateInSet();
                serverDir.saveNewState(new File(Config.getProperty("secondprevstate")));
                
        }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
                in.close();
                fromclient.close();
                server.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
    /**
     * Метод, который получает файл из входящего потока и сохраняет его в директорию
     * @param objectIn входящий поток
     * @param dirToSave директория для сохранения
     * @param fi информация о файле
     */
    public void getFile(DataInputStream objectIn, Directory dirToSave, FileInfo fi){
        File file = new File(dirToSave.getPath()+File.separator+(String)fi.getPath());
        if ((boolean)fi.isDirectory()) {
           file.mkdir();
        } else {
           try {
                file.createNewFile();
            } catch (IOException ex) {

             }

            try (FileOutputStream os = new FileOutputStream(file);){            
                System.out.println("SERVER:Saving " + (String)fi.getPath() + "... ");
                long length = objectIn.readLong();
                int readedBytesCount, total = 0;
                byte[] buffer = new byte[1000];
                while ((readedBytesCount = objectIn.read(buffer, 0, Math.min(buffer.length, (int)length-total))) != -1) {
                    total += readedBytesCount;
                     os.write(buffer, 0, readedBytesCount);

                    if (total == (int)length){
                        break;
                    }
                 }

                System.out.println("file has been saved");
            } catch (SocketException ex) {            
                System.out.println("file has been saved*");
           } catch (IOException ex) {            
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
    /**
     * Метод, отправляющий файл из директории по исходящему потоку
     * @param os исходящий поток
     * @param dir директория, из которой отправляется файл
     * @param fi информация о файле
     */
     public void sendFile(DataOutputStream os, Directory dir, FileInfo fi) {
        if (!(boolean)fi.isDirectory()) {
            try (FileInputStream is = new FileInputStream(dir.getPath()+File.separator+(String)fi.getPath());) {
                File file = new File(dir.getPath()+File.separator+(String)fi.getPath());
                long length = file.length();            
                System.out.println("Sending " + (String)fi.getPath());
                os.writeLong(length);
                byte[] buffer = new byte[1000];
                while (true){
                    int readedBytesCount = is.read(buffer);
                    if (readedBytesCount == -1) {
                        break;
                    }
                    if (readedBytesCount > 0) {
                        os.write(buffer, 0, readedBytesCount);
                    }
                }            
                System.out.println("sending has been finished");
            } catch (SocketException ex) {
                System.out.println("sending has been finished*");
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
     }
    
    
    }



