package com.minister.component.utils;

import cn.hutool.core.codec.BCD;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

import javax.crypto.SecretKey;

/**
 * MacUtil
 * <p>
 * 参考：https://www.cnblogs.com/alsodzy/p/9324794.html
 * </p>
 *
 * @author QIUCHANGQING620
 * @date 2021-03-19 10:25
 */
public class MacUtil {

    private MacUtil() {
    }

    public static void main(String[] args) {
        // DESede key size must be equal to 112 or 168, default is 168.
        SecretKey tKey = SecureUtil.generateKey(SymmetricAlgorithm.DESede.getValue(), 112);
        System.out.println("DESede 密匙 ：" + HexUtil.encodeHexStr(tKey.getEncoded(), false));
        System.out.println("DESede 密匙长度 ：" + HexUtil.encodeHexStr(tKey.getEncoded(), false).length());

//        String key = HexUtil.encodeHexStr(tKey.getEncoded());
        String key = "FB8DE32B321071E73802DD8D4C37DA9D";
        String content = "15dd563c3f5e6b5756bd29bdced711060835d66f58adc61257d7b14b9469ae6095a3f380a203c5ebc794b9754e50138989539d21d1a60cc26a9fd5bd83e7ba31123500261f01b6db67356B3F0FF975390FCCB9D178937C463826735F471C57DA83444A08F02A33346E5500000000000000000608";
        String r = "21E7DA25399002AB";
        String res = HexUtil.encodeHexStr(calculatePboc3desMAC(BCD.strToBcd(content), BCD.strToBcd(key), ZERO_IVC), false);
        System.out.println(res);
        System.out.println(r.equals(res));
    }

    public static final byte[] ZERO_IVC = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * PBOC-3DES-MAC算法
     * <p>
     * PBOC_3DES_MAC(符合ISO9797Alg3Mac标准) (16的整数补8000000000000000)。
     * 前n-1组使用单长密钥DES 使用密钥是密钥的左8字节） 最后1组使用双长密钥3DES （使用全部16字节密钥）。
     * <p>
     * 算法步骤：<br/>
     * 初始数据为D，初始向量为I，3DES秘钥为K0，秘钥低8字节DES秘钥K1.<br/>
     * 1、数据D分组并且填充：<br/>
     * (1)将字节数组D进行分组，每组8个字节，分组编号从0开始,分别为D0...Dn;<br/>
     * (2)最后一个分组不满8字节的，先填充一个字节80，然后根据字节长度在后面填充若干个00;
     * (3)满8字节的，新增一个8字节分组（80000000 00000000）.
     * 2、进行des循环加密：<br/>
     * (1)D0和初始向量I进行按位异或得到结果O0;<br/>
     * (2)使用秘钥K1，DES加密结果O0得到结果I1,将I1和D1按位异或得到结果D1;<br/>
     * (3)循环第二步骤得到结果Dn.<br/>
     * 3、将Dn使用16字节秘钥K0进行3DES加密，得到的结果就是我们要的MAC。
     *
     * @param data 数据
     * @param key  密钥
     * @param icv  偏移量
     * @return MAC值
     */
    public static byte[] calculatePboc3desMAC(byte[] data, byte[] key, byte[] icv) {
        if (key == null || data == null) {
            throw new RuntimeException("data or key is null.");
        }
        if (key.length != 16) {
            throw new RuntimeException("key length is not 16 byte.");
        }
        // 拆分数据（8字节块/Block）
        final int dataLength = data.length;
        // 最终块数量
        final int blockCount = dataLength / 8 + 1;
        // 最后一个块的长度
        final int lastBlockLength = dataLength % 8;

        byte[][] dataBlock = new byte[blockCount][8];
        for (int i = 0; i < blockCount; i++) {
            int copyLength = i == blockCount - 1 ? lastBlockLength : 8;
            System.arraycopy(data, i * 8, dataBlock[i], 0, copyLength);
        }
        // 最后一个块的 lastBlockLength 位置替换为 0x80
        dataBlock[blockCount - 1][lastBlockLength] = (byte) 0x80;

        byte[] desXor = ArrayUtil.xor(dataBlock[0], icv);
        SymmetricCrypto des = DesUtil.createDesCbcNoPadding(key);
        for (int i = 1; i < blockCount; i++) {
            // 先des加密，然后进行异或运算
            desXor = ArrayUtil.xor(dataBlock[i], des.encrypt(desXor));
        }

        // 3DES加密
        SymmetricCrypto desede = DesUtil.create3DesCbcNoPadding(key);
        return desede.encrypt(desXor);
    }

