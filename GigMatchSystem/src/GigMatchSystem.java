import java.util.ArrayList;

public class GigMatchSystem {

    private MyHashMap<Customer> customers;
    private MyHashMap<Freelancer> freelancers;
    private MyAVLTree[] serviceTrees;

    private ArrayList<Customer> allCustomers;
    private ArrayList<Freelancer> allFreelancers;

    // OPTIMIZATION: Track only freelancers that need processing at month end
    private ArrayList<Freelancer> freelancersActiveThisMonth;
    private ArrayList<Freelancer> freelancersWithQueuedChanges;
    private ArrayList<Freelancer> burnedOutFreelancers;

    // OPTIMIZATION: Track customers that need loyalty update
    private ArrayList<Customer> customersActiveThisMonth;

    public GigMatchSystem(){
        // FIXED: Increased initial capacity to avoid expensive resizing
        customers = new MyHashMap<>(131072);
        freelancers = new MyHashMap<>(524288);

        serviceTrees = new MyAVLTree[ServiceType.SERVICE_COUNT];
        for(int i = 0; i < serviceTrees.length ; i++){
            serviceTrees[i] = new MyAVLTree();
        }

        allCustomers = new ArrayList<>();
        allFreelancers = new ArrayList<>();
        freelancersActiveThisMonth = new ArrayList<>();
        freelancersWithQueuedChanges = new ArrayList<>();
        burnedOutFreelancers = new ArrayList<>();
        customersActiveThisMonth = new ArrayList<>();
    }

    public String registerCustomer(String id){
        if(id == null || id.isEmpty() || customers.containsKey(id) || freelancers.containsKey(id)){
            return "Some error occurred in register customer.";
        }

        Customer c = new Customer(id);
        customers.put(id, c);
        allCustomers.add(c);

        return "registered customer " + id;
    }

    public String registerFreelancer(String id, String service, int price,
                                     int T, int C, int R, int E, int A){

        if (id == null || id.isEmpty() || freelancers.containsKey(id) || customers.containsKey(id)){
            return "Some error occurred in register freelancer.";
        }

        int serviceType = ServiceType.fromString(service);
        if(serviceType == -1){
            return "Some error occurred in register freelancer.";
        }

        if(price <= 0){
            return "Some error occurred in register freelancer.";
        }

        if(!validSkill(T) || !validSkill(C) || !validSkill(R)
                || !validSkill(E) || !validSkill(A)){
            return "Some error occurred in register freelancer.";
        }

        int[] skills = new int[]{T, C, R, E, A};
        Freelancer f = new Freelancer(id, serviceType, price, skills);

        f.computeComposite();

        freelancers.put(id, f);
        allFreelancers.add(f);

        if(f.available && !f.platformBanned){
            serviceTrees[serviceType].insert(f);
        }

        return "registered freelancer " + id;
    }

    public String requestJob(String customerId, String serviceName, int k){
        Customer c = customers.get(customerId);

        if(c == null){
            return "Some error occurred in request job.";
        }

        int serviceType = ServiceType.fromString(serviceName);

        if(serviceType == -1){
            return "Some error occurred in request job.";
        }

        ArrayList<Freelancer> candidates = new ArrayList<>();
        serviceTrees[serviceType].collectTopK(k , candidates, c.blacklistedFreelancers);

        if (candidates.isEmpty()){
            return "no freelancers available";
        }

        Freelancer f = candidates.get(0);
        Employment e = new Employment(customerId, f.id , serviceType, f.price);
        f.currentEmployment = e;

        // FIXED: Use HashMap put instead of ArrayList add
        c.activeEmployments.put(f.id, e);
        c.totalEmploymentCount++;
        f.available = false;
        serviceTrees[serviceType].remove(f);

        StringBuilder output = new StringBuilder();
        output.append("available freelancers for ").append(serviceName).append(" (top ").append(k).append("):\n");
        for(Freelancer freelancer : candidates){
            output.append(freelancer.id).append(" - composite: ").append(freelancer.compositeScore)
                    .append(", price: ").append(freelancer.price)
                    .append(", rating: ").append(String.format("%.1f", freelancer.avgRating)).append("\n");
        }
        output.append("auto-employed best freelancer: ").append(f.id).append(" for customer ").append(customerId);

        return output.toString();
    }

