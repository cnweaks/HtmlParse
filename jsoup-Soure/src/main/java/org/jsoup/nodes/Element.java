package org.jsoup.nodes;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.select.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * HTML 元素包含的标签名称、 属性和子节点 (包括文本节点和
 * 其他元素)。
 * * 从一个元素，你可以提取数据，遍历节点对象图，以及操纵 HTML。
 Jonathan 赫德利的 ** @author、 jonathan@hedley.net
 */
public class Element extends Node {
    private Tag tag;
    private Set<String> classNames;

    /**
     *创建一个新的、 独立的元素。(在这独立是没有父)。
     此元素的 ** @param 标记标记
     * @param baseUri 的基 URI
     * @param 属性初始属性
     * @see #appendChild(Node)
     * @see #appendElement(String)
     */
    public Element(Tag tag, String baseUri, Attributes attributes) {
        super(baseUri, attributes);

        Validate.notNull(tag);
        this.tag = tag;
    }

    /**
     * 从标记和基 URI 创建一个新的元素。
     ** @param 标记元
     * @param baseUri 此元素的基 URI。它是可以接受的基 URI 为空
     * 字符串，但不是为 null。
     * @see Tag#valueOf(String)
     */
    public Element(Tag tag, String baseUri) {
        this(tag, baseUri, new Attributes());
    }

    @Override
    public String nodeName() {
        return tag.getName();
    }

    /**
     * 获取此元素的标记的名称。例如 {@code div}
     标记名称的 ** @return
     */
    public String tagName() {
        return tag.getName();
    }

    /**
     * 更改此元素的标记。For example, convert a {@code <span>} to a {@code<div>} 与
     * {@code el.tagName("div");}。
     *
     此元素的 * @param tagName 新标记名称
     * @return 此元素的链接</div></span>
     */
    public Element tagName(String tagName) {
        Validate.notEmpty(tagName, "Tag name must not be empty.");
        tag = Tag.valueOf(tagName);
        return this;
    }

    /**
     * 获取此元素的标签。
     *
     * @return the tag object
     */
    public Tag tag() {
        return tag;
    }

    /**
     * 如果此元素是一个块级元素进行测试。(例如至 @code {<div>= = true} 或内联元素
     * {至 @code<p>= = false})。
     ** @return 如果阻止，假如果不 (和内联)</div>
     */
    public boolean isBlock() {
        return tag.isBlock();
    }

    /**
     * 获取此元素的 {@code id} 属性。
     id 属性，如果存在的 ** @return 或如果不为空字符串。
     */
    public String id() {
        String id = attr("id");
        return id == null ? "" : id;
    }

    /**
     * 在此元素上设置属性值。如果此元素已具有的属性
     * 关键，它的值被更新;否则，添加了一个新的属性。
     ** @return 此元素
     */
    public Element attr(String attributeKey, String attributeValue) {
        super.attr(attributeKey, attributeValue);
        return this;
    }

    /**
     *获取此元素的 HTML5 自定义数据属性。每个属性都有一个键的元素中
     与"数据 — —"开始是包含数据集。
     *<p>* 例如，元素至 @code {<div data-package="jsoup" data-language="Java" class="group">...}具有数据集
     * {@code 包 = jsoup，语言 = java}。
     *<p></div>
     * 这张地图是元素的属性映射的筛选的视图。更改为一张地图 (添加、 删除、 更新) 反映
     * 在其他地图。
     *<p>* 你可以查找具有数据属性使用元素 {@code [^ 数据-]} 属性键前缀选择器。
     一张地图的 * @return {@code 键 = 值} 自定义数据属性。
     */
    public Map<String, String> dataset() {
        return attributes.dataset();
    }

    @Override
    public final Element parent() {
        return (Element) parentNode;
    }

    /**
     * 获取此元素的父和祖先，和文档根。
     * @return 此元素堆栈的父母，最近第一次。
     */
    public Elements parents() {
        Elements parents = new Elements();
        accumulateParents(this, parents);
        return parents;
    }

    private static void accumulateParents(Element el, Elements parents) {
        Element parent = el.parent();
        if (parent != null && !parent.tagName().equals("#root")) {
            parents.add(parent);
            accumulateParents(parent, parents);
        }
    }

    /**
     * 获取此元素的子元素按其基于 0 的索引编号。
     *<p>* 请注意，元素可以具有混合节点和元素作为儿童。此方法检查
     * 儿童的元素和索引筛选的列表基于该筛选后的列表。
     ** @param 指数要检索的元素的索引号
    子元素的 * @return 如果它存在，否则将引发 {从以下版本 @code}
     * @see #childNode(int)
     */
    public Element child(int index) {
        return children().get(index);
    }

