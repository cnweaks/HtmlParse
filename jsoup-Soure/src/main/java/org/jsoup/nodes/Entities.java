package org.jsoup.nodes;

import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 实体和转义例程。
 * 资料来源: <a href="http://www.w3.org/TR/html5/named-character-references.html#named-character-references">W3C HTML
 * 命名字符引用</a>。
 */
public class Entities {
    public enum EscapeMode {
        / * * 限制实体适合 XHTML 输出: lt，gt，amp、 apos 和 quot 只。*/
        xhtml(xhtmlByVal),
        / * * 默认 HTML 输出的实体。*/
        base(baseByVal),
        / * * 完成 HTML 实体。*/
        extended(fullByVal);

        private Map<Character, String> map;

        EscapeMode(Map<Character, String> map) {
            this.map = map;
        }

        public Map<Character, String> getMap() {
            return map;
        }
    }

    private static final Map<String, Character> full;
    private static final Map<Character, String> xhtmlByVal;
    private static final Map<String, Character> base;
    private static final Map<Character, String> baseByVal;
    private static final Map<Character, String> fullByVal;
    private static final Pattern unescapePattern = Pattern.compile("&(#(x|X)?([0-9a-fA-F]+)|[a-zA-Z]+\\d*);?");
    private static final Pattern strictUnescapePattern = Pattern.compile("&(#(x|X)?([0-9a-fA-F]+)|[a-zA-Z]+\\d*);");

    private Entities() {}

    /**
     * 请检查输入是否已知的命名实体
     * @param 的名字，可能的实体 (例如"lt"或"amp")
    如果已知的命名实体，则 * @return true
     */
    public static boolean isNamedEntity(String name) {
        return full.containsKey(name);
    }

    /**
     * 请检查输入是否已知的命名的实体，在基地的实体集。
     * @param 的名字，可能的实体 (例如"lt"或"amp")
    如果已知的命名实体在基地集中，则 * @return true
     * @see #isNamedEntity(String)
     */
    public static boolean isBaseNamedEntity(String name) {
        return base.containsKey(name);
    }

    /**
     * 获得命名实体的字符值
     * @param 的名字命名实体 (例如"lt"或"amp")
    命名实体的字符值的 * @return (例如 ' <' 或 '与')
     */
    public static Character getCharacterByName(String name) {
        return full.get(name);
    }

    static String escape(String string, Document.OutputSettings out) {
        return escape(string, out.encoder(), out.escapeMode());
    }

    static String escape(String string, CharsetEncoder encoder, EscapeMode escapeMode) {
        StringBuilder accum = new StringBuilder(string.length() * 2);
        Map<Character, String> map = escapeMode.getMap();

        final int length = string.length();
        for (int offset = 0; offset < length; ) {
            final int codePoint = string.codePointAt(offset);

            //代理项对，拆分为单个字符常见的情况上效率执行 (保存创建字符串，char[]):
            if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
                final char c = (char) codePoint;
                if (map.containsKey(c))
                    accum.append('&').append(map.get(c)).append(';');
                else if (encoder.canEncode(c))
                    accum.append(c);
                else
                    accum.append("&#x").append(Integer.toHexString(codePoint)).append(';');
            } else {
                final String c = new String(Character.toChars(codePoint));
                if (encoder.canEncode(c))
                    accum.append(c);
                else
                    accum.append("&#x").append(Integer.toHexString(codePoint)).append(';');
            }

            offset += Character.charCount(codePoint);
        }

        return accum.toString();
    }

    static String unescape(String string) {
        return unescape(string, false);
    }

    /**
     *取消转义的输入的字符串。
     * @param 字符串
    严格的如果"严格的"* @param (即，需要尾随 ';' 的焦炭，否则为是可选的)
     * @return
     */
    static String unescape(String string, boolean strict) {
        return Parser.unescapeEntities(string, strict);
    }

    // xhtml 已经限制实体
    private static final Object[][] xhtmlArray = {
            {"quot", 0x00022},
            {"amp", 0x00026},
            {"apos", 0x00027},
            {"lt", 0x0003C},
            {"gt", 0x0003E}
    };

    static {
        xhtmlByVal = new HashMap<Character, String>();
        base = loadEntities("entities-base.properties");  // most common / default
        baseByVal = toCharacterKey(base);
        full = loadEntities("entities-full.properties"); // extended and overblown.
        fullByVal = toCharacterKey(full);

        for (Object[] entity : xhtmlArray) {
            Character c = Character.valueOf((char) ((Integer) entity[1]).intValue());
            xhtmlByVal.put(c, ((String) entity[0]));
        }
    }

    private static Map<String, Character> loadEntities(String filename) {
        Properties properties = new Properties();
        Map<String, Character> entities = new HashMap<String, Character>();
        try {
            InputStream in = Entities.class.getResourceAsStream(filename);
            properties.load(in);
            in.close();
        } catch (IOException e) {
            throw new MissingResourceException("Error loading entities resource: " + e.getMessage(), "Entities", filename);
        }

        for (Map.Entry entry: properties.entrySet()) {
            Character val = Character.valueOf((char) Integer.parseInt((String) entry.getValue(), 16));
            String name = (String) entry.getKey();
            entities.put(name, val);
        }
        return entities;
    }

    private static Map<Character, String> toCharacterKey(Map<String, Character> inMap) {
        Map<Character, String> outMap = new HashMap<Character, String>();
        for (Map.Entry<String, Character> entry: inMap.entrySet()) {
            Character character = entry.getValue();
            String name = entry.getKey();

            if (outMap.containsKey(character)) {
                // 重复数据消除、 喜欢的小写版本
                if (name.toLowerCase().equals(name))
                    outMap.put(character, name);
            } else {
                outMap.put(character, name);
            }
        }
        return outMap;
    }
}
