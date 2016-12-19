package io.pivotal.bacon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by mdodge on 19.12.16.
 */
class DoubleMapQueueTest {
    static final String uno = "uno";

    static final String dos = "dos";

    static final String tres = "tres";

    static final String cuatro = "cuatro";

    SingleMapQueue<String> queue;

    @BeforeEach
    public void setUp() {
        queue = new SingleMapQueue<String>();
    }

    @AfterEach
    public void tearDown() {
        queue = null;
    }

    @Test
    void addElementAndRemove() {
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        assertFalse(queue.contains(uno));

        queue.add(uno);
        queue.add(dos);
        queue.add(tres);

        assertEquals(3, queue.size());
        assertFalse(queue.isEmpty());
        assertTrue(queue.contains(uno));
        assertTrue(queue.contains(dos));
        assertTrue(queue.contains(tres));
        assertSame(uno, queue.element());

        final String s1 = queue.remove();
        assertEquals(2, queue.size());
        assertFalse(queue.isEmpty());
        assertFalse(queue.contains(uno));
        assertTrue(queue.contains(dos));
        assertTrue(queue.contains(tres));
        assertSame(uno, s1);
        assertSame(dos, queue.element());

        final String s2 = queue.remove();
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
        assertFalse(queue.contains(uno));
        assertFalse(queue.contains(dos));
        assertTrue(queue.contains(tres));
        assertSame(dos, s2);
        assertSame(tres, queue.element());

        final String s3 = queue.remove();
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        assertFalse(queue.contains(uno));
        assertFalse(queue.contains(dos));
        assertFalse(queue.contains(tres));
        assertSame(tres, s3);
    }

    @Test
    void offerPeekAndPoll() {
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        assertFalse(queue.contains(uno));

        queue.offer(uno);
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
        assertTrue(queue.contains(uno));
        assertSame(uno, queue.peek());

        String s = queue.poll();
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        assertFalse(queue.contains(uno));
        assertSame(uno, s);
    }

    @Test
    void containsAll() {
        List<String> present = Arrays.asList(uno, dos, tres);
        for (String s : present) {
            queue.offer(s);
        }
        assertTrue(queue.containsAll(present));

        List<String> absent = Arrays.asList(uno, dos, tres, cuatro);
        assertFalse(queue.containsAll(absent));
    }

    @Test
    void addAllAndClear() {
        queue.addAll(Arrays.asList(uno, dos, tres));
        assertEquals(3, queue.size());
        assertFalse(queue.isEmpty());
        queue.clear();
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    void iterator() {
        queue.addAll(Arrays.asList(uno, dos, tres));
        Iterator<String> iterator = queue.iterator();
        assertTrue(iterator.hasNext());
        assertSame(iterator.next(), uno);
        assertTrue(iterator.hasNext());
        assertSame(iterator.next(), dos);
        assertTrue(iterator.hasNext());
        assertSame(iterator.next(), tres);
        assertFalse(iterator.hasNext());
    }

    @Test
    void remove() {
        queue.addAll(Arrays.asList(uno, dos, tres));
        assertTrue(queue.remove(dos));
        assertTrue(queue.remove(tres));
        assertTrue(queue.remove(uno));
        assertFalse(queue.remove(cuatro));
    }

    @Test
    void removeAll() {
        try {
            queue.removeAll(Arrays.asList(uno, dos, tres));
            fail("removeAll is not supported");
        }
        catch (Exception e) {
            // PASS
        }
    }

    @Test
    void retainAll() {
        try {
            queue.retainAll(Arrays.asList(uno, dos, tres));
            fail("retainAll is not supported");
        }
        catch (Exception e) {
            // PASS
        }
    }

    @Test
    void toArray() {
        queue.addAll(Arrays.asList(uno, dos, tres));
        Object[] strings = queue.toArray(new String[1]);
        assertNotNull(strings);
        assertEquals(3, strings.length);
        assertSame(uno, strings[0]);
        assertSame(dos, strings[1]);
        assertSame(tres, strings[2]);
    }
}