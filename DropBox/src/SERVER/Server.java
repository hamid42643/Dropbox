package SERVER;
import Message.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Server implements Settings{
	private static int PORT=0;
	private static boolean isLoggedin = false;
	static InputStream in = null;
	static OutputStream out = null;
	
	
	@SuppressWarnings({ "null", "resource" })
	public static void main(String[] args) throws Exception {
		
		getArgs(args);
		
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		
		
		try 
		{
			System.setProperty("java.net.preferIPv4Stack", "true");
			serverSocket = new ServerSocket(20112, 1); // no backlog
			
		} catch (IOException e) {
			System.out.println("Could not listen on port: " + PORT);
			System.exit(-1);
		}

			try 
			{
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("Accept failed: " + 20112);
				System.exit(-1);
			}

				out = clientSocket.getOutputStream();
				in = clientSocket.getInputStream();
				
			while (true) {	
				
					Message msg = null;
						//decode the byte stream coming from client
						DecoderServerSide c = new DecoderServerSide();
						
						msg = c.decoder(in, false);
						System.out.println("-------Client------");
						System.out.print(msg);
						System.out.println();
					
					if(msg.getOperation().equals(LOG)){
						login(msg);
					}
					if(!msg.getOperation().equals(null)){
						if(isLoggedin){
							switch(msg.getOperation()){
								case DIR:
									dir(msg);
								break;
							
								//when a get request is sent by the client
								case GET:
									get(msg);
							    break;
							    
								case PUT:
									put(msg);
								break;
								
								case DELETE:
									delete(msg);
							    break;
							    
								case EXIT:
									exit(msg);
									clientSocket.close();
									in.close();
									System.exit(0); //I shouldn't close the server!!
							    break;
							}
						}else{
							Message msg1 = new Message(true);
							msg1.setOperation(LOG);
							msg1.setResponseCode(REPONSECODE_CLIENTSIDEERROR);
							msg1.setResponseString("you_must_login_first!!!!");
							out.write(msg1.toString().getBytes());
						}
					}
		}//while
	}
	
	
	private static void exit(Message msg){
		try {
			Message msg2 = new Message(true);
			msg2.setOperation(EXIT);
			msg2.setResponseCode(REPONSECODE_SUCCESS);
			msg2.setResponseString("ok");
			out.write(msg2.toString().getBytes());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void delete(Message msg){
		try {
			File file = new File("./SERVER_SHARE_DIR/"+msg.getFileName());
			
			Message msg2 = new Message(true);
			msg2.setOperation(DELETE);
			if(file.delete()){
				msg2.setResponseCode(REPONSECODE_SUCCESS);
				msg2.setResponseString("ok");
			}else{
				msg2.setResponseCode(REPONSECODE_CLIENTSIDEERROR);
				msg2.setResponseString("cant_delete_the_file!!");
			}
			
			out.write(msg2.toString().getBytes());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	private static void login(Message msg){
		try {
			Message code = checkLogin(msg);
			Message m= new Message(true);
			m.setOperation(LOG);
			m.setResponseCode(code.getResponseCode());
			m.setResponseString(code.getResponseString());
			out.write(m.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void dir(Message msg){
		try {
			
			File dir = new File("./SERVER_SHARE_DIR");
			File[] listOfFiles = dir.listFiles();
			int i;
			String str="";
			
			for(i=0; i < listOfFiles.length ; i++){
				str+=listOfFiles[i].getName()+":"+listOfFiles[i].length()+Message.space;
			}
			
			Message m1= new Message(true);
			m1.setLines(i);
			m1.setBody(str);
			m1.setOperation(DIR);
			m1.setResponseCode(REPONSECODE_SUCCESS);
			m1.setResponseString("ok");
			out.write(m1.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private static void get(Message msg){
		try {
			
			//get the file on the local folder on the server
			File myFile;
			byte[] data = null;
			
			myFile = new File("./SERVER_SHARE_DIR/"+msg.getFileName());
			data = new byte[(int) myFile.length()];
			DataInputStream input = new DataInputStream(new FileInputStream(myFile));
			input.readFully(data);

			Message m2= new Message(true);
			m2.setBody(new String(data));
			m2.setOperation(GET);
			m2.setResponseCode(REPONSECODE_SUCCESS);
			m2.setResponseString("ok");
			m2.setBytes(String.valueOf(myFile.length()));
			out.write(m2.toString().getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void put(Message msg){
		try {
			//create the file on the server computer
			File myFile1 = new File("./SERVER_SHARE_DIR/"+msg.getFileName());
			DataOutputStream output = new DataOutputStream(new FileOutputStream(myFile1));
			output.write(msg.getFileStream());
			
			//send the response to the client
			Message m3 = new Message(true);
			m3.setResponseCode(REPONSECODE_SUCCESS);
			m3.setResponseString("ok");
			m3.setOperation(PUT);
			out.write(m3.toString().getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Message checkLogin(Message msg) throws IOException{

		Message msg1 = new Message(false);
		//adopted from :http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
		try {
			File fXmlFile = new File("./usernames.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
 
			doc.getDocumentElement().normalize();
 
			NodeList nList = doc.getElementsByTagName("record");
 
 
			for (int i = 0; i < nList.getLength(); i++) {
				Node n = nList.item(i);
				
				if (n.getNodeType() == Node.ELEMENT_NODE) {
 
					Element eElement = (Element) n;
					String user = eElement.getElementsByTagName("username").item(0).getTextContent();
					String pass = eElement.getElementsByTagName("password").item(0).getTextContent();
					
					if(msg.getUserName().equals(user)){
							if((msg.getPass().equals(pass))){
								//this should be changed later for support of multiple clients
								if(isLoggedin==false){ 
									msg1.setResponseCode(REPONSECODE_SUCCESS);
									msg1.setResponseString("ok");
									isLoggedin=true;
								}else{
									msg1.setResponseCode(REPONSECODE_CLIENTSIDEERROR);
									msg1.setResponseString("already_loggedin!!");
								}
						}else{
							msg1.setResponseCode(REPONSECODE_CLIENTSIDEERROR);
							msg1.setResponseString("wrong_password!!");
						}
					}
				}
			
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
		}
		return msg1; 
	}
	
	
	
	
	private static void getArgs(String args[]){
		try {
			for(int j=0 ; j<args.length ; j++){
				switch(args[j]){
					case "-p":
						PORT=Integer.parseInt(args[j+1]);
					break;
				
				}
			}
			
			if(PORT==0){
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
