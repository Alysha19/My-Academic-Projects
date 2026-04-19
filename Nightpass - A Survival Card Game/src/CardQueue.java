

import java.util.ArrayList;

public class CardQueue {

    private ArrayList<CardNode> queue;

    public CardQueue left;
    public CardQueue right;
    public int healthValue;
    public int height;

    public CardQueue(CardNode head){
        this.queue = new ArrayList<>();
        enqueue(head);
        this.healthValue = head.healthCurrent;
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

    public void copyFromAnotherQueue(CardQueue other){
        this.queue = other.getAllCards();
    }




}
