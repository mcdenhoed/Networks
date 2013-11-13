import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.TreeMap;

import edu.utulsa.unet.UDPSocket;

public class RSendUDP implements edu.utulsa.unet.RSendUDPI {

	private UDPSocket socket;
	private InetSocketAddress receiver;
	private int mode = 0;
	private int windowSize;
	private long timeout = 1000;
	private String fileName;
	private int localPort = 12987;
	private int remotePort;
	private int MTU = 1500;
	private TreeMap<Integer, DatagramPacket> frames;
	private String startMessage = "Sender at %s attempting transmission on port %d using ";
	private String stopAndWaitMessage = "Stop & Wait.\n";
	private String slidingWindowMessage = "Sliding window, with window size = %s.\n";
	private long endTime;
	private long startTime;
	private int bytes;
	private int messages;

	public RSendUDP(){
		receiver = new InetSocketAddress("localhost", localPort);
	}

	public static void main(String[] args) {
		// Papa's test code
		RSendUDP sender = new RSendUDP();
		sender.setMode(0);
		sender.setModeParameter(256);
		sender.setTimeout(1000);	
		sender.setFilename("unet.properties");
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
			socket = new UDPSocket(localPort);
			socket.setSoTimeout((int) timeout);
			//socket.connect(receiver);
			MTU = socket.getSendBufferSize();
			if (MTU < 6)
				throw new IOException("MTU must be at least 6");

			//BufferedInputStream fileBuffer = new BufferedInputStream(new FileInputStream(new File(System.getProperty("user.dir"), fileName)));
			BufferedInputStream fileBuffer = new BufferedInputStream(new FileInputStream(new File(fileName)));
			
			System.out.printf(startMessage, socket.getLocalAddress().toString(), localPort);
			startTime = System.nanoTime();
			if (mode == 0){
				System.out.printf(stopAndWaitMessage);
				stopAndWaitSend(fileBuffer, socket);
			}else if (mode == 1){
				System.out.printf(slidingWindowMessage, windowSize);
				slidingWindowSend(fileBuffer, socket);
			}
			fileBuffer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Please select an actual file.");
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		return true;
	}

	private int min(int a, int b) {
		if (a <= b)
			return a;
		return b;
	}

	private void stopAndWaitSend(BufferedInputStream file, UDPSocket socket)
			throws IOException {
		windowSize = 1;
		slidingWindowSend(file, socket);
	}

	private void slidingWindowSend(BufferedInputStream file, UDPSocket socket)
			throws IOException {
		boolean printStartMsg = true;
		int seq = 0;
		byte[] ack_buffer = new byte[5];
		frames = new TreeMap<Integer, DatagramPacket>();
		while (file.available() > 0 || !frames.isEmpty()) {
			while (file.available() > 0	&& (frames.isEmpty() || (seq - frames.firstKey() < windowSize))) {
				byte[] buffer = makePacket(seq, file);
				frames.put(seq++, new DatagramPacket(buffer, buffer.length,	receiver));
				
			}
			if(frames.isEmpty()){
				seq++;
				break;
			}
			for (DatagramPacket packet : frames.values()) {
				socket.send(packet);
				System.out.printf("Sent packet #%d, containing %d bytes of data.\n", getSequenceNumber(packet.getData()), packet.getLength()-5);

			}
			try {
				while (true) {
					DatagramPacket p = new DatagramPacket(ack_buffer, 5);
					socket.receive(p);
					if(printStartMsg){
						printStartMsg = false;
						System.out.printf("'Connection' made to %s:%d\n", p.getAddress().toString(), p.getPort());

					}
					frames.remove(getSequenceNumber(ack_buffer));
					System.out.printf("Received ACK for packet #%d\n", getSequenceNumber(ack_buffer));
					messages++;

				}

			} catch (SocketTimeoutException e) {
				//timed out
				if(!frames.isEmpty())
				System.out.println("Receive timed out. Resending buffered packets...");
			}

		}
		/*
		try{
			while(!finishPacket(ack_buffer)){
				socket.send(new DatagramPacket(generatePacketHeader(false, true, seq), 5, receiver));
				socket.receive(new DatagramPacket(ack_buffer, 5));
			}	
		}catch(SocketTimeoutException e){
		}*/
		
		while(!finishPacket(ack_buffer)){
			try{
				socket.send(new DatagramPacket(generatePacketHeader(false, true, seq), 5, receiver));
				socket.receive(new DatagramPacket(ack_buffer, 5));
			} catch(SocketTimeoutException e){
				//timed out
			}
		}

		
		for(int i = 0; i < 10; i++){
			socket.send(new DatagramPacket(generatePacketHeader(true, true, seq), 5, receiver));
		}
		endTime = System.nanoTime();
		printSummary();
	}

	private void printSummary() {
		int seconds = (int)((endTime-startTime)/Math.pow(10, 9));
		System.out.printf("Complete! Transmitted %d bytes in %d messages over %d seconds", bytes, messages, seconds);
	}
	
	private int getSequenceNumber(byte[] header) {
		return ByteBuffer.wrap(header, 1, 4).getInt();
	}

	private byte[] makePacket(int sequence, BufferedInputStream file) {
		byte[] header = generatePacketHeader(false, false, sequence);
		byte[] data;
		byte[] result = header;
		try {
			data = new byte[min(file.available(), MTU - 5)];
			file.read(data);
			bytes += data.length;
			result = new byte[header.length + data.length];
			System.arraycopy(header, 0, result, 0, header.length);
			System.arraycopy(data, 0, result, header.length, data.length);
			return result;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private byte[] generatePacketHeader(boolean ack, boolean finish,
			int sequence) {
		byte packetFlags = 0;
		if (ack)
			packetFlags += 1;
		if (finish)
			packetFlags += 2;

		byte[] header = { packetFlags, 0, 0, 0, 0 };
		for (int i = 0; i < 4; i++) {
			header[i + 1] = ByteBuffer.allocate(4).putInt(sequence).array()[i];
		}
		return header;
	}

	private boolean ackPacket(byte[] packet) {
		return (packet[0] & 1) != 0;
	}

	private boolean finishPacket(byte[] packet) {
		return (packet[0] & 2) != 0;
	}

	@Override
	public void setFilename(String arg0) {
		if((new File(arg0)).exists())
			fileName = arg0;
		else{
			System.out.println("File does not exist! Check filename.");
			System.exit(-1);
		}
		return;
	}

	@Override
	public boolean setLocalPort(int arg0) {
		if(0 < arg0 && arg0 <= 65535)
			localPort = arg0;
		else{
			System.out.println("Invalid port number! Must be between 0 and 65535.");
		}
		return true;
	}

	@Override
	public boolean setMode(int arg0) {
		if(mode == 0 || mode == 1)
			mode = arg0;
		else{
			System.out.println("Invalid mode! 0 for Stop and Wait; 1 for Sliding Window");
			System.exit(-1);
		}
		return true;
	}

	@Override
	public boolean setModeParameter(long arg0) {
		if (arg0 > 0)
			windowSize = (int) arg0;
		else{
			System.out.println("Invalid window size! Must be a positive integer.");
			System.exit(-1);
		}
		return true;
	}

	@Override
	public boolean setReceiver(InetSocketAddress arg0) {
		if(arg0 != null)
			receiver = arg0;
		else{
			System.out.println("Must specify a receiver");
			System.exit(-1);
		}
		return true;
	}

	@Override
	public boolean setTimeout(long arg0) {
		if(arg0 > 0)
			timeout = arg0;
		else{
			System.out.println("Invalid timeout! Must be a positive integer!");
			System.exit(-1);
		}
		return true;
	}

}
