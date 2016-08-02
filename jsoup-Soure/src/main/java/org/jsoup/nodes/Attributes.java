package org.jsoup.nodes;

import org.jsoup.helper.Validate;

import java.util.*;

/**
 * 元素的属性。
 *<p>* 属性被视为一张地图: 可以只有一个与属性键关联的值。
 *<p>* 属性键和值进行比较厚脸皮，做案例，向正常化了钥匙
 * 小写。
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class Attributes implements Iterable<Attribute>, Cloneable {
    protected static final String dataPrefix = "data-";

    private LinkedHashMap<String, Attribute> attributes = null;
    // 要保留插入顺序的链接的哈希映射。
    // null 被默认，由于这么多元素没有属性 — — 保存好块的内存
    /**
     通过键获取属性值。
     @param key the attribute key
     @如果返回的属性值设置;或者是空字符串，如果不是设置对抗的。
     @see #hasKey(String)
     */
    public String get(String key) {
        Validate.notEmpty(key);

        if (attributes == null)
            return "";

        Attribute attr = attributes.get(key.toLowerCase());
        return attr != null ? attr.getValue() : "";
    }

    /**
     设置一个新的属性，或替换一个现有的密钥。
     @param key attribute key
     @param value attribute value
     */
    public void put(String key, String value) {
        Attribute attr = new Attribute(key, value);
        put(attr);
    }

    /**
     设置一个新的属性，或替换一个现有的密钥。
     @param attribute attribute
     */
    public void put(Attribute attribute) {
        Validate.notNull(attribute);
        if (attributes == null)
            attributes = new LinkedHashMap<String, Attribute>(2);
        attributes.put(attribute.getKey(), attribute);
    }

    /**
     按键删除的属性。
     @param key attribute key to remove
     */
    public void remove(String key) {
        Validate.notEmpty(key);
        if (attributes == null)
            return;
        attributes.remove(key.toLowerCase());
    }

    /**
     如果这些属性包含与此键属性的测试。
     @param key key to check for
     @return true if key exists, false otherwise
     */
    public boolean hasKey(String key) {
        return attributes != null && attributes.containsKey(key.toLowerCase());
    }

    /**
     在这一组得到属性的数。
     @return size
     */
    public int size() {
        if (attributes == null)
            return 0;
        return attributes.size();
    }

    /**
     从传入设置到此集添加的所有属性。
     @param incoming attributes to add to these attributes.
     */
    public void addAll(Attributes incoming) {
        if (incoming.size() == 0)
            return;
        if (attributes == null)
            attributes = new LinkedHashMap<String, Attribute>(incoming.size());
        attributes.putAll(incoming.attributes);
    }

    public Iterator<Attribute> iterator() {
        return asList().iterator();
    }

    /**
     获取的特性列表，作为迭代。请不要修改此视图中，通过属性的键作为变化
     键不会确认包含集合中。
     @return an view of the attributes as a List.
     */
    public List<Attribute> asList() {
        if (attributes == null)
            return Collections.emptyList();

        List<Attribute> list = new ArrayList<Attribute>(attributes.size());
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            list.add(entry.getValue());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * 检索是 HTML5 的自定义数据属性; 属性筛选的视图那就是，属性与键
     * starting with {@code data-}.
     * @return map of custom data attributes.
     */
    public Map<String, String> dataset() {
        return new Dataset();
    }

    /**
     得到这些属性的 HTML 表示的形式。
     @return HTML
     */
    public String html() {
        StringBuilder accum = new StringBuilder();
        html(accum, (new Document("")).outputSettings()); // 输出设置有点臭，但很少使用这个 html()
        return accum.toString();
    }

    void html(StringBuilder accum, Document.OutputSettings out) {
        if (attributes == null)
            return;

        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            Attribute attribute = entry.getValue();
            accum.append(" ");
            attribute.html(accum, out);
        }
    }

    public String toString() {
        return html();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attributes)) return false;

        Attributes that = (Attributes) o;

        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return attributes != null ? attributes.hashCode() : 0;
    }

    @Override
    public Attributes clone() {
        if (attributes == null)
            return new Attributes();

        Attributes clone;
        try {
            clone = (Attributes) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.attributes = new LinkedHashMap<String, Attribute>(attributes.size());
        for (Attribute attribute: this)
            clone.attributes.put(attribute.getKey(), attribute.clone());
        return clone;
    }

    private class Dataset extends AbstractMap<String, String> {

        private Dataset() {
            if (attributes == null)
                attributes = new LinkedHashMap<String, Attribute>(2);
        }

        public Set<Entry<String, String>> entrySet() {
            return new EntrySet();
        }

        @Override
        public String put(String key, String value) {
            String dataKey = dataKey(key);
            String oldValue = hasKey(dataKey) ? attributes.get(dataKey).getValue() : null;
            Attribute attr = new Attribute(dataKey, value);
            attributes.put(dataKey, attr);
            return oldValue;
        }

        private class EntrySet extends AbstractSet<Entry<String, String>> {
            public Iterator<Entry<String, String>> iterator() {
                return new DatasetIterator();
            }

            public int size() {
                int count = 0;
                Iterator iter = new DatasetIterator();
                while (iter.hasNext())
                    count++;
                return count;
            }
        }

        private class DatasetIterator implements Iterator<Entry<String, String>> {
            private Iterator<Attribute> attrIter = attributes.values().iterator();
            private Attribute attr;
            public boolean hasNext() {
                while (attrIter.hasNext()) {
                    attr = attrIter.next();
                    if (attr.isDataAttribute()) return true;
                }
                return false;
            }

            public Entry<String, String> next() {
                return new Attribute(attr.getKey().substring(dataPrefix.length()), attr.getValue());
            }

            public void remove() {
                attributes.remove(attr.getKey());
            }
        }
    }

    private static String dataKey(String key) {
        return dataPrefix + key;
    }
}
