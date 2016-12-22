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
    private static final String INDICES = "Indices";
    private final Map<String, Indices> attributes;
    private final Map<Integer, E> elements;

    public DoubleMapQueue() {
        this(new HashMap<>(), new HashMap<>());
    }

    public DoubleMapQueue(Map<String, Indices> attributes, Map<Integer, E> elements) {
        this.attributes = attributes;
        this.elements = elements;

        this.attributes.put(INDICES, new Indices());
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

        while (!attributes.isEmpty()) {
            Indices indices = attributes.getOrDefault(INDICES, new Indices());
            //System.out.println("offer: indices=" + indices);
            if (attributes.remove(INDICES, indices)) {
                indices = indices.enqueue();
                attributes.put(INDICES, indices);
                elements.put(indices.getBackIndex(), e);
                return true;
            }
        }
        return false;
    }

    public E peek() {
        if (attributes.isEmpty() || elements.isEmpty()) {
            return null;
        }

        while (!attributes.isEmpty()) {
            //System.out.println(" peek: attributes=" + attributes + "\telements=" + elements);
            Indices indices = attributes.getOrDefault(INDICES, new Indices());
            //System.out.println(" peek: indices=" + indices);
            E e = elements.get(indices.getFrontIndex());
            //System.out.println(" peek: e=" + e);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    public E poll() {
        if (attributes.isEmpty() || elements.isEmpty()) {
            return null;
        }

        while (!attributes.isEmpty()) {
            Indices indices = attributes.getOrDefault(INDICES, new Indices());
            //System.out.println(" poll: indices=" + indices);
            if (attributes.remove(INDICES, indices)) {
                final int index = indices.getFrontIndex();
                indices = indices.dequeue();
                attributes.put(INDICES, indices);
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
        attributes.clear();
        elements.clear();
    }

    public boolean contains(Object o) {
        return (0 <= getIndex(o));
    }

    public boolean isEmpty() {
        return size() < 1;
    }

    public Iterator<E> iterator() {
        return new InternalIterator(new ArrayList<Integer>(elements.keySet()));
    }

    public boolean remove(Object o) {
        final int index = getIndex(o);
        if (0 <= index) {
            while (!attributes.isEmpty()) {
                Indices indices = attributes.getOrDefault(INDICES, new Indices());
                //System.out.println("remove: indices=" + indices);
                if (attributes.remove(INDICES, indices)) {
                    indices = indices.remove(index);
                    attributes.put(INDICES, indices);
                    elements.remove(index);
                    return true;
                }
            }
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
        if (!attributes.isEmpty()) {
            return attributes.getOrDefault(INDICES, new Indices()).span();
        }
        return 0;
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

    public class Indices {
        private int front = 0;

        private int back = 0;

        Indices() {
            this(0, 0);
        }

        private Indices(int front, int back) {
            this.front = front;
            this.back = back;
        }

        int getFrontIndex() {
            return front;
        }

        int getBackIndex() {
            if (front < back) {
                return back - 1;
            }
            return front;
        }

        int span() {
            return back - front;
        }

        Indices enqueue() {
            return new Indices(front, back + 1);
        }

        Indices dequeue() {
            if (back <= front + 1) {
                return new Indices();
            }
            return new Indices(front + 1, back);
        }

        Indices remove(int index) {
            if (index == front) {
                return dequeue();
            }
            if (index == back) {
                return new Indices(front, back - 1);
            }
            return (Indices) clone();
        }

        protected Object clone() {
            return new Indices(front, back);
        }

        public boolean equals(Object obj) {
            Indices other = (Indices) obj;
            if (other != null) {
                return front == other.front && back == other.back;
            }
            return false;
        }

        public String toString() {
            return "(" + front + "," + back + ")";
        }
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
