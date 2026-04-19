

public class AVLDiscardPile {


    private DiscardQueue root;
    public static int discardCount = 0;
    private int totalCards = 0;

    public AVLDiscardPile(){
        root = null;
    }
    //Helper methods for rotations to balance the tree
    private DiscardQueue rotateLeft(DiscardQueue x){
        DiscardQueue y = x.right;
        DiscardQueue T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = 1 + Math.max(height(x.left), height(x.right));
        y.height = 1 + Math.max(height(y.left), height(y.right));

        return y;
    }

    private DiscardQueue rotateRight(DiscardQueue y){
        DiscardQueue x = y.left;
        DiscardQueue T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = 1 + Math.max(height(y.left), height(y.right));
        x.height = 1 + Math.max(height(x.left), height(x.right));

        return x;
    }

    private int height(DiscardQueue node){
        return node == null ? 0 : node.height;
    }

    private int getBalance(DiscardQueue node){
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private DiscardQueue balance(DiscardQueue node){
        int balance = getBalance(node);

        // Left heavy
        if(balance > 1){
            if(getBalance(node.left) < 0){
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }

        // Right heavy
        if(balance < -1){
            if(getBalance(node.right) > 0){
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }

        return node;
    }
    private DiscardQueue deleteMinNode(DiscardQueue node){
        if(node.left == null) return node.right;

        node.left = deleteMinNode(node.left);
        node.height = 1 + Math.max(height(node.left), height(node.right));
        return balance(node);
    }
    public int getCount(){
        return totalCards;
    }
    private DiscardQueue delete(DiscardQueue node, CardNode card, int missing){
        if(node == null) return null;

        if(missing < node.missingHealthValue){
            node.left = delete(node.left, card, missing);
        } else if(missing > node.missingHealthValue){
            node.right = delete(node.right, card, missing);
        } else {
            // Found the Hmissing level, remove from queue
            node.dequeue();  // Remove first card in queue (the one we're reviving)

            // If queue is now empty, remove this node from tree
            if(node.isEmpty()){
                if(node.left == null) return node.right;
                if(node.right == null) return node.left;

                // Two children: find successor
                DiscardQueue successor = findMinNode(node.right);
                node.missingHealthValue = successor.missingHealthValue;
                node.copyFromAnotherQueue(successor);
                node.right = deleteMinNode(node.right);
            }
            if(node != null) {
                node.height = 1 + Math.max(height(node.left), height(node.right));
                return balance(node);
            }

            return node;  // Queue not empty, keep node
        }

        if(node == null) return null;

        node.height = 1 + Math.max(height(node.left), height(node.right));
        return balance(node);
    }
    public void removeCard(CardNode card){
        int missing = card.healthBase - card.revivalProgress;
        root = delete(root, card, missing);
        totalCards--;
    }
    private DiscardQueue findMinNode(DiscardQueue node){
        while(node.left != null){
            node = node.left;
        }
        return node;
    }
    public CardNode findSmallestMissingHealth(){
        if(root == null) return null;

        DiscardQueue min = findMinNode(root);
        return min.peek();  // First in queue (smallest discardPileID)
    }
    public void addCard(CardNode card){
        card.healthCurrent = 0;
        card.revivalProgress = 0;

        int missing = card.healthBase - card.revivalProgress;
        root = insert(root, card, missing);
        totalCards++;
    }
    private DiscardQueue insert(DiscardQueue node, CardNode card, int missing){
        if(node == null){
            return new DiscardQueue(card, missing);
        }

        if(missing < node.missingHealthValue){
            node.left = insert(node.left, card, missing);
        } else if(missing > node.missingHealthValue){
            node.right = insert(node.right, card, missing);
        } else {
            // Same Hmissing , add to queue
            node.enqueue(card);
            return node;  // No rebalancing needed when adding to queue
        }

        // Update height and rebalance
        node.height = 1 + Math.max(height(node.left), height(node.right));
        return balance(node);
    }
    public CardNode findLargestMissingHealth(int healPool){
        DiscardQueue foundNode = findLargestNode(root, healPool);
        if(foundNode == null) return null;
        return foundNode.peek();  // First in queue
    }

    private DiscardQueue findLargestNode(DiscardQueue node, int healPool){
        if(node == null) return null;

        if(node.missingHealthValue <= healPool){
            // This node qualifies, but check if right has larger
            DiscardQueue rightResult = findLargestNode(node.right, healPool);
            if(rightResult != null){
                return rightResult;  // Found larger in right
            }
            return node;  // This is the largest that fits
        } else {
            // This node too large, only check left
            return findLargestNode(node.left, healPool);
        }
    }
    public void reinsertCard(CardNode card){
        int missing = card.healthBase - card.revivalProgress;
        root = insert(root, card, missing);
        // Don't increment totalCards as the card was already in discard pile
    }
}
