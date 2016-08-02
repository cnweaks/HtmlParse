package org.jsoup.nodes;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;

/**
 一个文本节点。

 @author Jonathan Hedley, jonathan@hedley.net */
public class TextNode extends Node {
    /*
    文本节点是一个节点，所以默认情况下附带属性和儿童。属性很少使用，但使用
    决不使用内存和子节点。所以我们不拥有它们，和重写属性的访问器来创建
    他们根据需要对飞。
     */
    private static final String TEXT_KEY = "text";
    String text;

    /**
     创建一个新的文本节点代表提供 (未编码的) 的文本)。

     @param 的原始文本
     @param baseUri 基 uri
     #createFromEncoded (字符串，字符串) @see
     */
    public TextNode(String text, String baseUri) {
        this.baseUri = baseUri;
        this.text = text;
    }

    public String nodeName() {
        return "#text";
    }

    /**
     *获取此文本节点的文本内容。
     Unencoded 的 * @return，正常的文本。
     * @see TextNode#getWholeText()
     */
    public String text() {
        return normaliseWhitespace(getWholeText());
    }

    /**
     *设置此文本节点的文本内容。
     * @param 未编码的文本中的文本
     * @return 这为链接
     */
    public TextNode text(String text) {
        this.text = text;
        if (attributes != null)
            attributes.put(TEXT_KEY, text);
        return this;
    }

    /**
     获取此文本节点，包括任何换行符和空格在原来的 (未编码的) 文本。
     @return text
     */
    public String getWholeText() {
        return attributes == null ? text : attributes.get(TEXT_KEY);
    }

    /**
     如果此文本节点是片空白 — — 也就是测试、 空或只包含空格 (包括换行符)。
     @return 如果此文件是空的或唯一的空白，假如果它包含任何文本内容。
     */
    public boolean isBlank() {
        return StringUtil.isBlank(getWholeText());
    }

    /**
     * 将此文本节点拆分为两个节点在指定的字符串偏移量。分手之后, 此节点将包含
     * 原始文本达偏移量，和会有正负抵消之后包含文本的新文本节点同级。
     * @param 抵消字符串偏移的点拆分处的节点。
    新创建的文本节点包含的文本后偏移量个 * @return。
     */
    public TextNode splitText(int offset) {
        Validate.isTrue(offset >= 0, "Split offset must be not be negative");
        Validate.isTrue(offset < text.length(), "Split offset must not be greater than current text length");

        String head = getWholeText().substring(0, offset);
        String tail = getWholeText().substring(offset);
        text(head);
        TextNode tailNode = new TextNode(tail, this.baseUri());
        if (parent() != null)
            parent().addChildren(siblingIndex()+1, tailNode);

        return tailNode;
    }

    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        String html = Entities.escape(getWholeText(), out);
        if (out.prettyPrint() && parent() instanceof Element && !Element.preserveWhitespace((Element) parent())) {
            html = normaliseWhitespace(html);
        }

        if (out.prettyPrint() && ((siblingIndex() == 0 && parentNode instanceof Element && ((Element) parentNode).tag().formatAsBlock() && !isBlank()) || (out.outline() && siblingNodes().size()>0 && !isBlank()) ))
            indent(accum, depth, out);
        accum.append(html);
    }

    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {}

    public String toString() {
        return outerHtml();
    }

    /**
     * 创建一个新的文本节点从 HTML 编码 (aka 转义的) 数据。
     * @param encodedText 文本包含编码 HTML (例如 &amp; lt;)
     * @return 文本节点包含未编码的数据 (例如 &lt;)
     */
    public static TextNode createFromEncoded(String encodedText, String baseUri) {
        String text = Entities.unescape(encodedText);
        return new TextNode(text, baseUri);
    }

    static String normaliseWhitespace(String text) {
        text = StringUtil.normaliseWhitespace(text);
        return text;
    }

    static String stripLeadingWhitespace(String text) {
        return text.replaceFirst("^\\s+", "");
    }

    static boolean lastCharIsWhitespace(StringBuilder sb) {
        return sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ';
    }

    // 属性摆弄。创建第一次访问。
    private void ensureAttributes() {
        if (attributes == null) {
            attributes = new Attributes();
            attributes.put(TEXT_KEY, text);
        }
    }

    @Override
    public String attr(String attributeKey) {
        ensureAttributes();
        return super.attr(attributeKey);
    }

    @Override
    public Attributes attributes() {
        ensureAttributes();
        return super.attributes();
    }

    @Override
    public Node attr(String attributeKey, String attributeValue) {
        ensureAttributes();
        return super.attr(attributeKey, attributeValue);
    }

    @Override
    public boolean hasAttr(String attributeKey) {
        ensureAttributes();
        return super.hasAttr(attributeKey);
    }

    @Override
    public Node removeAttr(String attributeKey) {
        ensureAttributes();
        return super.removeAttr(attributeKey);
    }

    @Override
    public String absUrl(String attributeKey) {
        ensureAttributes();
        return super.absUrl(attributeKey);
    }
}
