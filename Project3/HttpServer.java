import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

	private class RequestHandler implements Runnable{
		public void run(){
			
		}
	}

	private int port = 16405;
	private ServerSocket serverSocket;
	private Socket socket;
	private String methodPattern = "(?<METHOD>\\S+)\\s(?<REQUEST-URL>\\S+)\\sHTTP/1.1\\r\\n";
	private String hostPatthern = "Host:\\s\\(?<HOST>\\S+)\\s\\r\\n";
	public HttpServer(){
		
	}
	public HttpServer(int p){
		port = p;
	}
	
	private void makeSocket(){
		try {
			serverSocket = new ServerSocket();
			socket = serverSocket.accept();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void run(){
		
		while(true){
			makeSocket();
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
	}
	
	public static void main(String[] args) {
		HttpServer h;
		if(args.length > 0)	h = new HttpServer(Integer.valueOf(args[0]));
		else h = new HttpServer();

		h.run();
	}

}
