public class Customer {
    public String id;
    public int totalSpent; // after discounts
    public String loyaltyTier; // "BRONZE", "SILVER", "GOLD", "PLATINUM"
    public MyHashMap<Boolean> blacklistedFreelancers;

    // FIXED: Changed from ArrayList to HashMap for O(1) removal
    public MyHashMap<Employment> activeEmployments;

    public int totalEmploymentCount;

    // For loyalty penalties (customer cancellations)
    public int loyaltyPenalty; // $250 per cancellation

    // OPTIMIZATION: Tracking flag for month-end processing
    public boolean markedActiveThisMonth;

    Customer(String id){
        this.id = id;
        this.totalSpent = 0;
        this.loyaltyTier = "BRONZE";
        this.totalEmploymentCount = 0;
        loyaltyPenalty = 0;
        markedActiveThisMonth = false;

        // FIXED: Use HashMap keyed by freelancerId for O(1) operations
        activeEmployments = new MyHashMap<>(64);
        blacklistedFreelancers = new MyHashMap<>(64);
    }

    public void updateTotalSpent(int price){
        double discountRate = discountRate(loyaltyTier);
        int payment = (int) Math.floor(price * (1 - discountRate));
        totalSpent += payment;
    }

    private double discountRate(String tier){
        return switch (tier) {
            case "BRONZE" -> 0;
            case "SILVER" -> 0.05;
            case "GOLD" -> 0.1;
            case "PLATINUM" -> 0.15;
            default -> 0;
        };
    }

    public void updateLoyaltyTier(){
        int effectiveSpending = totalSpent - loyaltyPenalty;

        if(effectiveSpending >= 5000){
            loyaltyTier = "PLATINUM";
        } else if(effectiveSpending >= 2000){
            loyaltyTier = "GOLD";
        } else if(effectiveSpending >= 500){
            loyaltyTier = "SILVER";
        } else{
            loyaltyTier = "BRONZE";
        }
    }
}