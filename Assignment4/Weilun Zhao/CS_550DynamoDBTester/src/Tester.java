import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;


public class Tester {
	static String[] neighbors;//The list of neighbor nodes
	static int theNumberOfHashMap; //the number of client in the static network
	static String clientName = "DynamoDBTester"; //initial local client name;
    static String p2pport = "9999"; //local client port
    static int executeTimes = 1000; // the test times
	public static void main(String[] args) throws IOException{
		neighbors = readFileContent("src/list.txt").split("//");//get other client info
        theNumberOfHashMap = neighbors.length;
        double totalTime = 0;
        long startTime = System.currentTimeMillis();
        double inputTest = testClientInput();
//        messagePrint("1: "+neighbors[0] );
	    double getTest = testClientGet();
	    double delTest = testClientDel();
	    
        long endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        messagePrint("Node: " + theNumberOfHashMap+ " ; execute times: " + executeTimes + " ; total running time: " + totalTime+" ms");
        messagePrint("latency of input " + inputTest + "ms");
        messagePrint("latency of get " + getTest + " ms");
        messagePrint("latency of del " + delTest + " ms");
        messagePrint("Node: " + theNumberOfHashMap + " throughout: "+ thoughputOps(inputTest, getTest, delTest) + " ops/s");
		
	}
	//output message
	 private static void messagePrint(String string) {
	        System.out.println(string);
	 }
	 
	 //input key randomly
	 private static String keyInput(){
	    	int length = 10;
			 String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";  
			    Random random = new Random();  
			    StringBuffer buf = new StringBuffer();  
			    for (int i = 0; i < length; i++) {  
			        int num = random.nextInt(62);  
			        buf.append(str.charAt(num));  
			    }  
			    return buf.toString(); 
	    }
	 //input value randomly
	 private static String valueInput(){
	    	int length = 90;
			 String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";  
			    Random random = new Random();  
			    StringBuffer buf = new StringBuffer();  
			    for (int i = 0; i < length; i++) {  
			        int num = random.nextInt(62);  
			        buf.append(str.charAt(num));  
			    }  
			    return buf.toString(); 
	    }
	 
