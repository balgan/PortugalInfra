/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logger;

import java.io.File;
import java.io.RandomAccessFile;

/**
 *
 * @author Tiago
 */
public class Logger implements Runnable {

    File _file = new File("/opt/sailfin/domains/domain1/logs/server.log");
    long _updateInterval = 1000;
    
    private String RED = "\033[1;31m";
    private String GREEN   = "\033[1;32m";
    private String YELLOW  = "\033[1;33m";
    private String BLUE    = "\033[1;34m";
    private String MAGENTA = "\033[1;35m";
    private String CYAN    = "\033[1;36m";
    private String NONE    = "\033[0m";

    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Logger l = new Logger();
        l.run();
    }

    public void run() {
        try {
            long pos = 0;
            while (true) {
                Thread.sleep(_updateInterval);
                long len = _file.length();
                if (len < pos) {
                    // Log must have been jibbled or deleted.
                    System.out.println("Log file was reset. Rest    arting logging from start of file.");
                    pos = len;
                } else if (len > pos) {
                    // File must have had something added to it!
                    RandomAccessFile raf = new RandomAccessFile(_file, "r");
                    raf.seek(pos);
                    String line = null;
                    while ((line = raf.readLine()) != null) {
                        System.out.println(line);
                    }
                    pos = raf.getFilePointer();
                    raf.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Fatal error reading log file, log tailing has stopped.");
        }
        // dispose();
    }
}
