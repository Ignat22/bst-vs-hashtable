# BST vs Hash Table: Performance Analysis

Design and Analysis of Algorithms  
Student: Igor Kuhta, SE-2530

## 1. Goal

The goal of this work is to implement a Binary Search Tree (BST) and a Hash Table, analyze insertion, search and deletion for both of them, and compare their real performance on 100, 1,000 and 10,000 elements.

## 2. Project structure

```
src/
  SymbolTable.java    common interface (insert, search, delete, size)
  BST.java            unbalanced binary search tree
  HashTable.java      hash table with separate chaining
  BadHashTable.java   same hash table, but every key goes to bucket 0 (worst case)
  Benchmark.java      experiments, writes results to results/
  Tests.java          correctness tests
results/
  results.csv
  structure_stats.csv
  benchmark_output.txt
```

## 3. How to run

```
javac -d out src/*.java
java -cp out Tests
java -cp out Benchmark
```

Java 17 or newer is needed. The benchmark must be started from the project root, because it saves files to `results/`.

## 4. Implementation

**BST.** Every node stores a key, a value and links to left and right child. Smaller keys go to the left, bigger keys go to the right. The tree is not balanced. Insert, search and delete are written with loops instead of recursion, because with sorted input the tree height becomes equal to n (10,000) and recursion could cause a StackOverflowError. Delete handles three cases: node without children, node with one child, and node with two children (in the last case the node is replaced by its in-order successor, the smallest key in the right subtree).

**Hash Table.** An array of buckets, every bucket is a linked list (separate chaining). The index is `hash(key) & (capacity - 1)`, capacity is always a power of two. The hash function mixes high bits into low bits (`h ^ (h >>> 16)`), like `java.util.HashMap`. When the number of elements becomes bigger than `0.75 * capacity`, the array is doubled and all elements are rehashed.

**BadHashTable** extends HashTable and returns the same hash for every key. It is used only to show the worst case of a hash table in practice.

Duplicate keys do not create a new node, the value is updated. `search` returns -1 if the key is not found.

## 5. Complexity analysis

n is the number of elements, h is the height of the tree.

| Operation | BST best | BST average | BST worst | Hash best | Hash average | Hash worst |
|---|---|---|---|---|---|---|
| Insert | O(1) | O(log n) | O(n) | O(1) | O(1) amortized | O(n) |
| Search | O(1) | O(log n) | O(n) | O(1) | O(1) | O(n) |
| Delete | O(1) | O(log n) | O(n) | O(1) | O(1) | O(n) |
| Memory | O(n) | | | O(n + m) | | |

(m is the number of buckets)

### BST

All three operations walk from the root down to the needed node, so their cost is O(h).

- **Best case O(1):** the key is in the root (search, delete) or the tree is empty / the new key becomes a child of the root (insert).
- **Average case O(log n):** if keys come in random order, the tree is close to balanced and its height is about c * log n. In my experiment the height for 10,000 random keys was 32, while log2(10,000) is about 13.3.
- **Worst case O(n):** if keys are inserted in sorted order, every new key goes to the right of the previous one, and the tree becomes a linked list with height n. Then every operation has to pass through almost all nodes. Building such a tree costs 1 + 2 + ... + n = O(n²).
- **Delete** first finds the node (O(h)) and, if the node has two children, also finds the successor (another O(h) at most), so it is still O(h).

### Hash Table

All three operations compute the hash (O(1)), go to the bucket and then walk through its chain. So the cost is O(1 + chain length).

- **Best case O(1):** the bucket is empty or the key is the first in the chain.
- **Average case O(1):** with a good hash function and load factor α = n / m ≤ 0.75, the expected chain length is about α, which is a constant. In my experiment the longest chain for 10,000 keys was only 6.
- **Worst case O(n):** if all keys have the same index (bad hash function or specially chosen keys), the table becomes one linked list, like BadHashTable.
- **Insert and resizing:** sometimes insert triggers a resize, which costs O(n). But the capacity doubles every time, so the total resize work for n inserts is about 2n, and the amortized cost of one insert is still O(1).

## 6. Experimental setup

- Keys are distinct random integers from 0 to 999,999.
- For every n (100, 1,000, 10,000) these tests were run:
  - BST with random insertion order
  - BST with the same keys in sorted order (worst case for BST)
  - HashTable with random order
  - HashTable with sorted order
  - BadHashTable with random order (worst case for hash table)
- In each run a new structure is created, then all n keys are inserted, then all n keys are searched (in a different random order), then all n keys are deleted (in another random order).
- Environment: Windows 10, Java 17.0.20.1, IntelliJ IDEA.
- Time is measured with `System.nanoTime()`. Every test is repeated 30 times and the median is taken. Before measuring, the JVM is warmed up so that the JIT compiler has already compiled the code.
- Times in the tables are the total time for all n operations, in microseconds (µs).

Full output is in `results/benchmark_output.txt`, raw data in `results/results.csv`.

## 7. Results

### Insertion (µs, total for n operations)

| n | BST random | BST sorted | HashTable random | HashTable sorted | BadHashTable |
|---|---|---|---|---|---|
| 100 | 1.9 | 8.8 | 2.8 | 2.8 | 9.8 |
| 1,000 | 43.2 | 731.2 | 19.5 | 19.8 | 832.9 |
| 10,000 | 707.1 | 83,383.5 | 262.4 | 261.9 | 87,561.5 |

