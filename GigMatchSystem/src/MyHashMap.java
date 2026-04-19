public class MyHashMap<V> {

    private static class Entry<V> {
        String key;
        V value;
        boolean isDeleted;

        Entry(String key, V value) {
            this.key = key;
            this.value = value;
            this.isDeleted = false;
        }
    }

    private Entry<V>[] table;
    private int size;
    private int capacity;
    private int tombstones;

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.capacity = 128;  // Slightly larger default
        this.table = (Entry<V>[]) new Entry[capacity];
        this.size = 0;
        this.tombstones = 0;
    }

    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity) {
        // Round up to next power of 2 for better distribution
        this.capacity = nextPowerOfTwo(initialCapacity);
        this.table = (Entry<V>[]) new Entry[capacity];
        this.size = 0;
        this.tombstones = 0;
    }

    private int nextPowerOfTwo(int n) {
        if (n <= 0) return 16;
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }

    private int hash(String key) {
        // Improved hash function
        int hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash * 31 + key.charAt(i));
        }
        // Use bitwise AND for power-of-2 capacity (faster than modulo)
        return (hash & 0x7FFFFFFF) % capacity;
    }

    public void put(String key, V value) {
        if ((size + tombstones) >= capacity * 0.7) {
            resize();
        }

        int index = hash(key);
        int firstDeletedSlot = -1;

        for (int i = 0; i < capacity; i++) {
            int probeIndex = (index + i) % capacity;
            Entry<V> entry = table[probeIndex];

            if (entry == null) {
                int insertIndex = (firstDeletedSlot != -1) ? firstDeletedSlot : probeIndex;
                table[insertIndex] = new Entry<>(key, value);

                if (firstDeletedSlot != -1) {
                    tombstones--;
                }
                size++;
                return;
            }

            if (entry.isDeleted) {
                if (firstDeletedSlot == -1) {
                    firstDeletedSlot = probeIndex;
                }
                continue;
            }

            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }

        // If we get here with a deleted slot, use it
        if (firstDeletedSlot != -1) {
            table[firstDeletedSlot] = new Entry<>(key, value);
            tombstones--;
            size++;
        }
    }

    public V get(String key) {
        int index = hash(key);

        for (int i = 0; i < capacity; i++) {
            int probeIndex = (index + i) % capacity;
            Entry<V> entry = table[probeIndex];

            if (entry == null) {
                return null;
            }

            if (entry.isDeleted) {
                continue;
            }

            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

    public void remove(String key) {
        int index = hash(key);

        for (int i = 0; i < capacity; i++) {
            int probeIndex = (index + i) % capacity;
            Entry<V> entry = table[probeIndex];

            if (entry == null) {
                return; // not found
            }

            if (entry.isDeleted) {
                continue;
            }

            if (entry.key.equals(key)) {
                entry.isDeleted = true;
                tombstones++;
                size--;

                if (tombstones > capacity * 0.3) {
                    resize();
                }
                return;
            }
        }
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        table = (Entry<V>[]) new Entry[capacity];
        size = 0;
        tombstones = 0;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<V>[] oldTable = table;
        int oldCapacity = capacity;

        capacity *= 2;
        table = (Entry<V>[]) new Entry[capacity];
        size = 0;
        tombstones = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Entry<V> entry = oldTable[i];
            if (entry != null && !entry.isDeleted) {
                put(entry.key, entry.value);
            }
        }
    }
}