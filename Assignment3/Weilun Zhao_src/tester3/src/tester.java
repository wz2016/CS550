//package tester3;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



public class tester {
	static Map<Integer, Double> tpList = new HashMap<Integer, Double>();
	static String[] neighbors;// The list of neighbor nodes
	static int theNumberOfHashMap; // the number of client in the static network
	static Set<FileNode> fileList = new HashSet<FileNode>();
	static int bufferSize = 1024;
	static private shareData sd=new shareData();
	static String clientName = "tester";
	static String p2pport = "9900";
//	static String fileFolder = "src/tester/UP/";
	static String fileFolder = "UP/";
//	static String downLoadFolder = "src/tester/DL/";
	static String downLoadFolder = "DL/";
	static int executeTimes = 10000; // the test times
	static double totalThoughoutput=0.0;
	public static void main(String[] args) throws IOException {
		neighbors = readFileContent("list.txt").split("//");// get other client info
		theNumberOfHashMap = neighbors.length;
		int testClientNumber = theNumberOfHashMap;
//		int testClientNumber = 1;
		
		P2PClient[] clientThread = new P2PClient[testClientNumber];
		for (int i = 0;i<testClientNumber; i++){
			clientThread[i] = new P2PClient(i);
			clientThread[i].start();
		}
		try {
	          for (int i = 0; i < testClientNumber; i++) {
	              clientThread[i].join(); // waiting all the thread to end
	          }
	      } catch (InterruptedException ex) {
	          messagePrint(ex.toString());
	      }
		messagePrint("total average throughput is "+(double)(sd.get()));
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
		
		private double thoughput;
		public P2PClient(){}
		
		public P2PClient(int threadNum){
			this.threadNum = threadNum;
		}
//		public void setTp(int tn,double tp){
//			tpList.put(tn, tp);
//		}
//		public double getTp(){
//			return thoughput;
//		}
		public void run() {
			obtainTime = obtain(threadNum);
			messagePrint("Thread "+ threadNum+": the average time of "+executeTimes +" obtain : "+ obtainTime);
			thoughput = (double)(1.0/obtainTime);
//			setTp(threadNum,thoughput);
//			tpList.put(threadNum, thoughput);
			messagePrint("tN: "+threadNum+" ; throughput: "+ thoughput);
			sd.set(thoughput);

//			messagePrint("t2: "+getTp());
		}

	}
	//output string info
	public static void messagePrint(String str){
		System.out.println(str);
	}
	
	public static double obtain(int threadNum){
		String fileInfo ="";
		switch(threadNum){
		case(0):
//			fileInfo = "A/19/A/9988";
			fileInfo = "A1MB_file/1048576/A/9999";
			break;
		case(1):
			fileInfo = "B1MB_file/1048576/B/9988";
			break;
		case(2):
			fileInfo = "C1MB_file/1048576/C/9977";
			break;
		case(3):
			fileInfo = "D1MB_file/1048576/D/9966";
			break;
		case(4):
			fileInfo = "E1MB_file/1048576/E/9955";
			break;
		case(5):
			fileInfo = "F1MB_file/1048576/F/9944";
			break;
		case(6):
			fileInfo = "G1MB_file/1048576/G/9933";
			break;
		case(7):
			fileInfo = "H1MB_file/1048576/H/9922";
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
class shareData{
//	int threadNum;
	shareData(){
		
	}
	double thoughput;
	public void set( double tp){
//		this.threadNum = tn;
		this.thoughput += tp;
	}

	public double get(){
//		System.out.println("total tp : "+thoughput);
		return thoughput;
	}
}