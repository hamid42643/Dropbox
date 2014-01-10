package CLIENT;
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

import Message.*;

import sun.nio.cs.US_ASCII;

public class DecoderClientSide implements Settings{
	//private Message msg;
	
	public DecoderClientSide(){
		
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
	    		//------------------------------------
			    	//client decoding 'login' message sent from the server
			    	if(firstWord.contains(Message.version)){
			    		token = scn.next();
			    		Scanner scn3 = new Scanner(firstLine);
			    		scn3.useDelimiter(" ");
			    		scn3.next();
			    		String oper = scn3.next();
			    		int rc = Integer.parseInt(scn3.next());
			    		String rs = scn3.next();
			    		msg = new Message(true);
			    		msg.setIsResponse(true);
			    		msg.setOperation(oper);
			    		msg.setResponseCode(rc);
			    		msg.setResponseString(rs);
			    		
			    	}

			    break;
	    	}
	    	else if(firstLine.contains(DIR)){
	    		//------------------------------------
	    		//client decoding 'dir' message sent from the server
	    	 	//is a response message
		    	if(firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);	
		    		scn3.useDelimiter(" ");
		    		scn3.next();scn3.next();
		    		String reponseCode = scn3.next();
		    		
		    		String secondLine = scn.next();//got to next line
		    		scn3 = new Scanner(secondLine);
		    		scn3.useDelimiter(":");
		    		scn3.next();
		    		String bytesNum = scn3.next();//gets the number of bytes in the body
		    		
		    		String thirdLine = scn.next();//got to next line
		    		scn3 = new Scanner(thirdLine);
		    		scn3.useDelimiter(":");
		    		scn3.next();
		    		String linesNum = scn3.next();//gets the number of lines in the body //lines:2
		    		
		    		//create message object to return to the client
		    		msg = new Message(true);
		    		msg.setOperation(DIR);
		    		msg.setResponseCode(Integer.parseInt(reponseCode));
		    		msg.setBytes(bytesNum);
		    		msg.setLines(Integer.parseInt(linesNum));
		    		
		    		String str = new String(Decoder.getBody(message.getInputStm(), Integer.parseInt(bytesNum)));
		    		
		    		//
		
		    		
		    		ArrayList<myFiles> arr = new ArrayList<myFiles>();
		    		myFiles file = null;
		    		Scanner lineScn = new Scanner(str);
		    		lineScn.useDelimiter("\r\n");
		    		
		    		while(lineScn.hasNext()){
			    	    String line = lineScn.next();
			    	    Scanner wordScn = new Scanner(line);
			    	    
			    		while(wordScn.hasNext()){
			    			wordScn.useDelimiter(":");
			    			file = new myFiles();
			    			
			    			file.setFileName(wordScn.next());
			    			file.setFileSize(Integer.parseInt(wordScn.next()));
			    		}
			    		
			    		arr.add(file);
		    		}
		    		
		    		msg.setFilesList(arr);
		    		msg.setBody(str);
		    		return msg;
		    	}
	    	}
	    	//------------------------------------
	    	else if(firstLine.contains(GET)){
	    		
	    		//is a response message
	    		//client decoding 'get' message sent from the server
	    		if(firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);	
		    		scn3.useDelimiter(" ");
		    		scn3.next();scn3.next();
		    		String reponseCode = scn3.next();
		    		
		    		String secondLine = scn.next();//got to next line
		    		scn3 = new Scanner(secondLine);
		    		scn3.useDelimiter(":");
		    		scn3.next();
		    		String bytesNum = scn3.next();//gets the number of bytes in the body
		    		
		    		//create message object to return to the client
		    		msg = new Message(true);
		    		msg.setOperation(GET);
		    		msg.setResponseCode(Integer.parseInt(reponseCode));
		    		msg.setBytes(bytesNum);
		    		//msg.setLines(Integer.parseInt(linesNum));
		    		
		    		byte[] data1 = Decoder.getBody(message.getInputStm(), Integer.parseInt(bytesNum));
		    		
		    		//if the file is text based
		    		//if not should print an appropriate error message later
		    		msg.setBody(new String(data1));
		    		msg.setFileStream(data1);
		    		return msg;
	    		}
	    	}
	    	
	    	else if(firstLine.contains(PUT)){
	    		//is a response message
	    		//client decoding 'put' message sent from the server
	    		if(firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);
		    		scn3.useDelimiter(" ");
		    		scn3.next(); scn3.next();
		    		String code = scn3.next();
		    		String codeString = scn3.next();
		    		
		    		msg = new Message(true);
		    		msg.setOperation(PUT);
		    		msg.setResponseCode(Integer.parseInt(code));
		    		msg.setResponseString(codeString);
		    		return msg;
	    		}
	    	}
	    	
	    	
	    	else if(firstLine.contains(DELETE)){
	    		//is a response message
	    		//client decoding 'DELETE' message sent from the server
	    		if(firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);
		    		scn3.useDelimiter(" ");
		    		scn3.next(); scn3.next();
		    		String code = scn3.next();
		    		String codeString = scn3.next();
		    		
		    		msg = new Message(true);
		    		msg.setOperation(DELETE);
		    		msg.setResponseCode(Integer.parseInt(code));
		    		msg.setResponseString(codeString);
		    		return msg;
	    		}
	    	}
	    	
	    	else if(firstLine.contains(EXIT)){
	    		//is a response message
	    		//client decoding 'EXIT' message sent from the server
	    		if(firstWord.contains(Message.version)){
		    		Scanner scn3 = new Scanner(firstLine);
		    		scn3.useDelimiter(" ");
		    		scn3.next(); scn3.next();
		    		String code = scn3.next();
		    		String codeString = scn3.next();
		    		
		    		msg = new Message(true);
		    		msg.setOperation(EXIT);
		    		msg.setResponseCode(Integer.parseInt(code));
		    		msg.setResponseString(codeString);
		    		return msg;
	    		}
	    	}
	    	
	    }

		return msg;
	}

}



