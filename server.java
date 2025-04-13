import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
public class server
{
public static void main(String [] args)
{
	String Client_MSG = "";
	String Server_MSG = "";
	BufferedReader Terminal_Reader = new BufferedReader(new InputStreamReader(System.in));
	try
	{
		ServerSocket Server_Socket = new ServerSocket(8080);
		System.out.println("Server started \n\n");
		Socket Real_Socket = Server_Socket.accept();
		System.out.println("Client Connected");
		
		PrintWriter Client_Writer = new PrintWriter(Real_Socket.getOutputStream(), true);
		// Client_Writer.println("\t\t======================");
		// Client_Writer.println("\t\tWelcome to the Server!");
		// Client_Writer.println("\t\t======================");
		BufferedReader Client_Reader = new BufferedReader(new InputStreamReader(Real_Socket.getInputStream()));

		do
		{
			
			Client_MSG = Client_Reader.readLine();
			if(Client_MSG != null)
			System.out.print("\nClient : "+Client_MSG);

			System.out.print("\nServer : ");
			Server_MSG = Terminal_Reader.readLine();
			Client_Writer.println(Server_MSG);
		}	
		while(!Client_MSG.equals("zzz") && !Server_MSG.equals("zzz"));
		Client_Reader.close();
		Client_Writer.close();
		Terminal_Reader.close();
		Real_Socket.close();
		Server_Socket.close();
		}
	catch(Exception e)
	{
		e.printStackTrace();
	}
}
}
