public class MinHeap {

    private int[] nodeIds;       // Numeric node ID
    private long[] latencies;    // Total latency
    private int[] segmentCounts; // Number of segments
    private int[] parentIndices; // Index into a separate path tracking array
    private int[] viaEdgeIndices;// Which edge was used to get here
    
    private int size;
    private int capacity;
    
    public MinHeap() {
        this(1024);
    }
    
    public MinHeap(int initialCapacity) {
        this.capacity = initialCapacity;
        this.nodeIds = new int[capacity];
        this.latencies = new long[capacity];
        this.segmentCounts = new int[capacity];
        this.parentIndices = new int[capacity];
        this.viaEdgeIndices = new int[capacity];
        this.size = 0;
    }
    
    public void insert(int nodeId, long latency, int segCount, int parentIdx, int viaEdge) {
        if (size == capacity) {
            grow();
        }
        
        int idx = size++;
        nodeIds[idx] = nodeId;
        latencies[idx] = latency;
        segmentCounts[idx] = segCount;
        parentIndices[idx] = parentIdx;
        viaEdgeIndices[idx] = viaEdge;
        
        bubbleUp(idx);
    }


    public int extractMin(long[] outLatency, int[] outSegCount, int[] outParent, int[] outViaEdge) {
        if (size == 0) {
            return -1;
        }
        
        int minNodeId = nodeIds[0];
        outLatency[0] = latencies[0];
        outSegCount[0] = segmentCounts[0];
        outParent[0] = parentIndices[0];
        outViaEdge[0] = viaEdgeIndices[0];
        
        size--;
        if (size > 0) {
            nodeIds[0] = nodeIds[size];
            latencies[0] = latencies[size];
            segmentCounts[0] = segmentCounts[size];
            parentIndices[0] = parentIndices[size];
            viaEdgeIndices[0] = viaEdgeIndices[size];
            bubbleDown(0);
        }
        
        return minNodeId;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public int size() {
        return size;
    }
    
    public void clear() {
        size = 0;
    }
    
    private void bubbleUp(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) >> 1;
            if (compare(idx, parent) < 0) {
                swap(idx, parent);
                idx = parent;
            } else {
                break;
            }
        }
    }
    
    private void bubbleDown(int idx) {
        while (true) {
            int left = (idx << 1) + 1;
            int right = left + 1;
            int smallest = idx;
            
            if (left < size && compare(left, smallest) < 0) {
                smallest = left;
            }
            if (right < size && compare(right, smallest) < 0) {
                smallest = right;
            }
            
            if (smallest != idx) {
                swap(idx, smallest);
                idx = smallest;
            } else {
                break;
            }
        }
    }

    private int compare(int a, int b) {
        // 1. Smallest latency first
        if (latencies[a] != latencies[b]) {
            return Long.compare(latencies[a], latencies[b]);
        }
        // 2. Fewer segments
        if (segmentCounts[a] != segmentCounts[b]) {
            return segmentCounts[a] - segmentCounts[b];
        }
        // 3. Lexicographically smaller, we'll handle this during path reconstruction
        return 0;
    }
    
    private void swap(int i, int j) {
        int tmpId = nodeIds[i];
        nodeIds[i] = nodeIds[j];
        nodeIds[j] = tmpId;
        
        long tmpLat = latencies[i];
        latencies[i] = latencies[j];
        latencies[j] = tmpLat;
        
        int tmpSeg = segmentCounts[i];
        segmentCounts[i] = segmentCounts[j];
        segmentCounts[j] = tmpSeg;
        
        int tmpParent = parentIndices[i];
        parentIndices[i] = parentIndices[j];
        parentIndices[j] = tmpParent;
        
        int tmpEdge = viaEdgeIndices[i];
        viaEdgeIndices[i] = viaEdgeIndices[j];
        viaEdgeIndices[j] = tmpEdge;
    }
    
    private void grow() {
        int newCapacity = capacity << 1;
        
        int[] newNodeIds = new int[newCapacity];
        long[] newLatencies = new long[newCapacity];
        int[] newSegmentCounts = new int[newCapacity];
        int[] newParentIndices = new int[newCapacity];
        int[] newViaEdgeIndices = new int[newCapacity];
        
        System.arraycopy(nodeIds, 0, newNodeIds, 0, size);
        System.arraycopy(latencies, 0, newLatencies, 0, size);
        System.arraycopy(segmentCounts, 0, newSegmentCounts, 0, size);
        System.arraycopy(parentIndices, 0, newParentIndices, 0, size);
        System.arraycopy(viaEdgeIndices, 0, newViaEdgeIndices, 0, size);
        
        nodeIds = newNodeIds;
        latencies = newLatencies;
        segmentCounts = newSegmentCounts;
        parentIndices = newParentIndices;
        viaEdgeIndices = newViaEdgeIndices;
        capacity = newCapacity;
    }
}
