import java.util.ArrayList;

public class MyHashMap<K, V> {
    private static final int INITIAL_CAPACITY = 32;
    private static final float LOAD_FACTOR = 0.5f;

    private Object[] keys;
    private Object[] values;
    private int size;
    private int capacity;
    private int mask;
    private int threshold;

    public MyHashMap() {
        capacity = INITIAL_CAPACITY;
        mask = capacity - 1;
        keys = new Object[capacity];
        values = new Object[capacity];
        size = 0;
        threshold = (int)(capacity * LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity) {
        // Round up to power of 2
        capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
        mask = capacity - 1;
        keys = new Object[capacity];
        values = new Object[capacity];
        size = 0;
        threshold = (int)(capacity * LOAD_FACTOR);
    }

    private int hash(Object key) {
        int h = key.hashCode();
        h ^= (h >>> 16);
        return h & mask;
    }

    public void put(K key, V value) {
        if (size >= threshold) {
            resize();
        }

        int idx = hash(key);
        while (keys[idx] != null) {
            if (keys[idx].equals(key)) {
                values[idx] = value;
                return;
            }
            idx = (idx + 1) & mask;
        }

        keys[idx] = key;
        values[idx] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        int idx = hash(key);
        while (keys[idx] != null) {
            if (keys[idx].equals(key)) {
                return (V) values[idx];
            }
            idx = (idx + 1) & mask;
        }
        return null;
    }

    public boolean containsKey(K key) {
        int idx = hash(key);
        while (keys[idx] != null) {
            if (keys[idx].equals(key)) {
                return true;
            }
            idx = (idx + 1) & mask;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<K> keySet() {
        ArrayList<K> result = new ArrayList<>(size);
        for (int i = 0; i < capacity; i++) {
            if (keys[i] != null) {
                result.add((K) keys[i]);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<V> values() {
        ArrayList<V> result = new ArrayList<>(size);
        for (int i = 0; i < capacity; i++) {
            if (values[i] != null) {
                result.add((V) values[i]);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Object[] oldKeys = keys;
        Object[] oldValues = values;
        int oldCapacity = capacity;

        capacity <<= 1;  // Double capacity
        mask = capacity - 1;
        threshold = (int)(capacity * LOAD_FACTOR);
        keys = new Object[capacity];
        values = new Object[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldKeys[i] != null) {
                put((K) oldKeys[i], (V) oldValues[i]);
            }
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Object[] getKeysArray() {
        return keys;
    }
    
    public Object[] getValuesArray() {
        return values;
    }
    
    public int getCapacity() {
        return capacity;
    }
}