    /**
     * PBOC-DES-MAC算法
     *
     * @param data 数据
     * @param key  密匙
     * @param icv  偏移量
     * @return MAC值
     */
    public static byte[] calculatePbocdesMAC(byte[] data, byte[] key, byte[] icv) {
        if (key == null || data == null) {
            throw new RuntimeException("data or key is null.");
        }
        if (key.length != 8) {
            throw new RuntimeException("key length is not 16 byte.");
        }

        // 拆分数据（8字节块/Block）
        final int dataLength = data.length;
        final int blockCount = dataLength / 8 + 1;
        final int lastBlockLength = dataLength % 8;

        byte[][] dataBlock = new byte[blockCount][8];
        for (int i = 0; i < blockCount; i++) {
            int copyLength = i == blockCount - 1 ? lastBlockLength : 8;
            System.arraycopy(data, i * 8, dataBlock[i], 0, copyLength);
        }
        dataBlock[blockCount - 1][lastBlockLength] = (byte) 0x80;

        byte[] desXor = ArrayUtil.xor(dataBlock[0], icv);
        for (int i = 1; i < blockCount; i++) {
            byte[] des = DesUtil.createDesCbcNoPadding(key).encrypt(desXor);
            desXor = ArrayUtil.xor(dataBlock[i], des);
        }
        desXor = DesUtil.createDesCbcNoPadding(key).encrypt(desXor);
        return desXor;
    }

    /**
     * ANSI X9.9MAC算法  <br/>
     * (1) ANSI X9.9MAC算法只使用单倍长密钥。  <br/>
     * (2)  MAC数据先按8字节分组，表示为D0～Dn，如果Dn不足8字节时，尾部以字节00补齐。 <br/>
     * (3) 用MAC密钥加密D0，加密结果与D1异或作为下一次的输入。 <br/>
     * (4) 将上一步的加密结果与下一分组异或，然后再用MAC密钥加密。<br/>
     * (5) 直至所有分组结束，取最后结果的左半部作为MAC。<br/>
     * 采用x9.9算法计算MAC (Count MAC by ANSI-x9.9).
     *
     * @param key  8字节密钥数据
     * @param data 待计算的缓冲区
     */
    public static byte[] calculateANSIX9_9MAC(byte[] key, byte[] data) {
        final int dataLength = data.length;
        final int lastLength = dataLength % 8;
        final int lastBlockLength = lastLength == 0 ? 8 : lastLength;
        final int blockCount = dataLength / 8 + (lastLength > 0 ? 1 : 0);

        // 拆分数据（8字节块/Block）
        byte[][] dataBlock = new byte[blockCount][8];
        for (int i = 0; i < blockCount; i++) {
            int copyLength = i == blockCount - 1 ? lastBlockLength : 8;
            System.arraycopy(data, i * 8, dataBlock[i], 0, copyLength);
        }

        byte[] desXor = new byte[8];
        for (int i = 0; i < blockCount; i++) {
            byte[] tXor = ArrayUtil.xor(desXor, dataBlock[i]);
            desXor = DesUtil.createDesEcbNoPadding(key).encrypt(tXor);
        }
        return desXor;
    }

    /**
     * 采用ANSI x9.19算法计算MAC (Count MAC by ANSI-x9.19).<br/>
     * 将ANSI X9.9的结果做如下计算<br/>
     * (6) 用MAC密钥右半部解密(5)的结果。 <br/>
     * (7) 用MAC密钥左半部加密(6)的结果。<br/>
     * (8) 取(7)的结果的左半部作为MAC。<br/>
     *
     * @param key  16字节密钥数据
     * @param data 待计算的缓冲区
     */
    public static byte[] calculateANSIX9_19MAC(byte[] key, byte[] data) {
        if (key == null || data == null) {
            return null;
        }
        if (key.length != 16) {
            throw new RuntimeException("秘钥长度错误.");
        }

        byte[] keyLeft = new byte[8];
        byte[] keyRight = new byte[8];
        System.arraycopy(key, 0, keyLeft, 0, 8);
        System.arraycopy(key, 8, keyRight, 0, 8);

        byte[] result99 = calculateANSIX9_9MAC(keyLeft, data);

        byte[] resultTemp = DesUtil.createDesEcbNoPadding(keyRight).decrypt(result99);
        return DesUtil.createDesEcbNoPadding(keyLeft).encrypt(resultTemp);
    }

}