    /**
     * 获取此元素的子元素。
     *<p>* 这是有效筛选器在 {@link #childNodes()} 要元素节点。
     * @return 子元素。如果此元素没有任何子级，则返回
     * empty list.
     * @see #childNodes()
     */
    public Elements children() {
        // 创建在飞，而不是维护两个列表。如果获取慢，memoize，并将标记脏变化
        List<Element> elements = new ArrayList<Element>();
        for (Node node : childNodes) {
            if (node instanceof Element)
                elements.add((Element) node);
        }
        return new Elements(elements);
    }

    /**
     *textNodes 获取此元素的子文本节点。列表是不可修改，但可能操纵文本节点。
     *<p>* 这是有效筛选器在 {@link #childNodes()} 要文本节点。
     * @return 子文本节点。如果此元素具有没有文本节点，返回
     * 空列表。
     *<p>*，例如，用 HTML 输入: 至 @code {<p>一个<span>两个</span>三<br>四</p>} {@code p} 元素选择:
     *<ul></ul>
     *     <li>{@code p.text()} = {@code "One Two Three Four"}</li>
     *     <li>{@code p.ownText()} = {@code "One Three Four"}</li>
     *     <li>{@code p.children()} = {@code Elements[<span>, <br>]}</li>
     *     <li>{@code p.childNodes()} = {@code List<Node>["One ", <span>, " Three ", <br>, " Four"]}</li>
     *     <li>{@code p.textNodes()} = {@code List<TextNode>["One ", " Three ", " Four"]}</li>
     * </ul>
     */
    public List<TextNode> textNodes() {
        List<TextNode> textNodes = new ArrayList<TextNode>();
        for (Node node : childNodes) {
            if (node instanceof TextNode)
                textNodes.add((TextNode) node);
        }
        return Collections.unmodifiableList(textNodes);
    }

    /**
     * 获取此元素的子级的数据节点。列表是不可修改，但可能操纵数据节点。
     * <p/>
     * 这实际上是对 {@link #childNodes()} 筛选器以获取数据节点。
     * @return 子数据节点。如果此元素具有没有数据节点，返回
     * empty list.
     * @see #data()
     */
    public List<DataNode> dataNodes() {
        List<DataNode> dataNodes = new ArrayList<DataNode>();
        for (Node node : childNodes) {
            if (node instanceof DataNode)
                dataNodes.add((DataNode) node);
        }
        return Collections.unmodifiableList(dataNodes);
    }

    /**
     * select找到 {选择器，@link} CSS 与查询匹配的与此作为起始上下文的元素的元素。匹配的元素
     * 可能包括这一内容，或任何它的孩子。
     *<p>* 此方法是一般更强大的使用比 DOM 类型 {@code getElementBy *} 方法因为
     * 多个筛选器是可以结合的例如:
     *<ul></ul>
     *<li>{@code el.select("a[href]")}-发现链接 ({@code} {@code href} 属性标签)
     *<li>{@code el.select("a[href*=example.com]")}-发现链接指向链接 (松)
     *
     *<p>* 请参见查询语法中 {@link org.jsoup.select.Selector}。
     *
     * @param cssQuery {选择器，@link} CSS 样查询
     * @return 元素与查询匹配的 (在没有匹配的情况下为空)
     * @see org.jsoup.select.Selector</li></li>
     */
    public Elements select(String cssQuery) {
        return Selector.select(cssQuery, this);
    }

    /**
     * appendChild向此元素添加一个节点的子节点。
     要添加的 ** @param 子节点。
     * @return 此元素，以便您可以添加更多的子节点或元素。
     */
    public Element appendChild(Node child) {
        Validate.notNull(child);

        addChildren(child);
        return this;
    }

    /**
     *prependChild将节点添加到此元素的子级的开始。
     要添加的 ** @param 子节点。
     * @return 此元素，以便您可以添加更多的子节点或元素。
     */
    public Element prependChild(Node child) {
        Validate.notNull(child);

        addChildren(0, child);
        return this;
    }


