package Message;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import com.sun.org.apache.xml.internal.utils.BoolStack;



public class Message implements Settings{
	private boolean isResponse;
	
	//response message
	private int responseCode;
	private String responseString;
	
	//message
	private String operation;
	private String path;
	public static final String version = "DROP/1.0";
	private String bytes;
	private String body;
	private InputStream inputStm;
	private byte[] outputStm;
	
	public static final String space= "\r\n";
	
	//operation related parameters
	//---------------------------------
	//login
	private String userName;
	private String pass;
	
	//directory
	private int lines; // number of lines in the body of the message
	private ArrayList<myFiles> filesList;

	//get
	private String fileName;
	private byte[] fileByteArray;
	
	
	
	
	
	public ArrayList<myFiles> getFilesList() {
		return filesList;
	}

	public void setFilesList(ArrayList<myFiles> filesList) {
		this.filesList = filesList;
	}

	public String getResponseString() {
		return responseString;
	}

	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}

	public byte[] getFileStream() {
		return fileByteArray;
	}

	public void setFileStream(byte[] fileByteArray) {
		this.fileByteArray = fileByteArray;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getInputStm() {
		return inputStm;
	}

	public void setInputStm(InputStream inputStm) {
		this.inputStm = inputStm;
	}

	public byte[] getOutputStm() {
		return outputStm;
	}

	public void setOutputStm(byte[] outputStm) {
		this.outputStm = outputStm;
	}

	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}

	public String getUserName() {
		return userName;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getBytes() {
		return bytes;
	}

	public void setBytes(String bytes) {
		this.bytes = bytes;
	}

	public boolean isResponse() {
		return isResponse;
	}

	public void setIsResponse(boolean isResponse) {
		this.isResponse = isResponse;
	}

	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	
	//the constructor determines if the message is request or a response
	public Message(boolean isResponse){
		this.isResponse = isResponse;
	}
	


	@Override
	public String toString() {
		String str="";
		//request
		if(!isResponse){
			switch(operation){
				case LOG:
					str = 
					operation + ' ' + version + space +
					"Authorization" + ":" +userName  + ":" + pass+ space + space;				
				break;

				case DIR:
					str = 
					operation + ' ' + version + space +	space;		
				break;
				
				case GET:
					str = 
					operation + ' ' + fileName + ' ' + version + space +	space;		
				break;
				
				case PUT:
					str =
					operation + ' ' + fileName + ' ' + version + ' ' + space +
					"Bytes:" + getBytes() +space+ space+
					getBody();	
				break;
				
				case DELETE:
					str = 
					operation + ' ' + fileName + ' ' + version + space +	space;		
				break;
				
				case EXIT:
					str = 
					operation + ' ' + version + space +	space;		
				break;
			}
		}
		
		//response
		else if(isResponse){
			switch(operation){
				case LOG:
					str = 
					version + ' ' + LOG + ' ' + responseCode + ' ' + responseString + space + space;	
				break;
				
				case DIR:
					str =
					version + ' ' + DIR + ' ' + responseCode + ' ' + responseString + space +
					"size:" + getBody().getBytes().length +space+
					"lines:"+ getLines() + space + space + getBody();	
				break;
		
				case GET:
					str =
					version + ' ' + GET + ' ' + responseCode + ' ' + responseString + space +
					"size:" + getBody().getBytes().length +space+ space+
					getBody();	
				break;
				
				case PUT:
					str = 
					version + ' ' + operation + ' ' + responseCode + ' ' + responseString + space +	space;		
				break;	
				
				case DELETE:
					str = 
					version + ' ' + operation + ' ' + responseCode + ' ' + responseString + space +	space;		
				break;
				
				case EXIT:
					str = 
					version + ' ' + operation + ' ' + responseCode + ' ' + responseString + space +	space;		
				break;	
			}		
			
		}
		return str;
	}
	
}




