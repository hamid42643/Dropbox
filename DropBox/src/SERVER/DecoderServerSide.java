package SERVER;
import Message.Decoder;
import Message.Message;
import Message.Settings;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.ByteArrayInputStream;



import sun.nio.cs.US_ASCII;

public class DecoderServerSide implements Settings{
	//private Message msg;
	
	public DecoderServerSide(){
		
	}
	

	
	//input is InputStream
	//output is a message object
	public Message decoder(InputStream in, boolean isResponseMessage) throws IOException{
		Message message = Decoder.getMessage(in, false);
		byte[] data = message.getOutputStm();
		Message msg=null;
	    ByteArrayInputStream msgStream = new ByteArrayInputStream(data);
	    Scanner scn = new Scanner(new InputStreamReader(msgStream));
	    scn.useDelimiter("\r\n");
	    
	    while(scn.hasNext()){
	    	String token = scn.next();//first line
	    	String firstLine = token;
	    	Scanner scn2 = new Scanner(token);
	    	scn2.useDelimiter(" ");	    	
	    	String firstWord = scn2.next();
	    	if(firstLine.contains(LOG)){
			    	//is a request message
			    	//server's decoding clients 'login' message
			    	if(!firstWord.contains(Message.version)){
			    		token = scn.next();
			    		Scanner scn3 = new Scanner(token);
			    		scn3.useDelimiter(":");
			    		scn3.next();
			    		String u = scn3.next();
			    		String p = scn3.next();
			    		msg = new Message(false);
			    		msg.setUserName(u);
			    		msg.setPass(p);
			    		msg.setOperation(LOG);
			    		return msg;
			    	}
			    break;
	    	}
	    	
	    	
	    	else if(firstLine.contains(DIR)){
		    	//server's decoding clients 'dir' message
		    	if(!firstWord.contains(Message.version)){
		    		msg= new Message(false);
		    		msg.setOperation(DIR);
		    		return msg;
		    	}
	    	}
	    	
	    	
	    	//------------------------------------
	    	else if(firstLine.contains(GET)){
	    		//server's decoding clients 'get' message
	    		if(!firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);
		    		scn3.useDelimiter(" ");
		    		scn3.next();
		    		String fileName = scn3.next();
		    		msg= new Message(false);
		    		msg.setFileName(fileName);
		    		msg.setOperation(GET);
		    		return msg;
	    		}
	    	}
	    	//--------------------------------
	    	else if(firstLine.contains(DELETE)){
	    		//server's decoding clients 'delete' message
	    		if(!firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);
		    		scn3.useDelimiter(" ");
		    		scn3.next();
		    		String fileName = scn3.next();
		    		msg = new Message(false);
		    		msg.setFileName(fileName);
		    		msg.setOperation(DELETE);
		    		return msg;
	    		}
	    	}
	    	
	    	else if(firstLine.contains(EXIT)){
	    		//server's decoding clients 'EXIT' message
	    		if(!firstWord.contains(Message.version)){
		    		msg = new Message(false);
		    		msg.setOperation(EXIT);
		    		return msg;
	    		}
	    	}
	    	//------------------------------------
	    	else if(firstLine.contains(PUT)){
	    		//server's decoding clients 'put' message
	    		if(!firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);	
		    		scn3.useDelimiter(" ");
		    		scn3.next();
		    		String fileName = scn3.next();
		    		
		    		String secondLine = scn.next();//got to next line
		    		scn3 = new Scanner(secondLine);
		    		scn3.useDelimiter(":");
		    		scn3.next();
		    		String bytesNum = scn3.next();//gets the number of bytes in the body
		    		

		    		
		    		byte[] data1 = Decoder.getBody(message.getInputStm(), Integer.parseInt(bytesNum));
		    		
		    		
		    		//create message object
		    		msg = new Message(false);
		    		//if the file is text based
		    		//if not should print an appropriate error message later
		    		msg.setBytes(bytesNum);
		    		msg.setBody(new String(data1));
		    		msg.setOperation(PUT);
		    		msg.setFileName(fileName);
		    		msg.setFileStream(data1);

		    		//msg.toString();
		    		
		    		return msg;
	    		}
	    	}
	    }

		return msg;
	}
	
}

	



