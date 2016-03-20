
package synchronize;

import java.io.Serializable;
/**
 * Класс, хранящий информацию о файле
 * @author Svetlana
 */
public class FileInfo<P, T, D> implements Serializable, Comparable{
    private P filePath;
    private T fileTime;
    private D fileType;
    
    FileInfo(P filePath, T fileTime, D fileType){
        this.filePath = filePath;
        this.fileTime = fileTime;
        this.fileType = fileType;
    }
    
    /**
     * Возвращает путь к файлу
     * @return путь к файлу
     */
    public P getPath(){
        return filePath;
    }
    
    /**
     * Возвращает время последнего редактирования файла
     * @return время последнего редактирования файла
     */
    public T getTime(){
        return fileTime;
    }
    
    /**
     * Метод, говорящий директория ли данный файл
     * @return да/нет
     */
    public D isDirectory(){
        return fileType;
    }
    
    @Override
    public int hashCode(){
        int hash = 37;
        hash = hash*17 + (filePath == null ? 0 : filePath.hashCode());
        hash = hash*17 + (fileType == null ? 0 : fileType.hashCode());
        hash = hash*17 + (fileTime == null ? 0 : fileTime.hashCode());
        return hash;
    }

    @Override
     public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileInfo<P, T, D> other = (FileInfo<P, T, D>) obj;
        if (this.filePath.toString().equals((String)other.filePath)) return true;
        if (this.fileType != other.fileType && (this.fileType == null || !this.fileType.equals(other.fileType))) {
            return false;
        }
        if (this.fileTime != other.fileTime && fileType.equals(false) && 
           (this.fileTime == null || !this.fileTime.equals(other.fileTime))) {
            return false;
        }
       return true;
     }

    @Override
    public int compareTo(Object o) {
        FileInfo f = (FileInfo) o;
        return (this.getPath().toString()).compareTo(f.getPath().toString());
    }
}
