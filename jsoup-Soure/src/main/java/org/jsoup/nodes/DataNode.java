package org.jsoup.nodes;

/**
 一个数据节点的样式的内容，编写脚本标记等，内容不应该显示在 text ()。

 @author Jonathan Hedley, jonathan@hedley.net */
public class DataNode extends Node{
    private static final String DATA_KEY = "data";

    /**
     创建新的 DataNode。
     @param data data contents
     @param baseUri base URI
     */
    public DataNode(String data, String baseUri) {
        super(baseUri);
        attributes.put(DATA_KEY, data);
    }

    public String nodeName() {
        return "#data";
    }

    /**
     获取此节点的数据内容。将非转义并与原始的新行，太空等等。
     @return data
     */
    public String getWholeData() {
        return attributes.get(DATA_KEY);
    }

    /**
     * 设置此节点的数据内容。
     * @param data unencoded data
     * @return this node, for chaining
     */
    public DataNode setWholeData(String data) {
        attributes.put(DATA_KEY, data);
        return this;
    }

    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        accum.append(getWholeData()); // 数据没有逃过回报从数据节点，所以"在脚本中，样式是平原
    }

    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {}

    public String toString() {
        return outerHtml();
    }

    /**
     创建新的 DataNode 从 HTML 编码数据。
     @param encodedData encoded data
     @param baseUri bass URI
     @return new DataNode
     */
    public static DataNode createFromEncoded(String encodedData, String baseUri) {
        String data = Entities.unescape(encodedData);
        return new DataNode(data, baseUri);
    }
}