    /**
     * insertChildren将给定的子节点插入到此指定索引处的元素。当前节点将被转移到
     * 权利。插入的节点将从其当前的父。为了防止移动，请先复制节点。
     *
     * @param 基于 0 的索引插入在儿童。指定 {0，@code} 要在开始时，{-1，@code} 在插入
     * 结束
     * @param 儿童子节点插入
     * @return 此元素的链接。
     */
    public Element insertChildren(int index, Collection<? extends Node> children) {
        Validate.notNull(children, "Children collection to be inserted must not be null.");
        int currentSize = childNodeSize();
        if (index < 0) index += currentSize +1; // roll around
        Validate.isTrue(index >= 0 && index <= currentSize, "Insert position out of bounds.");

        ArrayList<Node> nodes = new ArrayList<Node>(children);
        Node[] nodeArray = nodes.toArray(new Node[nodes.size()]);
        addChildren(index, nodeArray);
        return this;
    }

    /**
     * appendElement创建一个新元素的标记名称，并将其添加为最后一个子级。
     ** @param tagName 的标记的名称 (例如 {@code div})。
    新的元素，以允许您将内容添加到它，例如个 * @return:
     * {@code parent.appendElement("h1").attr ("id"、"header").text("Welcome");}
     */
    public Element appendElement(String tagName) {
        Element child = new Element(Tag.valueOf(tagName), baseUri());
        appendChild(child);
        return child;
    }

    /**
     * prependElement创建一个新元素的标记名称，并将其添加为第一个孩子。
     ** @param tagName 的标记的名称 (例如 {@code div})。
    新的元素，以允许您将内容添加到它，例如个 * @return:
     * {@code parent.prependElement("h1").attr ("id"、"header").text("Welcome");}
     */
    public Element prependElement(String tagName) {
        Element child = new Element(Tag.valueOf(tagName), baseUri());
        prependChild(child);
        return child;
    }

    /**
     * appendText创建并将一个新的文本节点追加到此元素。
     ** @param 文本要添加的未编码的文本
     * @return this element
     */
    public Element appendText(String text) {
        TextNode node = new TextNode(text, baseUri());
        appendChild(node);
        return this;
    }

    /**
     *prependText创建并添加一个新的文本节点到此元素。
     ** @param 文本要添加的未编码的文本
     * @return 此元素
     */
    public Element prependText(String text) {
        TextNode node = new TextNode(text, baseUri());
        prependChild(node);
        return this;
    }

    /**
     * append将内部 HTML 添加到此元素。提供的 HTML 将被解析，和每个节点追加到结束了孩子们。
     * @param html HTML 这个元素中，添加后的现有 HTML
     * @return 此元素
     * @see #html(String)
     */
    public Element append(String html) {
        Validate.notNull(html);

        List<Node> nodes = Parser.parseFragment(html, this, baseUri());
        addChildren(nodes.toArray(new Node[nodes.size()]));
        return this;
    }

    /**
     *prepend将内部 HTML 添加到此元素。提供的 HTML 将被解析，和每个节点其前面添加的元素的子元素开始。
     * @param html HTML 这个元素中，在现有的 HTML 之前添加
     * @return 此元素
     * @see #html(String)
     */
    public Element prepend(String html) {
        Validate.notNull(html);

        List<Node> nodes = Parser.parseFragment(html, this, baseUri());
        addChildren(0, nodes.toArray(new Node[nodes.size()]));
        return this;
    }

    /**
     *before 将指定的 HTML (作为前一个同级) 插入到 DOM 在此元素之前。
     *
     * @param html HTML 添加此元素之前
     * @return 此元素的链接
     * @see #after(String)
     */
    @Override
    public Element before(String html) {
        return (Element) super.before(html);
    }

    /*** 插入指定的节点到此节点前 DOM (作为前一个同级)。
     要添加此元素之前的 * @param 节点
     * @return 此元素的链接
     * @see #after(Node)
     */
    @Override
    public Element before(Node node) {
        return (Element) super.before(node);
    }

    /**
     * after插入指定的 HTML DOM 后此元素 (如下面的兄弟姐妹)。
     *
     * @param html HTML 添加此元素之后
     * @return 此元素的链接
     * @see #before(String)
     */
    @Override
    public Element after(String html) {
        return (Element) super.after(html);
    }

    /**
     * 插入指定的节点 DOM 后此节点 (如下面的兄弟姐妹)。
     要添加此元素之后的 * @param 节点
     * @return 此元素的链接
     * @see #before(Node)
     */
    @Override
    public Element after(Node node) {
        return (Element) super.after(node);
    }

    /**
     *empty 删除所有元素的子节点。任何属性保留为-是。
     * @return this element
     */
    public Element empty() {
        childNodes.clear();
        return this;
    }

