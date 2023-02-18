package me.superskidder.utils.irc;

import me.konago.nativeobfuscator.Macros;
import me.konago.nativeobfuscator.Native;
import me.konago.nativeobfuscator.Winlicense;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
@Native
public class SecureUtil {
    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static void setKey(String myKey)
    {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
    }

    public static String encrypt(String strToEncrypt, String secret)
    {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret)
    {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
        return null;
    }
}
