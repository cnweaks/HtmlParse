package org.jsoup.nodes;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 基地、 抽象的节点模型。元素，文档，评论等是节点的所有实例。

 @author Jonathan Hedley, jonathan@hedley.net */
public abstract class Node implements Cloneable {
    Node parentNode;
    List<Node> childNodes;
    Attributes attributes;
    String baseUri;
    int siblingIndex;

    /**
     创建一个新的节点。
     @param baseUri 基础 URI
     @param 属性的属性 (不为 null，但可能为空)
     */
    protected Node(String baseUri, Attributes attributes) {
        Validate.notNull(baseUri);
        Validate.notNull(attributes);

        childNodes = new ArrayList<Node>(4);
        this.baseUri = baseUri.trim();
        this.attributes = attributes;
    }

    protected Node(String baseUri) {
        this(baseUri, new Attributes());
    }

    /**
     * 默认构造函数。不设置基 uri、 儿童或属性;请谨慎使用。
     */
    protected Node() {
        childNodes = Collections.emptyList();
        attributes = null;
    }

    /**
     获取此节点的节点名称。用于调试目的和不逻辑开关 (为此，使用举例)。
     @return 节点名称
     */
    public abstract String nodeName();

    /**
     *由它的键获取属性值。
     *<p>* 向从属性，它可能是一个相对的 URL 获取绝对 URL，前缀与关键 <code><b>abs</b></code> ，
     * 这是 {见 @link #absUrl} 方法的快捷方式。
     * 例如:<blockquote><code>String url = a.attr("abs:href");</code></blockquote>
     该属性的 * @return 或空字符串，如果不存在 (以避免空值)。
     * @see #attributes()
     * @see #hasAttr(String)
     * @see #absUrl(String)
     */
    public String attr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributes.hasKey(attributeKey))
            return attributes.get(attributeKey);
        else if (attributeKey.toLowerCase().startsWith("abs:"))
            return absUrl(attributeKey.substring("abs:".length()));
        else return "";
    }

    /**
     * 得到的所有元素的属性。
     * @return 属性 (提出实现可迭代，在相同的顺序在原始 HTML)。
     */
    public Attributes attributes() {
        return attributes;
    }

    /**
     * 将属性设置 (键 = 值)。如果该属性已存在，则替换它。
     * @param attributeKey 属性键。
     * @param attributeValue 属性值。
     * @return 这个 (链接)
     */
    public Node attr(String attributeKey, String attributeValue) {
        attributes.put(attributeKey, attributeValue);
        return this;
    }

    /**
     * 如果此元素具有属性进行测试。
     * @param attributeKey 属性键来检查。
    如果该属性存在，假如果不 * @return true。
     */
    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.toLowerCase().startsWith("abs:")) {
            String key = attributeKey.substring("abs:".length());
            if (attributes.hasKey(key) && !absUrl(key).equals(""))
                return true;
        }
        return attributes.hasKey(attributeKey);
    }

    /**
     * 从这个元素中移除属性。
     * @param attributeKey 要移除的属性。
     * @return 这个 (链接)
     */
    public Node removeAttr(String attributeKey) {
        Validate.notNull(attributeKey);
        attributes.remove(attributeKey);
        return this;
    }

    /**
     获取此节点的基 URI。
     @return base URI
     */
    public String baseUri() {
        return baseUri;
    }

    /**
     更新此节点及其所有后代的基 URI。
     @param baseUri base URI to set
     */
    public void setBaseUri(final String baseUri) {
        Validate.notNull(baseUri);

        traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                node.baseUri = baseUri;
            }

            public void tail(Node node, int depth) {
            }
        });
    }

    /**
     *从可能相对 URL 属性获取绝对 URL (即 <code>&lt;a href></code> 或
     * <code>&lt;img src></code>).
     *<p>* 例如:<code>String absUrl = linkEl.absUrl("href");</code>
     * <p/>
     *如果属性值已经是绝对的 (即它始于一种协议，像
     * <code>http://</code> 或 <code>https://</code> 等)，和它成功地将其作为一个 URL，该属性是
     * 直接返回。否则为它是作为相对于元素 {又 @link #baseUri}，URL 处理，而且
     * 绝对使用的。
     *<p>
     *作为替代，您可以使用 {见 @link #attr} 方法与 <code>abs:</code> 前缀，例如:
     * <code>String absUrl = linkEl.attr("abs:href");</code>
     *
     * @param attributeKey 属性键
    可如果一个绝对 URL，影响的 * @return 或一个空的字符串 (非空)，如果该属性是缺掉的或
     * 可以不成为成功的 URL。
     * @see #attr
     * @see URL#URL(URL, String)
     */
    public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        String relUrl = attr(attributeKey);
        if (!hasAttr(attributeKey)) {
            return ""; // 没有什么要绝对与
        } else {
            URL base;
            try {
                try {
                    base = new URL(baseUri);
                } catch (MalformedURLException e) {
                    // 该基地是不合适的但该属性可能会自行 abs，所以试着，
                    URL abs = new URL(relUrl);
                    return abs.toExternalForm();
                }
                // 解决方法: java 解析 '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' 所需
                if (relUrl.startsWith("?"))
                    relUrl = base.getPath() + relUrl;
                URL abs = new URL(base, relUrl);
                return abs.toExternalForm();
            } catch (MalformedURLException e) {
                return "";
            }
        }
    }

    /**
     按其基于 0 的索引获取一个子节点。
     @param 指数指数的子节点
     在此索引的子节点个 @return。Throws a {@code IndexOutOfBoundsException} if the index is out of bounds.
     */
    public Node childNode(int index) {
        return childNodes.get(index);
    }

    /**
     获取此节点的子节点。显示为不可修改的列表: 不能添加新的儿童，但子节点
     自己可以操纵。
     @return 的子级的列表。如果没有孩子，返回空列表。
     */
    public List<Node> childNodes() {
        return Collections.unmodifiableList(childNodes);
    }

    /**
     * 返回此节点的子节点的深层副本。这些节点所做的更改将不会反映在原始
     * 节点
     此节点的子节点的深层副本的 * @return
     */
    public List<Node> childNodesCopy() {
        List<Node> children = new ArrayList<Node>(childNodes.size());
        for (Node node : childNodes) {
            children.add(node.clone());
        }
        return children;
    }

    /**
     * 获取包含此节点的子节点的数目。
     此节点包含的子节点数个 * @return。
     */
    public final int childNodeSize() {
        return childNodes.size();
    }

    protected Node[] childNodesAsArray() {
        return childNodes.toArray(new Node[childNodeSize()]);
    }

    /**
     获取此节点的父节点。
     @return 父节点;或如果没有父级，则为 null。
     */
    public Node parent() {
        return parentNode;
    }

    /**
     * 获取与此节点关联的文档。
     * @return 的相关文档与此节点，则返回 null 如果没有此类文件。
     */
    public Document ownerDocument() {
        if (this instanceof Document)
            return (Document) this;
        else if (parentNode == null)
            return null;
        else
            return parentNode.ownerDocument();
    }

    /**
     * 删除 (删除) 此从 DOM 树的节点。如果该节点没有子级，他们也会删除。
     */
    public void remove() {
        Validate.notNull(parentNode);
        parentNode.removeChild(this);
    }

    /**
     *此节点前 DOM (即作为前面的同级) 插入指定的 HTML。
     * @param html HTML 添加此节点之前
     * @return 此节点，链接
     * @see #after(String)
     */
    public Node before(String html) {
        addSiblingHtml(siblingIndex(), html);
        return this;
    }

    /**
     * 此节点前 DOM (即作为前一个同级) 插入指定的节点。
     要添加此节点之前的 * @param 节点
     * @return 此节点，链接
     * @see #after(Node)
     */
    public Node before(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex(), node);
        return this;
    }

    /**
     * 插入指定的 HTML DOM 后此节点 (即作为下面的兄弟姐妹)。
     * @param html HTML 添加此节点后
     * @return 此节点，链接
     * @see #before(String)
     */
    public Node after(String html) {
        addSiblingHtml(siblingIndex()+1, html);
        return this;
    }

    /**
     *指定的节点 DOM 后此节点 (即作为插入下面的兄弟姐妹)。
     要添加此节点后的 * @param 节点
     * @return 此节点，链接
     * @see #before(Node)
     */
    public Node after(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex()+1, node);
        return this;
    }

    private void addSiblingHtml(int index, String html) {
        Validate.notNull(html);
        Validate.notNull(parentNode);

        Element context = parent() instanceof Element ? (Element) parent() : null;
        List<Node> nodes = Parser.parseFragment(html, context, baseUri());
        parentNode.addChildren(index, nodes.toArray(new Node[nodes.size()]));
    }

    /**
     换行此节点周围的 HTML。
     @param html HTML 环绕此元素，例如至 @code {<div class="head"></div>}.可以任意地深。
     @return 此节点的链接。
     */
    public Node wrap(String html) {
        Validate.notEmpty(html);

        Element context = parent() instanceof Element ? (Element) parent() : null;
        List<Node> wrapChildren = Parser.parseFragment(html, context, baseUri());
        Node wrapNode = wrapChildren.get(0);
        if (wrapNode == null || !(wrapNode instanceof Element)) // nothing to wrap with; noop
            return null;

        Element wrap = (Element) wrapNode;
        Element deepest = getDeepChild(wrap);
        parentNode.replaceChild(this, wrap);
        deepest.addChildren(this);

        //剩下的人 (像失衡包装，<div></div><p>— —<p>余数
        if (wrapChildren.size() > 0) {
            for (int i = 0; i < wrapChildren.size(); i++) {
                Node remainder = wrapChildren.get(i);
                remainder.parentNode.removeChild(remainder);
                wrap.appendChild(remainder);
            }
        }
        return this;
    }

    /**
     *从 DOM 中移除此节点和它的孩子向上移动到该节点的父级。这有下降的影响
     * 节点，但保持它的孩子。
     *<p>*，例如，用输入的 html:<br>* {至 @code<div>一个<span>两个<b>三个</b></span></div>}<br>* 调用 {@code element.unwrap()} {@code 跨度} 元素上将导致在 html 中:<br>* {至 @code<div>一个两个<b>三个</b></div>}<br>
     * and the {@code "Two "} {@link TextNode} being returned.
     * 此节点，该节点已展开之后的第一个孩子个 @return。如果节点没有子级，则为 null。
     * @see #remove()
     * @see #wrap(String)
     */
    public Node unwrap() {
        Validate.notNull(parentNode);

        int index = siblingIndex;
        Node firstChild = childNodes.size() > 0 ? childNodes.get(0) : null;
        parentNode.addChildren(index, this.childNodesAsArray());
        this.remove();

        return firstChild;
    }

    private Element getDeepChild(Element el) {
        List<Element> children = el.children();
        if (children.size() > 0)
            return getDeepChild(children.get(0));
        else
            return el;
    }

    /**
     * 用提供的节点替换此 DOM 中的节点。
     * @param in the node that will will replace the existing node.
     */
    public void replaceWith(Node in) {
        Validate.notNull(in);
        Validate.notNull(parentNode);
        parentNode.replaceChild(this, in);
    }

    protected void setParentNode(Node parentNode) {
        if (this.parentNode != null)
            this.parentNode.removeChild(this);
        this.parentNode = parentNode;
    }

    protected void replaceChild(Node out, Node in) {
        Validate.isTrue(out.parentNode == this);
        Validate.notNull(in);
        if (in.parentNode != null)
            in.parentNode.removeChild(in);

        Integer index = out.siblingIndex();
        childNodes.set(index, in);
        in.parentNode = this;
        in.setSiblingIndex(index);
        out.parentNode = null;
    }

    protected void removeChild(Node out) {
        Validate.isTrue(out.parentNode == this);
        int index = out.siblingIndex();
        childNodes.remove(index);
        reindexChildren();
        out.parentNode = null;
    }

    protected void addChildren(Node... children) {
        //最常用。短路电流 addChildren(int)，哪个命中索引儿童和阵列复制
        for (Node child: children) {
            reparentChild(child);
            childNodes.add(child);
            child.setSiblingIndex(childNodes.size()-1);
        }
    }

    protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        for (int i = children.length - 1; i >= 0; i--) {
            Node in = children[i];
            reparentChild(in);
            childNodes.add(index, in);
        }
        reindexChildren();
    }

    private void reparentChild(Node child) {
        if (child.parentNode != null)
            child.parentNode.removeChild(child);
        child.setParentNode(this);
    }

    private void reindexChildren() {
        for (int i = 0; i < childNodes.size(); i++) {
            childNodes.get(i).setSiblingIndex(i);
        }
    }

    /**
     检索此节点的同级节点。类似于 {@link #childNodes() node.parent.childNodes()}，但并不
     包括此节点 (节点不是同级的本身)。
     @return 节点的兄弟姐妹。如果该节点没有父级，则返回空列表。
     */
    public List<Node> siblingNodes() {
        if (parentNode == null)
            return Collections.emptyList();

        List<Node> nodes = parentNode.childNodes;
        List<Node> siblings = new ArrayList<Node>(nodes.size() - 1);
        for (Node node: nodes)
            if (node != this)
                siblings.add(node);
        return siblings;
    }

    /**
     获取此节点的下一个同级。
     @return 下一个同级元素，则返回 null，如果这是最后一个同级
     */
    public Node nextSibling() {
        if (parentNode == null)
            return null; // root

        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        if (siblings.size() > index+1)
            return siblings.get(index+1);
        else
            return null;
    }

    /**
     获取此节点的上一个同级元素。
     上一个同级的 @return 或 null 如果这是第一个同级
     */
    public Node previousSibling() {
        if (parentNode == null)
            return null; // root

        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        if (index > 0)
            return siblings.get(index-1);
        else
            return null;
    }

    /**
     * 获取此节点的列表索引在其同级节点列表中。例如，如果这是在第一个节点
     * 兄弟姐妹，返回 0。
     * @return 在节点同级列表中的位置
     * @see org.jsoup.nodes.Element#elementSiblingIndex()
     */
    public int siblingIndex() {
        return siblingIndex;
    }

    protected void setSiblingIndex(int siblingIndex) {
        this.siblingIndex = siblingIndex;
    }

    /**
     * 执行此节点及其后代的深度优先遍历。
     * @param nodeVisitor 位访客回调，在每个节点上执行
     * @return 此节点，链接
     */
    public Node traverse(NodeVisitor nodeVisitor) {
        Validate.notNull(nodeVisitor);
        NodeTraversor traversor = new NodeTraversor(nodeVisitor);
        traversor.traverse(this);
        return this;
    }

    /**
     获取此节点的外部 HTML。
     @return HTML
     */
    public String outerHtml() {
        StringBuilder accum = new StringBuilder(128);
        outerHtml(accum);
        return accum.toString();
    }

    protected void outerHtml(StringBuilder accum) {
        new NodeTraversor(new OuterHtmlVisitor(accum, getOutputSettings())).traverse(this);
    }

    // 如果此节点没有文档 (或父)，，检索默认输出设置
    private Document.OutputSettings getOutputSettings() {
        return ownerDocument() != null ? ownerDocument().outputSettings() : (new Document("")).outputSettings();
    }

    /**
     获取此节点的外部 HTML。
     @param accum accumulator to place HTML into
     */
    abstract void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out);

    abstract void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out);

    public String toString() {
        return outerHtml();
    }

    protected void indent(StringBuilder accum, int depth, Document.OutputSettings out) {
        //out.indentAmount()是缩进长度，默认是1
        accum.append("\n").append(StringUtil.padding(depth * out.indentAmount()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        todo: 有节点举行儿童指数、 比较，和父 (不是儿童)
        return false;
    }

    @Override
    public int hashCode() {
        int result = parentNode != null ? parentNode.hashCode() : 0;
        // 不儿童，或将阻止堆栈，当他们回到父)
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    /**
     *创建此节点和它的所有子独立的深层副本。克隆的节点将有没有兄弟姐妹或
     * 父节点。作为一个独立的对象，任何对克隆或任何它的孩子所做的更改不会影响
     * 原始节点。
     *<p>* 克隆的节点可以通过到另一个文档或节点结构使用 {Element#appendChild(Node)，@link}。
     * @return 独立克隆的节点
     */
    @Override
    public Node clone() {
        return doClone(null); // splits for orphan
    }

    protected Node doClone(Node parent) {
        Node clone;
        try {
            clone = (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        clone.parentNode = parent; // can be null, to create an orphan split
        clone.siblingIndex = parent == null ? 0 : siblingIndex;
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new ArrayList<Node>(childNodes.size());
        for (Node child: childNodes)
            clone.childNodes.add(child.doClone(clone)); // clone() 创建孤儿，doClone() 保持父

        return clone;
    }

    private static class OuterHtmlVisitor implements NodeVisitor {
        private StringBuilder accum;
        private Document.OutputSettings out;

        OuterHtmlVisitor(StringBuilder accum, Document.OutputSettings out) {
            this.accum = accum;
            this.out = out;
        }

        public void head(Node node, int depth) {
            node.outerHtmlHead(accum, depth, out);
        }

        public void tail(Node node, int depth) {
            if (!node.nodeName().equals("#text")) // saves a void hit.
                node.outerHtmlTail(accum, depth, out);
        }
    }
}
