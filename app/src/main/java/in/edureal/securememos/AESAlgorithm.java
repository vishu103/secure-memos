
package in.edureal.securememos;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.*;

public class AESAlgorithm
{
    private static final String SALTED_STR = "Salted__";
    private static final byte[] SALTED_MAGIC = SALTED_STR.getBytes(US_ASCII);


    public String encrypt(String text, String password) throws Exception
    {
        final byte[] pass = password.getBytes(US_ASCII);
        final byte[] salt = (new SecureRandom()).generateSeed(8);
        final byte[] inBytes = text.getBytes(UTF_8);

        final byte[] passAndSalt = array_concat(pass, salt);
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];
        for (int i = 0; i < 3 && keyAndIv.length < 48; i++) {
            final byte[] hashData = array_concat(hash, passAndSalt);
            final MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = array_concat(keyAndIv, hash);
        }

        final byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        final byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);
        final SecretKeySpec key = new SecretKeySpec(keyValue, "AES");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] data = cipher.doFinal(inBytes);
        data = array_concat(array_concat(SALTED_MAGIC, salt), data);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }


    public String decrypt(String encrypted, String password) throws Exception
    {
        final byte[] pass = password.getBytes(US_ASCII);

        final byte[] inBytes = Base64.decode(encrypted, Base64.DEFAULT);

        final byte[] shouldBeMagic = Arrays.copyOfRange(inBytes, 0, SALTED_MAGIC.length);
        if (!Arrays.equals(shouldBeMagic, SALTED_MAGIC)) {
            throw new IllegalArgumentException("Initial bytes from input do not match OpenSSL SALTED_MAGIC salt value.");
        }

        final byte[] salt = Arrays.copyOfRange(inBytes, SALTED_MAGIC.length, SALTED_MAGIC.length + 8);

        final byte[] passAndSalt = array_concat(pass, salt);

        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];
        for (int i = 0; i < 3 && keyAndIv.length < 48; i++) {
            final byte[] hashData = array_concat(hash, passAndSalt);
            final MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = array_concat(keyAndIv, hash);
        }

        final byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        final SecretKeySpec key = new SecretKeySpec(keyValue, "AES");

        final byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        final byte[] clear = cipher.doFinal(inBytes, 16, inBytes.length - 16);
        return new String(clear, UTF_8);
    }

    private byte[] array_concat(final byte[] a, final byte[] b)
    {
        final byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

}