    public String employFreelancer(String customerId, String freelancerId){
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);

        if(c == null || f == null){
            return "Some error occurred in employ freelancer.";
        }

        if(!f.available || f.platformBanned){
            return "Some error occurred in employ freelancer.";
        }

        if(c.blacklistedFreelancers.containsKey(freelancerId)){
            return "Some error occurred in employ freelancer.";
        }

        Employment e = new Employment(customerId, freelancerId, f.serviceType, f.price);
        f.currentEmployment = e;
        f.available = false;

        // FIXED: Use HashMap put instead of ArrayList add
        c.activeEmployments.put(freelancerId, e);
        c.totalEmploymentCount++;
        serviceTrees[f.serviceType].remove(f);

        return customerId + " employed " + freelancerId + " for " + ServiceType.toString(f.serviceType);
    }

    // Helper to track freelancer for month-end processing
    private void markActiveThisMonth(Freelancer f) {
        if (!f.markedActiveThisMonth) {
            f.markedActiveThisMonth = true;
            freelancersActiveThisMonth.add(f);
        }
    }

    // Helper to track customer for month-end loyalty update
    private void markCustomerActive(Customer c) {
        if (!c.markedActiveThisMonth) {
            c.markedActiveThisMonth = true;
            customersActiveThisMonth.add(c);
        }
    }

    public String completeAndRate(String freelancerId, int rating){
        Freelancer f = freelancers.get(freelancerId);

        if(f == null || rating < 0 || rating > 5){
            return "Some error occurred in complete_and_rate.";
        }
        if(f.available){
            return "Some error occurred in complete_and_rate.";
        }
        if(f.currentEmployment == null){
            return "Some error occurred in complete_and_rate.";
        }

        Customer c = customers.get(f.currentEmployment.customerId);

        if(c == null){
            return "Some error occurred in complete_and_rate.";
        }

        f.updateRating(rating);

        // FIXED: Use HashMap remove - O(1) instead of O(n)
        c.activeEmployments.remove(freelancerId);

        f.currentEmployment = null;
        f.available = true;

        c.updateTotalSpent(f.price);

        // Track customer for month-end loyalty update
        markCustomerActive(c);

        f.totalCompleted++;
        f.completedThisMonth++;

        // Track for month-end burnout processing
        markActiveThisMonth(f);

        if(rating >= 4){
            f.updateSkills();
        }

        f.computeComposite();

        if (!f.platformBanned) {
            serviceTrees[f.serviceType].insert(f);
        }

        return freelancerId + " completed job for " + c.id + " with rating " + rating;
    }

    public String cancelByFreelancer(String freelancerId){
        Freelancer f = freelancers.get(freelancerId);

        if(f == null){
            return "Some error occurred in cancel by freelancer.";
        }

        if(f.available || f.currentEmployment == null){
            return "Some error occurred in cancel by freelancer.";
        }

        Customer c = customers.get(f.currentEmployment.customerId);

        if(c == null){
            return "Some error occurred in cancel by freelancer.";
        }

        String customerId = c.id;

        // FIXED: Use HashMap remove - O(1) instead of O(n)
        c.activeEmployments.remove(freelancerId);

        f.updateRating(0);

        for(int i = 0; i < f.skills.length ; i++){
            f.skills[i] = Math.max(0, f.skills[i] - 3);
        }

        f.totalCancelled++;
        f.cancelledThisMonth++;

        // Track for month-end burnout processing
        markActiveThisMonth(f);

        f.currentEmployment = null;
        f.available = true;

        String output = "cancelled by freelancer: " + freelancerId + " cancelled " + customerId;

        if(f.cancelledThisMonth >= 5 && !f.platformBanned){
            f.platformBanned = true;
            output += "\nplatform banned freelancer: " + freelancerId;
        }

        f.computeComposite();

        if(!f.platformBanned){
            serviceTrees[f.serviceType].insert(f);
        }

        return output;
    }

    public String cancelByCustomer(String customerId, String freelancerId){
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);

        if(c == null || f == null){
            return "Some error occurred in cancel by customer.";
        }

        // FIXED: Use HashMap get - O(1) instead of O(n)
        Employment targetEmployment = c.activeEmployments.get(freelancerId);

        if(targetEmployment == null){
            return "Some error occurred in cancel by customer.";
        }

        // FIXED: Use HashMap remove - O(1) instead of O(n)
        c.activeEmployments.remove(freelancerId);

        c.loyaltyPenalty += 250;

        // Track customer for month-end loyalty update
        markCustomerActive(c);

        f.currentEmployment = null;
        f.available = true;

        if(!f.platformBanned){
            serviceTrees[f.serviceType].insert(f);
        }
        return "cancelled by customer: " + customerId + " cancelled " + freelancerId;
    }

    public String blacklist(String customerId, String freelancerId){
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);

        if(c == null || f == null){
            return "Some error occurred in blacklist.";
        }

        if(c.blacklistedFreelancers.containsKey(freelancerId)){
            return "Some error occurred in blacklist.";
        }

        c.blacklistedFreelancers.put(freelancerId , true);

        return customerId + " blacklisted " + freelancerId;
    }

    public String unblacklist(String customerId, String freelancerId){
        Customer c = customers.get(customerId);
        Freelancer f = freelancers.get(freelancerId);

        if(c == null || f == null){
            return "Some error occurred in unblacklist.";
        }

        if(!c.blacklistedFreelancers.containsKey(freelancerId)){
            return "Some error occurred in unblacklist.";
        }

        c.blacklistedFreelancers.remove(freelancerId);

        return customerId + " unblacklisted " + freelancerId;
    }

    public String changeService(String freelancerId, String newService, int newPrice){
        Freelancer f = freelancers.get(freelancerId);

        if(f == null){
            return "Some error occurred in change service.";
        }

        int newServiceType = ServiceType.fromString(newService);

        if(newServiceType == -1){
            return "Some error occurred in change service.";
        }

        if(newPrice <= 0){
            return "Some error occurred in change service.";
        }

        String oldService = ServiceType.toString(f.serviceType);

        f.hasQueuedServiceChange = true;
        f.queuedService = newService;
        f.queuedPrice = newPrice;

        // Track for month-end processing
        if (!f.markedForServiceChange) {
            f.markedForServiceChange = true;
            freelancersWithQueuedChanges.add(f);
        }

        return "service change for " + freelancerId + " queued from " + oldService + " to " + newService;
    }

    public String simulateMonth(){
        // OPTIMIZATION: Process only freelancers with queued service changes
        for(Freelancer f : freelancersWithQueuedChanges){
            if(f.hasQueuedServiceChange){
                if (f.available && !f.platformBanned){
                    serviceTrees[f.serviceType].remove(f);
                }

                int newServiceType = ServiceType.fromString(f.queuedService);
                f.serviceType = newServiceType;
                f.price = f.queuedPrice;

                f.computeComposite();

                if(f.available && !f.platformBanned){
                    serviceTrees[f.serviceType].insert(f);
                }

                f.hasQueuedServiceChange = false;
                f.queuedService = "";
                f.queuedPrice = 0;
            }
            f.markedForServiceChange = false;
        }
        freelancersWithQueuedChanges.clear();

        // OPTIMIZATION: Process burnout for active freelancers
        for(Freelancer f : freelancersActiveThisMonth){
            boolean wasBurnedOut = f.isBurnedOut;

            // Check for new burnout (5+ completions)
            if(!f.isBurnedOut && f.completedThisMonth >= 5){
                f.isBurnedOut = true;
                burnedOutFreelancers.add(f);
            }
            // Check for recovery (already burned out + <=2 completions)
            else if(f.isBurnedOut && f.completedThisMonth <= 2){
                f.isBurnedOut = false;
            }

            boolean burnoutChanged = (wasBurnedOut != f.isBurnedOut);

            if(burnoutChanged && f.available && !f.platformBanned){
                serviceTrees[f.serviceType].remove(f);
                f.computeComposite();
                serviceTrees[f.serviceType].insert(f);
            }

            f.completedThisMonth = 0;
            f.cancelledThisMonth = 0;
            f.markedActiveThisMonth = false;
        }
        freelancersActiveThisMonth.clear();

        // CRITICAL: Handle burned out freelancers who did NOTHING this month
        // They should recover because 0 <= 2
        ArrayList<Freelancer> stillBurnedOut = new ArrayList<>();
        for(Freelancer f : burnedOutFreelancers){
            if(f.isBurnedOut){
                // Check if this freelancer wasn't already processed (i.e., did nothing)
                if(f.completedThisMonth <= 2){  // Will be 0 if not active
                    f.isBurnedOut = false;
                    if(f.available && !f.platformBanned){
                        serviceTrees[f.serviceType].remove(f);
                        f.computeComposite();
                        serviceTrees[f.serviceType].insert(f);
                    }
                } else {
                    stillBurnedOut.add(f);
                }
            }
        }
        burnedOutFreelancers = stillBurnedOut;

        // OPTIMIZATION: Only update loyalty for customers who had spending changes
        for(Customer c : customersActiveThisMonth){
            c.updateLoyaltyTier();
            c.markedActiveThisMonth = false;
        }
        customersActiveThisMonth.clear();

        return "month complete";
    }

    public String queryFreelancer(String freelancerId){
        Freelancer f = freelancers.get(freelancerId);
        if(f == null){
            return "Some error occurred in query freelancer.";
        }

        return freelancerId + ": " + ServiceType.toString(f.serviceType) + ", "
                + "price: " + f.price + ", rating: " + String.format("%.1f",f.avgRating) +
                ", completed: " + f.totalCompleted + ", cancelled: " + f.totalCancelled +
                ", skills: " + String.format("(%d,%d,%d,%d,%d)", f.skills[0] , f.skills[1] , f.skills[2] , f.skills[3] , f.skills[4]) +
                ", available: " + ((f.available) ? "yes" : "no") + ", burnout: " + ((f.isBurnedOut) ? "yes" : "no");
    }

    public String queryCustomer(String customerId){
        Customer c = customers.get(customerId);

        if(c == null){
            return "Some error occurred in query customer.";
        }

        return customerId + ": total spent: " + "$" + c.totalSpent + ", loyalty tier: " +
                c.loyaltyTier + ", blacklisted freelancer count: " + c.blacklistedFreelancers.size() +
                ", total employment count: " + c.totalEmploymentCount;
    }

    public String updateSkill(String freelancerId, int T, int C, int R, int E, int A){
        Freelancer f = freelancers.get(freelancerId);

        if(f==null){
            return "Some error occurred in update skill.";
        }

        if(!validSkill(T) || !validSkill(C) || !validSkill(R) || !validSkill(E) || !validSkill(A)){
            return "Some error occurred in update skill.";
        }

        f.skills[0] = T;
        f.skills[1] = C;
        f.skills[2] = R;
        f.skills[3] = E;
        f.skills[4] = A;

        if(f.available && !f.platformBanned){
            serviceTrees[f.serviceType].remove(f);
        }

        f.computeComposite();

        if(f.available && !f.platformBanned){
            serviceTrees[f.serviceType].insert(f);
        }

        return "updated skills of " + freelancerId + " for " + ServiceType.toString(f.serviceType);
    }

    private boolean validSkill(int s) {
        return s >= 0 && s <= 100;
    }
}