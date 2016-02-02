//package MyClient;

import java.io.*;
import java.net.*;
import java.util.*;

public class MyClient {
	static Map<String, String> qList = new HashMap<String, String>();// The list of queries received
	static Set<FileNode> fileList = new HashSet<FileNode>();																	
	static String[] neighbors;// The list of neighbor nodes
	static int theNumberOfHashMap; // the number of client in the static network
	static int bufferSize = 1024;
	
//	static String fileFolder = "src/MyClient/UP/";
	static String fileFolder = "UP/";
//	static String downLoadFolder = "src/MyClient/DL/";
	static String downLoadFolder = "DL/";
	// set the p2p port and client name
	static String clientName = "E"; // initial local client name;
	static String p2pport = "9955"; // local client port

	public static void main(String[] args) throws IOException {
		 
		
		messagePrint("************************************");
		messagePrint("* Client: " + clientName + " start ...              *");
		messagePrint("* Author Weilun Zhao ; A20329942   *");
		messagePrint("************************************");

		neighbors = readFileContent("list.txt").split("//");// get other client info
		theNumberOfHashMap = neighbors.length;
//		messagePrint("num of hashmap: " + theNumberOfHashMap);
		new P2PClient().start();
		new P2PServer().start();
	}

	// this method prints info
	private static void messagePrint(String string) {
		System.out.println(string);
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

	// this is the p2p client implementing input, get and del;
	private static class P2PClient extends Thread {
		int i;
		
		public void run() {
			regPrepare();
//			reg();
			while (true) {
				String fileSearch = search();
				if (!fileSearch.equals("") && fileSearch != null) {
	                perpareDownload(fileSearch);
	            } else {
	                messagePrint("No file found ");
	            }
			}
		}
	}
	//this method let client prepare register local files
	public static int regPrepare(){
		int flag = 1;
		while(flag == 1){
		messagePrint("Prepare to register local files?(input yes to start)");
		Scanner inputYN = new Scanner(System.in);
		String input = inputYN.nextLine();
		if(input.trim().equalsIgnoreCase("Yes")){
			reg();
			flag=0;
			return 1;
		}
		}
		return 0;
	}
	//register local files into the DHT
	public static int reg(){
		int i;
		try{
			//get files from fileFolder
		File[] files = new File(fileFolder).listFiles();
		if (files == null) {
            messagePrint("No file");
            return -1;
        }
		messagePrint("Registering client files");
		String fileName="";
		long fileSize;
//		int clientNumber;//prepare send files to the destination clients 
		
		for (File file : files) {
			fileName = file.getName();
			fileSize = file.length();
			messagePrint("fileName: " + fileName + " ;fileSize: " + fileSize);
			FileNode fileNode = new FileNode(fileName, fileSize);
			if (fileList.contains(fileNode) == false) {
				fileList.add(fileNode);//add file nodes to local file list 
			}
			int clientNumber = getClientNumberWithInputKey(fileName);
			int clientNumber_backup = getClientNumberWithInputKey_Resilience(fileName);
//			messagePrint("client: "+ clientNumber);
			String clientInfo = getClientInfo(clientNumber);
			String clientInfo_backup = getClientInfo(clientNumber_backup);//get backup server info
			
			
			if(clientInfo == ""){
				messagePrint("Not find client");
				return -1;
			}
			
			i = regFileToDHT(clientInfo, fileName, fileSize);
			int j = regFileToDHT(clientInfo_backup, fileName, fileSize);//register file to backup server
			if(i==1&&j==1){
				messagePrint("Save key and value to "+clientInfo);
				messagePrint("Back up the key and value to "+ clientInfo_backup);
			}
			else if(i==1&&j==-1){
				messagePrint("Save key and value to "+clientInfo);
//				messagePrint("backup filed");
			}
			else if(i==-1){
//				messagePrint("register file failed");
			}
		}
		
		}catch(Exception ex){
			messagePrint(ex.toString());
		}
        messagePrint("");
		return -1;
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
//		Socket socket = new Socket(InetAddress.getLocalHost(), port);
		
		outputInfo = "INPUT" +"/"+ fileName+"/"+fileSize+"/"+clientName + "/" + p2pport;
		out = new PrintWriter(socket.getOutputStream(), true);
		out.println(outputInfo);
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		inputInfo = in.readLine();
		if (inputInfo.trim().equalsIgnoreCase("INPUTSUC")){
//			messagePrint("register successfully");
			socket.close();
			in.close();
			out.close();
			return 1;
		}else if(inputInfo.trim().equalsIgnoreCase("INPUTFAIL")){
//			messagepPrint("register failed");
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
//    	messagePrint("the map : " +theNumberOfHashMap);
    	int i = k % theNumberOfHashMap;
    	return i;
    }
    ////using the key to get the another resilience client number from DHT to back up data
    private static int getClientNumberWithInputKey_Resilience(String key){
    	char k = key.charAt(0);
    	int i = ((k % theNumberOfHashMap)+1)%theNumberOfHashMap;
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
    
    //search file function
    public static String search(){
    	String i;
    	try{
    		while(true){
    		messagePrint("Input the fileName to search in DHT: ");
    		Scanner inputFileName = new Scanner(System.in);
    		String fileName = inputFileName.nextLine();
    		//
    		i = searchFileInDHT(fileName);
    		if(i==""||i==null){
    			messagePrint("try to find the file in the back up server");
    			i = searchFileInDHT_backup(fileName);
    		}
//    		messagePrint("i : " + i);
    		return i;
    		}
    	}catch(Exception ex){
    		messagePrint(ex.toString());
    	}
    	return "";
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
    					messagePrint("get the value is : " + value);
    					
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
  //the method searches file in the DHT by using back up server
    public static String searchFileInDHT_backup(String fileName){
    	int clientNumber = getClientNumberWithInputKey_Resilience(fileName);
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
    					messagePrint("get the value is : " + value);
    					
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
            messagePrint("Download ......");
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
            messagePrint("");
            messagePrint("Download Success, file saved to: " + savePath);
            messagePrint("---------------------------------------");
            fileOut.flush();
            fileOut.close();
            socket.close();
            return 1;
        }catch(Exception ex){
        	messagePrint(ex.toString());
        }
    	return -1;
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
				try {
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String inputInfo = in.readLine();
					String[] input = inputInfo.split("/");
					if (inputInfo == null) {
						messagePrint("-----------------------------");
						socket.close();
					} else if (input[0].trim().equalsIgnoreCase("INPUT")) {//register files in the DHT
						String fileName = input[1];
	                	String fileSize = input[2];
	                	String clientName = input[3];
	                	String port = input[4];
	                	inputKeyAndValue(fileName, fileSize, clientName, port, socket);
	                	
					} else if (input[0].trim().equalsIgnoreCase("SER")) {//search file in the DHT
//						messagePrint("ser");
						String fileName = input[1];
//						messagePrint("ser"  + fileName);
						returnTheKeyValue(fileName, socket);
						
					} else if (input[0].trim().equalsIgnoreCase("DL")) {
						String fileName = input[1];
//						messagePrint("dl: " + fileName);
						returnDownloadFile(fileName, socket);
					}
					in.close();
					socket.close();
				} catch (Exception ex) {
					messagePrint(ex.toString());
				}
			}
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
		socket.close();
		}catch (Exception ex){
			messagePrint(ex.toString());
		}
    	return -1;
    }
	  //input the key and value into hash table
    private static int inputKeyAndValue(String fileName, String fileSize, String clientName, String port, Socket socket){
    	PrintWriter out = null;
    	try{
    		out = new PrintWriter(socket.getOutputStream(), true);
		if(!qList.containsKey(fileName)){
			String clientInfo =  fileSize + "/" + clientName + "/" + port;
			messagePrint("Put <" + fileName + "," + clientInfo + "> into hashmap");
			qList.put(fileName, clientInfo);
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
    
    //the method return the download file when the client being a server and preparing to be downloaded
    private static int returnDownloadFile(String fileName, Socket socket){
    	DataInputStream filein = null;
        DataOutputStream out = null;
        try{
			out = new DataOutputStream(socket.getOutputStream());
            if (fileName == null) {
            	socket.close();
            	return -1;
            }
            String filePath = "" + fileFolder + fileName;
            File file = new File(filePath);
            filein = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            out.writeLong((long) file.length());
            out.flush();
            byte[] bufArray = new byte[bufferSize];
            while (true) {
                int read = 0;
                if (filein != null) {
                    read = filein.read(bufArray);
                }
                
                if (read == -1) {
                    break;
                }
                out.write(bufArray, 0, read);
            }
            out.flush();
            
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