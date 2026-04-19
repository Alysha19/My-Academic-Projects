import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MatrixNet {
    private MyHashMap<String, Host> hosts;
    private MyHashMap<String, Backdoor> backdoors;
    
    // numeric host id
    private ArrayList<Host> hostsByNumericId;
    private int nextNumericId;

    private int[] dfsStack;
    private boolean[] visited;
    private int stackCapacity;
    private int visitedCapacity;
    
    // path reconstruction arrays for dijkstra
    private int[] pathParentNode;
    private int[] pathParentIdx;
    private int pathArraySize;
    private int pathArrayCapacity;
    
    public MatrixNet() {
        hosts = new MyHashMap<>(1024);
        backdoors = new MyHashMap<>(4096);
        hostsByNumericId = new ArrayList<>();
        nextNumericId = 0;
        
        stackCapacity = 1024;
        dfsStack = new int[stackCapacity];
        visitedCapacity = 1024;
        visited = new boolean[visitedCapacity];
        
        pathArrayCapacity = 4096;
        pathParentNode = new int[pathArrayCapacity];
        pathParentIdx = new int[pathArrayCapacity];
    }
    
    public String spawnHost(String hostId, int clearanceLevel) {
        if (!isValidHostId(hostId)) {
            return "Some error occurred in spawn_host.";
        }
        
        if (hosts.containsKey(hostId)) {
            return "Some error occurred in spawn_host.";
        }
        
        Host host = new Host(hostId, clearanceLevel);
        host.numericId = nextNumericId++;
        hosts.put(hostId, host);
        hostsByNumericId.add(host);
        
        // ensure visited array is large enough
        ensureVisitedCapacity(nextNumericId);
        
        return "Spawned host " + hostId + " with clearance level " + clearanceLevel + ".";
    }
    
    public String linkBackdoor(String id1, String id2, int latency, int bandwidth, int firewall) {
        Host h1 = hosts.get(id1);
        Host h2 = hosts.get(id2);
        
        if (h1 == null || h2 == null) {
            return "Some error occurred in link_backdoor.";
        }
        
        if (id1.equals(id2)) {
            return "Some error occurred in link_backdoor.";
        }
        
        String key = createBackdoorKey(id1, id2);
        
        if (backdoors.containsKey(key)) {
            return "Some error occurred in link_backdoor.";
        }
        
        Backdoor backdoor = new Backdoor(h1, h2, latency, bandwidth, firewall);
        backdoors.put(key, backdoor);
        h1.addBackdoor(backdoor);
        h2.addBackdoor(backdoor);
        
        return "Linked " + id1 + " <-> " + id2 + " with latency " + latency + "ms, bandwidth " +
                bandwidth + "Mbps, firewall " + firewall + ".";
    }
    
    public String sealBackdoor(String id1, String id2) {
        Host h1 = hosts.get(id1);
        Host h2 = hosts.get(id2);
        
        if (h1 == null || h2 == null) {
            return "Some error occurred in seal_backdoor.";
        }
        
        String key = createBackdoorKey(id1, id2);
        Backdoor backdoor = backdoors.get(key);
        
        if (backdoor == null) {
            return "Some error occurred in seal_backdoor.";
        }
        
        backdoor.toggleSeal();
        
        if (backdoor.isSealed()) {
            return "Backdoor " + id1 + " <-> " + id2 + " sealed.";
        } else {
            return "Backdoor " + id1 + " <-> " + id2 + " unsealed.";
        }
    }
    
    public String traceRoute(String sourceId, String destId, int minBandwidth, int lambda) {
        Host source = hosts.get(sourceId);
        Host dest = hosts.get(destId);
        
        if (source == null || dest == null) {
            return "Some error occurred in trace_route.";
        }
        
        if (sourceId.equals(destId)) {
            return "Optimal route " + sourceId + " -> " + destId + ": " + sourceId + " (Latency = 0ms)";
        }
        
        PathResult result = findOptimalPath(source.numericId, dest.numericId, minBandwidth, lambda);
        
        if (!result.found) {
            return "No route found from " + sourceId + " to " + destId;
        }
        
        StringBuilder pathBuilder = new StringBuilder();
        ArrayList<String> path = result.path;
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) pathBuilder.append(" -> ");
            pathBuilder.append(path.get(i));
        }
        
        return "Optimal route " + sourceId + " -> " + destId + ": " + pathBuilder.toString() +
                " (Latency = " + result.totalLatency + "ms)";
    }
    
    public String scanConnectivity() {
        int components = countConnectedComponents();
        
        if (components <= 1) {
            return "Network is fully connected.";
        } else {
            return "Network has " + components + " disconnected components.";
        }
    }
    
    public String simulateBreachHost(String hostId) {
        Host host = hosts.get(hostId);
        
        if (host == null) {
            return "Some error occurred in simulate_breach.";
        }
        
        int originalComponents = countConnectedComponents();
        int newComponents = countComponentsWithoutHost(host.numericId);
        
        if (newComponents <= originalComponents) {
            return "Host " + hostId + " is NOT an articulation point. Network remains the same.";
        } else {
            return "Host " + hostId + " IS an articulation point.\nFailure results in " + newComponents + " disconnected components.";
        }
    }
    
    public String simulateBreachBackdoor(String id1, String id2) {
        Host h1 = hosts.get(id1);
        Host h2 = hosts.get(id2);
        
        if (h1 == null || h2 == null) {
            return "Some error occurred in simulate_breach.";
        }
        
        String key = createBackdoorKey(id1, id2);
        Backdoor backdoor = backdoors.get(key);
        
        if (backdoor == null) {
            return "Some error occurred in simulate_breach.";
        }
        
        if (backdoor.isSealed()) {
            return "Some error occurred in simulate_breach.";
        }
        
        int originalComponents = countConnectedComponents();
        
        backdoor.toggleSeal();
        int newComponents = countConnectedComponents();
        backdoor.toggleSeal();
        
        if (newComponents <= originalComponents) {
            return "Backdoor " + id1 + " <-> " + id2 + " is NOT a bridge. Network remains the same.";
        } else {
            return "Backdoor " + id1 + " <-> " + id2 + " IS a bridge.\nFailure results in " +
                    newComponents + " disconnected components.";
        }
    }
    
    public String oracleReport() {
        StringBuilder sb = new StringBuilder(256);
        
        sb.append("--- Resistance Network Report ---\n");
        sb.append("Total Hosts: ").append(hosts.size()).append("\n");
        
        int unsealedCount = 0;
        long totalBandwidth = 0;
        
        Object[] keysArray = backdoors.getKeysArray();
        Object[] valuesArray = backdoors.getValuesArray();
        int cap = backdoors.getCapacity();
        
        for (int i = 0; i < cap; i++) {
            if (keysArray[i] != null) {
                Backdoor backdoor = (Backdoor) valuesArray[i];
                if (!backdoor.isSealed()) {
                    unsealedCount++;
                    totalBandwidth += backdoor.getBandwidth();
                }
            }
        }
        sb.append("Total Unsealed Backdoors: ").append(unsealedCount).append("\n");
        
        int components = countConnectedComponents();
        sb.append("Network Connectivity: ").append(components <= 1 ? "Connected" : "Disconnected").append("\n");
        sb.append("Connected Components: ").append(components).append("\n");
        
        boolean hasCycle = containsCycle();
        sb.append("Contains Cycles: ").append(hasCycle ? "Yes" : "No").append("\n");
        
        String avgBandwidth;
        if (unsealedCount == 0) {
            avgBandwidth = "0.0Mbps";
        } else {
            avgBandwidth = formatAverage(totalBandwidth, unsealedCount) + "Mbps";
        }
        sb.append("Average Bandwidth: ").append(avgBandwidth).append("\n");
        
        String avgClearance;
        int hostSize = hosts.size();
        if (hostSize == 0) {
            avgClearance = "0.0";
        } else {
            long totalClearance = 0;
            for (int i = 0; i < nextNumericId; i++) {
                totalClearance += hostsByNumericId.get(i).getClearanceLevel();
            }
            avgClearance = formatAverage(totalClearance, hostSize);
        }
        sb.append("Average Clearance Level: ").append(avgClearance);
        
        return sb.toString();
    }

    private String formatAverage(long sum, int count) {
        if (count == 0) return "0.0";
        BigDecimal bd = new BigDecimal(sum);
        BigDecimal divisor = new BigDecimal(count);
        return bd.divide(divisor, 1, RoundingMode.HALF_UP).toString();
    }

    private String createBackdoorKey(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + "|" + id2;
        } else {
            return id2 + "|" + id1;
        }
    }
    
    private void ensureVisitedCapacity(int minCapacity) {
        if (visitedCapacity < minCapacity) {
            while (visitedCapacity < minCapacity) {
                visitedCapacity <<= 1;
            }
            visited = new boolean[visitedCapacity];
        }
    }
    
    private void ensureStackCapacity(int minCapacity) {
        if (stackCapacity < minCapacity) {
            while (stackCapacity < minCapacity) {
                stackCapacity <<= 1;
            }
            dfsStack = new int[stackCapacity];
        }
    }

    
    private static class PathResult {
        ArrayList<String> path;
        long totalLatency;
        boolean found;
        
        PathResult() {
            path = new ArrayList<>();
            totalLatency = 0;
            found = false;
        }
    }

    private PathResult findOptimalPath(int sourceId, int destId, int minBandwidth, int lambda) {
        PathResult result = new PathResult();
        int n = nextNumericId;
        
        // Best state tracking: for each node, track best (latency, segCount) seen
        // We need to handle the case where same node can be visited with different segment counts
        // due to the dynamic latency formula

        int[] bestSegmentCounts = new int[n];
        for (int i = 0; i < n; i++) {
            bestSegmentCounts[i] = Integer.MAX_VALUE;
        }
        
        //store all visited states
        pathArraySize = 0;
        ensurePathArrayCapacity(n * 4);
        
        MinHeap pq = new MinHeap(n * 2);
        
        //insert params: nodeId, latency, segCount, parentIdx (-1 = no parent), viaEdge (-1 = none)
        int sourcePathIdx = addPathEntry(-1, sourceId);
        pq.insert(sourceId, 0L, 0, sourcePathIdx, -1);
        
        // output arrays for extractMin
        long[] outLatency = new long[1];
        int[] outSegCount = new int[1];
        int[] outParent = new int[1];
        int[] outViaEdge = new int[1];
        
        // for lexicographic tie-breaking, we may need to track multiple candidates
        ArrayList<int[]> destCandidates = new ArrayList<>();
        long bestDestLatency = Long.MAX_VALUE;
        int bestDestSegCount = Integer.MAX_VALUE;
        
        while (!pq.isEmpty()) {
            int currId = pq.extractMin(outLatency, outSegCount, outParent, outViaEdge);
            long currLatency = outLatency[0];
            int currSegCount = outSegCount[0];
            int currPathIdx = outParent[0];
            
            // skip if we've seen this node with fewer or equal segments
            if (bestSegmentCounts[currId] <= currSegCount) {
                continue;
            }
            bestSegmentCounts[currId] = currSegCount;
            
            // Check if destination
            if (currId == destId) {
                // Check if this is a valid candidate
                if (currLatency < bestDestLatency ||
                    (currLatency == bestDestLatency && currSegCount < bestDestSegCount)) {
                    destCandidates.clear();
                    bestDestLatency = currLatency;
                    bestDestSegCount = currSegCount;
                }
                if (currLatency == bestDestLatency && currSegCount == bestDestSegCount) {
                    destCandidates.add(new int[]{currPathIdx, currSegCount});
                }
                // Continue searching for potential ties
                continue;
            }
            
            // If we've found destination with better latency, prune
            if (currLatency > bestDestLatency) {
                continue;
            }
            if (currLatency == bestDestLatency && currSegCount >= bestDestSegCount) {
                continue;
            }
            
            Host currHost = hostsByNumericId.get(currId);
            int currClearance = currHost.getClearanceLevel();
            ArrayList<Backdoor> edges = currHost.getAdjacentBackdoors();
            
            for (int e = 0; e < edges.size(); e++) {
                Backdoor backdoor = edges.get(e);
                
                if (backdoor.isSealed()) continue;
                if (backdoor.getBandwidth() < minBandwidth) continue;
                if (currClearance < backdoor.getFirewallLevel()) continue;
                
                Host neighbor = backdoor.getOtherByNumericId(currId);
                int neighborId = neighbor.numericId;
                int newSegCount = currSegCount + 1;
                
                // Pruning
                if (bestSegmentCounts[neighborId] <= newSegCount) {
                    continue;
                }
                
                // Calculate dynamic latency
                long dynamicLatency = backdoor.getLatency() + (long) lambda * currSegCount;
                long newLatency = currLatency + dynamicLatency;
                
                // Prune if can't beat best destination
                if (newLatency > bestDestLatency) {
                    continue;
                }
                
                // Add path entry
                int newPathIdx = addPathEntry(currPathIdx, neighborId);
                
                pq.insert(neighborId, newLatency, newSegCount, newPathIdx, e);
            }
        }
        
        // If we found candidates, pick the lexicographically smallest path
        if (!destCandidates.isEmpty()) {
            result.found = true;
            result.totalLatency = bestDestLatency;
            
            if (destCandidates.size() == 1) {
                // Single candidate - reconstruct path
                result.path = reconstructPath(destCandidates.get(0)[0]);
            } else {
                // Multiple candidates - need to compare paths lexicographically
                ArrayList<String> bestPath = null;
                for (int[] cand : destCandidates) {
                    ArrayList<String> path = reconstructPath(cand[0]);
                    if (bestPath == null || comparePaths(path, bestPath) < 0) {
                        bestPath = path;
                    }
                }
                result.path = bestPath;
            }
        }
        
        return result;
    }
    
    private void ensurePathArrayCapacity(int minCapacity) {
        if (pathArrayCapacity < minCapacity) {
            while (pathArrayCapacity < minCapacity) {
                pathArrayCapacity <<= 1;
            }
            int[] newParentNode = new int[pathArrayCapacity];
            int[] newParentIdx = new int[pathArrayCapacity];
            System.arraycopy(pathParentNode, 0, newParentNode, 0, pathArraySize);
            System.arraycopy(pathParentIdx, 0, newParentIdx, 0, pathArraySize);
            pathParentNode = newParentNode;
            pathParentIdx = newParentIdx;
        }
    }
    
    private int addPathEntry(int parentIdx, int nodeId) {
        if (pathArraySize >= pathArrayCapacity) {
            ensurePathArrayCapacity(pathArrayCapacity * 2);
        }
        int idx = pathArraySize++;
        pathParentIdx[idx] = parentIdx;
        pathParentNode[idx] = nodeId;
        return idx;
    }
    
    private ArrayList<String> reconstructPath(int pathIdx) {
        ArrayList<String> path = new ArrayList<>();
        int idx = pathIdx;
        while (idx >= 0) {
            path.add(hostsByNumericId.get(pathParentNode[idx]).getId());
            idx = pathParentIdx[idx];
        }
        // Reverse the path
        int left = 0, right = path.size() - 1;
        while (left < right) {
            String tmp = path.get(left);
            path.set(left, path.get(right));
            path.set(right, tmp);
            left++;
            right--;
        }
        return path;
    }
    
    // compare two paths lexicographically
    private int comparePaths(ArrayList<String> a, ArrayList<String> b) {
        int minLen = Math.min(a.size(), b.size());
        for (int i = 0; i < minLen; i++) {
            int cmp = a.get(i).compareTo(b.get(i));
            if (cmp != 0) return cmp;
        }
        return a.size() - b.size();
    }
    
    // iterative DFS for counting connected components
    private int countConnectedComponents() {
        int n = nextNumericId;
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        // Clear visited array
        for (int i = 0; i < n; i++) {
            visited[i] = false;
        }
        
        ensureStackCapacity(n);
        
        int componentCount = 0;
        
        for (int startId = 0; startId < n; startId++) {
            if (!visited[startId]) {
                // DFS from this node
                dfsIterative(startId, -1);
                componentCount++;
            }
        }
        
        return componentCount;
    }
    
    private int countComponentsWithoutHost(int excludeId) {
        int n = nextNumericId;
        if (n <= 1) return 0;
        if (n == 2) return 1;
        
        // clear visited array and mark excluded
        for (int i = 0; i < n; i++) {
            visited[i] = (i == excludeId);
        }
        
        ensureStackCapacity(n);
        
        int componentCount = 0;
        
        for (int startId = 0; startId < n; startId++) {
            if (!visited[startId]) {
                dfsIterative(startId, excludeId);
                componentCount++;
            }
        }
        
        return componentCount;
    }
    
    // iterative DFS
    private void dfsIterative(int startId, int excludeId) {
        int stackTop = 0;
        dfsStack[stackTop++] = startId;
        visited[startId] = true;
        
        while (stackTop > 0) {
            int currId = dfsStack[--stackTop];
            Host host = hostsByNumericId.get(currId);
            ArrayList<Backdoor> edges = host.getAdjacentBackdoors();
            
            for (int i = 0; i < edges.size(); i++) {
                Backdoor backdoor = edges.get(i);
                if (backdoor.isSealed()) continue;
                
                int neighborId = backdoor.getOtherByNumericId(currId).numericId;
                
                if (!visited[neighborId]) {
                    visited[neighborId] = true;
                    if (stackTop >= stackCapacity) {
                        ensureStackCapacity(stackCapacity * 2);
                    }
                    dfsStack[stackTop++] = neighborId;
                }
            }
        }
    }

    private boolean containsCycle() {
        int n = nextNumericId;
        if (n <= 2) return false;
        
        // need to track parent to avoid false positive on bidirectional edges
        
        for (int i = 0; i < n; i++) {
            visited[i] = false;
        }
        
        // stack entries: [nodeId, parentId]
        int[] parentStack = new int[stackCapacity];
        
        for (int startId = 0; startId < n; startId++) {
            if (!visited[startId]) {
                if (dfsCycleIterative(startId, parentStack)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean dfsCycleIterative(int startId, int[] parentStack) {
        int stackTop = 0;
        dfsStack[stackTop] = startId;
        parentStack[stackTop] = -1;
        stackTop++;
        visited[startId] = true;
        
        while (stackTop > 0) {
            stackTop--;
            int currId = dfsStack[stackTop];
            int parentId = parentStack[stackTop];
            
            Host host = hostsByNumericId.get(currId);
            ArrayList<Backdoor> edges = host.getAdjacentBackdoors();
            
            for (int i = 0; i < edges.size(); i++) {
                Backdoor backdoor = edges.get(i);
                if (backdoor.isSealed()) continue;
                
                int neighborId = backdoor.getOtherByNumericId(currId).numericId;
                
                if (!visited[neighborId]) {
                    visited[neighborId] = true;
                    if (stackTop >= stackCapacity - 1) {
                        ensureStackCapacity(stackCapacity * 2);
                        // resize parent stack too
                        int[] newParentStack = new int[stackCapacity];
                        System.arraycopy(parentStack, 0, newParentStack, 0, stackTop);
                        parentStack = newParentStack;
                    }
                    dfsStack[stackTop] = neighborId;
                    parentStack[stackTop] = currId;
                    stackTop++;
                } else if (neighborId != parentId) {
                    // found a back edge = cycle detected
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isValidHostId(String hostId) {
        if (hostId == null || hostId.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < hostId.length(); i++) {
            char c = hostId.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')) {
                return false;
            }
        }
        
        return true;
    }
}