    /**
     *wrap换行此元素周围的 HTML。
     *
     * @param html HTML 环绕此元素，例如至 @code {<div class="head"></div>}.可以任意地深。
     * @return 此元素的链接。
     */
    @Override
    public Element wrap(String html) {
        return (Element) super.wrap(html);
    }

    /**
     *siblingElements获取一个同级元素。如果元素具有没有同级元素，将返回一个空列表。元素不是一对兄妹
     * 本身，所以将不被包括在返回的列表。
     * @return 同级元素
     */
    public Elements siblingElements() {
        if (parentNode == null)
            return new Elements(0);

        List<Element> elements = parent().children();
        Elements siblings = new Elements(elements.size() - 1);
        for (Element el: elements)
            if (el != this)
                siblings.add(el);
        return siblings;
    }

    /**
     * nextElementSibling获取此元素的下一个同级元素。例如，如果一个 div，{@code} 包含两个 {@code p} s，* {@code nextElementSibling} {@code p} 第一次是第二个 {@code p}。
     *<p>* 这是类似于 {@link #nextSibling()}，但具体认为只有元素
     下一个元素的 * @return 或 null 如果没有下一个元素
     * @see #previousElementSibling()
     */
    public Element nextElementSibling() {
        if (parentNode == null) return null;
        List<Element> siblings = parent().children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        if (siblings.size() > index+1)
            return siblings.get(index+1);
        else
            return null;
    }

    /**
     *previousElementSibling获取此元素的上元一个同级。
     前面的元素的 * @return 或 null 如果没有前一个元素
     * @see #nextElementSibling()
     */
    public Element previousElementSibling() {
        if (parentNode == null) return null;
        List<Element> siblings = parent().children();
        Integer index = indexInList(this, siblings);
        Validate.notNull(index);
        if (index > 0)
            return siblings.get(index-1);
        else
            return null;
    }

    /**
     * firstElementSibling获取此元素的第一个元素兄弟。
     是元素 (aka 父级的第一个元素子级) 的第一个同级的 * @return
     */
    public Element firstElementSibling() {
        // todo: should firstSibling() exclude this?
        List<Element> siblings = parent().children();
        return siblings.size() > 1 ? siblings.get(0) : null;
    }

    /**
     * elementSiblingIndex其元素同级列表中获取此元素的列表索引。例如，如果这是的第一个元素
     * 兄弟姐妹，返回 0。
     * @return 元素同级列表中的位置
     */
    public Integer elementSiblingIndex() {
        if (parent() == null) return 0;
        return indexInList(this, parent().children());
    }

    /**
     lastElementSibling*获取此元素的最后一个元素同级
     是 (aka 父级的最后一个子元素) 的元素的最后一个同级的 * @return
     */
    public Element lastElementSibling() {
        List<Element> siblings = parent().children();
        return siblings.size() > 1 ? siblings.get(siblings.size() - 1) : null;
    }

    private static <E extends Element> Integer indexInList(Element search, List<E> elements) {
        Validate.notNull(search);
        Validate.notNull(elements);

        for (int i = 0; i < elements.size(); i++) {
            E element = elements.get(i);
            if (element.equals(search))
                return i;
        }
        return null;
    }

    // DOM type methods

    /*** getElementsByTag发现元素，包括和递归下此元素，与指定的标记名称。
     * @param tagName 要搜索的标签名称 (厚脸皮地案例)。
    元素的匹配不可修改列表个 * @return。将是空的如果此元素和无其儿童比赛。
     */
    public Elements getElementsByTag(String tagName) {
        Validate.notEmpty(tagName);
        tagName = tagName.toLowerCase().trim();

        return Collector.collect(new Evaluator.Tag(tagName), this);
    }

    /*** getElementById查找按 ID，包括或在此元素下的元素。
     *<p>* 请注意这就是找到第一个匹配 ID，开始与此元素。如果你向下搜索从不同
     * 起始点，它是可以找到一个不同的元素 id。通过在文档中，ID 的独特元素
     * 使用 {Document#getElementById(String)，@link}
     * @param id 的 ID 来搜索。
    第一个匹配的元素的 ID，开始与此元素，则返回 null，如果找不到任何个 * @return。
     */
    public Element getElementById(String id) {
        Validate.notEmpty(id);

        Elements elements = Collector.collect(new Evaluator.Id(id), this);
        if (elements.size() > 0)
            return elements.get(0);
        else
            return null;
    }

