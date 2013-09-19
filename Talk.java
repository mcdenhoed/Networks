import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.Exception;
public class Talk{
	private static enum Mode{CLIENT, SERVER, AUTO, HELP}
	private static Mode mode;
	private static String host = "127.0.0.1";
	private static int port = 12987;

	private static void setPort(String number) throws Exception, NumberFormatException{
		port = Integer.parseInt(number);
		if(port > 65535)
			throw new Exception("Invalid TCP port number.");
	}
	private static int parseArgs(String[] args) throws Exception, NumberFormatException{
		if(args.length == 0){
			throw new Exception("No arguments given.");
		}
		int index = 0;
		if(args[index].equals("-h")){
			//client mode
			mode = Mode.CLIENT;
			index++;
			while(index < args.length){
				if(args[index].equals("-p")){
					index++;
					if(index < args.length){
						setPort(args[index]);
						return 0;
					}else{
						throw new Exception("Please supply a port!");
					}
				}else{
					host = args[index];
					index++;
				}
			}
		}else if(args[0].equals("-s")){
			//server mode
			index += 1;
			if(args.length == 3){
				if(args[index].equals("-p")){
					index += 1;
					setPort(args[index]);
				}else throw new Exception("Bad arguments!");	
			}else if(args.length != 1) throw new Exception("Bad arguments!");
			mode = Mode.SERVER;
		}else if(args[0].equals("-a")){
			//auto mode
			mode = Mode.AUTO;
			index++;
			while(index < args.length){
				if(args[index].equals("-p")){
					index++;
					if(index < args.length){
						setPort(args[index]);
						return 0;
					}else{
						throw new Exception("Please supply a port!");
					}
				}else{
					host = args[index];
					index++;
				}
			}
		}else if(args[0].equals("-help")){
			//output help message
			mode = Mode.HELP;
		}else throw new Exception("Argument " + args[0] + " is not recognized.");
		return 0;
	}
	static class ListenToThings extends Thread {
		public BufferedReader in;
		public String message;
		public ListenToThings(BufferedReader b, String m){
			in = b;
			message = m;
		}
		public void run(){
			try{
				while(true){
						message = in.readLine();
						if(message == null){
							System.out.println("Remote party disconnected. Exiting.");
							System.exit(1);
						}
						System.out.println();
						System.out.println("[remote]: " + message);
				}
			}catch (IOException e){
				System.out.println("Read failed");
				System.exit(1);
			}
		}
	}
	private static int clientMode(){
		String message = null;
		String remote_message = null;
		BufferedReader remote_in = null; 
		try{
			Socket socket = new Socket(host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			remote_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			(new ListenToThings(remote_in, remote_message)).start();
			System.out.println("Connected to " + socket.getInetAddress());
			while(true){
				message = in.readLine();
				if(!message.replaceAll("\\s+", "").equals("STATUS"))
					out.println(message);
				else
					printStatus(socket);
			}
		}catch(UnknownHostException e){
			System.out.println("Unknown Host:" + host);
			System.exit(1);
		}catch(IOException e){
			System.out.println("Client unable to communicate with server");
			if(mode == Mode.CLIENT){
				System.exit(1);
			}
			else if(mode == Mode.AUTO){
				return 1;
			}
		}
		return 0;
	}

	private static Socket serverNegotiateConnection(){
		ServerSocket server = null;
		Socket client = null;
		try{
			server = new ServerSocket(port);
			System.out.println("Server listening on port " + port);
		}catch(IOException e){
			System.out.println("Server unable to listen on specified port");
			System.exit(1);
		}
		try{
			client = server.accept();
			System.out.println("Server accepted connection from " + client.getInetAddress());
		}catch(IOException e){
			System.out.println("Accept failed on port " + port);
			System.exit(1);
		}
		return client;
	}

	private static int serverMode(){
		BufferedReader remote_in = null;
		PrintWriter remote_out = null;
		BufferedReader sysInBuffer = null;
		String message = null;
		String remote_message = null;
		Socket client = null;

		client = serverNegotiateConnection();

		try{
			remote_in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			sysInBuffer = new BufferedReader(new InputStreamReader(System.in));
			remote_out = new PrintWriter(client.getOutputStream(), true);
		}catch(IOException e){
			System.out.println("Couldn't get an inputStream from the client");
			System.exit(1);
		}


		(new ListenToThings(remote_in, remote_message)).start();
		try{
			while(true){
				message = sysInBuffer.readLine();
				if(!message.replaceAll("\\s+", "").equals("STATUS"))
					remote_out.println(message);
				else
					printStatus(client);
			}
		}catch (IOException e){
			System.out.println("Read failed");
			System.exit(1);
		}
		return 0;
	}

	private static int autoMode(){
		if(clientMode() != 0) serverMode();
		return 0;
	}

	private static void printStatus(Socket s){
		System.out.println("Local IP Address:\t" + s.getLocalAddress());
		System.out.println("Remote IP Address:\t" + s.getInetAddress());
		System.out.println("Local Port Number:\t" + s.getLocalPort());
		System.out.println("Remote Port Number:\t" + s.getPort());
		return;
	}
	private static int help(){
		System.out.println("Mark DenHoed (he's the author!)\n");
		System.out.println("Program usage:\n");
		System.out.println("Talk -h [hostname | IPaddress] [-p portnumber]");
		System.out.println("The program behaves as a client connecting to [hostname | IPaddress] on port portnumber. If a server is not available the program exits. Note: portnumber in this case refers to the server and not to the client.");
		System.out.println();
		System.out.println("Talk -s [-p portnumber]");
		System.out.println("The program behaves as a server listening for connections on port portnumber. If the port is not available for use, the program exits.");
		System.out.println();
		System.out.println("Talk -a [hostname|IPaddress] [-p portnumber]");
		System.out.println("The program enters ``auto’’ mode. When in auto mode, your program should start as a client attempting to communicate with hostname|IPaddress on port portnumber. If a server is not found, the program detects this condition and start behaving as a server listening for connections on port portnumber.");
		System.out.println();
		System.out.println("Talk -help");
		System.out.println("The program prints this message.");

		return 0;
	}
	public static void main(String[] args){
		try{
			parseArgs(args);
		}catch (Exception e){
			System.out.println("ERROR. Now exiting.");
			System.out.println(e.getMessage());
			help();
			System.exit(1);
			return;
		}
		if (mode == Mode.CLIENT){
			clientMode();
		}else if(mode == Mode.SERVER){
			serverMode();
		}else if(mode == Mode.AUTO){
			autoMode();
		}else if(mode == Mode.HELP){
			help();
		}
	}
}
