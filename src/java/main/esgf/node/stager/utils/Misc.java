package esgf.node.stager.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * All help methods that don't fit anywhere else....
 *
 * @author Estanislao Gonzalez
 */
public class Misc {
    
    private static Cipher cipher;
    private static SecretKey key;
    private static boolean init;

    private static void init() throws InvalidKeyException,
            InvalidKeySpecException, NoSuchAlgorithmException,
            NoSuchPaddingException {
        init = true;
        cipher = Cipher.getInstance("DES");
        key = SecretKeyFactory.getInstance("DES").generateSecret(
                new DESKeySpec(new byte[] { 115, 32, -112, -87, 111, -26, -121,
                        -91, -84, 67, -31, 14, 7, 39, -35, 82 }));
    }

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
    public synchronized static String transform(boolean reversed, String str) {
        try {
            //init the cipher only once.
            if (!init) init();
            
            if (reversed) {
                if (!str.startsWith("des:")) {
                    throw new InvalidKeyException("key not recognized as being encoded.");
                } else {
                    str = str.substring(4);
                }

                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] result = cipher.doFinal(Base64.decodeBase64(str));
                return new String(result);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] result = cipher.doFinal(str.getBytes());
                return "des:" + new String(Base64.encodeBase64(result));
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
