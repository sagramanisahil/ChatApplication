import java.net.Socket;
import java.io.*;
public class client
{
public static void main(String [] args)
{
	String Client_MSG = "";
	String Server_MSG = "";
	BufferedReader Terminal_Reader = new BufferedReader(new InputStreamReader(System.in));
	
	System.out.println("Client Started");	
	


	
	try
	{
		Socket Client_Socket = new Socket("100.115.92.196", 8080);
		System.out.println("Connection Established");
		PrintWriter Client_Writer = new PrintWriter(Client_Socket.getOutputStream(),true);
		BufferedReader Server_Reader = new BufferedReader(new InputStreamReader(Client_Socket.getInputStream()));

		do {
			
			System.out.print("\nClient : ");
			
			Client_MSG = Terminal_Reader.readLine();
			Client_Writer.println(Client_MSG);
			
			Server_MSG = Server_Reader.readLine();
			if(Server_MSG != null)
			System.out.print("\nServer : "+Server_MSG);
			
			}
			while(!Client_MSG.equals("zzz") && !Server_MSG.equals("zzz"));
			Client_Socket.close();
			Terminal_Reader.close();
			Server_Reader.close();
			Client_Writer.close();
	}

	catch(Exception e)
	{
		e.printStackTrace();

	}
}
}
