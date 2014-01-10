package Message;

public interface Settings {
	public String LOG = "LOG";
	public String PUT = "PUT";
	public String GET = "GET";
	public String DIR = "DIR";
	public String DELETE = "DELETE";
	public String EXIT = "EXIT";
	
	public int REPONSECODE_INFORMATIONAL  = 100;
	public int REPONSECODE_SUCCESS  = 200;
	public int REPONSECODE_REDIRECTION  = 300;
	public int REPONSECODE_CLIENTSIDEERROR  = 400;
	public int REPONSECODE_SERVERSIDEERROR  = 500;
	
}
