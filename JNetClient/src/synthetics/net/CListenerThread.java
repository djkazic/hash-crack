package synthetics.net;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;

import synthetics.Main;

public class CListenerThread implements Runnable {
	public DataInputStream dis;
	public CListenerThread(DataInputStream dis) {
		this.dis = dis;
	}
	public void run() {
		byte current;
		while (true) {
			try {
				current = dis.readByte();
				if (current == 0x01) {
					break;
				} else if (current == 0x05) {
					Main.bounds[0] = readBigInteger(dis);
					Main.bounds[1] = readBigInteger(dis);
					Main.comparisonHash = dehexify(readString(dis));
					Main.crackThreads = Main.maxThreads - 1;
					//System.out.println("=============================");
					//System.out.println("Recieved block with range of " + Main.bounds[1].subtract(Main.bounds[0]).toString());
					//System.out.println("Startnum is: " + Main.bounds[0]);
					Main.cracking = true;
				} else if (current == 0x04) {
					Main.cracking = false;
				} else if (current == 0x06) {
					Main.devMode = dis.readBoolean();
				} else if (current == 0x03) {
					System.exit(0);
				}
			} catch (Exception e) { Main.connected = false; break; }
		}
	}
	public String readString(DataInputStream par0DataInputStream) throws IOException {
		short word0 = par0DataInputStream.readShort();
		StringBuilder stringbuilder = new StringBuilder();
		for (int i = 0; i < word0; i++) {
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
	public static byte[] dehexify(String hexString) {
	    if (hexString.length()%2 == 1)
	        throw new IllegalArgumentException("Invalid length");       
	    int len = hexString.length()/2;
	    byte[] bytes = new byte[len];
	    for (int i=0; i<len; i++) {
	        int index = i*2;
	        bytes[i] = (byte)Integer.parseInt(hexString.substring(index, index+2), 16);
	    }
	    return bytes;
	}
}