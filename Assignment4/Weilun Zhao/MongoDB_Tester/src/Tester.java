import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import com.mongodb.Block;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.util.Arrays.asList;

import java.text.DateFormat;
import java.util.Arrays;

public class Tester {
	static String[] neighbors;//The list of neighbor nodes
	static int theNumberOfHashMap; //the number of client in the static network
	static String clientName = "MongoDBTester"; //initial local client name;
    static int executeTimes = 100; // the test times
	public static void main(String[] args) throws IOException{
		neighbors = readFileContent("list.txt").split("//");//get other client info
        theNumberOfHashMap = neighbors.length;
        double totalTime = 0;
        long startTime = System.currentTimeMillis();
        double inputTest = testClientInput();
//        messagePrint("1: "+neighbors[0] );
	    double getTest = testClientGet();
	    double delTest = testClientDel();
	    
        long endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
		double throughput = thoughputOps(inputTest, getTest, delTest);
		messagePrint("Node: " + theNumberOfHashMap+ " ; execute times: " + executeTimes + " ; total running time: " + totalTime+" ms");
		messagePrint("latency of input " + inputTest + "ms");
		messagePrint("latency of get " + getTest + " ms");
		messagePrint("latency of del " + delTest + " ms");
		messagePrint("Node: " + theNumberOfHashMap + " throughput: "+ throughput + " ops/s");
		outputRecord(inputTest, getTest,delTest,totalTime, throughput);
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
			String inputInfo = "";
			String clientIP = addr[0];
			int port = Integer.parseInt(addr[1]);
			try{
			 MongoClient mongoClient = new MongoClient(clientIP, port);
			 MongoDatabase db = mongoClient.getDatabase("test");
			 MongoCollection<Document> collection = db.getCollection("test");
			 db.getCollection("test").insertOne(
			    	  new Document("key",key).append("value", value));
			 
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
			try{
				 MongoClient mongoClient = new MongoClient(clientIP, port);
				 MongoDatabase db = mongoClient.getDatabase("test");
				 MongoCollection<Document> collection = db.getCollection("test");
				 Document document = collection.findOneAndDelete(new Document("key",key));
				 
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
			try{
				MongoClient mongoClient = new MongoClient(clientIP, port);
				 MongoDatabase db = mongoClient.getDatabase("test");
				 MongoCollection<Document> collection = db.getCollection("test");
				 collection.deleteMany(new Document("key",key));
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
//		*****************output*************************
		private static void outputRecord(double inputTest, double getTest, double delTest, double totalTime, double throughput) throws IOException{
			File file = new File("/home/ubuntu/record.txt");
			if (!file.exists()) {// 判断文件是否存在
				try {
					file.createNewFile(); // 创建文件

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			FileWriter out = new FileWriter(file, true);
			out.write("Node: " + theNumberOfHashMap+ " ; execute times: " + executeTimes + " ; total running time: " + totalTime+" ms");
			out.flush();
			out.write("latency of input " + inputTest + "ms");
			out.flush();
			out.write("latency of get " + getTest + " ms");
			out.flush();
			out.write("latency of del " + delTest + " ms");
			out.flush();
			out.write("Node: " + theNumberOfHashMap + " throughput: "+ throughput + " ops/s");
			out.flush();
			out.close();
		}
}
