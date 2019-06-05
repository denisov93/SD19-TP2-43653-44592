package discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/*
 * 
 */
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 10000;

	private static final String DELIMITER = "\t";

	/**
	 * 
	 * @param serviceName
	 * @param serviceURI
	 */
	public static void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s", DISCOVERY_ADDR, serviceName, serviceURI));

		byte[] pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();

		DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);
		new Thread(() -> {
			try (DatagramSocket ms = new DatagramSocket()) {
				for (;;) {
					ms.send(pkt);
					Thread.sleep(DISCOVERY_PERIOD);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}


	public static URI[] findUrisOf(String serviceName, int minRepliesNeeded) {
		Log.info(String.format("Starting discovery for: %s\n", serviceName));

		final int MAX_DATAGRAM_SIZE = 65535;

		Set<URI> results = new HashSet<>();
		long deadline = System.currentTimeMillis() + DISCOVERY_TIMEOUT;

		DatagramPacket pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);

		try (MulticastSocket ds = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
			ds.joinGroup(DISCOVERY_ADDR.getAddress());
			ds.setSoTimeout(DISCOVERY_PERIOD);
			while (System.currentTimeMillis() < deadline && results.size() < minRepliesNeeded) {
				try {
					pkt.setLength(MAX_DATAGRAM_SIZE);
					ds.receive(pkt);
					String[] announcement = new String(pkt.getData(), 0, pkt.getLength()).split(DELIMITER);
					if (announcement.length == 2 && announcement[0].equals(serviceName)) {
						results.add(URI.create(announcement[1]));
					}
				} catch (SocketTimeoutException e) {
					System.err.println("Listening for: " + serviceName);
				}
			}
		} catch (IOException e) {
		}

		if (results.isEmpty())
			Log.info(String.format("No discoveries after timeout [%s ms]", DISCOVERY_TIMEOUT));

		return results.toArray(new URI[results.size()]);
	}
}
