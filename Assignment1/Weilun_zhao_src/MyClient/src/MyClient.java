//package MyClient;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

//import MyServer.MyServer.serverThread.RequestHandler;
public class MyClient {
	static Set<FileNode> fileList = new HashSet<FileNode>();
	static String serverAddress = "localhost:9999"; // Set the Server address
	static int bufferSize = 1024;
//	static String fileFolder = "src/MyClient/UP/";
	static String fileFolder = "UP/";
//	static String downLoadFolder = "src/MyClient/DL/";
	static String downLoadFolder = "DL/";
	// set the p2p port and client name
	static String clientName = "A"; // initial local client name;
//	static String serverPort = "9999"; // server port
	static String p2pPort = "9911";//client port
	public static void main(String[] args) throws IOException {
		 
		
		messagePrint("************************************");
		messagePrint("* Client: " + clientName + " start ...              *");
		messagePrint("* Author Weilun Zhao ; A20329942   *");
		messagePrint("************************************");
		new P2PClient().start();
		new P2PServer().start();
	}
	//the p2p client thread 
	private static class P2PClient extends Thread {
		int i;
		String searchResult = "";
		public void run() {
			reg();
			while (true) {
				try{
					sleep(500);
					displayAll();
					searchResult = search();
		            if (!searchResult.equals("") && searchResult != null) {
		            	messagePrint("find: "+searchResult);
		                perpareDownload(searchResult);
		            } else {
		                messagePrint("No file found ");
		            }

				}catch(Exception ex){
					messagePrint(ex.toString());
				}
			}
		}
	}
	//the displayAll shows all the files info before search()
	public static int displayAll(){
		PrintWriter out = null;
		BufferedReader in = null;
		String inputList = "";
		int len = 0;
		int i;
		try {
			String[] serAddr = serverAddress.split(":");
	        Socket socket = new Socket(serAddr[0], Integer.parseInt(serAddr[1]));
	        
	        String outputString = "ALL_"+"all";//send string with header "ALL"
	        out = new PrintWriter(socket.getOutputStream(),true);
			out.println(outputString);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        inputList = in.readLine();
	        String[] files = inputList.split("//");
	        len = files.length;
	        messagePrint("");
	        messagePrint("this is all the registed files in the server");
	        for(i = 0; i < len ;i++){
	        	messagePrint("fileInfo: " + files[i]);

	        }
		}catch(Exception ex){
			messagePrint(ex.toString());
		}
		return -1;
	}
	//the method is used to register all the files in the "fileFolder"
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
		
