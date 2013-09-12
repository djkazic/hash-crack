import java.net.ServerSocket;
import java.net.Socket;

public class Listener implements Runnable {
	public int port;
	public boolean run;
	public ServerSocket ss;
	public Listener(int port) {
		this.port = port;
		run = true;
	}
	public void run() {
		try {
			ss = new ServerSocket(port);
		} catch (Exception e) { 
			Startup.out("Failed to bind to port " + port);
			Startup.delListener(port);
			return;
		}
		Socket cs;
		while (run) {
			try {
				cs = ss.accept();
				if (run) {
					(new Thread(new Client(cs))).start();
				}
			} catch (Exception e) {  }
		}
	}
	public void stop() {
		run = false;
	}
}