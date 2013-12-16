import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HttpServer {

	public class RequestHandler implements Runnable{
		Socket socket;

		public RequestHandler(Socket s){
			socket = s;
		}
		
		private boolean containsHostline(ArrayList<String> request){
			for(int i = 0; i < request.size() ; i++)
				if(request.get(i).split(" ")[0].equals("Host:"))
					return true;
			return false;
		}
		
		
		private void handleGET(OutputStreamWriter o, ArrayList<String> request) throws IOException{
			if(!containsHostline(request)){
				badRequest(o);
				return;
			}
			
			String URI = request.get(0).split(" ")[1];
			File f = new File("public_html" + URI);
			if(f.exists() && !f.isDirectory()){
				FileInputStream reader = new FileInputStream("public_html" + URI);
				byte[] fileData = new byte[(int)f.length()];
				reader.read(fileData);
				
				String type = "";
				if(URI.endsWith(".htm") || URI.endsWith("html"))
					type = "text/html";
				else if(URI.endsWith(".gif"))
					type = "image/gif";
				else if(URI.endsWith(".jpg") || URI.endsWith("jpeg"))
					type = "image/jpeg";
				else if(URI.endsWith(".pdf"))
					type = "application/pdf";
				
				String response = "HTTP/1.1 200 \r\n";
				response += entity((int)f.length(), type);
				response += "\r\n";
				
				char[] yolo = new char[response.length() + fileData.length];
				System.arraycopy(response.toCharArray(), 0, yolo,  0, response.length());
				System.arraycopy(new String(fileData).toCharArray(), 0, yolo, response.length(), fileData.length);
				o.write(yolo);
				o.flush();
				System.out.println(yolo);
			}else notFound(o);
		}

		private void handleHEAD(OutputStreamWriter o, ArrayList<String> request) throws IOException{
			if(!containsHostline(request)){
				badRequest(o);
				return;
			}
			String URI = request.get(0).split(" ")[1];
			File f = new File("public_html" + URI);
			if(f.exists() && !f.isDirectory()){
			
				String type = "";
				if(URI.endsWith(".htm") || URI.endsWith("html"))
					type = "text/html";
				else if(URI.endsWith(".gif"))
					type = "image/gif";
				else if(URI.endsWith(".jpg") || URI.endsWith("jpeg"))
					type = "image/jpeg";
				else if(URI.endsWith(".pdf"))
					type = "application/pdf";
				
				String response = "HTTP/1.1 200 \r\n";
				response += entity((int)f.length(), type);
				response += "\r\n";
				
				o.write(response);
				o.flush();
				System.out.println(response);
			}else notFound(o);
		}
		
		private void badRequest(OutputStreamWriter o) throws IOException{
			
		}
		
		private void notFound(OutputStreamWriter o) throws IOException{
			
			String response = "HTTP/1.1 404 File not found.\r\n";
			response += entity(0, "");
			System.out.println(response);
			o.write(response);
			o.flush();
		}
		
		private String entity(int length, String type){
			return "Server: yoloswag/0.01\r\nContent-Length: " + length + "\r\nContent-Type: " + type + "\r\n";
		}
		private void notImplemented(OutputStreamWriter o) throws IOException{
			String response = "HTTP/1.1 501 This functionality is not implemented yet. \r\n";
			response += "Server: yoloswag/0.01\r\n";
			response += "Content-Length: 0\r\n";
			response += "Content-Type: 0";
			o.write(response);
			o.flush();
		}
		public void run(){
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				OutputStreamWriter o = new OutputStreamWriter(socket.getOutputStream());
				ArrayList<String> request = new ArrayList<String>();
				String temp = r.readLine();
				while(temp != null && !temp.equals("")){
					request.add(temp);
					if(r.ready())
						temp = r.readLine();
				}
				if(request.size() > 0){
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
				}else
					badRequest(o);
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
