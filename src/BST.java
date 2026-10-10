import java.util.ArrayDeque;

public class BST implements SymbolTable {

    private static class Node {
        int key;
        int value;
        Node left;
        Node right;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node root;
    private int size;

    @Override
    public void insert(int key, int value) {
        if (root == null) {
            root = new Node(key, value);
            size++;
            return;
        }
        Node cur = root;
        while (true) {
            if (key < cur.key) {
                if (cur.left == null) {
                    cur.left = new Node(key, value);
                    size++;
                    return;
                }
                cur = cur.left;
            } else if (key > cur.key) {
                if (cur.right == null) {
                    cur.right = new Node(key, value);
                    size++;
                    return;
                }
                cur = cur.right;
            } else {
                cur.value = value;
                return;
            }
        }
    }

    @Override
    public int search(int key) {
        Node cur = root;
        while (cur != null) {
            if (key < cur.key) {
                cur = cur.left;
            } else if (key > cur.key) {
                cur = cur.right;
            } else {
                return cur.value;
            }
        }
        return -1;
    }

    @Override
    public boolean delete(int key) {
        Node parent = null;
        Node cur = root;
        while (cur != null && cur.key != key) {
            parent = cur;
            if (key < cur.key) {
                cur = cur.left;
            } else {
                cur = cur.right;
            }
        }
        if (cur == null) {
            return false;
        }

        if (cur.left != null && cur.right != null) {
            Node succParent = cur;
            Node succ = cur.right;
            while (succ.left != null) {
                succParent = succ;
                succ = succ.left;
            }
            cur.key = succ.key;
            cur.value = succ.value;
            parent = succParent;
            cur = succ;
        }

        Node child = (cur.left != null) ? cur.left : cur.right;
        if (parent == null) {
            root = child;
        } else if (parent.left == cur) {
            parent.left = child;
        } else {
            parent.right = child;
        }
        size--;
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    public int height() {
        if (root == null) {
            return 0;
        }
        ArrayDeque<Node> queue = new ArrayDeque<>();
        queue.add(root);
        int h = 0;
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Node n = queue.poll();
                if (n.left != null) queue.add(n.left);
                if (n.right != null) queue.add(n.right);
            }
            h++;
        }
        return h;
    }

    public boolean isValid() {
        if (root == null) {
            return size == 0;
        }
        ArrayDeque<Node> stack = new ArrayDeque<>();
        Node cur = root;
        boolean first = true;
        int prev = 0;
        int count = 0;
        while (cur != null || !stack.isEmpty()) {
            while (cur != null) {
                stack.push(cur);
                cur = cur.left;
            }
            cur = stack.pop();
            if (!first && cur.key <= prev) {
                return false;
            }
            first = false;
            prev = cur.key;
            count++;
            cur = cur.right;
        }
        return count == size;
    }
}
