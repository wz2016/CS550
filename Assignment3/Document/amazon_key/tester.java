//package tester;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;



public class tester {
    static String[] neighbors;// The list of neighbor nodes
    static int theNumberOfHashMap; // the number of client in the static network
    static Set<FileNode> fileList = new HashSet<FileNode>();
    static int bufferSize = 1024;
    
    static String clientName = "tester";
    static String p2pport = "9900";
//    static String fileFolder = "src/tester/UP/";
    	static String fileFolder = "UP/";
//    static String downLoadFolder = "src/tester/DL/";
    	static String downLoadFolder = "DL/";
    static int executeTimes = 1000; // the test times
    public static void main(String[] args) throws IOException {
        neighbors = readFileContent("list.txt").split("//");// get other client info
        theNumberOfHashMap = neighbors.length;
        int testClientNumber = theNumberOfHashMap;
//        int testClientNumber = 2;
        P2PClient[] clientThread = new P2PClient[testClientNumber];
        for (int i = 0;i<testClientNumber; i++){
            clientThread[i] = new P2PClient(i);
            clientThread[i].start();
        }
        //		new P2PClient().start();
    }
    
    // read file "list.txt" which conclude client info and port
    private static String readFileContent(String fileName) throws IOException {
        File file = new File(fileName);
        
        BufferedReader bf = new BufferedReader(new FileReader(file));
        
        String content = "";
        StringBuilder sb = new StringBuilder();
        
        while (content != null) {
            content = bf.readLine();
            
            if (content == null) {
                break;
            }
            
            sb.append(content.trim());
        }
        bf.close();
        return sb.toString();
    }
    
    //using the key to get the expected client number from DHT
    private static int getClientNumberWithInputKey(String key){
        char k = key.charAt(0);
        //	    	messagePrint("the map : " +theNumberOfHashMap);
        int i = k % theNumberOfHashMap;
        return i;
    }
    //return the client info: client name and port
    private static String getClientInfo(int clientNumber){
        String clientInfo = "";
        String [] Info = null;
        int i;
        for(i = 0; i < theNumberOfHashMap; i++){
            Info = neighbors[i].split(":");
            int num =  getClientNumberWithInputKey(Info[0]);
            if(num == clientNumber){
                clientInfo = clientInfo+Info[0]+ ":"+Info[1];
            }
        }
        return clientInfo;
    }
    // this is the p2p client implementing input, get and del;
    private static class P2PClient extends Thread {
        int i;
        int threadNum;
        double searchTime;
        double registerTime;
        double obtainTime;
        public P2PClient(){}
        
        public P2PClient(int threadNum){
            this.threadNum = threadNum;
        }
        public void run() {
            searchTime = search(threadNum);
            messagePrint("Thread "+ threadNum+": the average time of "+executeTimes +"  search : "+ searchTime);
            //			messagePrint("");
            registerTime = reg();
            messagePrint("Thread "+ threadNum+": the average time of "+executeTimes +" register : "+ registerTime);
            //			messagePrint("");
            obtainTime = obtain(threadNum);
            messagePrint("Thread "+ threadNum+": the average time of "+executeTimes +" obtain : "+ obtainTime);
            //			messagePrint("");
        }
    }
    
    public static double search(int threadNum){
        String fileName;
        int i;
        fileName = "A";
        char k = (char) (fileName.charAt(0)+threadNum);
        fileName = String.valueOf(k);
        //		messagePrint("Start search file test, and execute : "+executeTimes +" times");
        long startTime = System.currentTimeMillis(); //get the start of the test time
        
        for(i = 0; i<executeTimes; i++){
            searchFileInDHT(fileName);
        }
        long endTime = System.currentTimeMillis(); //get the end of the test time
        double time = (endTime - startTime) / (1000.0*executeTimes);
        return time;
    }
    //the method searches file in the DHT
    public static String searchFileInDHT(String fileName){
        int clientNumber = getClientNumberWithInputKey(fileName);
        String clientInfo = getClientInfo(clientNumber);
        if(clientInfo != null||clientInfo!=""){
            String[] client = clientInfo.split(":");
            String clientName = client[0];
            String clientPort = client[1];
            int port = Integer.parseInt(clientPort);
            String getInfo = "";
            Socket socket = null;
            PrintWriter out = null;
            BufferedReader in = null;
            String value = "";
            try{
                socket = new Socket("localhost",port);
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("SER/"+fileName);
                //    			messagePrint("file: " + fileName);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                getInfo = in.readLine();
                //    			messagePrint("get: " + getInfo);
                if(getInfo==""||getInfo == null){
                    messagePrint("Trere is no such key in the DHT");
                    return "";
                }
                
                if(getInfo != ""){
                    String[] getInfoValue = getInfo.split("/");
                    if(getInfoValue[0].trim().equalsIgnoreCase("RET") ){
                        value = fileName + "/" +getInfoValue[1]+"/" + getInfoValue[2]+"/"+getInfoValue[3];
                        //    					messagePrint("get the value is : " + value);
                    }
                    return value;
                }
                in.close();
                out.close();
                socket.close();
            }catch(Exception ex){
                messagePrint(ex.toString());
            }
        }
        return "";
    }
    public static double reg(){
        int i;
        double time=0;
        try{
            //get files from fileFolder
            File[] files = new File(fileFolder).listFiles();
            if (files == null) {
                messagePrint("No file");
                return -1;
            }
            //		messagePrint("Start register testing, and execute : " + executeTimes +"s");
            String fileName="";
            long fileSize;
            //		int clientNumber;//prepare send files to the destination clients
            
            long startTime = System.currentTimeMillis();
            for (File file : files) {
                fileName = file.getName();
                fileSize = file.length();
                //			messagePrint("fileName: " + fileName + " ;fileSize: " + fileSize);
                FileNode fileNode = new FileNode(fileName, fileSize);
                if (fileList.contains(fileNode) == false) {
                    fileList.add(fileNode);//add file nodes to local file list
                }
                int clientNumber = getClientNumberWithInputKey(fileName);
                String clientInfo = getClientInfo(clientNumber);//get backup server info
                if(clientInfo == ""){
                    messagePrint("Not find client");
                    return -1;
                }
                for(int e = 0; e<executeTimes; e++){
                    i = regFileToDHT(clientInfo, fileName, fileSize);
                }
            }
            long endTime = System.currentTimeMillis();
            time = (endTime - startTime) / (1000.0*executeTimes);
        }catch(Exception ex){
            messagePrint(ex.toString());
        }
        
        return time;
    }
    
