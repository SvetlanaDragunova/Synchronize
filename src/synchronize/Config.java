
package synchronize;

/**
 * 
 * @author Svetlana
 */
import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс, реализаующий интерфейс работы с конфигурациями.
 */
public class Config {
    private final String file;
    private Properties prop;
    
    /**
     * Конструктор класса.
     * @param file путь к XML-файлу конфигурации.
     */
    public Config(String file) {
        this.file = file;
        prop = new Properties();
    }
    
    
    
    /**
     * Метод для загрузки конфигураций из XML-файла.
     */
    public void loadFromXML() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            prop.loadFromXML(fis);
            fis.close();
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Метод для сохранения конфигураций в XML-файл.
     */
    public void saveToXML() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            prop.storeToXML(fos,"Sync");
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Метод для установки/изменения параметра в конфигурации.
     * @param key ключ.
     * @param value значение.
     */
    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }
    
    /**
     * Метод для получения значения параметра по ключу.
     * @param key ключ.
     * @return значение параметра.
     */
    public String getProperty(String key) {
        return prop.getProperty(key);
    }
    
    /**
     * Метод для выводя на экран список всех ключей их их значений.
     */
    public void printAll() {
        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = (String)prop.get(key);
            System.out.println(key + ": " + value);
        }
    }

}