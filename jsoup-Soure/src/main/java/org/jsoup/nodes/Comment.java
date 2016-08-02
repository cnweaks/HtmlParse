package org.jsoup.nodes;

/**
 注释节点。

 @author Jonathan Hedley, jonathan@hedley.net */
public class Comment extends Node {
    private static final String COMMENT_KEY = "comment";

    /**
     创建新的注释节点。
     @param data The contents of the comment
     @param baseUri base URI
     */
    public Comment(String data, String baseUri) {
        super(baseUri);
        attributes.put(COMMENT_KEY, data);
    }

    public String nodeName() {
        return "#comment";
    }

    /**
     获取注释的内容。
     @return comment content
     */
    public String getData() {
        return attributes.get(COMMENT_KEY);
    }

    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        if (out.prettyPrint())
            indent(accum, depth, out);
        accum
                .append("<!--")
                .append(getData())
                .append("-->");
    }

    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {}

    public String toString() {
        return outerHtml();
    }
}
