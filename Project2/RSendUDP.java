import java.net.InetSocketAddress;
import edu.utulsa.unet.*;

public class RSendUDP implements edu.utulsa.unet.RSendUDPI{

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
		//Papa's test code
		RSendUDP sender = new RSendUDP();
		sender.setMode(2);
		sender.setModeParameter(512);
		sender.setTimeout(10000);
		sender.setFilename("important.txt");
		sender.setLocalPort(23456);
		sender.setReceiver(new InetSocketAddress("172.17.34.56", 32456));
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
		if(mode == 0)
			stopAndWaitSend();
		else if(mode == 1)
			slidingWindowSend();
		else return false;
		return true;
	}

	private void stopAndWaitSend() {
		// TODO Auto-generated method stub
		
	}

	private void slidingWindowSend() {
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