    //using clientInfo to get connection with p2p client and send fileName as key and fileSize as value
    public static int regFileToDHT(String clientInfo, String fileName, long fileSize){
        String[] addr = clientInfo.split(":");
        String inputInfo = "";
        String outputInfo = "";
        int port = Integer.parseInt(addr[1]);
        PrintWriter out = null;
        BufferedReader in = null;
        try{
            Socket socket = new Socket("localhost", port);
            //  		Socket socket = new Socket(InetAddress.getLocalHost(), port);
            
            outputInfo = "INPUT" +"/"+ fileName+"/"+fileSize+"/"+clientName + "/" + p2pport;
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(outputInfo);
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            inputInfo = in.readLine();
            if (inputInfo.trim().equalsIgnoreCase("INPUTSUC")){
                //  			messagePrint("register successfully");
                socket.close();
                in.close();
                out.close();
                return 1;
            }else if(inputInfo.trim().equalsIgnoreCase("INPUTFAIL")){
                //  			messagepPrint("register failed");
                socket.close();
                in.close();
                out.close();
                return -1;
            }
            
        }
        catch(Exception ex){
            messagePrint(ex.toString());
            
        }
        return -1;
    }
    //output string info
    public static void messagePrint(String str){
        System.out.println(str);
    }
    
    public static double obtain(int threadNum){
        String fileInfo ="";
        switch(threadNum){
            case(0):
                fileInfo = "A/19/A/9999";
                break;
            case(1):
                fileInfo = "B/19/B/9988";
                break;
            case(2):
                fileInfo = "C/19/C/9977";
                break;
            case(3):
                fileInfo = "D/19/D/9966";
                break;
            case(4):
                fileInfo = "E/19/E/9955";
                break;
            case(5):
                fileInfo = "F/19/F/9944";
                break;
            case(6):
                fileInfo = "G/19/G/9933";
                break;
            case(7):
                fileInfo = "H/19/H/9922";
                break;
                
            default:
                fileInfo = "A/19/A/9999";
                break;
        }
        int i ;
        //		messagePrint("Test obtain :" + executeTimes + " times");
        long startTime = System.currentTimeMillis();
        for(i=0; i<executeTimes; i++){
            perpareDownload(fileInfo);
        }
        long endTime = System.currentTimeMillis();
        double time = (endTime - startTime) / (1000.0*executeTimes);
        return time;
    }
    //this method prepare download file from DHT
    public static int perpareDownload(String fileInfo) {
        String [] file = fileInfo.split("/");
        String fileName = file[0];
        long fileSize = Long.parseLong(file[1]);
        String clientName = file[2];
        int clientPort = Integer.parseInt(file[3]);
        
        int returnValue = getFile(fileName,fileSize,clientName,clientPort);
        if (returnValue == -1) {
            messagePrint("Download Failed.");
            
        }
        return -1;
    }
    //this method download file from other client in the DHT
    public static int getFile(String fileName, long fileSize, String clientName, int port){
        Socket socket = null;
        DataInputStream in = null;
        PrintWriter out = null;
        DataOutputStream fileOut = null;
        byte[] buf = new byte[bufferSize];
        long passedlen = 0;
        long len = 0;
        String savePath = "" + downLoadFolder + fileName;
        try{
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("DL/"+fileName);
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
            len = in.readLong();
            if (len == -1) {
                return -1;
            }
            //start download file
            //            messagePrint("Download ......");
            while (true) {
                int read = 0;
                if (in != null) {
                    read = in.read(buf);
                }
                passedlen += read;
                if (read == -1 || in == null) {
                    break;
                }
                fileOut.write(buf, 0, read);
                if ((int) ((passedlen * 100.0) / len) == 100) {
                    break;
                }
            }
            //            messagePrint("");
            //            messagePrint("Download Success, file saved to: " + savePath);
            //            messagePrint("---------------------------------------");
            fileOut.flush();
            fileOut.close();
            socket.close();
            return 1;
        }catch(Exception ex){
            messagePrint(ex.toString());
        }
        return -1;
    }
}
//File Node class
class FileNode {
    String fileName;
    long fileSize;
    
    public FileNode(String FileName, long FileSize) {
        this.fileName = FileName;
        this.fileSize = FileSize;
    }
}