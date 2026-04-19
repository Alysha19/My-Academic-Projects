import java.util.ArrayList;

public class MyAVLTree {

    private static class Node{

        Freelancer freelancer;
        Node left;
        Node right;
        int height;

        Node(Freelancer f){
            this.freelancer = f;
            this.height = 1;
        }

    }

    private Node root;

    public MyAVLTree(){
        this.root = null;
    }

    public void insert(Freelancer f){
        root = insert(root, f);
    }

    public void remove(Freelancer f){
        root = remove(root, f);
    }

    public void collectTopK(int k, ArrayList<Freelancer> out , MyHashMap<Boolean> blacklisted){
        collectTopK(root, k, out , blacklisted);
    }

    public void clear(){
        root = null;
    }

    public boolean isEmpty(){
        return root == null;
    }

    //Helpers
    //comparing method with reversed score comparison logic
    private int compare(Freelancer a, Freelancer b){
        if(a.compositeScore != b.compositeScore){
            return(a.compositeScore > b.compositeScore) ? -1 : 1 ;
        }

        return a.id.compareTo(b.id);
    }

    private int height(Node n){
        return (n==null) ? 0 : n.height;
    }

    private int getBalance(Node n){
        if(n == null) return 0;
        return height(n.left) - height(n.right);
    }

    private Node rightRotate(Node y){
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left) , height(y.right)) + 1;
        x.height = Math.max(height(x.left) , height(x.right)) + 1;

        return x;
    }

    private Node leftRotate(Node x){
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left) , height(x.right)) + 1;
        y.height = Math.max(height(y.left) , height(y.right)) + 1;

        return y;
    }

    private Node insert(Node node, Freelancer f){
        if(node==null){
            return new Node(f);
        }

        int cmp = compare(f, node.freelancer);

        if(cmp < 0){
            node.left = insert(node.left , f);
        }
        else if(cmp > 0){
            node.right = insert(node.right , f);
        }
        else{
            node.freelancer = f;
            return node;
        }

        node.height = 1 + Math.max(height(node.left) , height(node.right));
        //balancing
        int balance = getBalance(node);

        if(balance > 1){
            if(getBalance(node.left) >= 0){
                return rightRotate(node);
            }
            else{
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
        }

        if(balance < -1){
            if(getBalance(node.right) <= 0){
                return leftRotate(node);
            }
            else{
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }
        }

        return node;
    }

    private Node remove(Node node, Freelancer f){
        if(node == null){
            return null;
        }

        int cmp = compare(f , node.freelancer);

        if(cmp < 0){
            node.left = remove(node.left , f);
        }else if(cmp > 0){
            node.right = remove(node.right , f);
        }else{
            if(node.left == null || node.right == null){
                Node temp = (node.left != null) ? node.left : node.right;

                if(temp == null){
                    node = null;
                }else{
                    node = temp;
                }
            }else{

                Node successor = minValueNode(node.right);

                node.freelancer = successor.freelancer;

                node.right = remove(node.right, successor.freelancer);
            }
        }

        if(node==null) return null;

        node.height = 1 + Math.max(height(node.left) , height(node.right));
        //balancing
        int balance = getBalance(node);

        if(balance > 1){
            if(getBalance(node.left) >= 0){
                return rightRotate(node);
            }else{
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
        }

        if(balance < -1){
            if(getBalance(node.right) <= 0){
                return leftRotate(node);
            }else{
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }
        }

        return node;

    }

    private Node minValueNode(Node node){
        Node current = node;
        while(current.left != null){
            current = current.left;
        }
        return current;
    }
    //uses in-order traversal to find the best K candidates.
    private void collectTopK(Node node, int k , ArrayList<Freelancer> out , MyHashMap<Boolean> blacklisted){
        if(node == null || out.size() >= k){
            return;
        }

        collectTopK(node.left, k, out , blacklisted);

        if(out.size() < k && !node.freelancer.platformBanned && !blacklisted.containsKey(node.freelancer.id) && node.freelancer.available){
            out.add(node.freelancer);
        }

        collectTopK(node.right, k , out , blacklisted);
    }

}
