package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * HTML 表单元素提供准备访问窗体字段/控件与它相关联。它还允许
 * 表单很容易被提交。
 */
public class FormElement extends Element {
    private final Elements elements = new Elements();

    /**
     * 创建新的、 独立的窗体元素。
     *
     此元素的 * @param 标记标记
     * @param baseUri 的基 URI
     * @param 属性初始属性
     */
    public FormElement(Tag tag, String baseUri, Attributes attributes) {
        super(tag, baseUri, attributes);
    }

    /**
     * 获取窗体的列表与此窗体关联的控件元素。
     * @return 与此元素关联的窗体控件。
     */
    public Elements elements() {
        return elements;
    }

    /**
     *此窗体中添加一个窗体控件元素。
     * @param 元素要添加窗体控件
     * @return 此窗体元素，为链接
     */
    public FormElement addElement(Element element) {
        elements.add(element);
        return this;
    }

    /**
     * 准备提交此表单。从窗体值设置的要求创建一个连接对象。你
     * 可以设置其他选项 (如用户代理、 超时、 饼干)，然后执行它。
     * @return 连接准备从这种形式的值。
    如果不能确定该窗体的绝对操作 URL 则抛出个 * @throws。确保你通过
    当解析文档的基 URI。
     */
    public Connection submit() {
        String action = hasAttr("action") ? absUrl("action") : baseUri();
        Validate.notEmpty(action, "Could not determine a form action URL for submit. Ensure you set a base URI when parsing.");
        Connection.Method method = attr("method").toUpperCase().equals("POST") ?
                Connection.Method.POST : Connection.Method.GET;

        Connection con = Jsoup.connect(action)
                .data(formData())
                .method(method);

        return con;
    }

    /**
     * 获取此表单提交的数据。返回的列表是数据的副本，并更改为的内容
     * 列表将不会反映在 dom。
     列表中的键的 * @return vals
     */
    public List<Connection.KeyVal> formData() {
        ArrayList<Connection.KeyVal> data = new ArrayList<Connection.KeyVal>();

        // 循环访问窗体控件元素和积累他们的价值观
        for (Element el: elements) {
            if (!el.tag().isFormSubmittable()) continue; // 内容是 submitable 的窗体的取值情况超集
            String name = el.attr("name");
            if (name.length() == 0) continue;

            if ("select".equals(el.tagName())) {
                Elements options = el.select("option[selected]");
                for (Element option: options) {
                    data.add(HttpConnection.KeyVal.create(name, option.val()));
                }
            } else {
                data.add(HttpConnection.KeyVal.create(name, el.val()));
            }
        }
        return data;
    }
}