### Search (µs, total for n operations)

| n | BST random | BST sorted | HashTable random | HashTable sorted | BadHashTable |
|---|---|---|---|---|---|
| 100 | 3.6 | 9.7 | 1.0 | 1.0 | 8.4 |
| 1,000 | 48.1 | 855.0 | 6.8 | 6.7 | 725.2 |
| 10,000 | 803.7 | 87,880.4 | 92.4 | 91.8 | 76,755.0 |

### Deletion (µs, total for n operations)

| n | BST random | BST sorted | HashTable random | HashTable sorted | BadHashTable |
|---|---|---|---|---|---|
| 100 | 4.1 | 5.4 | 1.1 | 1.2 | 5.1 |
| 1,000 | 47.2 | 383.0 | 7.9 | 7.9 | 367.1 |
| 10,000 | 753.6 | 60,929.2 | 105.3 | 105.3 | 52,138.2 |

### Shape of the structures after inserting n keys

| n | BST height (random) | BST height (sorted) | Hash capacity | Longest chain |
|---|---|---|---|---|
| 100 | 14 | 100 | 256 | 3 |
| 1,000 | 21 | 1,000 | 2,048 | 4 |
| 10,000 | 32 | 10,000 | 16,384 | 6 |

### Average time per one operation, n = 10,000

| | Insert | Search | Delete |
|---|---|---|---|
| BST random | 71 ns | 80 ns | 75 ns |
| BST sorted | 8,338 ns | 8,788 ns | 6,093 ns |
| HashTable random | 26 ns | 9 ns | 11 ns |
| BadHashTable | 8,756 ns | 7,676 ns | 5,214 ns |

## 8. Comparison of results

- **Hash table is the fastest in normal conditions.** For 10,000 random keys search in the hash table is about 9 times faster than in the BST (92 µs vs 804 µs), delete is about 7 times faster, and insert is about 2.7 times faster.
- **Growth with n matches the theory.** When n grows 10 times (1,000 → 10,000), total BST time grows about 16 times. O(n log n) for n operations predicts about 13 times, the extra part comes from cache misses in a bigger tree. Hash table time grows about 13-14 times, close to linear O(n) for n operations (a bit more than 10 because 10,000 elements do not fit in the fastest CPU cache).
- **Sorted input destroys the BST.** With sorted input the BST height is exactly n, and time grows about 100-160 times when n grows 10 times, which is close to quadratic O(n²) (theory says 100). For 10,000 elements the degenerate BST is about 80-120 times slower than the BST with random input (search: 87,880 µs vs 804 µs).
- **Order of keys does not matter for the hash table.** Random and sorted input give almost the same times, because the hash function spreads keys over buckets regardless of their order.
- **Bad hash function makes the hash table as slow as the degenerate BST.** BadHashTable has one chain of length n, and its times are almost the same as BST with sorted input. This shows that the O(n) worst case of a hash table is real.
- **Hash table insert is slower than its search.** Insert has to allocate a new node and sometimes resize the whole array, search does not do this.
- **For n = 100 the differences are very small** (a few microseconds), and here measurement noise and JVM effects are more important than the asymptotic complexity.

## 9. Why performance may differ in practice

1. **Big-O hides constants.** O(1) for a hash table includes computing the hash, the modulo/mask, and following one or more links. O(log n) for a BST means about 15-20 comparisons for n = 10,000. For very small n the BST can be as fast as the hash table or even faster.
2. **Input order.** An unbalanced BST depends on the order of insertion. Sorted or almost sorted data (IDs, dates, timestamps) is very common in real life and turns the BST into a list. Balanced trees (AVL, Red-Black, for example `java.util.TreeMap`) solve this and guarantee O(log n).
3. **Quality of the hash function.** A hash table is fast only if keys are spread evenly. A bad hash function or keys that collide on purpose give O(n) per operation.
4. **Load factor and resizing.** A high load factor means longer chains. Resizing is O(n) and can cause a sudden slow insert, even though the amortized cost is O(1).
5. **CPU cache.** The hash table goes to the array directly and usually reads one or two nodes. A BST follows about 20 pointers to nodes in different places of memory, and each one can be a cache miss. This is a big reason why the BST is slower than the comparison count suggests.
6. **JVM effects.** JIT compilation, garbage collection and memory allocation change the times, especially for small n. That is why warm-up and the median of 30 runs were used.
7. **Different features.** A BST keeps keys sorted, so it can give min, max, sorted order and range queries. A hash table cannot do this without extra work. So the "faster" structure is not always the better choice.

## 10. Conclusions

- In the average case the hash table (O(1)) is faster than the BST (O(log n)) for all three operations, and the experiment confirmed it: search and delete were about 7-9 times faster at 10,000 elements.
- The unbalanced BST is very sensitive to input order. With sorted input it becomes O(n) per operation, and this was clearly visible in the results.
- The hash table also has an O(n) worst case, but only with a bad hash function. With a normal hash function and resizing it stays fast for any order of keys.
- If only fast insert, search and delete by key are needed, a hash table is the better choice. If sorted order or range queries are needed, a balanced BST should be used.
