import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.TreeMap;

import edu.utulsa.unet.UDPSocket;

public class RReceiveUDP implements edu.utulsa.unet.RReceiveUDPI{

	private UDPSocket socket;
	private InetAddress receiver;
	private int mode = 0;
	private long windowSize = 256;
	//private long timeout;
	private String fileName;
	private int localPort;
	private int remotePort;
	private int MTU;
	private int lowest_wanted = 0;
	private int highest_wanted;
	private TreeMap<Integer, byte[]> frames;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RReceiveUDP receiver = new RReceiveUDP();
		receiver.setMode(0);
		receiver.setModeParameter(256);
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
			//socket.setSoTimeout((int)timeout);
			MTU = socket.getSendBufferSize();
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
		windowSize = 1;
		slidingWindowReceive(output, socket);
	}

	private void slidingWindowReceive(FileOutputStream output, UDPSocket socket) throws IOException {
		boolean finished = false;
		int seq = Integer.MIN_VALUE;
		byte[] buffer = new byte[MTU];
		highest_wanted = (int) (lowest_wanted + windowSize);
		frames = new TreeMap<Integer, byte[]>();

		while(!finished){
			//Receive a packet
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			byte[] message = Arrays.copyOf(buffer, packet.getLength());
			seq = getSequenceNumber(message);
			if(seq == 0){
				receiver = packet.getAddress();
				remotePort = packet.getPort();
				//socket.connect(receiver, remotePort);
			}
			if(finishPacket(message)){
				finished = true;
				break;
			}
				
			if(seq<=highest_wanted){
				sendAck(seq, finished, socket);
			}
			
			if(seq >= lowest_wanted && !frames.containsKey(seq)){
				frames.put(seq, message);
			}
			
			while(frames.containsKey(lowest_wanted)){
				output.write(packetData(frames.get(lowest_wanted)));
				frames.remove(lowest_wanted++);
				highest_wanted++;
			}
		}
		byte[] handshake = new byte[5];
		socket.setSoTimeout(10);

		try{
			while(!ackPacket(handshake) || !finishPacket(handshake)){
				sendAck(seq, true, socket);
				socket.receive(new DatagramPacket(handshake, handshake.length));
			}
		}
		catch(SocketTimeoutException e){
			//receive timed out
		}
	}

	private boolean ackPacket(byte[] packet){
		return (packet[0]&1)!=0;
	}
	
	private int getSequenceNumber(byte[] header){
		return ByteBuffer.wrap(header, 1, 4).getInt();
	}
	
	private void sendAck(int seq, boolean finished, UDPSocket socket){
		byte[] packet = generatePacketHeader(true, finished, seq);
		
		try {
			socket.send(new DatagramPacket(packet, packet.length, receiver, remotePort));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	private byte[] generatePacketHeader(boolean ack, boolean finish, int sequence){
		byte packetFlags = 0;
		if(ack)
			packetFlags += 1;
		if(finish)
			packetFlags += 2;
		
		byte[] header = {packetFlags, 0, 0, 0, 0};
		for(int i = 0; i<4; i++){
			header[i+1] = ByteBuffer.allocate(4).putInt(sequence).array()[i];
		}
		return header;
	}
	
	private boolean finishPacket(byte[] packet){
		return (packet[0]&2)!=0;
	}
	
	
	private byte[] packetData(byte[] packet){
		if(packet.length > 5)
		return Arrays.copyOfRange(packet, 5, packet.length);
		else return new byte[0];
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
