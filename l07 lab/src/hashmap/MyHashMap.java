package hashmap;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class MyHashMap<K, V> implements Map61B<K, V> {

    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    private Collection<Node>[] buckets;
    private int size;
    private double loadFactor;

    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, 0.75);
    }

    public MyHashMap(int initialCapacity, double loadFactor) {
        this.buckets = new Collection[initialCapacity];
        this.size = 0;
        this.loadFactor = loadFactor;
    }

    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    @Override
    public void put(K key, V value) {
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        buckets[index].add(new Node(key, value));
        size++;
        if ((double) size / buckets.length > loadFactor) {
            resize();
        }
    }

    @Override
    public V get(K key) {
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            return null;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            return false;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        buckets = new Collection[buckets.length];
        size = 0;
    }

    private void resize() {
        Collection<Node>[] oldBuckets = buckets;
        buckets = new Collection[oldBuckets.length * 2];
        size = 0;
        for (Collection<Node> bucket : oldBuckets) {
            if (bucket != null) {
                for (Node node : bucket) {
                    put(node.key, node.value);
                }
            }
        }
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
