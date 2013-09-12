import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;


public class CrackThread implements Runnable {
	public int threadID;
	public char[] array;
	public int hashes = 0;
	public int hashesPerSecond = 0;
	public String sequence;
	public boolean finished = false;
	public BigInteger maxCrack;
	public CrackThread(int threadID, BigInteger max) {
		this.threadID = threadID;
		maxCrack = max;
	}
	public int min = 1;
	public int max;
	public int[] chars;
	public void run() {
		try {
			array = comblist;
			init();
			(new Thread(new Runnable() {
				public void run() {
					while (true) {
						hashes = 0;
						try { Thread.sleep(1000); } catch (Exception e) {  }
						if (Main.cracking && !finished) {
							hashesPerSecond = hashes;
						} else {
							break;
						}
					}
				}
			})).start();
			byte[] sequence;
			byte[] digest;
			MessageDigest md = MessageDigest.getInstance("MD5");
			while (Main.cracking && CrackManager.bi[threadID].compareTo(maxCrack) == -1) {
				this.sequence = getCur();
				sequence = this.sequence.getBytes();
				digest = md.digest(sequence);
				md.reset();
				if (isMatching(digest)) {
					Main.solution = new String(sequence);
					Main.foundSolution = true;
					Main.out("Found solution: " + Main.solution);
					Main.cracking = false;
				}
				increment();
			}
			finished = true;
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void increment() {
		hashes += 1;
		CrackManager.bi[threadID] = CrackManager.bi[threadID].add(BigInteger.ONE);
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] < max) {
				chars[i] ++;
				return;
			}
			chars[i] = min;
		}
	}
	
	public void init() {
		BigInteger start = CrackManager.bi[threadID];
		max = array.length - 1;
		chars = new int[14];
		for (int i = 0; i < chars.length; i ++) {
			chars[i] = 0;
		}
		int tempNum;
		int increment = 0;
		while(start.compareTo(BigInteger.ZERO) == 1) {
			try {
				tempNum = start.mod(BigInteger.valueOf(array.length)).intValue();
				if (tempNum == 0) {
					tempNum += 1;
				}
				start = start.divide(BigInteger.valueOf(array.length));
				chars[increment] = tempNum;
				increment ++;
			} catch (Exception e) {
				break;
			}
		}
	}
	
	public boolean isMatching(byte[] input) {
		return Arrays.equals(input, Main.comparisonHash);
	}
	
	public String getCur() {
		StringBuilder append = new StringBuilder();
		for (int i = chars.length - 1; i >= 0; i --) {
			if (chars[i] != 0) {
				append.append(array[chars[i]]);
			}
		}
		return append.toString();
	}
	public static char[] comblist = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9','0','!','£','$','%',',','.','?','@','#'};
}