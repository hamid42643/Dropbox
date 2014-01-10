package CLIENT; 
import Message.*;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.font.CreatedFontTracker;


/**
 * This code has been adapted from:
 * http://download.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
 */
public class Client implements Settings{
	
	static InputStream inStream=null;
	static OutputStream outStream=null;
	
	static String USERNAME="";
	static String PASSWORD="";
	static int PORT=0;
	static String SHAREDIR="";
	
	public static void main(String args[]) throws IOException {
		//get arguments
		
		gerArgs(args);

		
		Socket echoSocket = null;
		String input = "s";
		try {
			System.setProperty("java.net.preferIPv4Stack", "true");
			echoSocket = new Socket("localhost", PORT);
			
			outStream = echoSocket.getOutputStream();
			inStream = echoSocket.getInputStream();
			
			login(USERNAME, PASSWORD);
			
			do{
				System.out.print("enter command:");
				input = (new Scanner(System.in)).nextLine();
				
				switch(input){
				
					case "l":
						login(USERNAME, PASSWORD);
					break;
					
					case "d":
						dir();
					break;
					
					
					case "g":
						//get();
					break;
					
					
					case "p":
						//put();
					break;
					
					
					case "de":
						//delete();
					break;
					
					case "s":
						synchronize();
					break;
					
					case "exit":
						exit();
					break;
				}
				
			}while(!input.equals("exit"));
			
		} catch (IOException e) {
			System.err.println("Couldn't open socket for the connection.");
			System.exit(1);
		}

		//out.close();
		//echoSocket.close();
	}
	
	
	
	
	//------------------Synchronization-----------------------

	
	private static void synchronize(){
		ArrayList<myFiles> filesListSnapshot = null;
		ArrayList<myFiles> filesListClient = null;
		ArrayList<myFiles> fileListServer;
		ArrayList<myFiles> fileListCompared;
		Message msg;
		
		msg = dir();
		

		CreatSnapshot(msg.getFilesList(), "");
		
		//file list from the snapshot
		filesListSnapshot = readSnapshot();
		
		//file list that server sent
		fileListServer = msg.getFilesList();
		
		//file list from client local directory
		filesListClient = getFiles();
		
		
		fileListCompared = compareFileLists(filesListSnapshot, filesListClient, fileListServer);

		
		for(int i=0 ; i<fileListCompared.size() ; i++){
			String code = fileListCompared.get(i).getFileExistsCode();
			
			switch(code){
				case "110":
					//delete the file on the client
					deleteLocal(fileListCompared.get(i).getFileName());
				break;
				
				case "101":
					//delete file on the server
					delete(fileListCompared.get(i).getFileName());
				break;
				
				case "111":
					//compare files for changes
				break;
				
				case "100":
					//File was deleted on both client and server do nothing

				break;
				
				case "010":
					//Put file on the server
					put(fileListCompared.get(i).getFileName());
				break;
				
				case "011":
					//File was created on both client and server do nothing
				break;
				
				case "001":
					//Get file from the server
					get(fileListCompared.get(i).getFileName());
				break;
			}
		}
		
		CreatSnapshot(getFiles(), "update");
	}
	


/*	gets 3 arraylists of myFile type and compare them together,
	it returns another arraylist of myFile type, with a code corresponding to each filename, 
	indicating what has to be done to the file
	example: 001 means file exists on the server but doesn't exist on the snapshot or the client
	
	  snapshot|client|server
		0	  |  0   |   1
		*/
		
