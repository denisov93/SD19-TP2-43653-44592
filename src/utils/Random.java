package utils;

import java.math.BigInteger;

public class Random {
	static byte[] bytes = new byte[16];
	static java.util.Random rnd = new java.util.Random();
	
	synchronized public static String key128() {
		rnd.nextBytes( bytes );
		return new BigInteger( bytes ).abs().toString(32);
	}

	public static boolean nextBoolean() {
		return rnd.nextBoolean();
	}

	public static int nextInt(int bound) {
		return rnd.nextInt(bound);
	}
}
