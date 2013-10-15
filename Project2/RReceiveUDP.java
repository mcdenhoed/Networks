import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

import edu.utulsa.unet.UDPSocket;

public class RReceiveUDP implements edu.utulsa.unet.RReceiveUDPI{

	private UDPSocket socket;
	private InetAddress receiver;
	private int mode;
	private long windowSize;
	private long timeout;
	private String fileName;
	private int localPort;
	private int remotePort;
	private int MTU;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RReceiveUDP receiver = new RReceiveUDP();
		receiver.setMode(0);
		receiver.setModeParameter(512);
		receiver.setFilename("less_important.txt");
		receiver.setLocalPort(32456);
		receiver.receiveFile();
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
	public boolean receiveFile() {
		FileOutputStream output = null;
		try {
			
			output = new FileOutputStream(fileName);
			socket = new UDPSocket(localPort);
			socket.setSoTimeout((int)timeout);
			MTU = socket.getReceiveBufferSize();
			receiver = socket.getInetAddress();
			if(mode == 0)
				stopAndWaitReceive(output, socket);
			else if(mode == 1)
				slidingWindowReceive(output, socket);
			else return false;
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;

	}

	private void stopAndWaitReceive(FileOutputStream output, UDPSocket socket) throws IOException {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[MTU];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		do{
			System.out.println("Waiting...");
			socket.receive(packet);
			output.write(buffer, 2, packet.getLength()-2);
			if(finishPacket(buffer)){
				socket.send(new DatagramPacket(generatePacketHeader(true, true, sequence(buffer)), 2));
				return;
			}
			else
				socket.send(new DatagramPacket(generatePacketHeader(true, false, sequence(buffer)), 2));

		}while(true);
	}

	private void slidingWindowReceive(FileOutputStream output, UDPSocket socket) throws IOException {
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
	
	private boolean finishPacket(byte[] packet){
		return (packet[0]&2)==0;
	}
	
	private byte sequence(byte[] packet){
		return packet[1];
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
		windowSize = arg0;
		return true;
	}

}
