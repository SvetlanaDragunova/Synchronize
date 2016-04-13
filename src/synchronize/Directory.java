
package synchronize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс директории
 * @author Svetlana
 */
public class Directory<P> implements Serializable{
    private P path;
    private TreeSet<FileInfo> prevState, newState;
    
    Directory(P path){
        this.path = path;
    }
    
    /**
     * Возвращает путь к директории
     * @return путь к директории
     */
    public P getPath(){
        return path;
    }
    
    /**
     * Возвращает множество последнего состояния директории
     * @return последнее состояние
     */
    public Set<FileInfo> getPrevState(){
        return prevState;
    }
    
    /**
     * Возвращает множество текущего состояния директории
     * @return текущее состояние
     */
    public Set<FileInfo> getNewState(){
        return newState;
    }
    
    /**
     * Сохраняет текущее состояние из множества в файл
     * @param file файл для сохранения
     * @return успех/неудача
     */
    public boolean saveNewState(File file){
         if (newState!=null) {
             try {
                 FileOutputStream fs = new FileOutputStream(file);
                 ObjectOutputStream os = new ObjectOutputStream(fs);
                 os.writeObject(newState);
                 os.close();
                 fs.close();
                 return true;
             } catch (IOException e) {
                 return false;
             }
         } else {
             return false;
         }
     }
    
    /**
     * Сохраняет предыдущее состояние из файла во множество
     * @param file файла для загрузки
     * @return успех/неудача
     */
    public boolean takePrevState(File file){
         try {
             FileInputStream fs = new FileInputStream(file);
             ObjectInputStream os = new ObjectInputStream(fs);
             if (!file.exists()) return false;
             prevState = (TreeSet<FileInfo>)os.readObject();
             os.close();
             fs.close();
             return true;
         } catch (IOException | ClassNotFoundException e) {
             return false;
         }
     }
    
    /**
     * Рекурсивно сканирует директорию для получения текущего состояния директории
     * @param dirPath путь к директории
     * @param set множество для сохранения текущего состояния
     */
    public void takeNewState(String dirPath, TreeSet<FileInfo> set){
        File main = new File(dirPath);
        File[] files = main.listFiles();
        for(File f:files){
            boolean isDirectory = f.isDirectory();
            long modifyTime = 0;
            if(!isDirectory){
              modifyTime = f.lastModified();
            }
            String filePath = f.getPath();
            if(isDirectory){
                takeNewState(filePath, set);
            }
            filePath = filePath.substring(((String)path).length()+1);   //преобразует абсолютный путь в относительный
            set.add(new FileInfo(filePath, modifyTime, isDirectory));
        }
    }
    
    /**
     * Метод для вызова рекурсивной функции сканирования
     */
    public void putNewStateInSet(){
        newState = new TreeSet<FileInfo>();
        takeNewState((String)path, newState);
    }
    
    
    /**
     * Метод для синхронизации
     * Разбивает обе директории на состоавные части(есть только в этой директории, есть в обеих директориях)
     * Для каждой из частей запускает свой метод для синхронизации
     * @param other директория, с кторой синхронизируется данная директория
     */
    public void letsSinchronize(Directory other, TreeSet<FileInfo> toDeleteServer, TreeSet<FileInfo> toAddServer, TreeSet<FileInfo> toCopyServer, TreeSet<FileInfo> toDeleteClient, TreeSet<FileInfo> toAddClient, TreeSet<FileInfo> toCopyClient){
        //TreeSet <FileInfo> onlyNewMe = new TreeSet<FileInfo>();
        //TreeSet <FileInfo> onlyNewOther = new TreeSet<FileInfo>();
        TreeSet <FileInfo> hasOnlyMe = new TreeSet<FileInfo>();
        TreeSet <FileInfo> hasOnlyOther = new TreeSet<FileInfo>();
        TreeSet <FileInfo> hasBothMe = new TreeSet<FileInfo>();
        TreeSet <FileInfo> hasBothOther = new TreeSet<FileInfo>();
        
        
        //onlyNewMe.addAll(newState);
        //onlyNewMe.removeAll(prevState);
        
        //onlyNewOther.addAll(other.newState);
        //onlyNewOther.removeAll(other.prevState);
        
        hasOnlyMe.addAll(newState);
        hasOnlyMe.removeAll(other.newState);
        
        hasOnlyOther.addAll(other.newState);
        hasOnlyOther.removeAll(newState);
        
        hasBothMe.addAll(newState);
        hasBothMe.retainAll(other.newState);
        
        hasBothOther.addAll(other.newState);
        hasBothOther.retainAll(newState);
        
        SyncByHaving(this, other, hasOnlyMe, toAddClient, toDeleteServer);
        SyncByHaving(other, this, hasOnlyOther, toAddServer, toDeleteClient);
        SyncByTime(this, other, hasBothMe, hasBothOther, toCopyServer, toCopyClient);
    }

