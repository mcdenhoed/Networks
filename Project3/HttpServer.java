import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpServer {

	public class RequestHandler implements Runnable{
		Socket socket;

		public RequestHandler(Socket s){
			socket = s;
		}
		
		private boolean containsHostline(ArrayList<String> request){
			for(Iterator<String> i = request.iterator(); i.hasNext(); )
				if(i.next().split(" ")[0] == "Host:")
					return true;
			return false;
		}
		
		
		private void handleGET(OutputStreamWriter o, ArrayList<String> request) throws IOException{
			if(!containsHostline(request)){
				badRequest(o);
				return;
			}
		}

		private void handleHEAD(OutputStreamWriter o, ArrayList<String> request) throws IOException{
			if(!containsHostline(request)){
				badRequest(o);
				return;
			}
		}
		
		private void badRequest(OutputStreamWriter o) throws IOException{
			
		}
		
		private void notFound(OutputStreamWriter o) throws IOException{
			
		}
		
		private void notImplemented(OutputStreamWriter o) throws IOException{
			String response = "HTTP/1.1 501 This functionality is not implemented yet. \r\n";
			response += "Server: yoloswag/0.01\r\n";
			response += "Content-Length: 0\r\n";
			response += "Content-Type: 0";
			o.write(response);
		}
		public void run(){
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				OutputStreamWriter o = new OutputStreamWriter(socket.getOutputStream());
				ArrayList<String> request = new ArrayList<String>();
				String temp = r.readLine();
				while(temp != null){
					request.add(temp);
					temp = r.readLine();
				}
				String[] method = request.get(0).split(" ");
				try{
					switch(method[0]){
					case "GET":
						handleGET(o, request);
						break;
					case "HEAD":
						handleHEAD(o, request);
						break;
					default:
						notImplemented(o);
					}
				}catch(IOException e){
					System.out.println("Somthing broke.");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch blocks
				e.printStackTrace();
			} finally{
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private int port = 16405;
	private ServerSocket serverSocket;
	private String methodPatternString = "(?<METHOD>\\S+)\\s(?<REQUESTURL>\\S+)\\sHTTP/1\\.1\\s*";
	private String hostPatternString = "Host:\\s(?<HOST>\\S+)\\s*";
	public HttpServer(){
		
	}
	public HttpServer(int p){
		port = p;
	}
	
	private String handleGET(String resource){
		return null;
	}


	private Socket makeSocket(){
		try {
			return serverSocket.accept();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
		
	}
	public void run(){
		try {
			serverSocket = new ServerSocket(port);
			while(true){
				Socket s = makeSocket();
				new Thread(new RequestHandler(s)).start();
			}
		} catch (IOException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		HttpServer h;
		if(args.length > 0)	h = new HttpServer(Integer.valueOf(args[0]));
		else h = new HttpServer();
		h.run();
	}

}
