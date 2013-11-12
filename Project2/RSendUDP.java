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

	public RSendUDP(){
		receiver = new InetSocketAddress("localhost", localPort);
	}
	/**
	 * @param args
	 */
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

			BufferedInputStream fileBuffer = new BufferedInputStream(
					new FileInputStream(new File(
							System.getProperty("user.dir"), fileName)));

			if (mode == 0)
				stopAndWaitSend(fileBuffer, socket);
			else if (mode == 1)
				slidingWindowSend(fileBuffer, socket);
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
			}
			try {
				while (true) {
					socket.receive(new DatagramPacket(ack_buffer, 5));
					frames.remove(getSequenceNumber(ack_buffer));
				}

			} catch (SocketTimeoutException e) {
				//timed out
			}

		}
		try{
			while(!finishPacket(ack_buffer)){
				socket.send(new DatagramPacket(generatePacketHeader(false, true, seq), 5, receiver));
				socket.receive(new DatagramPacket(ack_buffer, 5));
			}	
		}catch(SocketTimeoutException e){
			
		}
		/*while(!finishPacket(ack_buffer)){
			try{
				socket.send(new DatagramPacket(generatePacketHeader(false, true, seq), 5, receiver));
				socket.receive(new DatagramPacket(ack_buffer, 5));
			} catch(SocketTimeoutException e){
				//timed out
			}
		}*/

		socket.setSoTimeout(socket.getSoTimeout()*4);
		
		
		try{
			socket.receive(new DatagramPacket(ack_buffer, 5));
		}catch(SocketTimeoutException e){
			//no ack received. Fine with it though.
		}
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
		if (arg0 > 0)
			windowSize = (int) arg0;
		else
			System.exit(-1);
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