	private static ArrayList<myFiles> compareFileLists(ArrayList<myFiles> filesListSnapshot, ArrayList<myFiles> filesListClient, 
			ArrayList<myFiles> fileListServer){
		
		ArrayList<myFiles> filesList = new ArrayList<myFiles>();
		myFiles file;

		String fileExistsCode;
		for(int i=0 ; i<filesListSnapshot.size() ; i++){
			fileExistsCode="0";
			String fileName = filesListSnapshot.get(i).getFileName();
			
			if(fileName.equals("newfile3.txt")){
				@SuppressWarnings("unused")
				int jj=0;
			}
			if(containsStr(filesListClient, fileName)){
				fileExistsCode="110";
			}
			if(containsStr(fileListServer, fileName)){
				if(fileExistsCode=="110"){
					fileExistsCode="111";
				}else{
					fileExistsCode="101";
				}
			}
			else{
				if(fileExistsCode!="110"){
					fileExistsCode="100";
				}
			}
			
			file = new myFiles();
			file.setFileName(fileName);
			file.setFileExistsCode(fileExistsCode);
			filesList.add(file);
		}
		
		
		for(int i=0 ; i<filesListClient.size() ; i++){
			String fileName1 = filesListClient.get(i).getFileName();
			
			if(fileName1.equals("myfile5.txt")){
				@SuppressWarnings("unused")
				int i1=0;
			}
			
			if(!containsStr(filesListSnapshot, fileName1)){
				

				if(containsStr(fileListServer, fileName1)){
						fileExistsCode="011";
				}else{
					fileExistsCode="010"; 
				}
				
				file = new myFiles();
				file.setFileName(fileName1);
				file.setFileExistsCode(fileExistsCode);
				filesList.add(file);
			}
		}

		for(int i=0 ; i<fileListServer.size() ; i++){
			String fileName2 = fileListServer.get(i).getFileName();
			fileExistsCode="0";
			
			if(!containsStr(filesListClient, fileName2)){
				if(!containsStr(filesListSnapshot, fileName2)){
					fileExistsCode="001";
					
					file = new myFiles();
					file.setFileName(fileName2);
					file.setFileExistsCode(fileExistsCode);
					filesList.add(file);
				}
			}
		
		}
		return filesList;
	}
	

	
	//------------------creating and reading snapshot file-----------------------
	
