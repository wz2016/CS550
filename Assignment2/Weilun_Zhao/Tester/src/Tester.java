
import java.io.*;
import java.net.*;

public class Tester {
    static String[] neighbors;//The list of neighbor nodes
    static int theNumberOfHashMap; // the number of client in the DHT
    static int executeTimes = 100000; // the test times
    public static void main(String [] args) throws IOException{
        neighbors = readFileContent("list.txt").split("//");//get other client info
        theNumberOfHashMap = neighbors.length;
        int i;
        char _key;
        String key = "A";
        String value = "test value";
        for(i = 0; i<theNumberOfHashMap;i++){
            //test in different distribute p2p server
            double inputTest = testClientInput(neighbors[i], key, value);
            double getTest = testClientGet(neighbors[i], key, value);
            double delTest = testClientDel(neighbors[i], key, value);
            
            //get the input/get/del test time after executing "executeTimes" times
            messagePrint("The " + (i+1)+ " client test");
            messagePrint("The average time of "+executeTimes+" times input "+inputTest + "s");
            messagePrint("The average time of "+executeTimes+" times get "+getTest + "s");
            messagePrint("The average time of "+executeTimes+" times del "+delTest + "s");
            _key =(char) (key.charAt(0)+1);
            key = String.valueOf(_key);
        }
    }
    //this method to test client to do input "executetimes" times
    private static double testClientInput(String client, String key, String value){
        char _key;
        int i;
        messagePrint("Test input :" + executeTimes + " times");
        long startTime = System.currentTimeMillis(); //get the start of the test time
        for(i = 0; i<executeTimes; i++){
            _key =(char) (key.charAt(0)+theNumberOfHashMap);
            key = String.valueOf(_key);
            testInput(client, key, value);
        }
        long endTime = System.currentTimeMillis(); //get the end of the test time
        double time = (endTime - startTime) / (1000.0*executeTimes);
        //	     messagePrint("db " + time );
        return time;
    }
    
    //this method let client 1 to do input()
    private static void testInput(String clientInfo, String key, String value){
        String[] addr = clientInfo.split(":");
        int port = Integer.parseInt(addr[1]);
        //		messagePrint(""+ port);
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String Info = null;
        try{
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int i;
            
            out.println("INPUT" + "/" + key + "/" + value);
            Info = in.readLine();
            //			messagePrint(Info);
            
            
            
        }catch(Exception ex){
            messagePrint(ex.toString());
        }
    }
    //this method to test client to do get "executetimes" times
    private static double testClientGet(String client, String key, String value){
        char _key;
        int i;
        messagePrint("Test get :" + executeTimes + " times");
        long startTime = System.currentTimeMillis();
        for(i = 0; i<executeTimes; i++){
            //change String type key into char and add "theNumberOfHashMap"
            _key =(char) (key.charAt(0)+theNumberOfHashMap);
            key = String.valueOf(_key); //get the String type key
            testGet(client, key);
        }
        long endTime = System.currentTimeMillis();
        double time = (endTime - startTime) / (1000.0*executeTimes);
        //	     messagePrint("db " + time );
        return time;
    }
    
    //this method let client 1 to do get()
    private static void testDel(String clientInfo, String key){
        String[] addr = clientInfo.split(":");
        int port = Integer.parseInt(addr[1]);
        //		messagePrint(""+ port);
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String Info = null;
        try{
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int i;
            
            out.println("GET" + "/" + key );
            Info = in.readLine();
            //			messagePrint(Info);
            
        }catch(Exception ex){
            messagePrint(ex.toString());
        }
    }
    //this method to test client to do del "executetimes" times
    private static double testClientDel(String client,String key, String value){
        char _key;
        int i;
        messagePrint("Test del :" + executeTimes + " times");
        long startTime = System.currentTimeMillis();
        for(i = 0; i<executeTimes; i++){
            _key =(char) (key.charAt(0)+theNumberOfHashMap);
            key = String.valueOf(_key);
            testGet(client, key);
        }
        long endTime = System.currentTimeMillis();
        double time = (endTime - startTime) / (1000.0*executeTimes);
        //		     messagePrint("db " + time );
        return time;
    }
    
    //this method let client 1 to do del()
    private static void testGet(String clientInfo, String key){
        String[] addr = clientInfo.split(":");
        int port = Integer.parseInt(addr[1]);
        //			messagePrint(""+ port);
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String Info = null;
        try{
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int i;
            
            out.println("DEL" + "/" + key );
            Info = in.readLine();
            //				messagePrint(Info);
            
        }catch(Exception ex){
            messagePrint(ex.toString());
        }
    }
    
    // this method prints info
    private static void messagePrint(String string) {
        System.out.println(string);
    }
    
    //read file "list.txt" which conclude client info and port
    private static String readFileContent(String fileName)throws IOException{
        File file = new File(fileName);
        
        BufferedReader bf  = new BufferedReader(new FileReader(file));
        
        String content = "";
        StringBuilder sb = new StringBuilder();
        
        while(content!= null){
            content = bf.readLine();
            
            if(content == null){
                break;
            }
            
            sb.append(content.trim());
        }
        bf.close();
        return sb.toString();
    }
}
