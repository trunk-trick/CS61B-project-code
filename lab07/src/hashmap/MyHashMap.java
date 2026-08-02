package hashmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *  A hash table-backed Map implementation.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size;
    private double loadFactor;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialCapacity.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialCapacity initial size of backing array
     * @param loadFactor maximum load factor
     */
    public MyHashMap(int initialCapacity, double loadFactor) {
        this.size = 0;
        this.loadFactor = loadFactor;
        this.buckets = (Collection<Node>[]) new Collection[initialCapacity];
        for (int i = 0; i < initialCapacity; i++) {
            this.buckets[i] = createBucket();
        }
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *  Note that that this is referring to the hash table bucket itself,
     *  not the hash map itself.
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    @Override
    public void put(K key, V value) {
        int bucketIndex = getBucketIndex(key);
        Collection<Node> bucket = buckets[bucketIndex];

        // Check if key already exists in this bucket
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }

        // Key not found, add new node
        bucket.add(new Node(key, value));
        size++;

        // Check if resize is needed
        if ((double) size / buckets.length > loadFactor) {
            resize(buckets.length * 2);
        }
    }

    @Override
    public V get(K key) {
        int bucketIndex = getBucketIndex(key);
        Collection<Node> bucket = buckets[bucketIndex];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        int bucketIndex = getBucketIndex(key);
        Collection<Node> bucket = buckets[bucketIndex];

        for (Node node : bucket) {
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
        size = 0;
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                keys.add(node.key);
            }
        }
        return keys;
    }

    @Override
    public V remove(K key) {
        int bucketIndex = getBucketIndex(key);
        Collection<Node> bucket = buckets[bucketIndex];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                V oldValue = node.value;
                bucket.remove(node);
                size--;
                return oldValue;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    private int getBucketIndex(K key) {
        int hash = key.hashCode();
        return Math.floorMod(hash, buckets.length);
    }

    private void resize(int newCapacity) {
        Collection<Node>[] oldBuckets = buckets;
        buckets = (Collection<Node>[]) new Collection[newCapacity];
        for (int i = 0; i < newCapacity; i++) {
            buckets[i] = createBucket();
        }
        size = 0;

        for (Collection<Node> bucket : oldBuckets) {
            for (Node node : bucket) {
                put(node.key, node.value);
            }
        }
    }

    private class MyHashMapIterator implements Iterator<K> {
        private int bucketIndex;
        private Iterator<Node> bucketIterator;

        MyHashMapIterator() {
            bucketIndex = 0;
            bucketIterator = buckets[0].iterator();
        }

        @Override
        public boolean hasNext() {
            if (bucketIterator.hasNext()) {
                return true;
            }
            // Move to next non-empty bucket
            for (int i = bucketIndex + 1; i < buckets.length; i++) {
                if (!buckets[i].isEmpty()) {
                    bucketIndex = i;
                    bucketIterator = buckets[i].iterator();
                    return bucketIterator.hasNext();
                }
            }
            return false;
        }

        @Override
        public K next() {
            if (bucketIterator.hasNext()) {
                return bucketIterator.next().key;
            }
            // Move to next non-empty bucket
            for (int i = bucketIndex + 1; i < buckets.length; i++) {
                if (!buckets[i].isEmpty()) {
                    bucketIndex = i;
                    bucketIterator = buckets[i].iterator();
                    return bucketIterator.next().key;
                }
            }
            return null;
        }
    }
}
