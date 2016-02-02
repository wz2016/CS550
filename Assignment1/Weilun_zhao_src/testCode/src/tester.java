
//This is the test program to run threads and each thread looks up file 1000 times
import java.net.*;
import java.util.Scanner;
import java.io.*;

public class tester {
    static int threadNum = 10; // set the thread number
    static int lookupTimes = 1000; // set the look up time
    
    //this is the 1000 threads and each one looks up once
    //	static int threadNum = 1000; // set the thread number
    //	static int lookupTimes = 1; // set the look up time
    
    public static void main(String[] args) {
        TestThread[] thread = new TestThread[threadNum];
        messagePrint("The thread number is : " + threadNum);
        messagePrint("the look up time is : " + lookupTimes);
        
        long startTime = System.currentTimeMillis(); // get start time
        
        for (int i = 0; i < threadNum; i++) {
            thread[i] = new TestThread(lookupTimes);
            thread[i].start();
        }
        try {
            for (int i = 0; i < threadNum; i++) {
                thread[i].join(); // waiting all the thread to end
            }
        } catch (InterruptedException ex) {
            messagePrint(ex.toString());
        }
        long endTime = System.currentTimeMillis(); // get the finish time
        messagePrint(threadNum + " client threads end and execute "+lookupTimes+" time searchFile() ");
        messagePrint("The average time of search "+(endTime - startTime) / (1000.0*lookupTimes) + "s");
    }
    
    // the method print input string info
    static private void messagePrint(String s) {
        System.out.println(s);
    }
}

class TestThread extends Thread {
    static String serverAddress = "localhost:9999";
    static int serverPort = 9999;
    static int times;
    
    public TestThread(int times) {
        this.times = times;
    }
    
    public void run() {
        String test = "";
        for (int i = 0; i < times; i++) {
            try {
                test = searchFile("Test1.txt");
            } catch (Exception ex) {
                messagePrint(ex.toString());
            }
        }
    }
    
    // this method search files with given "filesName" in server
    public static String searchFile(String fileName) throws IOException {
        String[] serAddr = serverAddress.split(":");
        Socket socket = new Socket(serAddr[0], Integer.parseInt(serAddr[1]));
        BufferedReader in = null;
        PrintWriter out = null;
        
        try {
            in = new BufferedReader(new InputStreamReader(
                                                          socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // send the search info to server
            out.println("SER_" + fileName);
            
            // get the client info which contains the file expected
            String fileInfo = in.readLine();
            // messagePrint("Search result : " + fileInfo);
            socket.close();
            return fileInfo;
        } catch (IOException ex) {
            messagePrint(ex.toString());
            
        }
        return "";
        
    }
    
    // the method print input string info
    static private void messagePrint(String s) {
        System.out.println(s);
    }
}