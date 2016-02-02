
import java.io.*;
import java.net.*;
import java.util.*;

public class MyClient {
	static Map<String, String> qList = new HashMap<String, String>();//The list of querys received
	static String[] neighbors;//The list of neighbor nodes
	static int theNumberOfHashMap; //the number of client in the static network
	static int  bufferSize = 1024;
	static String IPAdress = "localhost";
	// set the p2p port and client name
	static String clientName = "A"; //initial local client name;
    static String p2pport = "9999"; //local client port
    
	public static void main(String [] args)throws IOException{
		new P2PServer().start();
        messagePrint("************************************");
        messagePrint("* Client: " + clientName + " start ...              *");
        messagePrint("* Author Weilun Zhao ; A20329942   *");
        messagePrint("************************************");
	}
    // this method prints info
    private static void messagePrint(String string) {
        System.out.println(string);
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
