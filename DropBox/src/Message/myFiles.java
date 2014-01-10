package Message;

import java.util.ArrayList;


public class myFiles{
	String fileName;
	long fileSize;
	String fileExistsCode="0";
	
	
	
	public String getFileExistsCode() {
		return fileExistsCode;
	}
	public void setFileExistsCode(String fileExistsCode2) {
		this.fileExistsCode = fileExistsCode2;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long l) {
		this.fileSize = l;
	}
	@Override
	public String toString() {
		String space = Message.space;
		String str;
		if(fileExistsCode=="0"){
			str = 
				fileName+":"+fileSize+space;
		}else{
			str = 
					fileName+":"+fileSize+":"+"fileExistsCode"+fileExistsCode+space;
		}
		
		return str;
	}
	


	
}
