import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;

import edu.utulsa.unet.UDPSocket;

public class RSendUDP implements edu.utulsa.unet.RSendUDPI{

	private UDPSocket socket;
	private InetSocketAddress receiver;
	private int mode;
	private long windowSize;
	private long timeout = 1000;
	private String fileName;
	private int localPort;
	private int remotePort;
	private int MTU = 1500;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Papa's test code
		RSendUDP sender = new RSendUDP();
		sender.setMode(0);
		sender.setTimeout(10000);
		sender.setFilename("important.txt");
		sender.setLocalPort(23456);
		sender.setReceiver(new InetSocketAddress("localhost", 32456));
		sender.sendFile();
	}

	@Override
	public String getFilename() {
		return fileName;
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public long getModeParameter() {
		return windowSize;
	}

	@Override
	public InetSocketAddress getReceiver() {
		return receiver;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public boolean sendFile() {
		
		try {
			BufferedInputStream fileBuffer = new BufferedInputStream(new FileInputStream(new File(System.getProperty("user.dir"),fileName)));
			socket = new UDPSocket(localPort);
			socket.setSoTimeout((int)timeout);
			//MTU = socket.getReceiveBufferSize();
			if(mode == 0)
				stopAndWaitSend(fileBuffer, socket);
			else if(mode == 1)
				slidingWindowSend(fileBuffer, socket);
			fileBuffer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Please select an actual file.");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(MTU);
			System.out.println("IO Error.");
		}

		return true;
	}

	private int min(int a, int b){
		if(a <= b)
			return a;
		return b;
	}
	private void stopAndWaitSend(BufferedInputStream file, UDPSocket socket) throws IOException {
		//ArrayList<Byte> buffer = new ArrayList<Byte>();

		byte seqNum = 0;
		boolean acked = true;
		int transmit = MTU;
		do{
			byte[] buffer = new byte[min(file.available() + 2, MTU)];
			if(acked){
				int result = 0;
				if(file.available() > 0){
					result = file.read(buffer, 2, buffer.length - 2);		
				}
				byte[] header = new byte[2];
				boolean finishFlag = false;
				if(file.available() > 0)
					finishFlag = true;
				header = generatePacketHeader(false, finishFlag, seqNum);
				buffer[0] = header[0];
				buffer[1] = header[1];
				transmit = buffer.length;
								
			}
			acked = false;
			socket.send(new DatagramPacket(buffer, transmit,receiver));
			socket.receive(new DatagramPacket(buffer,2));
			if(ackPacket(buffer)){
				acked = true;
				seqNum++;
				if(finishPacket(buffer)){
					break;
				}
			}
				
		}while(true);
	}

	private void slidingWindowSend(BufferedInputStream file, UDPSocket socket) throws IOException{
		// TODO Auto-generated method stub
		
	}
	
	private byte[] generatePacketHeader(boolean ack, boolean finish, byte sequence){
		byte packetFlags = 0;
		if(ack)
			packetFlags += 1;
		if(finish)
			packetFlags += 2;
		
		byte[] header = {packetFlags, sequence};
		return header;
	}

	private boolean ackPacket(byte[] packet){
		return (packet[0]&1)==0;
	}
	
	private boolean finishPacket(byte[] packet){
		return (packet[0]&2)==0;
	}
	
	@Override
	public void setFilename(String arg0) {
		fileName = arg0;
		return;
	}

	@Override
	public boolean setLocalPort(int arg0) {
		localPort = arg0;
		return true;
	}

	@Override
	public boolean setMode(int arg0) {
		mode = arg0;
		return true;
	}

	@Override
	public boolean setModeParameter(long arg0) {
		if(arg0 > 2)
		windowSize = arg0;
		else System.exit(-1);
		return true;
	}

	@Override
	public boolean setReceiver(InetSocketAddress arg0) {
		receiver = arg0;
		return true;
	}

	@Override
	public boolean setTimeout(long arg0) {
		timeout = arg0;
		return true;
	}

}
