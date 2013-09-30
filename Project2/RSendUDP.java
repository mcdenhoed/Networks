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
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return false;
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
