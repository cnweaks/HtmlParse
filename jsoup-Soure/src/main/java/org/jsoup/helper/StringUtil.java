package org.jsoup.helper;

import java.util.Collection;
import java.util.Iterator;

/**
 * 一个最小的字符串实用程序类。仅用于内部 jsoup 设计。
 */
public final class StringUtil {
    // memoised padding up to 10
    private static final String[] padding = {"", " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ", "         ", "          "};

    /**
     * 加入一个分隔符的字符串的集合
     * @参数字符串字符串对象的集合
     * @param sep 字符串，字符串之间的地方
     * @返回连接的字符串
     */
    public static String join(Collection strings, String sep) {
        return join(strings.iterator(), sep);
    }

    /**
     * 加入一个分隔符的字符串的集合
     * @param strings iterator of string objects
     * @param sep string to place between strings
     * @return joined string
     */
    public static String join(Iterator strings, String sep) {
        if (!strings.hasNext())
            return "";

        String start = strings.next().toString();
        if (!strings.hasNext()) // 只有一个，避免生成器
            return start;

        StringBuilder sb = new StringBuilder(64).append(start);
        while (strings.hasNext()) {
            sb.append(sep);
            sb.append(strings.next());
        }
        return sb.toString();
    }

    /**
     * 返回空间填充
     * @param width amount of padding desired
     * @return string of spaces * width
     */
    public static String padding(int width) {
        if (width < 0)
            throw new IllegalArgumentException("width must be > 0");

        if (width < padding.length)
            return padding[width];

        char[] out = new char[width];
        for (int i = 0; i < width; i++)
            out[i] = ' ';
        return String.valueOf(out);
    }

    /**
     * 测试字符串是否为空: null、 为空，或者只包含空格 (""，\r\n，\t，等等)
     * @param string string to test
     * @return if string is blank
     */
    public static boolean isBlank(String string) {
        if (string == null || string.length() == 0)
            return true;

        int l = string.length();
        for (int i = 0; i < l; i++) {
            if (!StringUtil.isWhitespace(string.codePointAt(i)))
                return false;
        }
        return true;
    }

    /**
     * 测试如果一个字符串是数字，即仅包含数字字符
     * @param string string to test
     * @返回 true，如果只有数字字符数，假如果空或 null，或包含非数字 chrs
     */
    public static boolean isNumeric(String string) {
        if (string == null || string.length() == 0)
            return false;

        int l = string.length();
        for (int i = 0; i < l; i++) {
            if (!Character.isDigit(string.codePointAt(i)))
                return false;
        }
        return true;
    }

    /**
     * 如果代码点的测试是"空白"所定义的 HTML 规范。
     * @param c code point to test
     * @如果代码点是虚假的空白，否则，则返回 true
     */
    public static boolean isWhitespace(int c){
        return c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r';
    }

    public static String normaliseWhitespace(String string) {
        StringBuilder sb = new StringBuilder(string.length());

        boolean lastWasWhite = false;
        boolean modified = false;

        int l = string.length();
        int c;
        for (int i = 0; i < l; i+= Character.charCount(c)) {
            c = string.codePointAt(i);
            if (isWhitespace(c)) {
                if (lastWasWhite) {
                    modified = true;
                    continue;
                }
                if (c != ' ')
                    modified = true;
                sb.append(' ');
                lastWasWhite = true;
            }
            else {
                sb.appendCodePoint(c);
                lastWasWhite = false;
            }
        }
        return modified ? sb.toString() : string;
    }

    public static boolean in(String needle, String... haystack) {
        for (String hay : haystack) {
            if (hay.equals(needle))
                return true;
        }
        return false;
    }
}
