import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.Exception;
public class Talk
{
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

					if(in.ready()){
						message = in.readLine();
						System.out.println();
						System.out.println("[remote]: " + message);
					}
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
		BufferedReader remote_in = null; try{
			Socket socket = new Socket(host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			remote_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			(new ListenToThings(remote_in, remote_message)).start();

			while(true){
				message = in.readLine();
				out.println(message);
			}
		}catch(UnknownHostException e){
			System.out.println("Unknown Host:" + host);
			System.exit(1);
		}catch(IOException e){
			System.out.println("Client unable to communicate with server");
			System.exit(1);
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
				remote_out.println(message);
			}
		}catch (IOException e){
			System.out.println("Read failed");
			System.exit(1);
		}
		return 0;
	}

	private static int autoMode(){

		return 0;
	}

	private static int help(){
			
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
