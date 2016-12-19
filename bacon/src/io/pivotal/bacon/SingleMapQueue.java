package io.pivotal.bacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Created by mdodge on 15/12/2016.
 */
public class SingleMapQueue<E> implements Queue<E> {
    static final int NUMBER_OF_RETRIES = 5;

    final Map<Integer, E> map;

    public SingleMapQueue() {
        this(new HashMap<>());
    }

    public SingleMapQueue(Map<Integer, E> map) {
        this.map = map;
    }

    private int getFirstIndex() {
        if (!map.isEmpty()) {
            List<Integer> indices = new ArrayList<Integer>(map.keySet());
            indices.sort(Comparator.naturalOrder());
            return indices.get(0);
        }
        return 0;
    }

    private int getLastIndex() {
        if (!map.isEmpty()) {
            List<Integer> indices = new ArrayList<Integer>(map.keySet());
            indices.sort(Comparator.naturalOrder());
            return indices.get(indices.size() - 1) + 1;
        }
        return 0;
    }

    private int getIndex(Object o) {
        for (Integer key : map.keySet()) {
            if (map.get(key) == o) {
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

        // There is a race condition here in a multi-processing
        // environment where two adders both get the same last
        // index.
        for (int i = 0; i < NUMBER_OF_RETRIES; ++i) {
            if (null == map.putIfAbsent(getLastIndex(), e)) {
                return true;
            }
        }
        return false;
    }

    public E peek() {
        if (!isEmpty()) {
            return map.get(getFirstIndex());
        }
        return null;
    }

    public E poll() {
        if (!isEmpty()) {
            // There is a race condition here in a multi-processing
            // environment where two removers both get the same first
            // index.
            for (int i = 0; i < NUMBER_OF_RETRIES; ++i) {
                final int index = getFirstIndex();
                final E e = map.get(index);
                if (map.remove(index, e)) {
                    return e;
                }
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
        map.clear();
    }

    public boolean contains(Object o) {
        return (0 <= getIndex(o));
    }

    public boolean isEmpty() {
        return (getFirstIndex() >= getLastIndex());
    }

    public Iterator<E> iterator() {
        return new InternalIterator(new ArrayList<Integer>(map.keySet()));
    }

    public boolean remove(Object o) {
        final int index = getIndex(o);
        if (0 <= index) {
            map.remove(index);
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
        for (Integer key : map.keySet()) {
            a[i++] = (E) map.get(key);
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
            return (E) map.get(indices.remove(0));
        }
    }
}
