public class HashTable implements SymbolTable {

    private static class Node {
        int key;
        int value;
        Node next;

        Node(int key, int value, Node next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private static final double LOAD_FACTOR = 0.75;

    private Node[] table;
    private int size;

    public HashTable() {
        this(16);
    }

    public HashTable(int capacity) {
        int cap = 1;
        while (cap < capacity) {
            cap *= 2;
        }
        table = new Node[cap];
    }

    protected int hash(int key) {
        int h = Integer.hashCode(key);
        return h ^ (h >>> 16);
    }

    private int indexFor(int key, int length) {
        return hash(key) & (length - 1);
    }

    @Override
    public void insert(int key, int value) {
        int idx = indexFor(key, table.length);
        Node cur = table[idx];
        while (cur != null) {
            if (cur.key == key) {
                cur.value = value;
                return;
            }
            cur = cur.next;
        }
        table[idx] = new Node(key, value, table[idx]);
        size++;
        if (size > table.length * LOAD_FACTOR) {
            resize();
        }
    }

    @Override
    public int search(int key) {
        Node cur = table[indexFor(key, table.length)];
        while (cur != null) {
            if (cur.key == key) {
                return cur.value;
            }
            cur = cur.next;
        }
        return -1;
    }

    @Override
    public boolean delete(int key) {
        int idx = indexFor(key, table.length);
        Node cur = table[idx];
        Node prev = null;
        while (cur != null) {
            if (cur.key == key) {
                if (prev == null) {
                    table[idx] = cur.next;
                } else {
                    prev.next = cur.next;
                }
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    public int capacity() {
        return table.length;
    }

    public int longestChain() {
        int max = 0;
        for (Node head : table) {
            int len = 0;
            for (Node cur = head; cur != null; cur = cur.next) {
                len++;
            }
            if (len > max) {
                max = len;
            }
        }
        return max;
    }

    private void resize() {
        Node[] newTable = new Node[table.length * 2];
        for (Node head : table) {
            Node cur = head;
            while (cur != null) {
                Node next = cur.next;
                int idx = indexFor(cur.key, newTable.length);
                cur.next = newTable[idx];
                newTable[idx] = cur;
                cur = next;
            }
        }
        table = newTable;
    }
}
