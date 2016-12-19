package io.pivotal.bacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Created by mdodge on 19.12.16.
 */
public class DoubleMapQueue<E> implements Queue<E> {
    private static final String FIRST = "First";

    private static final String LAST = "Last";

    private final Map<String, Integer> indices;

    private final Map<Integer, E> elements;

    public DoubleMapQueue() {
        this(new HashMap<>(), new HashMap<>());
    }

    public DoubleMapQueue(Map<String, Integer> indices, Map<Integer, E> elements) {
        this.indices = indices;
        this.elements = elements;
    }

    private int getFirstIndex() {
        return indices.getOrDefault(FIRST, 0);
    }

    private int getLastIndex() {
        return indices.getOrDefault(LAST, -1) + 1;
    }

    private int getIndex(Object o) {
        for (Integer key : elements.keySet()) {
            if (elements.get(key) == o) {
                return key;
            }
        }
        return -1;
    }

    public boolean add(E e) {
        return offer(e);
    }

    public E element() {
        final E e = peek();
        if (e == null) {
            throw new NoSuchElementException("Empty queue");
        }
        return e;
    }

    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException("Null element");
        }

        if (indices.isEmpty() || elements.isEmpty()) {
            indices.put(LAST, 1);
            indices.put(FIRST, 0);
            elements.put(0, e);
            return true;
        }

        while (!indices.isEmpty()) {
            final Integer index = indices.get(LAST);
            if (indices.remove(LAST, index)) {
                indices.put(LAST, index + 1);
                elements.put(index + 1, e);
                return true;
            }
        }
        return false;
    }

    public E peek() {
        if (indices.isEmpty() || elements.isEmpty()) {
            return null;
        }

        return elements.get(indices.get(FIRST));
    }

    public E poll() {
        if (indices.isEmpty() || elements.isEmpty()) {
            return null;
        }

        while (!indices.isEmpty()) {
            final Integer index = indices.get(FIRST);
            if (indices.remove(FIRST, index)) {
                indices.put(FIRST, index + 1);
                return elements.remove(index);
            }
        }
        return null;
    }

    public E remove() {
        final E e = poll();
        if (e == null) {
            throw new NoSuchElementException("Empty queue");
        }
        return e;
    }

    public boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            if (!add(e)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        indices.clear();
        elements.clear();
    }

    public boolean contains(Object o) {
        return (0 <= getIndex(o));
    }

    public boolean isEmpty() {
        return (getFirstIndex() >= getLastIndex());
    }

    public Iterator<E> iterator() {
        return new InternalIterator(new ArrayList<Integer>(elements.keySet()));
    }

    public boolean remove(Object o) {
        final int index = getIndex(o);
        if (0 <= index) {
            elements.remove(index);

            if (index == getLastIndex()) {
                indices.put(LAST, index - 1);
            }
            if (index == getFirstIndex()) {
                indices.put(FIRST, index + 1);
            }

            return true;
        }
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("SingleMapQueue does not support removeAll");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("SingleMapQueue does not support retainAll");
    }

    public int size() {
        return getLastIndex() - getFirstIndex();
    }

    public Object[] toArray() {
        return toArray(new Object[0]);
    }

    public <E> E[] toArray(E[] a) {
        if (a.length < size()) {
            a = (E[]) new Object[size()];
        }

        int i = 0;
        for (Integer key : elements.keySet()) {
            a[i++] = (E) elements.get(key);
        }

        return a;
    }

    class InternalIterator<E> implements Iterator<E> {
        final List<Integer> indices;

        InternalIterator(List<Integer> indices) {
            this.indices = indices;
        }

        public boolean hasNext() {
            return !indices.isEmpty();
        }

        public E next() {
            return (E) elements.get(indices.remove(0));
        }
    }
}
