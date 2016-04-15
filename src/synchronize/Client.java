
package synchronize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Svetlana
 */

/**
 * Класс реализует Client Socket.
 * Класс создан на основе класса Thread и для работы с ним необходимо перегрузить
 * метод run(), который выполняется при запуске потока.
 */
public class Client extends Thread {
    /*
    * Хост клиента
    */
    private String host;
    /*
    * Порт клиента
    */
    private int port;
    /*
    * Конфигурация синхронизации
    */
    public static Config Config;
    
    public Client(Config Config) {
        this.host = Config.getProperty("host");
        this.port = Integer.valueOf(Config.getProperty("port"));
        this.Config = Config;
    }
    
    @Override
    public void run() {
        Socket fromserver = null;
        BufferedReader in = null;
        PrintWriter out = null;
        ObjectOutputStream objectOut = null;
        Scanner stdin = null;
        ObjectInputStream objectIn = null;
        try {
            System.out.println("CLIENT:Connecting to " + host + ":" + port);
            fromserver = new Socket(host, port);
            objectOut = new ObjectOutputStream(fromserver.getOutputStream());
            objectIn = new ObjectInputStream(fromserver.getInputStream());
            in = new BufferedReader(new InputStreamReader(fromserver.getInputStream()));
            out = new PrintWriter(fromserver.getOutputStream(), true);
            stdin = new Scanner(System.in);
            
            System.out.println("CLIENT:Sending information to server...");
            Directory clientDir = new Directory(Config.getProperty("firstpath"));
            clientDir.putNewStateInSet();
            clientDir.takePrevState(new File(Config.getProperty("firstprevstate")));
            objectOut.writeObject(clientDir);
            
            TreeSet<FileInfo> toDelete = (TreeSet)objectIn.readObject();
            //System.out.println(toDelete.size());
            
            System.out.println("CLIENT:Got the set!");
            
            System.out.println("CLIENT:Deleting files from client...");
            for(FileInfo a:toDelete){
                Files.delete(Paths.get((String)clientDir.getPath()+ File.separator+(String)a.getPath()));
            }
            System.out.println("CLIENT:Deleted successfully!");
            
            System.out.println("SERVERC:Sending client set of files to be added to server from client...");
            TreeSet<FileInfo> toSendServerToAdd = (TreeSet)objectIn.readObject();
            System.out.println("CLIENT:Got the set!");
            DataOutputStream fileOut = new DataOutputStream(fromserver.getOutputStream());
            System.out.println("CLIENT:Sending files to server...");
            for(FileInfo a:toSendServerToAdd){
                sendFile(fileOut,clientDir,a);
            }
            System.out.println("CLIENT:Got the set!");
            TreeSet<FileInfo> toAdd = (TreeSet)objectIn.readObject();
            DataInputStream obj = new DataInputStream(fromserver.getInputStream());
            for(FileInfo fi:toAdd){
                getFile(obj,clientDir, fi);
            } 
            System.out.println("CLIENT:Files added");
            
            
            System.out.println("SERVERC:Sending client set of files to be copied to server from client...");
            TreeSet<FileInfo> toSendServerToCopy = (TreeSet)objectIn.readObject();
            System.out.println("CLIENT:Got the set!");
            
            System.out.println("CLIENT:Sending files to server...");
            for(FileInfo a:toSendServerToCopy){
                sendFile(fileOut,clientDir,a);
            }
            System.out.println("CLIENT:Got the set!");
            TreeSet<FileInfo> toCopy = (TreeSet)objectIn.readObject();
            
            for(FileInfo fi:toCopy){
                getFile(obj,clientDir, fi);
            } 
            System.out.println("CLIENT:Files copied");
            System.out.println("Synchronization finished! Now you can talk to our server. Say 'thanks' to exit!");
            String fuser, fserver;
            while ((fuser = stdin.nextLine()) != null) {
                out.println(fuser);
                fserver = in.readLine();
                System.out.println(fserver);
                if (fuser.equalsIgnoreCase("thanks")) {
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
                in.close();
                stdin.close();
                fromserver.close();
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
                System.out.println("CLIENT:Saving " + (String)fi.getPath() + "... ");
                long length = (long)objectIn.readLong();
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