    /**
     * Синхронизирует файлы, которые есть в обеих директориях, по времени изменения
     * В синхронизированных директориях остается файл, который редактировался позже
     * @param first первая директория
     * @param second вторая директория
     * @param hasFirst множество файлов, которые есть в обеих директориях, из первой директории
     * @param hasSecond множество файлов, которые есть в обеих директориях, из второй директории
     */
    public void SyncByTime(Directory first, Directory second, TreeSet<FileInfo> hasFirst, TreeSet<FileInfo> hasSecond, TreeSet<FileInfo> toCopyServer, TreeSet<FileInfo> toCopyClient ) {
        Iterator it1 = hasFirst.iterator();
        Iterator it2 = hasSecond.iterator();
        
        FileInfo info1;
        FileInfo info2;
        
        while(it1.hasNext()){
            if(it2.hasNext()){
                info1 = (FileInfo)it1.next();
                info2 = (FileInfo)it2.next();
                if(info1.equals(info2)&&((long)info1.getTime()==(long)info2.getTime())){
                    it1.remove();//эти элементы сравнили, они равны, можно убрать их из проверяемых коллекций
                    it2.remove();
                } else if(info1.equals(info2)){
                    if((long)info1.getTime()>(long)info2.getTime()){
                        
                            toCopyClient.add(info1);
                            //Files.copy(Paths.get((String)first.getPath(), File.separator,(String)info1.getPath()),Paths.get((String)second.path, File.separator,(String)info2.getPath()),REPLACE_EXISTING);
                            
                    } else{
                            toCopyServer.add(info2);
                            //Files.copy(Paths.get((String)second.getPath(), File.separator,(String)info2.getPath()),Paths.get((String)first.path, File.separator,(String)info1.getPath()),REPLACE_EXISTING);
                        
                    }
                    it1.remove();
                    it2.remove();
                } 
            }
        }
        
    }

    /**
     * Синхронизирует файлы, которые есть только в одной из директорий
     * Если файл был в предыдущем состоянии второй директории, значит, он был удален, и необходимо удалить его из первой директории
     * Если файл не был в предыдущем состоянии второй директории, значит, он был создан, и необходимо копировать его во вторую директорию
     * @param first первая директория
     * @param second вторая директория
     * @param hasFirst множество файлов, которые есть только в первой директории
     */
    public void SyncByHaving(Directory first, Directory second, TreeSet<FileInfo> hasFirst, TreeSet<FileInfo> toAddSecond, TreeSet<FileInfo> toDeleteFirst)  {
        Iterator it1 = hasFirst.descendingIterator();
        FileInfo info1;
        while(it1.hasNext()){
            info1 = (FileInfo)it1.next();
            if(second.getPrevState().contains(info1)){
                    toDeleteFirst.add(info1);
                   // Files.delete(Paths.get((String)first.getPath()+ File.separator+(String)info1.getPath()));
                    it1.remove();
               
        }
        }
        it1 = hasFirst.iterator();
        while(it1.hasNext()){
            info1 = (FileInfo)it1.next();
            if(!second.getPrevState().contains(info1)){
                    toAddSecond.add(info1);
                    //Files.copy(Paths.get((String)first.getPath()+File.separator+(String)info1.getPath()),Paths.get((String)second.path+ File.separator+(String)info1.getPath()));
                    it1.remove();
                
            }
        }
        
        
    }

    
}
