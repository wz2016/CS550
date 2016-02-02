import java.io.*;
import java.net.*;
import java.util.*;

public class MyClient {
	static Map<String, String> qList = new HashMap<String, String>();//The list of querys received
	static String[] neighbors;//The list of neighbor nodes
	static int theNumberOfHashMap; //the number of client in the static network
	static int  bufferSize = 1024;
	// set the p2p port and client name
	static String clientName = "D"; //initial local client name;
    static String p2pport = "9966"; //local client port
    
	public static void main(String [] args)throws IOException{
		new P2PClient().start();
		new P2PServer().start();
        messagePrint("************************************");
        messagePrint("* Client: " + clientName + " start ...              *");
        messagePrint("* Author Weilun Zhao ; A20329942   *");
        messagePrint("************************************");
        neighbors = readFileContent("list.txt").split("//");//get other client info
        theNumberOfHashMap = neighbors.length;
//        messagePrint("num of hashmap: " + theNumberOfHashMap);
	}
    // this method prints info
    private static void messagePrint(String string) {
        System.out.println(string);
    }
    
    //this method to input the key
    private static String keyInput(){
    	String key = "";
    	messagePrint("Please input the KEY: ");
    	Scanner input = new Scanner(System.in);
    	
    		key = input.nextLine();
    		if (key.length()>0 && key.length() < 20){ //limit the input key
    			return key;
    		}else{
    		messagePrint("Input key type is wrong and restart");
    		return "";
    		}
    }
    //this method to input the value
    private static String valueInput(){
    	String value = "";
    	messagePrint("Pleas input the VALUE: ");
    	Scanner input = new Scanner(System.in);
    	
    		value = input.nextLine();
    		if (value.length()>0 && value.length() < 1000){ //limit the input value
    			return value;
    		}else{
    		messagePrint("Input value type is wrong and restart");
    		return "";
    		}
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
	
	//this is the p2p client implementing input, get and del;
	private static class P2PClient extends Thread{
		String key;
		String value;
		int i;
		
		public void run(){
			while(true){
			i = inputOperation();	
			if(i == -1){
				break;
			}
			}
		}
	}
	
	//client input a operation(input/get/del) and send request
	private  static int inputOperation(){
		int i = 0;
		boolean flag = false;
		messagePrint("Please input the operation you want : (input/get/del)  ");
		Scanner inputOperation = new Scanner(System.in);
		String inputOpt = inputOperation.nextLine();
		if(inputOpt.trim().equalsIgnoreCase("input")){
			flag = input();
			if(flag){
				messagePrint("input successfully");
			}else{
				messagePrint("input failed");
			}
			
		}else if(inputOpt.trim().equalsIgnoreCase("get")){
			i = get();
			if(i == 1){
				messagePrint("get successfully");
			}else if(i == -1){
				messagePrint("get failed");
			}
		}else if(inputOpt.trim().equalsIgnoreCase("del")){
			flag = del();
			if(flag){
				messagePrint("delete successfully");
			}else{
				messagePrint("delete failed");
			}
		}else if(inputOpt.trim().equalsIgnoreCase("exit")){
			return -1;
		}
		else{
			messagePrint("Wrong input operation type and input again!");
			inputOperation();
		}
		return 1;
	}
	
	//input the key and value and save the key and value to DHT
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
		
		String clientInfo = null;
		clientInfo = getClientInfo(clientNumber);
		if(clientInfo == ""){
			messagePrint("Not find client");
			return false;	
		}
		messagePrint("Save key and value to "+clientInfo);
		
		i = contectedWithClient(clientInfo,key,value);
		if(i == 1){
			return true;
		}
		return false;
	}
	//input the key and send get request
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
			messagePrint("Not find client");
			return -1;	
		}
		
		value = sendKeyGetValue(clientInfo, key);
		if ( value == "" || value == null){
			return -1;
		}
		return 1;
		
	}
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
			messagePrint("Not find client");
			return false;
		}
		
		i = sendDelKeyRequest(clientInfo, key);
		if(i == 1){
			messagePrint("Del successful");
			return true;
		}else{
			messagePrint("Del failed");
			return false;
		}
		
	}
	
	//send the key and get value 
	private static String sendKeyGetValue(String clientInfo, String key){
		String value = "";
		String outputInfo = "";
		String getInfo = "";
		String[] addr = clientInfo.split(":");
		int port = Integer.parseInt(addr[1]);
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try{
			socket = new Socket("localhost", port);
			outputInfo = "GET" + "/" + key;
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(outputInfo);
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			getInfo = in.readLine();
			
			if(getInfo==""||getInfo == null){
				messagePrint("Trere is no such key in the DHT");
				return "";
			}
			
			if(getInfo != ""){
				String[] getInfoValue = getInfo.split("/");
				if(getInfoValue[0].trim().equalsIgnoreCase("RET") && getInfoValue.length == 2  ){
					value = getInfoValue[1];
					messagePrint("get the value is : " + value);
					
				}	
			}
			in.close();
			out.close();
			socket.close();
		}catch(Exception ex){
			messagePrint(ex.toString());
		}
		
		return value;
		
	}
	//this method sends delete message with the key to client-server 
	private static int sendDelKeyRequest(String clientInfo, String key){
		String value = "";
		String outputInfo = "";
		String inputInfo = "";
		String[] addr = clientInfo.split(":");
		int port = Integer.parseInt(addr[1]);
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try{
			socket = new Socket("localhost", port);
			outputInfo = "DEL" + "/" + key;
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(outputInfo);
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			inputInfo = in.readLine();
			if(inputInfo.trim().equalsIgnoreCase("DELFAIL")){
				return -1;
			}else if(inputInfo.trim().equalsIgnoreCase("DELSUC")){
				return 1;
			}
			out.close();
			socket.close();
		}catch(Exception ex){
			messagePrint(ex.toString());
		}
		return -1;
	}
	
	//using clientInfo to get connection with p2p client and send value and key
	private static int contectedWithClient(String clientInfo,String key, String value){
		String[] addr = clientInfo.split(":");
		String inputInfo = "";
		String outputInfo = "";
		int port = Integer.parseInt(addr[1]);
		PrintWriter out = null;
		BufferedReader in = null;
		try{
		Socket socket = new Socket("localhost", port);
//		Socket socket = new Socket(InetAddress.getLocalHost(), port);
		
		outputInfo = "INPUT" +"/"+ key+"/"+value;
		out = new PrintWriter(socket.getOutputStream(), true);
		out.println(outputInfo);
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		inputInfo = in.readLine();
		if (inputInfo.trim().equalsIgnoreCase("INPUTSUC")){
			socket.close();
			in.close();
			out.close();
			return 1;
		}else if(inputInfo.trim().equalsIgnoreCase("INPUTFAIL")){
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
    			clientInfo = clientInfo+Info[0]+ ":"+Info[1];
    		}
    	}
    	return clientInfo;
    }
	
	 // this is the p2p client-server and set the server socket
    private static class P2PServer extends Thread {
        int p2p_port = Integer.parseInt(p2pport);
        ServerSocket listenerSocket = null;
        
        public void run() {
            try {
                listenerSocket = new ServerSocket(p2p_port);
                
                if (listenerSocket != null) {
                    while (true) {
                        new RequestHandler(listenerSocket.accept()).start();
                    }
                }
            } catch (IOException e) {
                messagePrint(e.toString());
                return;
            } finally {
                try {
                    if (listenerSocket != null)
                        listenerSocket.close();
                } catch (IOException ex) {
                    messagePrint(ex.toString());
                }
            }
        }
        
        // this is p2p client to handle other clients and send the file
        public static class RequestHandler extends Thread {
            Socket socket = null;
            BufferedReader in = null;
            PrintWriter out = null;
            int i;
            public RequestHandler(Socket socket) {
                this.socket = socket;
            }
            
            public void run() {
            	try{
	            	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            	
	            	String inputInfo = in.readLine();
	            	String[] input = inputInfo.split("/");
	                if (inputInfo == null) {
	                    messagePrint("-----------------------------");
	                    socket.close();
	                }
	                else if(input[0].trim().equalsIgnoreCase("INPUT") && input.length == 3){
	                	String key = input[1];
	                	String value = input[2];
	                	inputKeyAndValue(key, value, socket);
	                }
	                else if(input[0].trim().equalsIgnoreCase("GET") && input.length == 2){
	                	String key = input[1];
	                	returnTheKeyValue(key, socket);
	                }
	                else if(input[0].trim().equalsIgnoreCase("DEL") && input.length == 2){
	                	String key = input[1];
	                	i = deleteTheKeyAndValue(key, socket);
	                	
	                }
	            	in.close();
	            	socket.close();
            	}
            	catch(Exception ex){
            		messagePrint(ex.toString());
            	}
            } 
        }
        
        //input the key and value into hash table
        private static int inputKeyAndValue(String key, String value, Socket socket){
        	PrintWriter out = null;
        	try{
        		out = new PrintWriter(socket.getOutputStream(), true);
    		if(!qList.containsKey(key)){
    			messagePrint("Put <" + key + "," + value + "> into hashmap");
    			qList.put(key, value);
    			out.println("INPUTSUC");
    			return 1;
    		}else{
    			messagePrint("The key have been initialed; put fail");
    			out.println("INPUTFAIL");
    			return -1;
    		}
        	}catch(Exception ex){
        		messagePrint(ex.toString());
        	}
    			
        	return -1;
        }
        
    }
    //using the input key to get value from hash map
    private static int returnTheKeyValue(String key, Socket socket){
    	Set set = qList.entrySet();
		Iterator iterator = set.iterator();
		String value = "";
		PrintWriter out = null;
		try{
			if(qList.isEmpty()||!qList.containsKey(key)){
				out = new PrintWriter(socket.getOutputStream(), true);
				messagePrint("Unexist key be requested to get");
				out.println(value);
				return -1;
			}
		while(iterator.hasNext()){
			Map.Entry mentry = (Map.Entry)iterator.next();

			if(key.trim().equalsIgnoreCase((String) mentry.getKey())){
				value = (String) mentry.getValue();
			
					out = new PrintWriter(socket.getOutputStream(), true);
					out.println("RET" + "/" + value);
				
				return 1;
			}
		}
		out.close();
		}catch (Exception ex){
			messagePrint(ex.toString());
		}
    	return -1;
    }
    
    //using the key to delete the key and its value
    private static int deleteTheKeyAndValue(String key, Socket socket){
    	try{
    	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    	if(qList.isEmpty()||!qList.containsKey(key)){
    		messagePrint("Unexist key be requested to delete");
    		out.println("DELFAIL");
			return -1;
		}
    	if(qList.containsKey(key)){
    		messagePrint("<" + key + ", " + qList.get(key)+ "> be removed in DHT");
    		qList.remove(key);
    		out.println("DELSUC");
    		return 1;
    	}
    	out.close();
    	}catch(Exception ex){
    		messagePrint(ex.toString());
    	}
    	return -1;
    }
}
