package org.jsoup.nodes;

import org.jsoup.helper.Validate;

import java.util.Map;

/**
 单个键 + 值属性。关键是修剪和正常化为小写。

 @author Jonathan Hedley, jonathan@hedley.net */
public class Attribute implements Map.Entry<String, String>, Cloneable  {
    private String key;
    private String value;

    /**
     * 从编码 (原始) 键和值创建一个新的属性。
     * @param key attribute key
     * @param value attribute value
     * @see #createFromEncoded
     */
    public Attribute(String key, String value) {
        Validate.notEmpty(key);
        Validate.notNull(value);
        this.key = key.trim().toLowerCase();
        this.value = value;
    }

    /**
     获取属性键。
     @return the attribute key
     */
    public String getKey() {
        return key;
    }

    /**
     设置属性键。获取正常化按构造函数方法。
     @param key the new key; must not be null
     */
    public void setKey(String key) {
        Validate.notEmpty(key);
        this.key = key.trim().toLowerCase();
    }

    /**
     获取属性值。
     @return the attribute value
     */
    public String getValue() {
        return value;
    }

    /**
     设置属性值。
     @param value the new attribute value; must not be null
     */
    public String setValue(String value) {
        Validate.notNull(value);
        String old = this.value;
        this.value = value;
        return old;
    }

    /**
     得到的 HTML 表示的此属性;例如 {@code href="index.html"}。
     @return HTML
     */
    public String html() {
        return key + "=\"" + Entities.escape(value, (new Document("")).outputSettings()) + "\"";
    }

    protected void html(StringBuilder accum, Document.OutputSettings out) {
        accum
                .append(key)
                .append("=\"")
                .append(Entities.escape(value, out))
                .append("\"");
    }

    /**
     获取此属性，{@link #html()} 作为实现的字符串表示形式。
     @return string
     */
    public String toString() {
        return html();
    }

    /**
     *从编码的键和一个 HTML 编码的属性值创建新的属性。
     * @param unencodedKey 假定键不进行编码，因为可以只运行的简单 \w 字符。
     * @param encodedValue HTML attribute encoded value
     * @return attribute
     */
    public static Attribute createFromEncoded(String unencodedKey, String encodedValue) {
        String value = Entities.unescape(encodedValue, true);
        return new Attribute(unencodedKey, value);
    }

    protected boolean isDataAttribute() {
        return key.startsWith(Attributes.dataPrefix) && key.length() > Attributes.dataPrefix.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;

        Attribute attribute = (Attribute) o;

        if (key != null ? !key.equals(attribute.key) : attribute.key != null) return false;
        if (value != null ? !value.equals(attribute.value) : attribute.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public Attribute clone() {
        try {
            return (Attribute) super.clone(); // 只有字段是不可变的字符串键和值，所以没有更多的深层副本所需
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
