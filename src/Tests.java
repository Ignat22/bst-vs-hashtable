import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Tests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testEmpty(new BST(), "BST");
        testEmpty(new HashTable(), "HashTable");
        testEmpty(new BadHashTable(), "BadHashTable");

        testBasic(new BST(), "BST");
        testBasic(new HashTable(), "HashTable");
        testBasic(new BadHashTable(), "BadHashTable");

        testBstDeleteCases();
        testHashResize();

        testRandom(new BST(), "BST", 1);
        testRandom(new HashTable(), "HashTable", 2);
        testRandom(new BadHashTable(), "BadHashTable", 3);

        testSortedInsert();

        System.out.println();
        System.out.println("Passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void check(boolean condition, String name) {
        if (condition) {
            passed++;
            System.out.println("[OK]   " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    static void testEmpty(SymbolTable t, String name) {
        check(t.size() == 0, name + " empty size is 0");
        check(t.search(5) == -1, name + " search in empty");
        check(!t.delete(5), name + " delete from empty");
    }

    static void testBasic(SymbolTable t, String name) {
        t.insert(10, 100);
        t.insert(20, 200);
        t.insert(5, 50);
        check(t.size() == 3, name + " size after 3 inserts");
        check(t.search(20) == 200, name + " search existing key");
        check(t.search(7) == -1, name + " search missing key");

        t.insert(20, 999);
        check(t.size() == 3, name + " duplicate key does not change size");
        check(t.search(20) == 999, name + " duplicate key updates value");

        check(t.delete(10), name + " delete existing key");
        check(t.search(10) == -1, name + " deleted key not found");
        check(!t.delete(10), name + " delete same key twice");
        check(t.size() == 2, name + " size after delete");

        t.insert(-3, 30);
        check(t.search(-3) == 30, name + " negative key");
    }

    static void testBstDeleteCases() {
        int[] keys = {50, 30, 70, 20, 40, 60, 80, 35, 45, 65};

        BST t = build(keys);
        check(t.delete(20) && t.search(20) == -1 && t.isValid(), "BST delete leaf");

        t = build(keys);
        check(t.delete(60) && t.search(65) == 65 && t.isValid(), "BST delete node with one child");

        t = build(keys);
        check(t.delete(30) && t.search(35) == 35 && t.search(45) == 45 && t.isValid(),
                "BST delete node with two children");

        t = build(keys);
        check(t.delete(50) && t.search(50) == -1 && t.size() == keys.length - 1 && t.isValid(),
                "BST delete root");

        t = build(keys);
        for (int k : keys) {
            t.delete(k);
        }
        check(t.size() == 0 && t.height() == 0 && t.isValid(), "BST delete all keys");
    }

    static BST build(int[] keys) {
        BST t = new BST();
        for (int k : keys) {
            t.insert(k, k);
        }
        return t;
    }

    static void testHashResize() {
        HashTable t = new HashTable();
        int startCap = t.capacity();
        for (int i = 0; i < 1000; i++) {
            t.insert(i, i);
        }
        boolean allFound = true;
        for (int i = 0; i < 1000; i++) {
            if (t.search(i) != i) {
                allFound = false;
            }
        }
        check(t.capacity() > startCap, "HashTable grows when load factor is exceeded");
        check(allFound, "HashTable keeps all keys after resize");
        check(t.size() <= t.capacity() * 0.75, "HashTable load factor <= 0.75");
    }

    static void testRandom(SymbolTable t, String name, long seed) {
        Random rnd = new Random(seed);
        Map<Integer, Integer> ref = new HashMap<>();
        boolean ok = true;
        for (int i = 0; i < 20000; i++) {
            int key = rnd.nextInt(2000) - 500;
            int op = rnd.nextInt(3);
            if (op == 0) {
                int val = rnd.nextInt(100000);
                t.insert(key, val);
                ref.put(key, val);
            } else if (op == 1) {
                int expected = ref.getOrDefault(key, -1);
                if (t.search(key) != expected) {
                    ok = false;
                }
            } else {
                if (t.delete(key) != (ref.remove(key) != null)) {
                    ok = false;
                }
            }
            if (t.size() != ref.size()) {
                ok = false;
            }
        }
        if (t instanceof BST && !((BST) t).isValid()) {
            ok = false;
        }
        check(ok, name + " random operations match java.util.HashMap");
    }

    static void testSortedInsert() {
        BST t = new BST();
        for (int i = 0; i < 1000; i++) {
            t.insert(i, i);
        }
        check(t.height() == 1000, "BST height is n for sorted input");

        BST r = new BST();
        Random rnd = new Random(7);
        for (int i = 0; i < 1000; i++) {
            r.insert(rnd.nextInt(1000000), i);
        }
        check(r.height() < 50, "BST height is small for random input");
    }
}