	//this methods returns true, if input is an Integer; vice versa 
	    private static boolean isInteger(String input) {
	        try {
	            Integer.parseInt(input);
	        } catch (NumberFormatException e) {
	            return false;
	        }
	        return true;
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
		//using the key to get the expected client number from DHT
	    private static int getClientNumberWithInputKey(String key){
	    	char k = key.charAt(0);
	    	int i = k % theNumberOfHashMap;
	    	return i;
	    }
	    //return all the client info
	    private static String getClientInfo(int clientNumber){
	    	String clientInfo = "";
	    	String [] Info = null;
	    	int i;
	    	for(i = 0; i < theNumberOfHashMap; i++){
	    		Info = neighbors[i].split(":");
	    		int num =  getClientNumberWithInputKey(Info[0]);
	    		if(num == clientNumber){
	    			clientInfo = Info[0]+ ":"+Info[1];
	    		}
	    	}
	    	return clientInfo;
	    }
//	    *****************input*********************
	    //input request
	    private static boolean input(){
	    	String key;
			String value;
			int i;
			int clientNumber;
			key = keyInput();//input key
			if(key == ""){
				return false;
			}
			
			value =  valueInput();//input value
			if(value == ""){
				return false;
			}
			
			clientNumber = getClientNumberWithInputKey(key);
			String clientInfo = getClientInfo(clientNumber);
			if(clientInfo == ""){
//				messagePrint("Not find client");
				return false;	
			}
			i = contectedWithClient(clientInfo,key,value);
			if(i == 1){
				return true;
			}
	    	return true;
	    }
	  //using clientInfo to get connection with p2p client and send value and key
		private static int contectedWithClient(String clientInfo,String key, String value){
			String[] addr = clientInfo.split(":");
			String outputInfo = "";
			String clientIP = addr[0];
			int port = Integer.parseInt(addr[1]);
			PrintWriter out = null;
			try{
			Socket socket = new Socket(clientIP, port);
//			Socket socket = new Socket(InetAddress.getLocalHost(), port);
			
			outputInfo = "INPUT" +"/"+ key+"/"+value;
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(outputInfo);
			
				socket.close();
				out.close();
				return 1;

			}
			catch(Exception ex){
				messagePrint(ex.toString());
				
			}
			return -1;
		}
//		**********************scan************************
		private static int get(){
			String key;
			String value;
			key = keyInput();//input key
			if(key == ""){
				return -1;
			}
			int clientNumber = getClientNumberWithInputKey(key);
			String clientInfo = getClientInfo(clientNumber);
			if(clientInfo == ""){
//				messagePrint("Not find client");
				return -1;	
			}
			
			value = sendKeyGetValue(clientInfo, key);
			if ( value == "" || value == null){
				return -1;
			}
			return 1;
		}
		//send the key and get value 
		private static String sendKeyGetValue(String clientInfo, String key){
			String value = "";
			String outputInfo = "";
			String getInfo = "";
			String[] addr = clientInfo.split(":");
			String clientIP = addr[0];
			int port = Integer.parseInt(addr[1]);
			Socket socket = null;
			PrintWriter out = null;
//			BufferedReader in = null;
			try{
				socket = new Socket(clientIP, port);
				outputInfo = "GET" + "/" + key;
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(outputInfo);
				
				out.close();
				socket.close();
			}catch(Exception ex){
//				messagePrint(ex.toString());
			}
			
			return value;
		}
		
//		********************delete************************
		//input the key and send del request 
		private static boolean del(){
			String key;
			String value;
			int i;
			key = keyInput();//input key
			if(key == ""){
				return false;
			}
			int clientNumber = getClientNumberWithInputKey(key);
			String clientInfo = getClientInfo(clientNumber);
			if(clientInfo == ""){
//				messagePrint("Not find client");
				return false;
			}
			
			i = sendDelKeyRequest(clientInfo, key);
			if(i == 1){
//				messagePrint("Del successful");
				return true;
			}else{
//				messagePrint("Del failed");
				return false;
			}
			
		}
		
		//this method sends delete message with the key to client-server 
		private static int sendDelKeyRequest(String clientInfo, String key){
			String value = "";
			String outputInfo = "";
			String inputInfo = "";
			String[] addr = clientInfo.split(":");
			String clientIP = addr[0];
			int port = Integer.parseInt(addr[1]);
			Socket socket = null;
			PrintWriter out = null;
			BufferedReader in = null;
			try{
				socket = new Socket(clientIP, port);
				outputInfo = "DEL" + "/" + key;
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(outputInfo);
				
				out.close();
				socket.close();
			}catch(Exception ex){
				messagePrint(ex.toString());
			}
			return -1;
		}
//		************************latency******************
		//this method to test client to do input "executetimes" times
		private static double testClientInput(){
		     int i;
		     long startTime = System.currentTimeMillis(); //get the start of the test time
		     for(i = 0; i<executeTimes; i++){
		    	 input();
		     }
		     long endTime = System.currentTimeMillis(); //get the end of the test time
		     double time = (endTime - startTime) / (executeTimes);
		     return time;
		}
		
		private static double testClientGet(){
		     int i;
		     long startTime = System.currentTimeMillis(); //get the start of the test time
		     for(i = 0; i<executeTimes; i++){
		    	 get();
		     }
		     long endTime = System.currentTimeMillis(); //get the end of the test time
		     double time = (endTime - startTime) / (executeTimes);
		     return time;
		}
		private static double testClientDel(){
		     int i;
		     long startTime = System.currentTimeMillis(); //get the start of the test time
		     for(i = 0; i<executeTimes; i++){
		    	 del();
		     }
		     long endTime = System.currentTimeMillis(); //get the end of the test time
		     double time = (endTime - startTime) / (executeTimes);
		     return time;
		}
		
//		************************troughPut******************
		private static double thoughputOps(double a, double b, double c){
			double timeA = a/1000;
			double timeB = b/1000;
			double timeC = c/1000;
			
			double throughA = 1/timeA;
			double throughB = 1/timeB;
			double throughC = 1/timeC;
			
			double max = throughA;
			if(max < throughB){
				max = throughB;
			}
			if(max < throughC){
				max = throughC;
			}
			return max;
		}
}