    /*** getElementsByClass发现有此类，包括或在此元素下的元素。大小写不敏感。
     *<p>* 元素可以有多个类 (例如至 @code {<div class="header round first">}.这种方法
     * 检查每个类，所以你可以找到与上述 {@code el.getElementsByClass("header");}。
     ** @param 类的类要搜索的名字。
     * @return 元素与提供的类的名称，如果没有空</div>
     * @see #hasClass(String)
     * @see #classNames()
     */
    public Elements getElementsByClass(String className) {
        Validate.notEmpty(className);

        return Collector.collect(new Evaluator.Class(className), this);
    }

    /*** getElementsByAttribute查找已命名的属性集的元素。大小写不敏感。
     *
     * @param 关键属性的名称，如 {@code href}
    如果没有一个具有此属性，空的 * @return 元素
     */
    public Elements getElementsByAttribute(String key) {
        Validate.notEmpty(key);
        key = key.trim().toLowerCase();

        return Collector.collect(new Evaluator.Attribute(key), this);
    }

    /**
     *getElementsByAttributeStarting与所提供的前缀开始一个属性名称的元素。使用 {@code 数据}-查找元素
     * 有 HTML5 的数据集。
     * @param keyPrefix 名称前缀的属性例如 {@code 数据-}
     * @return 有开头的前缀，空如果没有的属性名称的元素。
     */
    public Elements getElementsByAttributeStarting(String keyPrefix) {
        Validate.notEmpty(keyPrefix);
        keyPrefix = keyPrefix.trim().toLowerCase();

        return Collector.collect(new Evaluator.AttributeStarting(keyPrefix), this);
    }

    /**
     * getElementsByAttributeValue查找具有特定值的属性的元素。大小写不敏感。
     ** @param 关键属性的名称
     * @param 值的属性的值
    具有此属性与此值，如果没有空的 * @return 元素
     */
    public Elements getElementsByAttributeValue(String key, String value) {
        return Collector.collect(new Evaluator.AttributeWithValue(key, value), this);
    }

    /**
     * getElementsByAttributeValueNot查找元素不具有此属性，或有一个不同的值。大小写不敏感。
     ** @param 关键属性的名称
     * @param 值的属性的值
     * @return 元素，而没有匹配的属性
     */
    public Elements getElementsByAttributeValueNot(String key, String value) {
        return Collector.collect(new Evaluator.AttributeWithValueNot(key, value), this);
    }

    /*** getElementsByAttributeValueStarting查找具有以值前缀开头的属性元素。大小写不敏感。
     ** @param 关键属性的名称
     * @param valuePrefix 开始的属性值
     * @return elements that have attributes that start with the value prefix
     */
    public Elements getElementsByAttributeValueStarting(String key, String valuePrefix) {
        return Collector.collect(new Evaluator.AttributeWithValueStarting(key, valuePrefix), this);
    }

    /**
     * getElementsByAttributeValueEnding查找具有以价值后缀结尾的属性元素。大小写不敏感。
     ** @param 关键属性的名称
    属性值的 * @param valueSuffix 结尾
     * @return 元素具有属性以价值后缀结尾的
     */
    public Elements getElementsByAttributeValueEnding(String key, String valueSuffix) {
        return Collector.collect(new Evaluator.AttributeWithValueEnding(key, valueSuffix), this);
    }

    /**
     * getElementsByAttributeValueContaining查找具有的属性值包含匹配字符串的元素。大小写不敏感。
     ** @param 关键属性的名称
     * @param 匹配字符串中要搜索的值
     * @return 元素具有属性包含此文本
     */
    public Elements getElementsByAttributeValueContaining(String key, String match) {
        return Collector.collect(new Evaluator.AttributeWithValueContaining(key, match), this);
    }

    /**
     * getElementsByAttributeValueMatching查找具有的属性的值与提供的正则表达式相匹配的元素。
     * @param 关键属性的名称
     * @param 模式编译正则表达式来匹配属性值
     * @return 元素具有属性匹配这个正则表达式
     */
    public Elements getElementsByAttributeValueMatching(String key, Pattern pattern) {
        return Collector.collect(new Evaluator.AttributeWithValueMatching(key, pattern), this);

    }

