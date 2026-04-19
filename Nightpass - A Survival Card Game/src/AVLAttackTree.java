

import java.sql.Array;
import java.util.ArrayList;

public class AVLAttackTree {

    public AVLHealthTree root;

    public AVLHealthTree insert(AVLHealthTree reference , int commonAttackValue , CardNode insertedCard){

        if(reference == null){
            CardQueue newQueue = new CardQueue(insertedCard);
            return new AVLHealthTree(commonAttackValue , newQueue , 1);}

        if(commonAttackValue == reference.commonAttackValue){
            reference.root = reference.insert(reference.root ,insertedCard);
            return reference;
        }
        if(commonAttackValue < reference.commonAttackValue){reference.left = insert(reference.left , commonAttackValue , insertedCard);}
        if(commonAttackValue > reference.commonAttackValue){reference.right = insert(reference.right , commonAttackValue , insertedCard);}

        reference.height = 1 + Math.max(height(reference.left) , height(reference.right));

        int balance = getBalance(reference);

        if(balance > 1 && getBalance(reference.left) >= 0){
            return rotateRight(reference);
        }
        if(balance < -1 && getBalance(reference.right) <= 0){
            return rotateLeft(reference);
        }
        if(balance > 1 && getBalance(reference.left) < 0){
            reference.left = rotateLeft(reference.left);
            return rotateRight(reference);
        }
        if(balance <-1 && getBalance(reference.right) > 0){
            reference.right = rotateRight(reference.right);
            return rotateLeft(reference);
        }
        return reference;
    }

    public int height(AVLHealthTree node){
        if(node==null){return 0;}
        return node.height;
    }

    public int getBalance(AVLHealthTree node){
        if(node==null){return 0;}

        return height(node.left) - height(node.right);
    }

