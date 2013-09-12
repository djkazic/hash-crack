import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;

public class Client implements Runnable {
	public int average = -1;
	public boolean sentStop = false;
	public BigInteger[] bounds = new BigInteger[2];
	public boolean hasBlock = false;
	public boolean applied = false;
	public boolean isCracking;
	public BigInteger clientLast = BigInteger.valueOf(0);
	public int clientHashRate;
	public DataOutputStream dos;
	public DataInputStream dis;
	public boolean connected;
	public long lastKeepAlive = 0L;
	public SListenerThread lt;
	public Socket cs;
	public Client thisClient;
	public Client(Socket soc) {
		connected = false;
		this.cs = soc;
		thisClient = this;
	}
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			dos = new DataOutputStream(cs.getOutputStream());
			dis = new DataInputStream(cs.getInputStream());
			Startup.clients.add(this);
			cs.setSoTimeout(3000);
			dos.write(0x00);
			dos.flush();
			dis.read();
			connected = true;
			Startup.out("Inbound From Client IP: " + cs.getInetAddress().getHostAddress() + " PORT: " + cs.getPort());
			lt = new SListenerThread(this, dis);
			(new Thread(lt)).start();
			(new Thread(new Runnable() {
				public void run() {
					while (connected) {
						try {
							if(Startup.devMode) {
								dos.write(0x06);
								dos.flush();
								dos.writeBoolean(true);
								dos.flush();
								Startup.devMode = false;
							}
							if(Startup.killAll) {
								dos.write(0x03);
								dos.flush();
								Startup.killAll = false;
							}
							if (Startup.cracking) {
								sentStop = false;
								if (!applied) {
									applied = true;
									JobManagement.requests.add(thisClient);
								}
								if (hasBlock) {
									hasBlock = false;
									dos.write(0x05);
									dos.flush();
									writeBigInteger(bounds[0], dos);
									dos.flush();
									writeBigInteger(bounds[1], dos);
									dos.flush();
									writeString(Startup.currentHashString, dos);
									dos.flush();
								}
							} else {
								if (!sentStop) {
									dos.write(0x04);
									dos.flush();
									sentStop = true;
								}
								applied = false;
								hasBlock = false;
							}
							if (System.currentTimeMillis() - lastKeepAlive > 1000L) {
								dos.write(0x00);
								dos.flush();
								lastKeepAlive = System.currentTimeMillis();
							}
							Thread.sleep(25);
						} catch (Exception e) {  }
					}
				}
			})).start();
		} catch (Exception e) {  }
	}
	@SuppressWarnings("unchecked")
	public void disconnect() {
		String host = cs.getInetAddress().getHostAddress();
		try { dos.write(0x01); } catch (Exception e) {  }
		try { dos.flush(); } catch (Exception e) {  }
		try { dos.close(); } catch (Exception e) {  }
		try { dis.close(); } catch (Exception e) {  }
		try { cs.close(); } catch (Exception e) {  }
		connected = false;
		if (applied) {
			System.out.println("Stored block with range of " + bounds[1].subtract(bounds[0]).toString());
			JobManagement.incomplete.add(new BigInteger[] { bounds[0], bounds[1] });
		}
		Startup.out(host + " disconnected");
		removeFromList(this);
	}
	
	public void removeFromList(Client cl) {
		Startup.clients.remove(cl);
	}
	public boolean isConnected() {
		return connected;
	}
	public void writeBigInteger(BigInteger big, DataOutputStream par1DataOutputStream) throws IOException {
		byte[] data = big.toByteArray();
		int length = data.length;
		par1DataOutputStream.writeInt(length);
		for (int i = 0; i < length; i++) {
			par1DataOutputStream.writeByte(data[i]);
		}
		return;
	}
	public void writeString(String par0Str, DataOutputStream par1DataOutputStream) throws IOException {
		if (par0Str.length() > 32767) {
			throw new IOException("String too big");
		} else {
			par1DataOutputStream.writeShort(par0Str.length());
			par1DataOutputStream.writeChars(par0Str);
			return;
		}
	}
}