    /**
     * getElementsByAttributeValueMatching查找具有的属性的值与提供的正则表达式相匹配的元素。
     * @param 关键属性的名称
     * @param 正则表达式正则表达式匹配的属性值。你可以使用<a href="http://java.sun.com/docs/books/tutorial/essential/regex/pattern.html#embedded">嵌入式的标志</a>(如 (? 我) 和 (? m) 为控制正则表达式选项。
     * @return 元素具有属性匹配这个正则表达式
     */
    public Elements getElementsByAttributeValueMatching(String key, String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Pattern syntax error: " + regex, e);
        }
        return getElementsByAttributeValueMatching(key, pattern);
    }

    /**
     * getElementsByIndexLessThan查找的元素的同级索引小于所提供的索引。
     * @param 基于 0 的索引
     * @return 元素小于索引
     */
    public Elements getElementsByIndexLessThan(int index) {
        return Collector.collect(new Evaluator.IndexLessThan(index), this);
    }

    /**
     * getElementsByIndexGreaterThan查找的元素的同级索引大于所提供的索引。
     * @param 基于 0 的索引
     * @return 元素大于索引
     */
    public Elements getElementsByIndexGreaterThan(int index) {
        return Collector.collect(new Evaluator.IndexGreaterThan(index), this);
    }

    /**
     * getElementsByIndexEquals查找其同级索引等于所提供的索引的元素。
     * @param 基于 0 的索引
     * @return 元素等于索引
     */
    public Elements getElementsByIndexEquals(int index) {
        return Collector.collect(new Evaluator.IndexEquals(index), this);
    }

    /**
     * getElementsContainingText查找包含指定的字符串的元素。搜索是大小写不敏感的。可直接显示的文本
     * 在元素中，或在它的后代。
     * @param 全文搜索文本在元素的文本中查找
     * @return 元素包含的字符串，区分大小写。
     * @see Element#text()
     */
    public Elements getElementsContainingText(String searchText) {
        return Collector.collect(new Evaluator.ContainsText(searchText), this);
    }

    /**
     * getElementsContainingOwnText查找直接包含指定的字符串的元素。搜索是大小写不敏感的。必须直接显示的文本
     * 在元素中，不在它的后代。
     * @param 全文搜索文本在元素的文本中查找
     * @return 元素包含的字符串，区分大小写。
     * @see Element#ownText()
     */
    public Elements getElementsContainingOwnText(String searchText) {
        return Collector.collect(new Evaluator.ContainsOwnText(searchText), this);
    }

    /**
     *getElementsMatchingText查找的元素的文本匹配提供的正则表达式。
     * @param 模式的正则表达式以匹配对文本
     * @return 元素匹配提供的正则表达式。
     * @see Element#text()
     */
    public Elements getElementsMatchingText(Pattern pattern) {
        return Collector.collect(new Evaluator.Matches(pattern), this);
    }

    /**
     * getElementsMatchingText查找的元素的文本匹配提供的正则表达式。
     * @param 正则表达式正则表达式以匹配对文本。你可以使用<a href="http://java.sun.com/docs/books/tutorial/essential/regex/pattern.html#embedded">嵌入式的标志</a>(如 (? 我) 和 (? m) 为控制正则表达式选项。
     * @return 元素匹配提供的正则表达式。
     * @see Element#text()
     */
    public Elements getElementsMatchingText(String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Pattern syntax error: " + regex, e);
        }
        return getElementsMatchingText(pattern);
    }

    /**
     * getElementsMatchingOwnText查找其自己的文本匹配提供的正则表达式的元素。
     * @param 模式的正则表达式以匹配对文本
     * @return 元素匹配提供的正则表达式。
     * @see Element#ownText()
     */
    public Elements getElementsMatchingOwnText(Pattern pattern) {
        return Collector.collect(new Evaluator.MatchesOwn(pattern), this);
    }

    /**
     * getElementsMatchingOwnText查找元素的文本匹配正则表达式。
     * @param 正则表达式正则表达式以匹配对文本。你可以使用<a href="http://java.sun.com/docs/books/tutorial/essential/regex/pattern.html#embedded">嵌入式的标志</a>(如 (? 我) 和 (? m) 为控制正则表达式选项。
     * @return elements matching the supplied regular expression.
     * @see Element#ownText()
     */
    public Elements getElementsMatchingOwnText(String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Pattern syntax error: " + regex, e);
        }
        return getElementsMatchingOwnText(pattern);
    }

    /**
     * getAllElements找到此元素 (包括自我，和子元素) 下的所有元素。
     *
     * @return all elements
     */
    public Elements getAllElements() {
        return Collector.collect(new Evaluator.AllElements(), this);
    }

    /**
     *text获取此元素及其所有子级的合并案文。
     * <p>
     * 例如，给定的 HTML 至 @code {<p>你好<b>那里</b>现在!</p>}，{@code p.text()} 返回 {@code"你好有现在!}
     *
     * @return unencoded text, or empty string if none.
     * @see #ownText()
     * @see #textNodes()
     */
    public String text() {
        final StringBuilder accum = new StringBuilder();
        new NodeTraversor(new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    appendNormalisedText(accum, textNode);
                } else if (node instanceof Element) {
                    Element element = (Element) node;
                    if (accum.length() > 0 &&
                            (element.isBlock() || element.tag.getName().equals("br")) &&
                            !TextNode.lastCharIsWhitespace(accum))
                        accum.append(" ");
                }
            }

            public void tail(Node node, int depth) {
            }
        }).traverse(this);
        return accum.toString().trim();
    }

    /**
     *  ownText获取由只; 此元素的文本不会所有子元素的合并案文。
     *  <p>* 为例，给出了 HTML 至 @code {<p>你好<b>那里</b>现在!</p>}，{@code p.ownText()} 返回 {"现在你好!"，@code}，
     * 而返回 {@code p.text()} {@code"你好有现在!"}。
     * 请注意，{@code b} 元素中的文本不会返回，因为它不是 {@code p} 元素的直接子级。
     *
     * @return 未编码的文本或空字符串，如果没有。
     * @see #text()
     * @see #textNodes()
     */
    public String ownText() {
        StringBuilder sb = new StringBuilder();
        ownText(sb);
        return sb.toString().trim();
    }

    private void ownText(StringBuilder accum) {
        for (Node child : childNodes) {
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                appendNormalisedText(accum, textNode);
            } else if (child instanceof Element) {
                appendWhitespaceIfBr((Element) child, accum);
            }
        }
    }

    private static void appendNormalisedText(StringBuilder accum, TextNode textNode) {
        String text = textNode.getWholeText();

        if (!preserveWhitespace(textNode.parent())) {
            text = TextNode.normaliseWhitespace(text);
            if (TextNode.lastCharIsWhitespace(accum))
                text = TextNode.stripLeadingWhitespace(text);
        }
        accum.append(text);
    }

    private static void appendWhitespaceIfBr(Element element, StringBuilder accum) {
        if (element.tag.getName().equals("br") && !TextNode.lastCharIsWhitespace(accum))
            accum.append(" ");
    }

    static boolean preserveWhitespace(Node node) {
        // 只是看此元素和一层，以防止递归 & 不必要堆栈搜索
        if (node != null && node instanceof Element) {
            Element element = (Element) node;
            return element.tag.preserveWhitespace() ||
                    element.parent() != null && element.parent().tag.preserveWhitespace();
        }
        return false;
    }

    /**
     *text设置此元素的文本。将清除任何现有的内容 (文本或元素)
     * @param text unencoded text
     * @return this element
     */
    public Element text(String text) {
        Validate.notNull(text);

        empty();
        TextNode textNode = new TextNode(text, baseUri);
        appendChild(textNode);

        return this;
    }

    /**
     hasText如果此元素具有任何文本内容 (即不只是空白) 进行测试。
     @return true if element has non-blank text content.
     */
    public boolean hasText() {
        for (Node child: childNodes) {
            if (child instanceof TextNode) {
                TextNode textNode = (TextNode) child;
                if (!textNode.isBlank())
                    return true;
            } else if (child instanceof Element) {
                Element el = (Element) child;
                if (el.hasText())
                    return true;
            }
        }
        return false;
    }

    /**
     *data获取此元素的组合的数据。数据是例如 {@code 脚本} 标记。
     * @return the data, or empty string if none
     *
     * @see #dataNodes()
     */
    public String data() {
        StringBuilder sb = new StringBuilder();

        for (Node childNode : childNodes) {
            if (childNode instanceof DataNode) {
                DataNode data = (DataNode) childNode;
                sb.append(data.getWholeData());
            } else if (childNode instanceof Element) {
                Element element = (Element) childNode;
                String elementData = element.data();
                sb.append(elementData);
            }
        }
        return sb.toString();
    }

    /**
     * className获取此元素的"class"属性，其中可能包括多个类名称，空间的文本值
     * 分开。(例如，关于 <code>&lt;div class="header gray"></code> 返回，" <code>header gray</code> ")
     * @return The literal class attribute, or <b>empty string</b> if no class attribute set.
     */
    public String className() {
        return attr("class");
    }

    /**
     * 获取所有元素的类名。例如，关于元素至 @code {<div class="header gray" }="">},
     * 返回一组的两个元素 {"标头"，"灰色"，@code}。请注意，此集的修改不被推到
     * 支持 {@code 类} 属性;使用 {@link #classNames(Set)} 方法将它们持久存储。</div>
     * @return set of classnames, empty if no class attribute
     */
    public Set<String> classNames() {
        if (classNames == null) {
            String[] names = className().split("\\s+");
            classNames = new LinkedHashSet<String>(Arrays.asList(names));
        }
        return classNames;
    }

    /**
     将元素的 {@code 类} 属性设置为提供的类名称。
     @param classNames set of classes
     @return this element, for chaining
     */
    public Element classNames(Set<String> classNames) {
        Validate.notNull(classNames);
        attributes.put("class", StringUtil.join(classNames, " "));
        return this;
    }

    /**
     *hasClass如果此元素具有一个类的测试。大小写不敏感。
     * @param className name of class to check for
     * @return true if it does, false if not
     */
    public boolean hasClass(String className) {
        Set<String> classNames = classNames();
        for (String name : classNames) {
            if (className.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    /**
     addClass将一个类名称添加到此元素 {@code 类} 属性。
     @param className class name to add
     @return this element
     */
    public Element addClass(String className) {
        Validate.notNull(className);

        Set<String> classes = classNames();
        classes.add(className);
        classNames(classes);

        return this;
    }

    /**
     removeClass删除此元素 {@code 类} 属性类的名称。
     @param className class name to remove
     @return this element
     */
    public Element removeClass(String className) {
        Validate.notNull(className);

        Set<String> classes = classNames();
        classes.remove(className);
        classNames(classes);

        return this;
    }

    /**
     toggleClass切换此元素 {@code 类} 属性上的类名称: 如果存在，请删除它;否则添加它。
     @param className class name to toggle
     @return this element
     */
    public Element toggleClass(String className) {
        Validate.notNull(className);

        Set<String> classes = classNames();
        if (classes.contains(className))
            classes.remove(className);
        else
            classes.add(className);
        classNames(classes);

        return this;
    }

    /**
     * val获取窗体元素 (输入、 文本等) 的值。
     * @return the value of the form element, or empty string if not set.
     */
    public String val() {
        if (tagName().equals("textarea"))
            return text();
        else
            return attr("value");
    }

    /**
     * 设置窗体元素 (输入、 文本等) 的值。
     * @param value value to set
     * @return this element (for chaining)
     */
    public Element val(String value) {
        if (tagName().equals("textarea"))
            text(value);
        else
            attr("value", value);
        return this;
    }

    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        if (accum.length() > 0 && out.prettyPrint()
                && (tag.formatAsBlock() || (parent() != null && parent().tag().formatAsBlock()) || out.outline()) )
            //换行并调整缩进
            indent(accum, depth, out);
        accum
                .append("<")
                .append(tagName());
        attributes.html(accum, out);

        if (childNodes.isEmpty() && tag.isSelfClosing())
            accum.append(" />");
        else
            accum.append(">");
    }

    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
        if (!(childNodes.isEmpty() && tag.isSelfClosing())) {
            if (out.prettyPrint() && (!childNodes.isEmpty() && (
                    tag.formatAsBlock() || (out.outline() && (childNodes.size()>1 || (childNodes.size()==1 && !(childNodes.get(0) instanceof TextNode))))
            )))
                //换行并调整缩进
                indent(accum, depth, out);
            accum.append("</").append(tagName()).append(">");
        }
    }

    /**
     *html检索元素的内部 HTML。例如，关于张 @code {<div>} 有一个空至 @code {<p>}，将返回</div>
     * {@code <p></p>}. (Whereas {@link #outerHtml()} would return {@code <div><p></p></div>}.)
     *
     * @return String of HTML.
     * @see #outerHtml()
     */
    public String html() {
        StringBuilder accum = new StringBuilder();
        html(accum);
        return accum.toString().trim();
    }

    private void html(StringBuilder accum) {
        for (Node node : childNodes)
            node.outerHtml(accum);
    }

    /**
     * 设置此元素的内部 HTML。首先清除现有的 HTML。
     * @param html HTML to parse and set into this element
     * @return this element
     * @see #append(String)
     */
    public Element html(String html) {
        empty();
        append(html);
        return this;
    }

    public String toString() {
        return outerHtml();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        // todo: 不是非常有用的链接地址信息
        int result = super.hashCode();
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    @Override
    public Element clone() {
        Element clone = (Element) super.clone();
        clone.classNames = null; // 推导了对第一次打击，否则获取一个指针，源类名
        return clone;
    }
}
