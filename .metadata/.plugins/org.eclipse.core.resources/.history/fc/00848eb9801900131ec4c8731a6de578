import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;


public class ListenerThread implements Runnable {
	public Client c;
	public DataInputStream dis;
	public ListenerThread(Client c, DataInputStream dis) {
		this.dis = dis;
		this.c = c;
	}
	public void run() {
		byte current;
		while (true) {
			try {
				current = dis.readByte();
				if (current == 0x01) {
					c.disconnect();
					break;
				} else if (current == 0x02) {
					c.clientHashRate = dis.readInt();
				} else if (current == 0x03) {
					c.clientLast = readBigInteger(dis);
				} else if (current == 0x04) {
					Startup.success(readString(dis), c.cs.getInetAddress().getHostAddress());
				} else if (current == 0x05) {
					c.average = dis.readInt();
				} else if (current == 0x06) {
					c.applied = false;
				}
			} catch (Exception e) { c.disconnect(); break; }
		}
	}
	public String readString(DataInputStream par0DataInputStream) throws IOException {
		short word0 = par0DataInputStream.readShort();
		StringBuilder stringbuilder = new StringBuilder();

		for (int i = 0; i < word0; i++)
		{
			stringbuilder.append(par0DataInputStream.readChar());
		}

		return stringbuilder.toString();
	}
	public BigInteger readBigInteger(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		byte[] data = new byte[length];
		for (int i = 0; i < length; i ++) {
			data[i] = dis.readByte();
		}
		BigInteger returnBig = new BigInteger(data);
		return returnBig;
	}
}