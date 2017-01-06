package io.pivotal.bacon;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mdodge on 19.12.16.
 * <p>
  * <table BORDER CELLPADDING=3 CELLSPACING=1>
  *  <tr>
  *    <td></td>
  *    <td ALIGN=CENTER><em>Throws exception</em></td>
  *    <td ALIGN=CENTER><em>Special value</em></td>
  *    <td ALIGN=CENTER><em>Blocks</em></td>
  *    <td ALIGN=CENTER><em>Times out</em></td>
  *  </tr>
  *  <tr>
  *    <td><b>Insert</b></td>
  *    <td>{@link #add add(e)}</td>
  *    <td>{@link #offer offer(e)}</td>
  *    <td>{@link #put put(e)}</td>
  *    <td>{@link #offer(Object, long, TimeUnit) offer(e, time, unit)}</td>
  *  </tr>
  *  <tr>
  *    <td><b>Remove</b></td>
  *    <td>{@link #remove remove()}</td>
  *    <td>{@link #poll poll()}</td>
  *    <td>{@link #take take()}</td>
  *    <td>{@link #poll(long, TimeUnit) poll(time, unit)}</td>
  *  </tr>
  *  <tr>
  *    <td><b>Examine</b></td>
  *    <td>{@link #element element()}</td>
  *    <td>{@link #peek peek()}</td>
  *    <td><em>not applicable</em></td>
  *    <td><em>not applicable</em></td>
  *  </tr>
  * </table>
 */
public class DoubleMapQueue<E>  extends AbstractQueue<E> implements BlockingQueue<E> {
    private static final String INDICES = "Indices";

    private final Map<String, Indices> attributes;

    private final Map<Integer, E> elements;

    private final int capacity;

    private final AtomicInteger count = new AtomicInteger(0);

    private final ReentrantLock takeLock = new ReentrantLock();

    private final Condition notEmpty = takeLock.newCondition();

    private final ReentrantLock putLock = new ReentrantLock();

    private final Condition notFull = putLock.newCondition();

    public DoubleMapQueue() {
        this(new HashMap<>(), new HashMap<>());
    }

    public DoubleMapQueue(Map<String, Indices> attributes, Map<Integer, E> elements) { this(attributes, elements, Integer.MAX_VALUE); }

    public DoubleMapQueue(Map<String, Indices> attributes, Map<Integer, E> elements, int capacity) {
        this.capacity = capacity;
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

    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        }
        finally {
            takeLock.unlock();
        }
    }

    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        }
        finally {
            putLock.unlock();
        }
    }

    private E get() {
        Indices indices = attributes.getOrDefault(INDICES, new Indices());
        //System.out.println(" peek: indices=" + indices);
        return elements.get(indices.getFrontIndex());
    }

    private E dequeue() {
        Indices indices = attributes.getOrDefault(INDICES, new Indices());
        //System.out.println(" poll: indices=" + indices);
        if (attributes.remove(INDICES, indices)) {
            final int index = indices.getFrontIndex();
            indices = indices.dequeue();
            attributes.put(INDICES, indices);
            return elements.remove(index);
        }
        return null;
    }

    private boolean enqueue(E e) {
        Indices indices = attributes.getOrDefault(INDICES, new Indices());
        //System.out.println("enqueue: indices=" + indices);
        if (attributes.remove(INDICES, indices)) {
            indices = indices.enqueue();
            attributes.put(INDICES, indices);
            elements.put(indices.getBackIndex(), e);
            return true;
        }
        return false;
    }

    // Summary of BlockingQueue methods
    //           Throws exception    Special value      Blocks         Times out
    // Insert        add(e)              offer(e)       put(e)     offer(e, time, unit)
    // Remove        remove()            poll()         take()      poll(time, unit)
    // Examine       element()           peek()    not applicable    not applicable
    public boolean add(E e) {
        if (e == null) {
            throw new NullPointerException("Null element");
        }
        if (!offer(e)) {
            throw new IllegalStateException("No space available");
        }
        return true;
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, capacity);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException("Null destination");
        }
        if (c == this) {
            throw new IllegalArgumentException("Identical source and destination");
        }

        int count = 0;
        for (int i = 0; i < maxElements; ++i) {
            E e = poll();
            if (e == null) {
                break;
            }

            ++count;
            c.add(e);
        }
        return count;
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

        final AtomicInteger count = this.count;
        if (capacity <= count.get()) {
            return false;
        }
        int c = -1;

        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                if (enqueue(e)) {
                    c = count.getAndIncrement();
                    if (c + 1 < capacity) {
                        notFull.signal();
                    }
                }
            }
        } finally {
            putLock.unlock();
        }

        if (c == 0) {
            signalNotEmpty();
        }

        return (0 <= c);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException("Null element");
        }

        final AtomicInteger count = this.count;
        int c = -1;

        final ReentrantLock putLock = this.putLock;
        putLock.lockInterruptibly();
        try {
            long nanos = unit.toNanos(timeout);
            for (; ; ) {
                if (count.get() < capacity) {
                    if (enqueue(e)) {
                        c = count.getAndIncrement();
                        if (c + 1 < capacity)
                            notFull.signal();
                        break;
                    }
                }
                if (nanos <= 0) {
                    return false;
                }
                try {
                    nanos = notFull.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notFull.signal(); // propagate to a non-interrupted thread
                    throw ie;
                }
            }
        } finally {
            putLock.unlock();
        }

        if (c == 0) {
            signalNotEmpty();
        }

        return true;
    }

    public E peek() {
        if (attributes.isEmpty() || elements.isEmpty()) {
            return null;
        }

        final AtomicInteger count = this.count;
        if (count.get() < 0) {
            return null;
        }

        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            return get();
        } finally {
            takeLock.unlock();
        }
    }

    public E poll() {
        if (attributes.isEmpty() || elements.isEmpty()) {
            return null;
        }

        final AtomicInteger count = this.count;
        if (count.get() < 0) {
            return null;
        }
        int c = -1;

        E e = null;
        final ReentrantLock takeLock= this.takeLock;
        takeLock.lock();
        try {
            if (0 < count.get()) {
                e = dequeue();
                if (e != null) {
                    c = count.getAndDecrement();
                    if (1 < c) {
                        notEmpty.signal();
                    }
                }
            }
        } finally {
            takeLock.unlock();
        }
        return e;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (attributes.isEmpty() || elements.isEmpty()) {
            return null;
        }

        final AtomicInteger count = this.count;
        int c = -1;

        E e = null;
        final ReentrantLock takeLock= this.takeLock;
        takeLock.lockInterruptibly();
        try {
            long nanos = unit.toNanos(timeout);
            for (;;){
                if (0 < count.get()) {
                    e = dequeue();
                    if (e != null) {
                        c = count.getAndDecrement();
                        if (1 < c) {
                            notEmpty.signal();
                        }
                        break;
                    }
                }
                if (nanos <= 0) {
                    return null;
                }
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal();
                    throw ie;
                }
            }
        } finally {
            takeLock.unlock();
        }

        if (c == capacity) {
            signalNotFull();
        }

        return e;
    }

    public void put(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException("Null element");
        }

        final AtomicInteger count = this.count;
        int c = -1;

        final ReentrantLock putLock = this.putLock;
        putLock.lockInterruptibly();
        try {
            try {
                while (count.get() == capacity) {
                    notFull.await();
                }
            } catch (InterruptedException ie) {
                notFull.signal();
                throw ie;
            }

            // TODO: Should this accommodate the attributes being locked?
            enqueue(e);
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }

        if (c == 0) {
            signalNotEmpty();
        }
    }

    public int remainingCapacity() {
        return capacity - count.get();
    }

    public E remove() {
        final E e = poll();
        if (e == null) {
            throw new NoSuchElementException("Empty queue");
        }
        return e;
    }

    public E take() throws InterruptedException {
        final AtomicInteger count = this.count;
        int c = -1;

        E e = null;
        final ReentrantLock putLock = this.putLock;
        putLock.lockInterruptibly();
        try {
            try {
                while (count.get() == 0) {
                    notEmpty.await();
                }
            } catch (InterruptedException ie) {
                notFull.signal();
                throw ie;
            }

            e = dequeue();
            c = count.getAndDecrement();
            if (1 < c) {
                notEmpty.signal();
            }
        } finally {
            putLock.unlock();
        }

        if (c == capacity) {
            signalNotFull();
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
        count.set(0);
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
        return count.get();
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
