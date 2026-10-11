import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class Benchmark {

    static final int[] SIZES = {100, 1000, 10000};
    static final int RUNS = 30;
    static final int WARMUP_ROUNDS = 5;

    static long checksum = 0;

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        Random rnd = new Random(2026);

        System.out.println("Java " + System.getProperty("java.version")
                + " | " + System.getProperty("os.name")
                + " | runs per test: " + RUNS + " (median)");
        System.out.println();

        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            for (int n : SIZES) {
                int[] keys = randomKeys(n, rnd);
                int[] sorted = keys.clone();
                Arrays.sort(sorted);
                measure(BST::new, keys, rnd);
                measure(BST::new, sorted, rnd);
                measure(HashTable::new, keys, rnd);
                measure(HashTable::new, sorted, rnd);
                if (n <= 1000) {
                    measure(BadHashTable::new, keys, rnd);
                }
            }
        }

        new File("results").mkdirs();
        PrintWriter csv = new PrintWriter("results/results.csv");
        csv.println("structure,input,n,insert_us,search_us,delete_us");

        PrintWriter stats = new PrintWriter("results/structure_stats.csv");
        stats.println("n,bst_height_random,bst_height_sorted,hash_capacity,hash_longest_chain");

        System.out.printf("%-14s %-8s %7s %14s %14s %14s%n",
                "Structure", "Input", "n", "Insert (us)", "Search (us)", "Delete (us)");
        System.out.println("-".repeat(76));

        for (int n : SIZES) {
            int[] randomKeys = randomKeys(n, rnd);
            int[] sortedKeys = randomKeys.clone();
            Arrays.sort(sortedKeys);

            double[] bstRandom = measure(BST::new, randomKeys, rnd);
            double[] bstSorted = measure(BST::new, sortedKeys, rnd);
            double[] htRandom = measure(HashTable::new, randomKeys, rnd);
            double[] htSorted = measure(HashTable::new, sortedKeys, rnd);
            double[] badRandom = measure(BadHashTable::new, randomKeys, rnd);

            print(csv, "BST", "random", n, bstRandom);
            print(csv, "BST", "sorted", n, bstSorted);
            print(csv, "HashTable", "random", n, htRandom);
            print(csv, "HashTable", "sorted", n, htSorted);
            print(csv, "BadHashTable", "random", n, badRandom);
            System.out.println();

            BST a = new BST();
            BST b = new BST();
            HashTable h = new HashTable();
            for (int i = 0; i < n; i++) {
                a.insert(randomKeys[i], i);
                b.insert(sortedKeys[i], i);
                h.insert(randomKeys[i], i);
            }
            stats.println(n + "," + a.height() + "," + b.height() + "," + h.capacity() + "," + h.longestChain());
            System.out.println("n = " + n + ": BST height random = " + a.height()
                    + ", sorted = " + b.height()
                    + " | HashTable capacity = " + h.capacity()
                    + ", longest chain = " + h.longestChain());
            System.out.println();
        }

        csv.close();
        stats.close();
        System.out.println("checksum: " + checksum);
        System.out.println("Saved to results/results.csv and results/structure_stats.csv");
    }

    static double[] measure(Supplier<SymbolTable> factory, int[] keys, Random rnd) {
        int n = keys.length;
        long[] ins = new long[RUNS];
        long[] sea = new long[RUNS];
        long[] del = new long[RUNS];

        for (int r = 0; r < RUNS; r++) {
            int[] searchOrder = shuffled(keys, rnd);
            int[] deleteOrder = shuffled(keys, rnd);
            SymbolTable t = factory.get();

            long start = System.nanoTime();
            for (int k : keys) {
                t.insert(k, k);
            }
            ins[r] = System.nanoTime() - start;

            long sum = 0;
            start = System.nanoTime();
            for (int k : searchOrder) {
                sum += t.search(k);
            }
            sea[r] = System.nanoTime() - start;

            int deleted = 0;
            start = System.nanoTime();
            for (int k : deleteOrder) {
                if (t.delete(k)) {
                    deleted++;
                }
            }
            del[r] = System.nanoTime() - start;

            if (deleted != n || t.size() != 0) {
                throw new IllegalStateException("wrong result for n = " + n);
            }
            checksum += sum;
        }

        return new double[]{median(ins) / 1000.0, median(sea) / 1000.0, median(del) / 1000.0};
    }

    static void print(PrintWriter csv, String name, String input, int n, double[] t) {
        System.out.printf("%-14s %-8s %7d %14.1f %14.1f %14.1f%n", name, input, n, t[0], t[1], t[2]);
        csv.printf("%s,%s,%d,%.1f,%.1f,%.1f%n", name, input, n, t[0], t[1], t[2]);
    }

    static int[] randomKeys(int n, Random rnd) {
        Set<Integer> used = new HashSet<>();
        int[] keys = new int[n];
        int i = 0;
        while (i < n) {
            int k = rnd.nextInt(1000000);
            if (used.add(k)) {
                keys[i++] = k;
            }
        }
        return keys;
    }

    static int[] shuffled(int[] arr, Random rnd) {
        int[] a = arr.clone();
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        return a;
    }

    static long median(long[] arr) {
        long[] a = arr.clone();
        Arrays.sort(a);
        return a[a.length / 2];
    }
}
