//Figured it would be better to make a separate queue node for the discard pile.

import java.util.ArrayList;

public class DiscardQueue {

    private ArrayList<CardNode> queue;

    public DiscardQueue left;
    public DiscardQueue right;
    public int missingHealthValue;
    public int height;

    public DiscardQueue(CardNode head , int missingHealthValue){
        this.queue = new ArrayList<>();
        enqueue(head);
        this.missingHealthValue = missingHealthValue;
        this.height = 1;
    }

    public void enqueue(CardNode card){
        queue.add(card);
    }

    public CardNode dequeue(){
        if(queue.isEmpty()){
            return null;
        }
        return queue.removeFirst();
    }

    public CardNode peek(){
        if(queue.isEmpty()){
            return null;
        }
        return queue.getFirst();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }

    public int size(){
        return queue.size();
    }

    public void clear(){
        queue.clear();
    }

    public boolean contains(CardNode card){
        return queue.contains(card);
    }

    public ArrayList<CardNode> getAllCards() {
        return new ArrayList<>(queue);
    }

    public void copyFromAnotherQueue(DiscardQueue other){
        this.queue = other.getAllCards();
    }
}
