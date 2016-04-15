
package synchronize;


/**
 * Класс, запускающий синхронизацию, запуская потоки сервера и клиента
 * @author Svetlana
 */

public class ThreadClass implements Runnable{
    public static Config Config;
    
    ThreadClass(Config Config){
        this.Config = Config;
    }
    /**
     * Путь к XML-файлу с конфигурациями синхронизации(пути к синхронизируемым директориям и к их предыдущим состояниям)
     */
   
    @Override
    public void run() {
        
        Thread s = new Server(Config);
        // запуск потока экземпляра класса Server
        s.start();
        Thread c = new Client(Config);
        // запуск потока экземпляра класса Client
        c.start();
    }    
}
