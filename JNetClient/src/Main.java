import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;

//Copyright Kevin Cai & Daniel Greenberg
public class Main {
	public static int[] rateArray = new int[2];
	public static BigInteger[] bounds = new BigInteger[2];
	public static BigInteger lowestNumber = BigInteger.valueOf(0);
	public InetSocketAddress connectionAddress;
	public Socket connectionSocket;
	public volatile DataOutputStream dos;
	public volatile DataInputStream dis;
	public static boolean requestNew = false;
	public static boolean cracking = false;
	public static boolean upperCase = false;
	public static boolean numbers = false;
	public static boolean special = false;
	public static boolean devMode = false;
	public static byte[] comparisonHash;
	public static long hashesThisSecond = 0;
	public static long lastKeepAlive = 0L;
	public CListenerThread lt;
	public static String hostIp = null;
	public static int hostPort = 0;
	public static int maxThreads;
	public static int crackThreads = 0;
	public static boolean connected = false;
	public static boolean sendAverage = false;
	public static int average = -1;
	public static String solution = "";
	public static boolean foundSolution = false;
	public static boolean firstHashSent = false;

	public static void main (String args[]) {
		new Main();
	}
	public Main() {
		initiate();
	}
	public void initiate() {
		maxThreads = Runtime.getRuntime().availableProcessors();
		try {
			System.out.println("[J-Net Client Initializing]");
			if(devMode) {
				(new Thread(new ServerFindThread())).start();
			} else {
				hostIp = "24.51.213.165";
				hostPort = 1800;
			}
			//INSERT THREAD TO SCAN HERE
			//hostIp = "192.168.1.10";
			//hostPort = 8888;
		} catch (Exception e) { out("[FATAL] Failed to load settings"); System.exit(1); }
		(new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						int total = 0;
						BigInteger storage = BigInteger.valueOf(0);
						for (int i = 0; i < CrackManager.cr.length; i ++) {
							if (storage.compareTo(BigInteger.valueOf(0)) == 0) {
								storage = CrackManager.bi[i];
							}
							if (CrackManager.bi[i].compareTo(storage) == -1) {
								storage = CrackManager.bi[i];
							}
							total += CrackManager.cr[i].hashesPerSecond;
						}
						lowestNumber = storage;
						hashesThisSecond = total;
					} catch (Exception e) {  }
					try { Thread.sleep(1000); } catch (Exception e) {  }
				}
			}
		})).start();
		(new Thread(new CrackManager())).start();
		Runtime.getRuntime().addShutdownHook((new Thread(new Runnable() {
			public void run() {
				try {
					dos.writeByte(0x01);
					dos.flush();
				} catch (Exception e) {  }
			}
		})));
		while (true) {
			if(ServerFindThread.gotServer || !devMode) {
				try {
					if(ServerFindThread.gotServer)
						System.out.println("[Attempting to connect...]");
						connectionSocket = new Socket();
						connectionAddress = new InetSocketAddress(hostIp, hostPort);
						connectionSocket.setSoTimeout(2500);
						connectionSocket.connect(connectionAddress);
						dos = new DataOutputStream(connectionSocket.getOutputStream());
						dis = new DataInputStream(connectionSocket.getInputStream());
						dis.read();
						dos.writeByte(0x00);
						dos.flush();
						System.out.println("[Connected to Server]");
						lt = new CListenerThread(dis);
						(new Thread(lt)).start();
						connected = true;
						(new Thread(new Runnable() {
							public void run() {
								while (connected) {
									try {
										int lastKnownHashes = 0;
										int sendHashes = 0;
										if (System.currentTimeMillis() - lastKeepAlive > 1000) {
											dos.writeByte(0x00);
											dos.flush();
											if (cracking) {
												dos.writeByte(0x02);
												dos.flush();
												double chashRate = hashesThisSecond/1000;
												for(int i=0; i<rateArray.length; i++) {
													chashRate = (int) chashRate;
													if(chashRate != 0) {
														//System.out.println(i);
														if(chashRate != rateArray[0]) {
															if(rateArray[0] == 0) {
																rateArray[0] = (int) chashRate;
															} else {
																rateArray[1] = rateArray[0];
																rateArray[0] = (int) chashRate;
															}
														}
														//System.out.println("INT [0] " + rateArray[0]);
														//System.out.println("INT [1] " + rateArray[1]);
													}
												}
												//System.out.println("INT ARRAY CONTENTS: " + Arrays.toString(rateArray));
		
												if(chashRate == 0 && !firstHashSent && lastKnownHashes == 0) {
													//System.out.println("FIRST hash 2000! Now hash is: " + nowHashes);
													firstHashSent = true;
												}
												
												if(chashRate != 0 && firstHashSent) {
													lastKnownHashes = (int) chashRate;
													//System.out.println(chashRate);
												}
												
												if(chashRate == 0 && lastKnownHashes == 0 && firstHashSent) {
													double sum = 0;
													//System.out.println("INT ARRAY CONTENTS: " + Arrays.toString(rateArray));
													for(int i = 0; i < rateArray.length; i++) {
														sum += rateArray[i];
													}
													double placeholder = sum / rateArray.length;
													if(placeholder != 0) {
														lastKnownHashes = (int) placeholder;
													} else {
														lastKnownHashes = 2300;
													}
												}
												sendHashes = lastKnownHashes;
												//if(sendHashes == 0) {System.out.println("FUCK I SENT A 0" + " THE LAST IS: " + lastKnownHashes + " THE NOW IS: " + nowHashes);}
												System.out.println("[R]: " + sendHashes);
												dos.writeInt(sendHashes);
												dos.flush();
												dos.writeByte(0x03);
												dos.flush();
												writeBigInteger(lowestNumber, dos);
												dos.flush();
											}
											lastKeepAlive = System.currentTimeMillis();
										}
										if (foundSolution) {
											dos.write(0x04);
											dos.flush();
											writeString(solution, dos);
											dos.flush();
											foundSolution = false;
										}
										//if (sendAverage) {
											//sendAverage = false;
											dos.write(0x05);
											dos.flush();
											//System.out.println(average);
											dos.writeInt(average);
											dos.flush();
										//}
										if (requestNew) {
											requestNew = false;
											dos.write(0x06);
											dos.flush();
										}
										dos.write(0x07);
										dos.flush();
										dos.writeBoolean(cracking);
										dos.flush();
										Thread.sleep(20);
								} catch (Exception e) {  }
							}
						}
					})).start();
					while (connected) {
						Thread.sleep(200);
					}
				} catch (Exception e) { connected = false; }
				try { connected = false; Thread.sleep(10000); } catch (Exception e) {  }
			}
		}
	}
	public String getFileString(String filename) throws Exception {
		String s13 = "";
		InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
		DataInputStream datainputstream = new DataInputStream(in);
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(datainputstream));
		String line;
		while ((line = bufferedreader.readLine()) != null) {
			s13 += line;
		}
		bufferedreader.close();
		datainputstream.close();
		in.close();
		return s13;
	}
	public static BigInteger power (int number, int exponent) {
		BigInteger num = BigInteger.valueOf(1);
		for (int i = 0; i < exponent; i ++) {
			num = num.multiply(BigInteger.valueOf((long)(number)));
		}
		return num;
	}
	public static void out(String output) {
		System.out.println(output);
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
	public void writeBigInteger(BigInteger big, DataOutputStream par1DataOutputStream) throws IOException {
		byte[] data = big.toByteArray();
		int length = data.length;
		par1DataOutputStream.writeInt(length);
		for (int i = 0; i < length; i++) {
			par1DataOutputStream.writeByte(data[i]);
		}
		return;
	}
}