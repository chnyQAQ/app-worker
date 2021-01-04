package com.dah.cem.app.sc.worker.workers.netty;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class MessageBuilder {

    public static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String KEY_ALGORITHM = "AES";
    public static final String MESSAGE_SPLITE = "@@";

    public static String getSendMsg(String enterpriseId, String report, String key, String iv)
            throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        String decryptData = encrypt(report, key, iv);
        StringBuilder reqSb = new StringBuilder();

        log.info("发送数据(原始)：" + report);
        reqSb.append("{\"enterpriseId\":\"")
                .append(enterpriseId)
                .append("\",\"report\":\"")
                .append(decryptData.replaceAll("[\\s*\t\n\r]", ""))
                .append("\"}")
                .append(MESSAGE_SPLITE);
        log.info("发送数据(加密)：" + reqSb.toString());
        return reqSb.toString();
    }

    public static String encrypt(String data, String key, String iv) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = initCipher(1, key, iv);
        byte[] bytes = cipher.doFinal(data.getBytes(DEFAULT_ENCODING));
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bytes);
    }

    /**
     * 初始化加密模式的密码器
     *
     * @param mode
     * @param key
     * @return
     */
    public static Cipher initCipher(int mode, String key, String iv) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(DEFAULT_ENCODING), KEY_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(mode, keySpec, ivParameterSpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
            log.error("初始化加密模式的密码器出错 - " + e);
            e.printStackTrace();
        }
        return cipher;
    }

}
