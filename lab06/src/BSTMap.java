import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap <K extends Comparable<K>,V> implements Map61B<K, V> {
    private class Node {
            private K key;
            private V value;
            private Node left;
            private Node right;

            // config function
            public Node (K key,V value) {
                this.key = key;
                this.value = value;
                this.left = null;
                this.right = null;
            }
    }

    private Node root;
    private int size;

    public BSTMap () {
        this.root = null;
        this.size = 0;
    }

    // from easy to complex

    @Override
            public int size () {
        return size;
    }
    @Override
            public void clear() {
        root = null;
        size = 0;
    }
    @Override
            public void put(K key, V value) {
        root = put_helper(root,key,value);
    }

    // trace back to modify node , we possibly need to add a left son or right son we need to change its value.

    private Node put_helper(Node node,K key,V value) {
        if(node == null) {
            size++;
            return new Node(key,value);
        }

        int cmp = key.compareTo(node.key);
        if(cmp < 0) {
            node.left = put_helper(node.left,key,value);
        } else if (cmp > 0) {
            node.right = put_helper(node.right,key,value);
        } else {
            node.value = value;
        }
        return node;
    }

    @Override
    public V get (K key) {
        Node node = get_helper(root,key);
        return node == null ? null : node.value;
    }

    private Node get_helper (Node node,K key) {
        if(node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if(cmp > 0) {
            return get_helper(node.right,key);
        } else if (cmp < 0) {
            return get_helper(node.left,key);
        } else {
            return node;
        }
    }


    public boolean containsKey(K key) {
        return get_helper(root,key) != null;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Iterator<K> iterator() {
        return new BSTMapIterator();
    }

    // 迭代器内部类
    private class BSTMapIterator implements Iterator<K> {
        private Stack<Node> stack;

        public BSTMapIterator() {
            stack = new Stack<>();
            // 从根节点开始，把最左边的路径全部压入栈
            pushLeft(root);
        }

        // 辅助方法：将节点及其所有左子节点压入栈
        private void pushLeft(Node node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            // 弹出栈顶元素
            Node node = stack.pop();
            // 如果有右子树，将右子树的最左路径压入栈
            pushLeft(node.right);
            return node.key;
        }
    }
}