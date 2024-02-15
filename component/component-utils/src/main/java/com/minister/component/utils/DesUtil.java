package com.minister.component.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * DesUtil
 * <p>
 * 参考：https://www.cnblogs.com/alsodzy/p/9324794.html
 * </p>
 *
 * @author QIUCHANGQING620
 * @date 2021-03-19 10:25
 */
public class DesUtil {

    private DesUtil() {
    }

    private static final byte[] ZERO_IVC = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

    private static final String DESEDE = "DESede";

    private static final String DES = "DES";

    private static final String DESEDE_CBC_NOPADDING = "DESede/CBC/NoPadding";

    private static final String DESEDE_ECB_NOPADDING = "DESede/ECB/NoPadding";

    private static final String DES_CBC_NOPADDING = "DES/CBC/NoPadding";

    private static final String DES_ECB_NOPADDING = "DES/ECB/NoPadding";

    /**
     * 创建加密模式 DESede/CBC/NoPadding
     *
     * @param key 秘钥
     * @return 加密模式
     */
    public static SymmetricCrypto create3DesCbcNoPadding(byte[] key) {
        return create3DesCbcNoPadding(key, ZERO_IVC);
    }

    /**
     * 创建加密模式 DESede/CBC/NoPadding
     *
     * @param key 秘钥
     * @param ivb 向量
     * @return 加密模式
     */
    public static SymmetricCrypto create3DesCbcNoPadding(byte[] key, byte[] ivb) {
        byte[] desedeKey = new byte[24];
        System.arraycopy(key, 0, desedeKey, 0, 16);
        System.arraycopy(key, 0, desedeKey, 16, 8);
        SecretKey secretKey = new SecretKeySpec(desedeKey, DESEDE);
        IvParameterSpec iv = new IvParameterSpec(ivb);
        return new SymmetricCrypto(DESEDE_CBC_NOPADDING, secretKey, iv);
    }

    /**
     * 创建加密模式 DESede/ECB/NoPadding
     *
     * @param key 加密秘钥
     * @return 加密模式
     */
    public static SymmetricCrypto create3DesEcbNoPadding(byte[] key) {
        byte[] desedeKey = new byte[24];
        System.arraycopy(key, 0, desedeKey, 0, 16);
        System.arraycopy(key, 0, desedeKey, 16, 8);
        SecretKey secretKey = new SecretKeySpec(desedeKey, DESEDE);
        return new SymmetricCrypto(DESEDE_ECB_NOPADDING, secretKey);
    }

    /**
     * DESede/ECB/NoPadding 模式
     *
     * @param data 待加密数据
     * @param key  加密秘钥
     * @return 加密结果
     */
    public static String encryptBy3DesEcb(String data, String key) {
        byte[] content = HexUtil.decodeHex(data);
        byte[] deskey = HexUtil.decodeHex(key);
        byte[] doFinal = create3DesEcbNoPadding(deskey).encrypt(content);
        return HexUtil.encodeHexStr(doFinal);
    }

    /**
     * 创建加密模式 DESede/ECB/NoPadding(3倍密钥长)
     *
     * @param key 加密秘钥
     * @return 加密模式
     */
    public static SymmetricCrypto create3DesEcbNoPaddingThreeThreeTimes(byte[] key) {
        if (key.length != 24) {
            throw new RuntimeException("key length has to be 12.");
        }

        SecretKey secretKey = new SecretKeySpec(key, DESEDE);
        return new SymmetricCrypto(DESEDE_ECB_NOPADDING, secretKey);
    }

    /**
     * 创建加密模式 DES/CBC/NoPadding
     *
     * @param key 秘钥
     * @return 加密模式
     */
    public static SymmetricCrypto createDesCbcNoPadding(byte[] key) {
        return createDesCbcNoPadding(key, ZERO_IVC);
    }

    /**
     * 创建加密模式 DES/CBC/NoPadding
     *
     * @param key 秘钥
     * @param ivb 向量
     * @return 加密模式
     */
    public static SymmetricCrypto createDesCbcNoPadding(byte[] key, byte[] ivb) {
        try {
            DESKeySpec dks = new DESKeySpec(key);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey secretKey = keyFactory.generateSecret(dks);
            IvParameterSpec iv = new IvParameterSpec(ZERO_IVC);
            return new SymmetricCrypto(DES_CBC_NOPADDING, secretKey, iv);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建加密模式 DES/ECB/NoPadding
     *
     * @param key 秘钥
     * @return 加密模式
     */
    public static SymmetricCrypto createDesEcbNoPadding(byte[] key) {
        try {
            DESKeySpec dks = new DESKeySpec(key);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey secretKey = keyFactory.generateSecret(dks);
            return new SymmetricCrypto(DES_ECB_NOPADDING, secretKey);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 本项目用于生成外部设备认证码和校验卡认证码（javacard GP规范 SCP02安全通道）（cardCryptogram）
     * B.1.2.1  Full Triple DES MAC
     * The full triple DES MAC is as defined in [ISO 9797-1] as MAC Algorithm 1 with output transformation 3,
     * without truncation, and with triple DES taking the place of the block cipher.
     *
     * @param content 待加密数据
     * @param key     加密密钥
     * @return 加密结果后8字节
     */
    public static byte[] encryptBy3DesCbcLast8Mac(byte[] content, byte[] key) {
        byte[] edata = create3DesCbcNoPadding(key).encrypt(content);

        byte[] result = new byte[8];
        System.arraycopy(edata, edata.length - 8, result, 0, 8);
        return result;
    }


    /**
     * 分散算法
     *
     * @param data 数据(卡号)
     * @param key  根密钥
     * @return
     */
    public static String getHashProtectKey(String data, String key) {
        String tempKey, protectKey;
        tempKey = HexUtil.encodeHexStr(ArrayUtil.xor(HexUtil.decodeHex(data), HexUtil.decodeHex("FFFFFFFFFFFFFFFF")));
        protectKey = encryptBy3DesEcb(data, key);
        protectKey = protectKey + encryptBy3DesEcb(tempKey, key);
        return protectKey;
    }

    /**
     * 分散密钥
     *
     * @param data 数据(卡号)
     * @param key  根密钥
     * @return
     */
    public static String getHashProtectKey(byte[] data, byte[] key) {
        byte[] tempKeyBytes = ArrayUtil.xor(data, HexUtil.decodeHex("FFFFFFFFFFFFFFFF"));
        byte[] key1 = create3DesEcbNoPadding(key).encrypt(data);
        byte[] key2 = create3DesEcbNoPadding(key).encrypt(tempKeyBytes);
        byte[] disperKeyBytes = ArrayUtils.addAll(key1, key2);
        return HexUtil.encodeHexStr(disperKeyBytes);
    }

}