		for (File file : files) {
			fileName = file.getName();
			fileSize = file.length();
			messagePrint("fileName: " + fileName + " ;fileSize: " + fileSize);
			FileNode fileNode = new FileNode(fileName, fileSize);
			if (fileList.contains(fileNode) == false) {//find all the files in the filePath
				fileList.add(fileNode);//add file nodes to local file list 
			}
			outputReg(fileName, fileSize, clientName, p2pPort);
			
		}
		}
		catch(Exception ex){
			messagePrint(ex.toString());
		}
		return -1;
	}
	//send register files info to server
	private static int outputReg(String fileName, long fileSize, String clientName, String p2pPort){
		try {
			String[] serAddr = serverAddress.split(":");
	        Socket socket = new Socket(serAddr[0], Integer.parseInt(serAddr[1]));
	        PrintWriter out = null;
	        String outputString = "REG_";
			outputString = outputString + fileName + "/"+fileSize+"/"+clientName+"/"+p2pPort;
			out = new PrintWriter(socket.getOutputStream(),true);
			out.println(outputString);
			out.close();
			socket.close();
		}catch(Exception ex){
			messagePrint(ex.toString());
		}
		return -1;
	}
	//this method is to input file name and send search request
	public static String search(){
		Socket socket = null;
		int i = 0;
		String[] serAddr = serverAddress.split(":");
		BufferedReader in = null;
        PrintWriter out = null;
		try{
			socket = new Socket(serAddr[0], Integer.parseInt(serAddr[1]));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);			
			
			// input the file name to search in the server
            messagePrint(" ");
            messagePrint("Input the file name to search ...");
            Scanner inputFileName = new Scanner(System.in);
            String fileName = inputFileName.nextLine();
            
            // send the search info to server
            out.println("SER_" + fileName);
            
            // get the client info which contains the file expected
            String fileInfo = "";
            fileInfo = fileInfo + in.readLine();
//            messagePrint("Search result : " + fileInfo);
            socket.close();
            return fileInfo;
            
		}catch(Exception ex){
			messagePrint(ex.toString());
		}
		return "";
	}
	//this method to input string message
	private static void messagePrint(String str){
		System.out.println(str);
	}
	
	//this methode obtains file from other client
	public static void perpareDownload(String fileInfo) {
		String[] fileContain = fileInfo.split("//");
        int count = fileContain.length;
        try {
        	// print how many file found
            messagePrint("There is/are " + count + " file you want");
            // set the index to client which contain the file
            messagePrint("File index and Info: ");
            for (int i = 0; i < count; i++) {
                String[] file = fileContain[i].split("/");
                messagePrint("" + i + ":" + file[0] + " " + file[1] + " "
                             + file[2] + " " + file[3] );
            }
            
            int index = 0;
            String selector;
         // using index to get file from client
            messagePrint("Please input the index : [0 ~ " + (count-1)+ "] to select files : ");
            Scanner inputFileIndex = new Scanner(System.in);
            selector = inputFileIndex.next();
            index = Integer.parseInt(selector);
            
            while (index > count || index < 0 || !isInteger(selector)) {
                messagePrint("Wrong index input and input again");
                index = inputFileIndex.nextInt();
            }
            String[] fileNeed = fileContain[index].split("/");
            messagePrint("The index of client is " + index + ";");
            String fileName = fileNeed[0];
            long fileSize = Long.parseLong(fileNeed[1]);
            String clientName = fileNeed[2];
            String clientPort = fileNeed[3];
            
            // get file from remote client
            int returnValue = getFile(fileName, fileSize, clientName, clientPort);
            if (returnValue == -1) {
                messagePrint("Download Failed.");
            }
            
        }catch(Exception ex){
        	messagePrint(ex.toString());
        }
	}
	//this method to download file
	public static int getFile(String fileName, long fileSize, String clientName, String clientPort){
		int port = Integer.parseInt(clientPort);
//		messagePrint("port : " + port);
		PrintWriter out = null;
		Socket socket = null;
		DataInputStream in = null;
		DataOutputStream fileOut = null;
        byte[] buf = new byte[bufferSize];
        long passedlen = 0;
        long len = 0;
        String savePath = "" + downLoadFolder + fileName;
        try{
        	socket = new Socket("localhost", port);
        	out = new PrintWriter(socket.getOutputStream(), true);
        	out.println(fileName);
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
	//this method to know input is integer or not
	private static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
	//this is P2PServer allowing other clients to download files 
	private static class P2PServer extends Thread {
		ServerSocket P2PSocket = null;
		int port = Integer.parseInt(p2pPort);
		public void run(){
			try{
				P2PSocket = new ServerSocket(port);
				if (P2PSocket != null) {
                    while (true) {
                        new P2PHandler(P2PSocket.accept()).start();
                    }
                }

			}catch(Exception ex){
				messagePrint(ex.toString());
			}
		}
		//local client is to be a server and send file to other client
		public static class P2PHandler extends Thread{
			Socket socket = null;
			private DataInputStream filein = null;
	        private BufferedReader in = null;
	        private DataOutputStream out = null;
	        
			P2PHandler(Socket socket){
				this.socket = socket;
			}
			public void run(){
				try{
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out = new DataOutputStream(socket.getOutputStream());
	                String input = in.readLine();
	                if (input == null) {
	                	socket.close();
	                	return ;
	                }
	                String filePath = "" + fileFolder + input;
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
			}
			
		}
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
