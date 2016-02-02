//package MyServer;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class MyServer {
//	static Set<ClientFileNode> clientList = new HashSet<ClientFileNode>();
	static Set<ClientFileNode> clientList = Collections.synchronizedSet(new HashSet<ClientFileNode>());
	 static int count = 0; //count the client number
	 static String serveAddress = "localhost";//initial server address
	 static String serverPort = "9999";//initial server port
	 public static void main(String[] args) throws Exception {
		new serverThread().start();
		String serveAddress = "localhost";
        messagePrint("*******************************************");
        messagePrint("* Server " + serveAddress + ":" + serverPort + " waiting connected *");
        messagePrint("* Author Weilun Zhao ; A20329942          *");
        messagePrint("*******************************************");
	 }
	 //this is server thread to listen request
	 public static class serverThread extends Thread{
		 ServerSocket listenerSocket = null;
		 int port = Integer.parseInt(serverPort);
		 //the listenerSocket is the serverSocket which is used to handle client thread
		 public void run(){
			 try {
	                listenerSocket = new ServerSocket(port);
	                
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
		 //this class is to handle request received
		 public static class RequestHandler extends Thread {
			 Socket socket = null;
	         String inputInfo = "";
			 public RequestHandler(Socket socket) {
	                this.socket = socket;
	            }
			 public void run() {
				try{
					inputInfo = getInput(socket);
					//get the header of the income message
					String[] headerIn = inputInfo.split("_");
					functionSelect(headerIn[0],headerIn[1], socket);
					socket.close();
				}catch(Exception ex){
//					messagePrint(ex.toString());
				}
			 }
		 }
		 
	 }
	 //select function basing on the income message
	 public static int functionSelect(String header, String inputInfo, Socket socket){
		 if(header.trim().equalsIgnoreCase("REG")){
			 regFiles(inputInfo);
		 }else if(header.trim().equalsIgnoreCase("SER")){
			 searchFile(inputInfo, socket);
		 }else if(header.trim().equalsIgnoreCase("ALL")){
			 displayAll(socket);
		 }
		 return -1;
	 }
	//this method is getting Input message
	 public static String getInput(Socket socket){
		 String inputInfo = "";
		 BufferedReader in = null;
		 try {
			 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 inputInfo = in.readLine();
		 }catch(Exception ex){
//			 ex.printStackTrace();
			 messagePrint(ex.toString());
		 }
		 return inputInfo; //return the received info
	 }
	 
	  // this method prints message/info
    public static void messagePrint(String string) {
        System.out.println(string);
    }
    
    //this method register files and client into into the server
    public static int regFiles(String str){
//    	messagePrint("R"+str);
    	String[] infoHandle = str.split("/");
    		if(infoHandle.length == 4){
    			//register file and client 
    			String fileName = infoHandle[0];
    			long fileSize = Long.parseLong(infoHandle[1]);
    			String ClientName = infoHandle[2];
    			String ClientPort = infoHandle[3];
    			ClientFileNode cfNode = new ClientFileNode(fileName, fileSize,ClientName, ClientPort);
    			if(clientList.contains(cfNode) == false){
    				clientList.add(cfNode);
    				messagePrint("File regirstered : " + fileName);
    				return 1;
    			}
    		}
    	return -1;
    }
    //this method to handle search request
    public static int searchFile(String fileName, Socket socket){
    	PrintWriter out = null;
    	String outStr = "";
    	synchronized (clientList) {
    		Iterator<ClientFileNode> i = clientList.iterator();
    		while (i.hasNext()) {
    			ClientFileNode tmp = (ClientFileNode) i.next();
    			if (tmp.FileName.equals(fileName)) {//register the client file info 
    				outStr = outStr + tmp.FileName + "/" + tmp.FileSize + "/" + tmp.ClientName + "/" + tmp.ClientPort +"//";
    			}
    		}
    	}
    	try{
    		out = new PrintWriter(socket.getOutputStream(), true);
    		out.println(outStr);
    		
    	}catch(Exception ex){
    		messagePrint(ex.toString());
    	}
    	return -1;
    }
    //this method sends all the cliendFileNodes to client
    public static int displayAll(Socket socket){
    	PrintWriter out = null;
    	String outStr = "";
    	synchronized (clientList) {
    		Iterator<ClientFileNode> i = clientList.iterator();
    		while (i.hasNext()) {
    			ClientFileNode tmp = (ClientFileNode) i.next();
    			outStr = outStr + tmp.FileName + "/" + tmp.FileSize + "/" + tmp.ClientName + "/" + tmp.ClientPort +"//";
    		}
    	}
    	try{
    		out = new PrintWriter(socket.getOutputStream(), true);
    		out.println(outStr);
    		
    	}catch(Exception ex){
    		messagePrint(ex.toString());
    	}
    	
    	return -1;
    }
}
//the class is the client containing with file info
class ClientFileNode{
	String FileName;
	long FileSize;
	String ClientName;
	String ClientPort;
	
	public ClientFileNode(String FileName, long FileSize, String ClientName, String ClientPort){
		this.FileName = FileName;
		this.FileSize = FileSize;
		this.ClientName = ClientName;
		this.ClientPort = ClientPort;
	}
}
