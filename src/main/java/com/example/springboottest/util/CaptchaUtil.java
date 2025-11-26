 package com.example.springboottest.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码工具类
 * 用于生成图形验证码和验证验证码
 * 
 * @author springboottest
 */
public class CaptchaUtil {

    private static final String CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_HEIGHT = 40;
    private static final int DEFAULT_LENGTH = 4;
    private static final Random RANDOM = new Random();

    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private String code;
        private BufferedImage image;

        public CaptchaResult(String code, BufferedImage image) {
            this.code = code;
            this.image = image;
        }

        public String getCode() {
            return code;
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    /**
     * 生成默认验证码（4位，120x40像素）
     *
     * @return 验证码结果对象
     */
    public static CaptchaResult generateCaptcha() {
        return generateCaptcha(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的验证码
     *
     * @param length 验证码长度
     * @return 验证码结果对象
     */
    public static CaptchaResult generateCaptcha(int length) {
        return generateCaptcha(DEFAULT_WIDTH, DEFAULT_HEIGHT, length);
    }

    /**
     * 生成指定尺寸和长度的验证码
     *
     * @param width  图片宽度
     * @param height 图片高度
     * @param length 验证码长度
     * @return 验证码结果对象
     */
    public static CaptchaResult generateCaptcha(int width, int height, int length) {
        // 生成随机验证码字符串
        String code = generateRandomCode(length);

        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 填充背景色
        g2d.setColor(getRandomLightColor());
        g2d.fillRect(0, 0, width, height);

        // 绘制干扰线
        drawInterferenceLines(g2d, width, height);

        // 绘制验证码字符
        drawCaptchaText(g2d, code, width, height);

        // 添加噪点
        drawNoiseDots(g2d, width, height);

        g2d.dispose();

        return new CaptchaResult(code, image);
    }

    /**
     * 生成随机验证码字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    private static String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * 绘制验证码文字
     *
     * @param g2d    图形上下文
     * @param code   验证码字符串
     * @param width  图片宽度
     * @param height 图片高度
     */
    private static void drawCaptchaText(Graphics2D g2d, String code, int width, int height) {
        int fontSize = height * 3 / 4;
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(code)) / (code.length() + 1);
        int y = (height + fm.getAscent()) / 2;

        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g2d.setColor(getRandomDarkColor());

            // 随机角度旋转
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4;
            g2d.rotate(angle, x + i * (width / code.length()) + fontSize / 2, y);

            // 绘制字符
            g2d.drawString(String.valueOf(code.charAt(i)),
                    x + i * (width / code.length()), y);

            // 恢复角度
            g2d.rotate(-angle, x + i * (width / code.length()) + fontSize / 2, y);
        }
    }

    /**
     * 绘制干扰线
     *
     * @param g2d    图形上下文
     * @param width  图片宽度
     * @param height 图片高度
     */
    private static void drawInterferenceLines(Graphics2D g2d, int width, int height) {
        int lineCount = 5 + RANDOM.nextInt(6); // 5-10条干扰线

        for (int i = 0; i < lineCount; i++) {
            g2d.setColor(getRandomMediumColor());
            g2d.setStroke(new BasicStroke(1.0f + RANDOM.nextFloat()));

            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);

            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制噪点
     *
     * @param g2d    图形上下文
     * @param width  图片宽度
     * @param height 图片高度
     */
    private static void drawNoiseDots(Graphics2D g2d, int width, int height) {
        int dotCount = width * height / 30; // 根据图片大小计算噪点数量

        for (int i = 0; i < dotCount; i++) {
            g2d.setColor(getRandomMediumColor());
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            g2d.fillRect(x, y, 1, 1);
        }
    }

    /**
     * 获取随机浅色（背景色）
     *
     * @return 随机浅色
     */
    private static Color getRandomLightColor() {
        int r = 200 + RANDOM.nextInt(56);
        int g = 200 + RANDOM.nextInt(56);
        int b = 200 + RANDOM.nextInt(56);
        return new Color(r, g, b);
    }

    /**
     * 获取随机深色（文字色）
     *
     * @return 随机深色
     */
    private static Color getRandomDarkColor() {
        int r = RANDOM.nextInt(100);
        int g = RANDOM.nextInt(100);
        int b = RANDOM.nextInt(100);
        return new Color(r, g, b);
    }

    /**
     * 获取随机中等色（干扰线和噪点）
     *
     * @return 随机中等色
     */
    private static Color getRandomMediumColor() {
        int r = 100 + RANDOM.nextInt(100);
        int g = 100 + RANDOM.nextInt(100);
        int b = 100 + RANDOM.nextInt(100);
        return new Color(r, g, b);
    }

    /**
     * 验证验证码（忽略大小写）
     *
     * @param userInput   用户输入
     * @param correctCode 正确的验证码
     * @return 是否匹配
     */
    public static boolean validateCaptcha(String userInput, String correctCode) {
        if (userInput == null || correctCode == null) {
            return false;
        }
        return userInput.trim().equalsIgnoreCase(correctCode.trim());
    }

    /**
     * 生成数字验证码
     *
     * @param length 验证码长度
     * @return 数字验证码字符串
     */
    public static String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成简单的字符验证码（仅字母）
     *
     * @param length 验证码长度
     * @return 字母验证码字符串
     */
    public static String generateAlphaCode(int length) {
        String letters = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(letters.charAt(RANDOM.nextInt(letters.length())));
        }
        return code.toString();
    }

    public static void main(String[] args) {
//        CaptchaResult captcha = generateCaptcha(8);
//        System.out.println("验证码：" + captcha.getCode());
    }
}