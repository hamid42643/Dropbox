package Message;
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

public class Decoder {
	//private Message msg;

	
	//input is inputstream
	//output is text inside the body of the message
	public static byte[] getBody(InputStream in, int bytesNum) throws IOException{
		DataInputStream input = new DataInputStream(in);

	    byte[] data = new byte[bytesNum];
	    input.readFully(data);
	    
	    return data;
	}

	
	
	//gets the InputStream and returns a Message object containing 
	//message byte array, and the input stream
	
	//getMessage reads the inputstream, return the message content
	//it also return back the inputstream, to be later used for reading messages with body contents
	public static Message getMessage(InputStream in, boolean containsBody) throws IOException{
        PushbackInputStream pInStream = new PushbackInputStream(in, 3);
        
        ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
        int a,b,c,d;
        boolean end = false;
        
        while (end == false) {
			//if encountered 13,10,13,10 pattern in the stream
			//or two CRLF it means its the end of the stream
			if((a = pInStream.read()) == 13){
				if((b = pInStream.read()) == 10){
					if((c = pInStream.read()) == 13){
						if((d = pInStream.read()) == 10){
							end = true;
						}else{
							pInStream.unread(d);
							pInStream.unread(c);
							pInStream.unread(b);
						}
					}else{
						pInStream.unread(c);
						pInStream.unread(b);
					}
				}else{
					pInStream.unread(b);
				}
			}
			messageBuffer.write(a);
			
			if(end==true){
				messageBuffer.write(10);
				messageBuffer.write(13);
				messageBuffer.write(10);
			}
		}
        
        Message msg = new Message(false);
        msg.setInputStm(in);
        msg.setOutputStm(messageBuffer.toByteArray());
        
        
        return msg;
	}
	
}



