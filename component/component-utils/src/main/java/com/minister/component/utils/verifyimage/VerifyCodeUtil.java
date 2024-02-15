package com.minister.component.utils.verifyimage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * 文字内容图片验证码Util
 *
 * @author QIUCHANGQING620
 * @date 2020-07-21 17:49
 */
public class VerifyCodeUtil {

    private VerifyCodeUtil() {
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成验证码
     *
     * @param codeCount 验证码长度
     * @return 验证码
     */
    public static String generateCode(Integer codeCount) {
        StringBuilder build;
        build = new StringBuilder();
        for (int i = 0; i < codeCount; i++) {
            final int temp = SECURE_RANDOM.nextInt(26) + 97;
            String s = String.valueOf((char) temp);
            build.append(s);
        }
        return build.toString();
    }

    /**
     * 生成图片验证码
     *
     * @param code 验证码
     * @return 图片验证码
     * @throws IOException IOException
     */
    public static byte[] createImg(String code) throws IOException {
        return createImg(800, 300, 50, code);
    }

    /**
     * 生成图片验证码
     *
     * @param width        图片长度
     * @param height       图片宽度
     * @param disturbLines 干扰线条
     * @param code         验证码
     * @return 图片验证码
     * @throws IOException IOException
     */
    public static byte[] createImg(Integer width, Integer height, Integer disturbLines, String code) throws IOException {
        char[] codeCharArray = code.toCharArray();

        // 生成画板
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = image.getGraphics();

        graphic.setColor(Color.getColor("F8F8F8"));
        graphic.fillRect(0, 0, width, height);

        Color[] colors = new Color[]{Color.BLUE, Color.GRAY, Color.GREEN, Color.RED, Color.BLACK, Color.ORANGE,
                Color.CYAN};

        // 在 "画板"上生成干扰线条
        for (int i = 0; i < disturbLines; i++) {
            graphic.setColor(colors[SECURE_RANDOM.nextInt(colors.length)]);
            final int x = SECURE_RANDOM.nextInt(width);
            final int y = SECURE_RANDOM.nextInt(height);
            final int w = SECURE_RANDOM.nextInt(20);
            final int h = SECURE_RANDOM.nextInt(20);
            final int signA = SECURE_RANDOM.nextBoolean() ? 1 : -1;
            final int signB = SECURE_RANDOM.nextBoolean() ? 1 : -1;
            graphic.drawLine(x, y, x + w * signA, y + h * signB);
        }

        // 在 "画板"上绘制字母
        graphic.setFont(new Font("Comic Sans MS", Font.BOLD, height / 2));
        for (int i = 0; i < codeCharArray.length; i++) {
            graphic.setColor(colors[SECURE_RANDOM.nextInt(colors.length)]);
            graphic.drawString(String.valueOf(codeCharArray[i]), i * (width / 6), height - (height / 3));
        }

        // 关闭画板
        graphic.dispose();

        // 输出文字验证码图片流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return os.toByteArray();
    }

}
