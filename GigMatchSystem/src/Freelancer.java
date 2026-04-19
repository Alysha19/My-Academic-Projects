public class Freelancer {

    public boolean platformBanned;
    public String id;
    public String profession;
    public int[] skills;
    public int workCount;
    public double avgRating;
    public int compositeScore;
    public int serviceType;
    public int price;
    public boolean available;
    public int ratingCount;
    public double ratingScore;

    // Monthly tracking (reset each simulate_month)
    public int completedThisMonth;
    public int cancelledThisMonth;

    // Burnout tracking
    public boolean isBurnedOut;

    // Service change queue
    public boolean hasQueuedServiceChange;
    public String queuedService;
    public int queuedPrice;

    // Total counts (never reset)
    public int totalCompleted;
    public int totalCancelled;

    public Employment currentEmployment;

    // OPTIMIZATION: Tracking flags to avoid full iteration in simulateMonth
    public boolean markedActiveThisMonth;
    public boolean markedForServiceChange;

    Freelancer(String id, int profession, int price, int[] skills){
        this.id = id;
        this.serviceType = profession;
        this.skills = skills;
        this.price = price;
        platformBanned = false;
        workCount = 0;
        avgRating = 5;
        completedThisMonth = 0;
        cancelledThisMonth = 0;
        isBurnedOut = false;
        hasQueuedServiceChange = false;
        queuedPrice = 0;
        totalCancelled = 0;
        totalCompleted = 0;
        queuedService = "";
        available = true;
        ratingCount = 1;
        markedActiveThisMonth = false;
        markedForServiceChange = false;
    }

    public void computeComposite(){
        double burnoutPenalty = (isBurnedOut) ? 0.45 : 0;

        double skillScore = computeSkillScore();
        double ratingScore = computeRatingScore();
        double reliability = computeReliability();

        compositeScore = (int) Math.floor(10000*(0.55 * skillScore + 0.25 * ratingScore + 0.20 * reliability - burnoutPenalty));
    }

    public double computeSkillScore(){
        int totalScore = 0;
        for(int i = 0; i < skills.length ; i++){
            totalScore += Services.services[serviceType][i] * skills[i];
        }
        int benchScores = 0;
        for(int i=0; i < Services.services[serviceType].length; i++){
            benchScores += Services.services[serviceType][i];
        }

        double skillScore = ((double) totalScore / (100 * benchScores));

        return skillScore;
    }

    public double computeRatingScore(){
        return avgRating / 5.0;
    }

    public double computeReliability(){
        double reliability;
        if(totalCompleted + totalCancelled == 0){
            reliability = 1.0;
        }
        else{
            reliability = 1.0 - ((double) totalCancelled / (totalCancelled + totalCompleted));
        }

        return reliability;
    }

    public void updateRating(int rating){
        avgRating = ((avgRating * ratingCount) + rating) / (ratingCount + 1);
        ratingCount++;
    }


    public void updateSkills() {
        int[] req = Services.services[serviceType]; // (Ts, Cs, Rs, Es, As)

        // 1) find primary skill index (max value in req)
        int primary = 0;
        for (int i = 1; i < 5; i++) {
            if (req[i] > req[primary]) {
                primary = i;
            }
        }

        // 2) find two secondary indices (2nd and 3rd highest in req)
        int sec1 = -1;
        int sec2 = -1;

        for (int i = 0; i < 5; i++) {
            if (i == primary) continue;

            if (sec1 == -1 || req[i] > req[sec1]) {
                // shift old sec1 down to sec2
                sec2 = sec1;
                sec1 = i;
            } else if (sec2 == -1 || req[i] > req[sec2]) {
                sec2 = i;
            }
        }

        // 3) apply gains with upper limit 100
        incSkill(primary, 2);
        if (sec1 != -1) incSkill(sec1, 1);
        if (sec2 != -1) incSkill(sec2, 1);
    }

    private void incSkill(int idx, int delta) {
        skills[idx] = Math.min(100, skills[idx] + delta);
    }



}