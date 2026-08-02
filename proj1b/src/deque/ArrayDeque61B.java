package deque;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements Deque61B using a circular array with resizing.
 * @param <T> the type of elements held in this deque
 */
public class ArrayDeque61B<T> implements Deque61B<T>, Iterable<T> {

    private T[] items;
    private int size;
    private int head; // index of the first element
    private int tail; // index after the last element (where next addLast goes)

    private static final int INITIAL_CAPACITY = 8;

    /** Creates an empty deque. */
    @SuppressWarnings("unchecked")
    public ArrayDeque61B() {
        items = (T[]) new Object[INITIAL_CAPACITY];
        size = 0;
        head = 0;
        tail = 0;
    }

    @Override
    public void addFirst(T x) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        head = (head - 1 + items.length) % items.length;
        items[head] = x;
        size++;
    }

    @Override
    public void addLast(T x) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[tail] = x;
        tail = (tail + 1) % items.length;
        size++;
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T x = items[head];
        items[head] = null;
        head = (head + 1) % items.length;
        size--;
        if (items.length > 8 && size * 4 < items.length) {
            resize(items.length / 2);
        }
        return x;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        tail = (tail - 1 + items.length) % items.length;
        T x = items[tail];
        items[tail] = null;
        size--;
        if (items.length > 8 && size * 4 < items.length) {
            resize(items.length / 2);
        }
        return x;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return items[(head + index) % items.length];
    }

    @Override
    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return getRecursiveHelper(0, index);
    }

    private T getRecursiveHelper(int current, int target) {
        if (current == target) {
            return items[(head + current) % items.length];
        }
        return getRecursiveHelper(current + 1, target);
    }

    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(get(i));
        }
        return list;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public T next() {
                T item = get(pos);
                pos++;
                return item;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayDeque61B<?> other)) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            T thisItem = this.get(i);
            Object otherItem = other.get(i);
            if (thisItem == null) {
                if (otherItem != null) {
                    return false;
                }
            } else if (!thisItem.equals(otherItem)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        T[] newItems = (T[]) new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newItems[i] = items[(head + i) % items.length];
        }
        items = newItems;
        head = 0;
        tail = size;
    }
}
