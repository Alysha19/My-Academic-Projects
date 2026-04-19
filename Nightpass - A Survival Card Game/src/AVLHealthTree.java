

import java.util.ArrayList;

public class AVLHealthTree {

    public static int deckCount;
    public int commonAttackValue , height;

    public AVLHealthTree left, right;

    public CardQueue root;

    public AVLHealthTree(int commonAttackValue , CardQueue root, int height){
        this.commonAttackValue = commonAttackValue;
        this.root = root;
        this.height = height;
    }

    public CardQueue insert(CardQueue reference , CardNode insertedCard){

        if(reference==null){
            CardQueue newNode = new CardQueue(insertedCard);
            return newNode;}

        if(insertedCard.healthCurrent < reference.healthValue){reference.left = insert(reference.left , insertedCard);}

        else if(insertedCard.healthCurrent > reference.healthValue){reference.right = insert(reference.right , insertedCard);}
        else{

            reference.enqueue(insertedCard);
            return reference; //No need to balance if we're only adding a card to a queue node
        }
        reference.height = 1 + Math.max(height(reference.left) , height(reference.right));

        int balance = getBalance(reference);

        //Left leaning
        if(balance > 1 && getBalance(reference.left) >= 0){
            return rotateRight(reference);
        }
        //Right leaning
        if(balance < -1 && getBalance(reference.right) <= 0){
            return rotateLeft(reference);
        }
        //Left leaning generally, but the left child is leaning right
        if(balance > 1 && getBalance(reference.left) < 0){
            reference.left = rotateLeft(reference.left);
            return rotateRight(reference);
        }
        //Right leaning generally, but the right child is leaning left
        if(balance < -1 && getBalance(reference.right) > 0){
            reference.right = rotateRight(reference.right);
            return rotateLeft(reference);
        }

        return reference;

    }

    public int height(CardQueue node){
        if(node==null){return 0;}

        return node.height;
    }

    public int getBalance(CardQueue node){
        if(node==null){return 0;}
        else{return height(node.left) - height(node.right);}
    }

    public CardQueue rotateRight(CardQueue y){
        // DO NOT FORGET TO RETURN THE NEW ROOT OR THE PARENTS WILL STILL POINT TO Y
        CardQueue x = y.left;
        CardQueue T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left) , height(y.right)) + 1;
        x.height = Math.max(height(x.left) , height(x.right)) + 1;

        return x;
    }

    public CardQueue rotateLeft(CardQueue x){

        CardQueue y = x.right;
        CardQueue T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left) , height(x.right)) + 1;
        y.height = Math.max(height(y.left) , height(y.right)) + 1;

        return y;
    }

    public CardNode findMin(CardQueue x){
        if(x==null){
            return null;
        }

        CardQueue min = x;
        while(min.left != null){
            min = min.left;
        }

        return min.peek();


    }

    public CardQueue deleteCard(CardNode card , CardQueue root){
        CardQueue foundQueue = findQueue(this.root , card.healthCurrent);

        CardNode temp = foundQueue.dequeue();
        if(foundQueue.isEmpty()){
            root = deleteQueue(root , foundQueue.healthValue);
        }
        return root;
    }
    public CardQueue findQueue(CardQueue root , int healthValue){
        if(root == null){return null;}

        if(root.healthValue < healthValue){
            return findQueue(root.right , healthValue);
        }else if(root.healthValue > healthValue){
            return findQueue(root.left , healthValue);
        }else{
            return root;
        }
    }
    public CardQueue deleteQueue(CardQueue root , int hpCurrent){
        if(root==null){return null;}

        //We need post-order traversal here to ensure we correctly calculate
        //the heights of the subtrees and build up to the current node

        if(hpCurrent < root.healthValue){
            root.left = deleteQueue(root.left , hpCurrent);
        }
        else if(hpCurrent > root.healthValue){
            root.right = deleteQueue(root.right , hpCurrent);
        }
        else{
                if(root.left == null || root.right == null){
                    CardQueue temp = (root.left != null) ? root.left : root.right;

                    if(temp == null){
                        root=null;
                    }
                    else{
                        root=temp;
                    }
                }else{
                    //Two children
                    CardQueue temp = findMinQueue(root.right);

                    root.healthValue = temp.healthValue;
                    root.copyFromAnotherQueue(temp);

                    root.right = deleteQueue(root.right , temp.healthValue);
                }
        }


        if(root==null){return root;}

        root.height = 1 + Math.max(height(root.left) , height(root.right));

        int balance = getBalance(root);

        //Left leaning
        if(balance > 1 && getBalance(root.left) >= 0){
            return rotateRight(root);
        }
        //Right leaning
        if(balance < -1 && getBalance(root.right) <= 0){
            return rotateLeft(root);
        }
        //Left leaning generally, but the left child is leaning right
        if(balance > 1 && getBalance(root.left) < 0){
            root.left = rotateLeft(root.left);
            return rotateRight(root);
        }
        //Right leaning generally, but the right child is leaning left
        if(balance < -1 && getBalance(root.right) > 0){
            root.right = rotateRight(root.right);
            return rotateLeft(root);
        }

        return root;

    }


    // Hcur > Astranger  (for P1 and P2)
    public CardNode collectSurvivor(CardQueue node, int aStr) {
        if (node == null) return null;

        if(node.healthValue > aStr){
            CardNode leftResult = collectSurvivor(node.left, aStr);
            if(leftResult!= null){
                return leftResult;
            }
            return node.peek();
        }
        else{return collectSurvivor(node.right , aStr);} //Check if left and current node don't return anything
    }

    // Hcur <= Astranger  (for P3)
    public void collectKillAndDie(CardQueue node, int aStr, ArrayList<CardNode> out) {
        if (node == null) return;
        collectKillAndDie(node.left, aStr, out);
        if (node.healthValue <= aStr) out.add(node.peek());
        collectKillAndDie(node.right, aStr, out);
    }


    public CardQueue findMinQueue(CardQueue node){
        if(node == null){return null;}

        if(node.left != null){return findMinQueue(node.left);}

        return node;
    }

    public CardNode collectVictim(CardQueue node, int aStr) {
        if (node == null) return null;

        if(node.healthValue <= aStr){
            // This card dies - try to find smaller in left subtree
            CardNode leftResult = collectVictim(node.left, aStr);
            if(leftResult != null) {
                return leftResult;
            }
            return node.peek();
        }
        else{
            // Card survives, need to find one that dies in right subtree
            return collectVictim(node.right, aStr);
        }
    }

}
