
package synchronize;

import java.io.File;


/**
 *
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
        synchronize();
    }
    
     /**
     * Метод, запускающий синхронизацию
     */
    public static void synchronize(){
        Directory first = new Directory(Config.getProperty("firstpath"));
        Directory second = new Directory(Config.getProperty("secondpath"));
        
        if((Config.getProperty("firstprevstate")==null)||(Config.getProperty("secondprevstate")==null)){
            first.putNewStateInSet();
            second.putNewStateInSet();
            
            first.saveNewState(new File(first.getPath()+File.separator+"temp1.txt"));
            second.saveNewState(new File(second.getPath()+File.separator+"temp2.txt"));
            
            first.takePrevState(new File(first.getPath()+File.separator+"temp1.txt"));
            second.takePrevState(new File(second.getPath()+File.separator+"temp2.txt"));
            
            first.letsSinchronize(second);
            
            first.putNewStateInSet();
            first.saveNewState(new File(first.getPath()+File.separator+"temp1.txt"));
                    
            second.putNewStateInSet();
            second.saveNewState(new File(second.getPath()+File.separator+"temp2.txt"));
        } else{
            first.putNewStateInSet();
            second.putNewStateInSet();
            
            first.takePrevState(new File(Config.getProperty("firstprevstate")));
            second.takePrevState(new File(Config.getProperty("secondprevstate")));
            
            first.letsSinchronize(second);
            
            first.putNewStateInSet();
            first.saveNewState(new File(Config.getProperty("firstprevstate")));
                    
            second.putNewStateInSet();
            second.saveNewState(new File(Config.getProperty("secondprevstate")));
        }
    }
}
