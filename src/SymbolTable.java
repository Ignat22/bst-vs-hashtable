public interface SymbolTable {
    void insert(int key, int value);

    int search(int key);

    boolean delete(int key);

    int size();
}
