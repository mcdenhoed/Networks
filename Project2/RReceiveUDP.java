import java.net.InetSocketAddress;

import edu.utulsa.unet.*;

public class RReceiveUDP implements edu.utulsa.unet.RReceiveUDPI{

	private UDPSocket socket;
	private InetSocketAddress receiver;
	private int mode;
	private long windowSize;
	private long timeout;
	private String fileName;
	private int localPort;
	private int remotePort;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RReceiveUDP receiver = new RReceiveUDP();
		receiver.setMode(2);
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
		if(mode == 0)
			stopAndWaitReceive();
		else if(mode == 1)
			slidingWindowReceive();
		else return false;
		return true;
	}

	private void stopAndWaitReceive() {
		// TODO Auto-generated method stub
		
	}

	private void slidingWindowReceive() {
		// TODO Auto-generated method stub
		
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
