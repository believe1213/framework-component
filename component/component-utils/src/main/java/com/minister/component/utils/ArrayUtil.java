package com.minister.component.utils;

/**
 * ArrayUtil
 *
 * @author QIUCHANGQING620
 * @date 2021-03-19 10:25
 */
public class ArrayUtil {

    private ArrayUtil() {
    }

    /**
     * 将b1和b2做异或，然后返回
     *
     * @param b1 字节数组
     * @param b2 字节数组
     * @return 异或结果
     */
    public static byte[] xor(byte[] b1, byte[] b2) {
        byte[] tXor = new byte[Math.min(b1.length, b2.length)];
        for (int i = 0; i < tXor.length; i++) {
            tXor[i] = (byte) (b1[i] ^ b2[i]);
        }
        return tXor;
    }

}
