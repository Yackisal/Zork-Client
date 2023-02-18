package cc.zork.utils.auth;

import cc.zork.ui.mainmanu.MainScreen;
import me.konago.nativeobfuscator.Macros;
import me.konago.nativeobfuscator.Native;
import me.konago.nativeobfuscator.Winlicense;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Native
public class Auth {

    public Auth() {
    }

    protected static @NotNull String getOriginal() {
        try {
            Macros.define(Winlicense.VM_TIGER_BLACK_START);
            String toEncrypt = "EmoManIsGay" + System.getProperty("COMPUTERNAME") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();

            byte byteData[] = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            Macros.define(Winlicense.VM_TIGER_BLACK_END);

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }

    }

    public static String getHWID() {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        String hwid = null;
        try {
            hwid = g(getOriginal());
        } catch (Exception ignored) {
        }
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
        return hwid;
    }



    protected static @NotNull String g(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        text = Base64.getUrlEncoder().encodeToString(text.getBytes());
        //System.out.println(text);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash;
        md.update(text.getBytes(StandardCharsets.UTF_8), 0, text.length());
        text = DigestUtils.shaHex(text);
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
        return text.toUpperCase();
    }

    public static void doCrash() {
        try {
            Macros.define(Winlicense.VM_TIGER_BLACK_START);
            MainScreen.verify_crash = true;
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = null;
            try {
                unsafe = (Unsafe) field.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Class<?> cacheClass = null;
            try {
                cacheClass = Class.forName("java.lang.Integer$IntegerCache");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Field cache = cacheClass.getDeclaredField("cache");
            long offset = unsafe.staticFieldOffset(cache);

            unsafe.putObject(Integer.getInteger("SkidSense.pub NeverDie"), offset, null);
            Macros.define(Winlicense.VM_TIGER_BLACK_END);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
