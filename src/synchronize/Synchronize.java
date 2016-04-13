
package synchronize;


/**
 * Главный класс синхронизации
 * @author Svetlana
 */
public class Synchronize {


    public static Config Config;

    /**
     * Путь к XML-файлу с конфигурациями синхронизации(пути к синхронизируемым директориям и к их предыдущим состояниям)
     */
    public static final String PATH_TO_PROP = "C:\\Users\\Зщльзователь\\Desktop\\ТП\\prop.xml";
    
    /**
     * Метод, загружающий свойства для синхронизации из файла либо, если его нет, из командной строки
     * @param args массив данных из командной строки
     * @return удалось/не удалось получить свойства для синхронизации
     */
    public static boolean getPropetiesToUse(String[] args){
        Config = new Config(PATH_TO_PROP);
        if(args.length==0){
            Config.loadFromXML();
            return true;
        } else if(args.length%2==0){
            for(int i =0; i<args.length;i = i+2){
                Config.setProperty(args[i], args[i+1]);
            }
            return true;
        }
        return false;
    }
    
   
    
    
    public static void main(String[] args) {
//        Directory first = new Directory("C:\\Users\\Зщльзователь\\Desktop\\ТП\\first");
//        Directory second = new Directory("C:\\Users\\Зщльзователь\\Desktop\\ТП\\second");
//        File firFile = new File("C:\\Users\\Зщльзователь\\Desktop\\ТП\\first\\try1.txt");
//        first.takePrevState(firFile);
//        File secFile = new File("C:\\Users\\Зщльзователь\\Desktop\\ТП\\second\\try1.txt");
//        second.takePrevState(secFile);
//        first.putNewStateInSet();
//        second.putNewStateInSet();
//        Iterator it = first.getPrevState().iterator();
//        Iterator it1 = first.getNewState().iterator();
//        Iterator it2 = second.getPrevState().iterator();
//        Iterator it3 = second.getNewState().iterator();
//        while(it.hasNext()){
//            FileInfo a = (FileInfo)it.next();
//            System.out.print(a.getPath()+" ");
//        }
//        System.out.println(" ");
//        while(it1.hasNext()){
//            FileInfo a = (FileInfo)it1.next();
//            System.out.print(a.getPath()+" ");
//        }
//        System.out.println(" ");
//        while(it2.hasNext()){
//            FileInfo a = (FileInfo)it2.next();
//            System.out.print(a.getPath()+" ");
//        }
//        System.out.println(" "); 
//        while(it3.hasNext()){
//            FileInfo a = (FileInfo)it3.next();
//            System.out.print(a.getPath()+" ");
//        }
//        System.out.println(" ");
//        
//        System.out.println(" ");
//        first.letsSinchronize(second);

          if ((getPropetiesToUse(args))&&(Config!=null)) {            
            ThreadClass syncThread = new ThreadClass(Config);
            syncThread.run();
        } else {
            System.out.println("Oops, try again");
        }
    }
    
    
}
