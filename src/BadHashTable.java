public class BadHashTable extends HashTable {

    @Override
    protected int hash(int key) {
        return 0;
    }
}