	//read the xml file on the local directory
	//and return an arraylist of myfile type
	public static ArrayList<myFiles> readSnapshot(){
		ArrayList<myFiles> filesList = new ArrayList<myFiles>();
		
		try {
			File fXmlFile = new File("./files_snapshot.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
 
			doc.getDocumentElement().normalize();
 
			NodeList nList = doc.getElementsByTagName("file");
			myFiles file = null;
 
			for (int i = 0; i < nList.getLength(); i++) {
				Node n = nList.item(i);
				
				if (n.getNodeType() == Node.ELEMENT_NODE) {
 
					Element eElement = (Element) n;
					String name = eElement.getElementsByTagName("name").item(0).getTextContent();
					String size = eElement.getElementsByTagName("size").item(0).getTextContent();
					
					file = new myFiles();
					file.setFileName(name);
					file.setFileSize(Integer.parseInt(size));
				}
			
				filesList.add(file);
			}//for
			
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		return filesList;
	}
	
	
	
	//get an arraylist of myfile type that came from the server
	//and create an xml file
	private static boolean CreatSnapshot(ArrayList<myFiles> filesList, String operation){
		//adopted from: http://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/
	
		boolean createSnapshot = false;
		File xmlFile = new File("./files_snapshot.xml");
		
		//if file exists and we are not updating
		if((xmlFile.exists()) && !operation.equals("update")) {
			createSnapshot = false;
		}
		
		//if we are updating
		if(operation.equals("update")){
			createSnapshot = true;
		}

		//if we are updating
		if(!xmlFile.exists()){
			createSnapshot = true;
		}
		
		
		if(createSnapshot){
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("snapshot");
				doc.appendChild(rootElement);
	
	
				for(int i=0 ; i<filesList.size() ; i++){
					Element file = doc.createElement("file");
					rootElement.appendChild(file);
	
					Element name = doc.createElement("name");
					name.appendChild(doc.createTextNode(filesList.get(i).getFileName()));
					file.appendChild(name);
	
					Element size = doc.createElement("size");
					size.appendChild(doc.createTextNode(Long.toString(filesList.get(i).getFileSize())));
					file.appendChild(size);
				}
	
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(xmlFile);
	
				
				transformer.transform(source, result);
	
				} catch (DOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			
			return true;
		}
		return false;
	}

	
	//------------------operations to send to server-----------------------
	
	private static Message exit(){
		try {
			Message msg3 = new Message(false);
			msg3.setOperation(EXIT);
			outStream.write(msg3.toString().getBytes());
			
			
			//decode the byte stream coming from server
			DecoderClientSide c3 = new DecoderClientSide();
			Message msg2 = c3.decoder(inStream, true);
			printServerResponse(msg2.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static Message delete(String remoteFileName){
		try {
			//send the message to the server
			Message msg3 = new Message(false);
			msg3.setOperation(DELETE);
			msg3.setFileName(remoteFileName);
			outStream.write(msg3.toString().getBytes());
			
			
			//decode the byte stream coming from server
			DecoderClientSide c3 = new DecoderClientSide();
			Message msg2 = c3.decoder(inStream, true);
			printServerResponse(msg2.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	private static Message put(String fileName){
		Message msg = new Message(false);
		try {
			//get the file on the local folder on the server
			File myFile1;
			byte[] data = null;
			

			myFile1 = new File(SHAREDIR+"/"+ fileName);
			data = new byte[(int) myFile1.length()];
			DataInputStream input1 = new DataInputStream(new FileInputStream(myFile1));
			input1.readFully(data);


			Message m2= new Message(false);
			m2.setFileName(fileName);
			m2.setBody(new String(data));
			m2.setOperation(PUT);
			m2.setBytes(String.valueOf(myFile1.length()));
			outStream.write(m2.toString().getBytes());
			
			//decode the byte stream coming from server
			DecoderClientSide c3 = new DecoderClientSide();
			msg = c3.decoder(inStream, true);
			
			printServerResponse(msg.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return msg;
	}
	
	private static Message get(String fileName){
		Message msg = new Message(false);
		
		try {
			//send the message to the server
			Message msg3 = new Message(false);
			msg3.setOperation(GET);
			msg3.setFileName(fileName);
			outStream.write(msg3.toString().getBytes());
			
			//decode the byte stream coming from server
			DecoderClientSide c3 = new DecoderClientSide();
			
			msg = c3.decoder(inStream, true);
			printServerResponse(msg.toString());
			
			//create the file on the client computer
			File myFile = new File(SHAREDIR+"/"+fileName);
			//byte[] data = new byte[Integer.parseInt(msg.getBytes())];
			DataOutputStream output = new DataOutputStream(new FileOutputStream(myFile));
			output.write(msg.getFileStream());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return msg;
	}
	
	private static Message dir(){
		Message msg = new Message(false);
		
		try {
			Message msg2 = new Message(false);
			msg2.setOperation(DIR);
			outStream.write(msg2.toString().getBytes());
			
			//decode the byte stream coming from server
			DecoderClientSide c2 = new DecoderClientSide();
			msg = c2.decoder(inStream, true);
			printServerResponse(msg.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return msg;
	}
	
	private static Message login(String u, String p){
		Message msg = new Message(false);
		
		try {
			msg.setOperation(LOG);
			msg.setUserName(u);
			msg.setPass(p);
			outStream.write(msg.toString().getBytes());
			
			//decode the byte stream coming from server
			DecoderClientSide c = new DecoderClientSide();
			
			msg = c.decoder(inStream, true);
			printServerResponse(msg.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return msg;
	}
	
	//--------------------------------------------
	
	private static void printServerResponse(String str){
		System.out.println("-------SERVER------");
		System.out.print(str);
		System.out.println();
	}
	
	
	//delete a local file
	private static void deleteLocal(String localFilename){
		try {
			File file = new File(SHAREDIR+"/"+localFilename);
			
			if(file.delete()){
				System.out.print("local file "+localFilename+" was deleted!");
			}else{
				System.out.print("local file "+localFilename+" cant be deleted!");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	private static boolean containsStr(ArrayList<myFiles> arr, String str){
		for(int i=0; i<arr.size() ; i++){
			if(arr.get(i).getFileName().equals(str)){
				return true;
			}
		}
		return false;
	}
	
	
	private static ArrayList<myFiles> getFiles(){
		ArrayList<myFiles> arr = new ArrayList<myFiles>();
		myFiles file;
		
		File dir = new File(SHAREDIR);
		File[] listOfFiles = dir.listFiles();
		int i;
		String str="";
		
		for(i=0; i < listOfFiles.length ; i++){
			String filename = listOfFiles[i].getName();
			long size = listOfFiles[i].length();
			
			file = new myFiles();
			file.setFileName(filename);
			file.setFileSize(size);
			
			arr.add(file);
		}
		
		return arr;
	}

	private static void gerArgs(String args[]){
		try {
			for(int j=0 ; j<args.length ; j++){
				switch(args[j]){
					case "-d":
						SHAREDIR=args[j+1];
					break;
					
					case "-p":
						PORT=Integer.parseInt(args[j+1]);
					break;
					
					case "-u":
						USERNAME=args[j+1];
					break;
					
					case "-q":
						PASSWORD=args[j+1];
					break;
				}
			}
			
			if((SHAREDIR.equals(""))||(PORT==0)||(USERNAME.equals(""))||(PASSWORD.equals(""))){
				System.out.print("wrong arguments!! ");
				System.exit(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.print("wrong arguments!! ");
			System.exit(0);
		}
		
	}


}