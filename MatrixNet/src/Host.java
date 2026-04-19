import java.util.ArrayList;

public class Host {
    private final String id;
    private final int clearanceLevel;
    private final ArrayList<Backdoor> adjacentBackdoors;
    
    //found that this makes it a lot faster accessing the object
    int numericId;

    public Host(String id, int clearanceLevel) {
        this.id = id;
        this.clearanceLevel = clearanceLevel;
        this.adjacentBackdoors = new ArrayList<>();
        this.numericId = -1;  // Will be set by MatrixNet
    }

    public void addBackdoor(Backdoor backdoor) {
        adjacentBackdoors.add(backdoor);
    }

    public String getId() {
        return id;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public ArrayList<Backdoor> getAdjacentBackdoors() {
        return adjacentBackdoors;
    }
}
