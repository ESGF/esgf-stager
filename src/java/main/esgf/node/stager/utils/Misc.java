package esgf.node.stager.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * All help methods that don't fit anywhere else....
 * 
 * @author Estanislao Gonzalez
 */
public class Misc {
	
	
	/**
	 * This is just to avoid having passwords and important information in plain text.
	 * It is no real security, but requires to have access to this code.
	 * The resulting transformation is encoded in Base64 so it is safe to be directly saved
	 * in any text format or encoded in an url.
	 * 
	 * @param reversed
	 *            if the reverse action is required (i.e. transform(true,
	 *            transform(false, "x")).equals("x"))
	 * @param str
	 *            String to transform
	 * @return The transformed string
	 */
	public static String transform(boolean reversed, String str) {
		try {
			Cipher c = Cipher.getInstance("DES");
			Key k = SecretKeyFactory.getInstance("DES").generateSecret(
					new DESKeySpec(new byte[] { 115, 32, -112, -87, 111, -26,
							-121, -91, -84, 67, -31, 14, 7, 39, -35, 82 }));
			if (reversed) {
				if (!str.startsWith("des:")) {
					throw new InvalidKeyException("key not recognized as being encoded.");
				} else {
					str = str.substring(4);
				}
				
				c.init(Cipher.DECRYPT_MODE, k);				
				BASE64Decoder dec = new BASE64Decoder();
				byte[] result = c.doFinal(dec.decodeBuffer(str));
				return new String(result);
			} else {
				c.init(Cipher.ENCRYPT_MODE, k);
				byte[] result = c.doFinal(str.getBytes());
				BASE64Encoder enc = new BASE64Encoder();
				return "des:" + enc.encode(result);
			}
			
		} catch (Exception e) {
			//In any case we cannot proceed.
			throw new IllegalStateException("Could not initialize password transformer", e);
		}
	}

	/**
	 * Encrypts a word if provided or ask the user to give one. No decription
	 * here.
	 * 
	 * @param args words to encode (or nothing for interactive)
	 * @throws IOException If cannot read from stdin
	 */
	public static void main(String[] args) throws IOException {
		if (args.length >= 1) {
			for (int i = 0; i < args.length; i++) {
				System.out.println(args[i] + ": " + transform(false, args[i]));			
			}
		} else {
			System.out.print("Word to encript: ");
			String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.out.println(transform(false, input));
		}
	}
}