    public AVLHealthTree rotateLeft(AVLHealthTree x){

        if(x==null || x.right == null){return x;}

        AVLHealthTree y = x.right;
        AVLHealthTree T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left) , height(x.right)) + 1;
        y.height = Math.max(height(y.left) , height(y.right)) + 1;

        return y;
    }

    public AVLHealthTree rotateRight(AVLHealthTree y){

        if(y == null || y.left == null){return y;}

        AVLHealthTree x = y.left;
        AVLHealthTree T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left) , height(y.right)) + 1;
        x.height = Math.max(height(x.left) , height(x.right)) + 1;

        return x;
    }

    //Figured this would be a nicer way to keep it simpler to access the insertion method from outside of this class
    //Also helps me access the root
    public void addCard(int attackValue , CardNode card){
        this.root = insert(this.root , attackValue , card);
    }

    public CardNode findCard(int strangerHP, int strangerAttack , int priority){

        CardNode candidate;
        ArrayList<CardNode> candidates = new ArrayList<>();

        switch(priority){
            case 1:
                candidate = findPriority1(root , strangerHP , strangerAttack);
                break;
            case 2 :
                candidate = findPriority2(root , strangerHP , strangerAttack);
                break;
            case 3 :
                candidate = findPriority3(root , strangerHP , strangerAttack);
                break;

            default:
                candidate = findPriority4(root);
                break;
        }
        return candidate;
    }

    //PRIORITY 1: SURVIVE AND KILL
    private CardNode findPriority1(AVLHealthTree node , int strangerHP, int strangerAttack){
        CardNode candidate;

        if(node==null) return null;

        if(node.commonAttackValue >= strangerHP){

            CardNode candidate1 = node.collectSurvivor(node.root , strangerAttack);

            //Might have lower attack values that are sufficient still
            CardNode candidate2 = findPriority1(node.left , strangerHP , strangerAttack);
            //turns out I was wrong to only look for left subtree

            if(candidate2 != null){return candidate2;}
            else if(candidate1 != null){return candidate1;}
            else{return findPriority1(node.right , strangerHP , strangerAttack);}

        }
        else{
            //Attack value is too weak for this node, check greater values
            return findPriority1(node.right , strangerHP , strangerAttack);
        }


    }

    //PRIORITY 2: SURVIVE AND DON'T KILL
    private CardNode findPriority2(AVLHealthTree node, int strangerHP, int strangerAttack){
        CardNode candidate;

        if(node==null) return null;

        if(node.commonAttackValue < strangerHP){
            //This attack value works, doesnt kill the stranger

            CardNode candidate1 = node.collectSurvivor(node.root , strangerAttack);

            //We need maximal damage, so check higher attack values
            CardNode candidate3 = findPriority2(node.right , strangerHP , strangerAttack);

            //Always check the left subtree
            if(candidate3 != null){return candidate3;}
            else if(candidate1 != null){return candidate1;}
            else{return findPriority2(node.left, strangerHP , strangerAttack);}

        }else{
            //Attack too strong, check lower levels of attacks.
            return findPriority2(node.left, strangerHP , strangerAttack);
        }

    }

    //PRIORITY 3: DON'T SURVIVE AND KILL
    private CardNode findPriority3(AVLHealthTree node, int strangerHP, int strangerAttack){
        ArrayList<CardNode> candidates = new ArrayList<>();

        if(node==null) return null;

        if(node.commonAttackValue >= strangerHP){
            //This attack value is sufficient


            CardNode candidate = node.collectVictim(node.root , strangerAttack);

            CardNode leftCandidate = findPriority3(node.left , strangerHP , strangerAttack);

            //Always check the right subtree
            CardNode rightCandidate = findPriority3(node.right , strangerHP , strangerAttack);

            if(leftCandidate != null){return leftCandidate;}
            if(candidate != null){return candidate;}
            else{return rightCandidate;}

        }else{
            //Card does not kill, check higher levels of attack values.

            return findPriority3(node.right , strangerHP , strangerAttack);
        }

    }

    //PRIORITY 4: MAXIMUM DAMAGE
    private CardNode findPriority4(AVLHealthTree node){
        ArrayList<CardNode> candidates = new ArrayList<>();

        if(node==null) return null;

        //Find the maximum attack value, do not care about health
        AVLHealthTree maxNode = node;
        while(maxNode.right != null){
            maxNode = maxNode.right;
        }

        //Now, get all cards with minimum health

        return maxNode.findMin(maxNode.root);

    }

    //I need a best-card selecting and tie-breaking method -- (used to in my previous attempts, do not need this anymore)
    private CardNode selectBest(ArrayList<CardNode> candidates , int priority){

        if(candidates.isEmpty()){return null;}

        CardNode best = candidates.get(0);

        for(int i=0 ; i < candidates.size() ; i++){
            CardNode current = candidates.get(i);
            boolean shouldReplace = false;

            switch(priority){
                case 1: // Minimal attack, then minimal health
                    if(current.attackCurrent < best.attackCurrent){
                        shouldReplace = true;
                    }else if(current.attackCurrent == best.attackCurrent){
                        if(current.healthCurrent < best.healthCurrent) {
                            shouldReplace = true;
                        }
                    }
                    break;
                case 2: //Maximal attack, then minimal health

                    if(current.attackCurrent > best.attackCurrent){
                        shouldReplace = true;
                    }else if(current.attackCurrent == best.attackCurrent){
                        if(current.healthCurrent < best.healthCurrent){
                            shouldReplace = true;
                        }
                    }
                    break;
                case 3: //Same as case 1

                    if(current.attackCurrent < best.attackCurrent){
                        shouldReplace = true;
                    }else if(current.attackCurrent == best.attackCurrent){
                        if(current.healthCurrent < best.healthCurrent){
                            shouldReplace = true;
                        }
                    }
                    break;
                case 4: //Minimal health, as attack is already max
                    if(current.healthCurrent < best.healthCurrent){
                        shouldReplace = true;
                    }
                    break;
            }

            if(shouldReplace){
                best = current;
            }

        }
        return best;

    }


}
