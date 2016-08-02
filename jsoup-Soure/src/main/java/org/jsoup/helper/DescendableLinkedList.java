package org.jsoup.helper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 提供一个降序的迭代器和其他 1.6 的方法允许上 1.5 JRE 的支持。
 */
public class DescendableLinkedList<E> extends LinkedList<E> {

    /**
     * 创建新的 DescendableLinkedList。
     */
    public DescendableLinkedList() {
        super();
    }

    /**
     *将新元素添加到列表的开头。
     * @param e element to add
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * 看看最后一个元素，如果有的话。
     * @return the last element, or null
     */
    public E peekLast() {
        return size() == 0 ? null : getLast();
    }

    /**
     * 移除并返回的最后一个元素，如果有的话
     * @return the last element, or null
     */
    public E pollLast() {
        return size() == 0 ? null : removeLast();
    }

    /**
     * 开始获取一个迭代器，开始和结束的列表和作品。
     * @return an iterator that starts and the end of the list and works towards the start.
     */
    public Iterator<E> descendingIterator() {
        return new DescendingIterator<E>(size());
    }

    private class DescendingIterator<E> implements Iterator<E> {
        private final ListIterator<E> iter;

        @SuppressWarnings("unchecked")
        private DescendingIterator(int index) {
            iter = (ListIterator<E>) listIterator(index);
        }

        /**
         *检查列表中是否有另一个元素。
         * @return if another element
         */
        public boolean hasNext() {
            return iter.hasPrevious();
        }

        /**
         * 得到的下一个元素。
         * @return the next element.
         */
        public E next() {
            return iter.previous();
        }

        /**
         * 删除当前元素。
         */
        public void remove() {
            iter.remove();
        }
    }
}
