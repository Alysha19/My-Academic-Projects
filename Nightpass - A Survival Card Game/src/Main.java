
/**
 * CMPE 250 Project 1 - Nightpass Survivor Card Game
 *
 * This skeleton provides file I/O infrastructure. Implement your game logic
 * as you wish. There are some import that is suggested to use written below.
 * You can use them freely and create as manys classes as you want. However,
 * you cannot import any other java.util packages with data structures, you
 * need to implement them yourself. For other imports, ask through Moodle before
 * using.
 *
 * TESTING YOUR SOLUTION:
 * ======================
 *
 * Use the Python test runner for automated testing:
 *
 * python test_runner.py              # Test all cases
 * python test_runner.py --type type1 # Test only type1
 * python test_runner.py --type type2 # Test only type2
 * python test_runner.py --verbose    # Show detailed diffs
 * python test_runner.py --benchmark  # Performance testing (no comparison)
 *
 * Flags can be combined, e.g.:
 * python test_runner.py -bv              # benchmark + verbose
 * python test_runner.py -bv --type type1 # benchmark + verbose + type1
 * python test_runner.py -b --type type2  # benchmark + type2
 *
 * MANUAL TESTING (For Individual Runs):
 * ======================================
 *
 * 1. Compile: cd src/ && javac *.java
 * 2. Run: java Main ../testcase_inputs/test.txt ../output/test.txt
 * 3. Compare output with expected results
 *
 * PROJECT STRUCTURE:
 * ==================
 *
 * project_root/
 * ├── src/                     # Your Java files (Main.java, etc.)
 * ├── testcase_inputs/         # Input test files
 * ├── testcase_outputs/        # Expected output files
 * ├── output/                  # Generated outputs (auto-created)
 * └── test_runner.py           # Automated test runner
 *
 * REQUIREMENTS:
 * =============
 * - Java SDK 8+ (javac, java commands)
 * - Python 3.6+ (for test runner)
 *
 * @author Ali Şimşek
 */

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.math.*;

public class Main {

    AVLAttackTree attackTree = new AVLAttackTree();
    AVLDiscardPile discardPile = new AVLDiscardPile();
    int survivorScore = 0;
    int strangerScore = 0;
    private int totalDeckSize;
    private int totalDiscardSize;

    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 2) {
            System.out.println("Usage: java Main <input_file> <output_file>");
            System.out.println("Example: java Main ../testcase_inputs/test.txt ../output/test.txt");
            return;
        }

        String inFile = args[0];
        String outFile = args[1];

        // Initialize file reader
        Scanner reader = null;
        try {
            reader = new Scanner(new File(inFile));
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found: " + inFile);
            e.printStackTrace();
            return;
        }

        // Initialize file writer
        FileWriter writer = null;
        try {
            writer = new FileWriter(outFile);
        } catch (IOException e) {
            System.out.println("Writing error: " + outFile);
            e.printStackTrace();
            if (reader != null)
                reader.close();
            return;
        }

        Main game = new Main();

        // Process commands line by line
        try {
            while (reader.hasNext()) {
                String line = reader.nextLine();
                Scanner scanner = new Scanner(line);
                String command = scanner.next();
                String out = "";

                switch (command) {
                    case "draw_card": {
                        String name = "";
                        int att = 0;
                        int hp = 0;
                        if (scanner.hasNext())
                            name = scanner.next();
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();
                        out = game.draw_card(name, att, hp); // suggested method for draw_card command
                        break;
                    }
                    case "battle": {
                        int att = 0;
                        int hp = 0;
                        int heal = 0;
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();
                        if (scanner.hasNext())
                            heal = scanner.nextInt();
                        out = game.battle(att, hp, heal); // suggested method for battle command
                        break;
                    }
                    case "find_winning": {
                        out = game.findWinning(); // suggested method for find_winning command
                        break;
                    }
                    case "deck_count": {
                        out = game.deckCount(); // suggested method for deck_count command
                        break;
                    }


                    //Comment this out if you are going to implement type-2 commands
                    case "discard_pile_count": {
                        out = game.discardPileCount(); // suggested method for discard_pile_count command
                        break;
                    }

                    case "steal_card": {
                        int att = 0;
                        int hp = 0;
                        if (scanner.hasNext())
                            att = scanner.nextInt();
                        if (scanner.hasNext())
                            hp = scanner.nextInt();
                        out = game.steal_card(att, hp); // suggested method for steal_card command
                        break;
                    }
                    default: {
                        System.out.println("Invalid command: " + command);
                        scanner.close();
                        writer.close();
                        reader.close();
                        return;
                    }
                }

                scanner.close();

                try {
                    writer.write(out);
                    writer.write("\n"); // uncomment if each output needs to be in a new line and
                    // you did not implement that inside the functions.
                } catch (IOException e2) {
                    System.out.println("Writing error");
                    e2.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("Error processing commands: " + e.getMessage());
            e.printStackTrace();
        }

        // Clean up resources
        try {
            writer.close();
        } catch (IOException e2) {
            System.out.println("Writing error");
            e2.printStackTrace();
        }

        if (reader != null) {
            reader.close();
        }

        System.out.println("end");
        return;
    }
    public String draw_card(String name , int attack , int hp){
        attackTree.addCard(attack , new CardNode(name , attack , hp));
        totalDeckSize++;
        return "Added " + name + " to the deck";
    }

    public String battle(int strangerAtt , int strangerHp , int healpts){
        boolean cardFound = false;
        String output, playedCard = "";
        int priority = 0;
        CardNode selectedCard = null;

        for(priority = 1 ; priority <= 4 ; priority++){
            selectedCard = attackTree.findCard(strangerHp , strangerAtt , priority);
            if(selectedCard != null){
                cardFound = true;
                break;
            }
        }

        if(!cardFound){

            strangerScore += 2;

            int revivedCount = 0;
            if(healpts > 0){
                revivedCount = healingPhase(healpts);
            }
            output = "No card to play, " + revivedCount + " cards revived";
            return output;
        }
        //Card Found
        playedCard = selectedCard.name;
        int originalAttack = selectedCard.attackCurrent;
        int cardHealthBefore = selectedCard.healthCurrent;
        int originalHealth = selectedCard.healthCurrent;

        //Apply damages
        int cardHealthAfter = Math.max(0 , selectedCard.healthCurrent - strangerAtt);
        int strangerHealthAfter = Math.max(0 , strangerHp - selectedCard.attackCurrent);


        //Deduce scores
        if(cardHealthAfter <= 0){
            strangerScore += 2;
        }else if(cardHealthAfter < cardHealthBefore){
            strangerScore += 1;
        }

        if(strangerHealthAfter <= 0){
            survivorScore += 2;
        } else if (strangerHealthAfter < strangerHp) {
            survivorScore += 1;
        }

        boolean returned = cardHealthAfter > 0;

        //Deleting the card before returning it to the deck or sending it to the discard pile
        attackTree.root = deleteCard(attackTree.root , selectedCard);
        totalDeckSize--;
        if(returned){

            //Creating a new card, changing the existing card's stats caused problems and resulted in malformed trees
            CardNode updatedCard = new CardNode(selectedCard.name, selectedCard.attackBase, selectedCard.healthBase);
            updatedCard.healthCurrent = cardHealthAfter;
            updatedCard.attackCurrent = selectedCard.attackBase;
            updatedCard.updateAttack(); // This will recalculate attack based on health ratio

            attackTree.addCard(updatedCard.attackCurrent, updatedCard);
            totalDeckSize++;

        }else{
            //selectedCard.discardPileID = DiscardPile.discardCount++;
            discardPile.addCard(selectedCard);
            totalDiscardSize++;
        }

        int revivedCount = 0;
        if(healpts > 0){
            revivedCount = healingPhase(healpts);
        }
        if(returned){
            output = "Found with priority " + priority + ", Survivor plays " + playedCard + ", the played card returned to deck, " + revivedCount + " cards revived";

        }else{
            output = "Found with priority " + priority + ", Survivor plays " + playedCard + ", the played card is discarded, " + revivedCount + " cards revived";

        }

        return output;

    }

    //Method for healing phase
    private int healingPhase(int healPool){

        int revivedCount = 0;

        //Priority 1 and 2
        while(healPool > 0){
            CardNode cardToRevive = discardPile.findLargestMissingHealth(healPool);
            if(cardToRevive == null){
                break; //no card can be revived
            }

            int missingHealth = cardToRevive.healthBase - cardToRevive.revivalProgress;

            healPool -= missingHealth;


            //Remove the fully revived card from the discard pile
            int newAttackBase = (int)Math.floor(cardToRevive.attackBase * 0.90);
            discardPile.removeCard(cardToRevive);
            totalDiscardSize--;

            // Create FRESH card
            CardNode revivedCard = new CardNode(cardToRevive.name, newAttackBase, cardToRevive.healthBase);
            revivedCard.healthCurrent = cardToRevive.healthBase;  // Fully healed
            revivedCard.attackCurrent = newAttackBase;

            attackTree.addCard(revivedCard.attackCurrent, revivedCard);
            totalDeckSize++;

            revivedCount++;
        }

        //Priority 3
        if(healPool > 0){
            CardNode cardToPartialRevive = discardPile.findSmallestMissingHealth();
            if(cardToPartialRevive != null){

                discardPile.removeCard(cardToPartialRevive); //Reinsert the card to not break the further priority checks

                cardToPartialRevive.attackBase = (int)Math.floor(cardToPartialRevive.attackBase * 0.95);

                cardToPartialRevive.revivalProgress += healPool;


                discardPile.reinsertCard(cardToPartialRevive);

                healPool = 0; //No need for checking if the card is revived since the priority 1&2 took care of that, just set this 0


            }
        }
        return revivedCount;
    }

    private AVLHealthTree deleteCard(AVLHealthTree node , CardNode card){
        if(node == null){return null;}

        //Classic binary search tree algorithm
        if(card.attackCurrent < node.commonAttackValue){
            node.left = deleteCard(node.left , card);
        }
        else if(card.attackCurrent > node.commonAttackValue){
            node.right = deleteCard(node.right , card);
        } else{
            node.root = node.deleteCard(card , node.root);

            if(node.root == null){
                //Checking if it has only 1 or 0 children
                if(node.left == null && node.right == null){
                    return null;
                }else if(node.left == null){return node.right;}
                else if(node.right == null){return node.left;}
                else{
                    //Two children
                    AVLHealthTree successor = findMinAttackNode(node.right);
                    node.commonAttackValue = successor.commonAttackValue;
                    node.root = successor.root;
                    node.right = deleteMinAttackNode(node.right);
                }
            }
        }
        if(node==null){
            return node;
        }

        //Balance the AVLTree after
        node.height = 1 + Math.max(attackTree.height(node.left) , attackTree.height(node.right));

        int balance = attackTree.getBalance(node);

        if(balance > 1 && attackTree.getBalance(node.left) >= 0){
            return attackTree.rotateRight(node);
        }
        if(balance < -1 && attackTree.getBalance(node.right) <= 0){
            return attackTree.rotateLeft(node);
        }
        if(balance > 1 && attackTree.getBalance(node.left) < 0){
            node.left = attackTree.rotateLeft(node.left);
            return attackTree.rotateRight(node);
        }
        if(balance < -1 && attackTree.getBalance(node.right) > 0){
            node.right = attackTree.rotateRight(node.right);
            return attackTree.rotateLeft(node);
        }
        return node;
    }

    private AVLHealthTree findMinAttackNode(AVLHealthTree node){
        while(node.left != null){
            node = node.left;
        }
        return node;
    }

    private AVLHealthTree deleteMinAttackNode(AVLHealthTree node){
        if(node.left == null){
            return node.right;
        }
        node.left = deleteMinAttackNode(node.left);

        node.height = 1 + Math.max(attackTree.height(node.left), attackTree.height(node.right));

        int balance = attackTree.getBalance(node);

        //Since the node with minimum value cannot have a left child, it is sufficient to only check for two cases
        if(balance < -1 && attackTree.getBalance(node.right) <= 0){
            return attackTree.rotateLeft(node);
        }

        if(balance < -1 && attackTree.getBalance(node.right) > 0){
            node.right = attackTree.rotateRight(node.right);
            return attackTree.rotateLeft(node);
        }

        return node;
    }

    public String deckCount(){;
        return "Number of cards in the deck: " + totalDeckSize ;
    }

    public int countCards(AVLHealthTree node){
        if(node==null) return 0;
        int count = countHealthTreeCards(node.root);
        count += countCards(node.left);
        count += countCards(node.right);
        return count;
    }

    private int countHealthTreeCards(CardQueue node){
        if(node == null) return 0;
        return node.size() + countHealthTreeCards(node.left) + countHealthTreeCards(node.right);
    }

    public String discardPileCount(){
        return "Number of cards in the discard pile: " + totalDiscardSize;
    }

    public String findWinning(){
        if(survivorScore >= strangerScore){
            return "The Survivor, Score: " + survivorScore;
        }else{
            return "The Stranger, Score: " + strangerScore;
        }
    }

    public String steal_card(int attackLimit, int healthLimit){
        CardNode stolen = findCardToSteal(attackTree.root , attackLimit , healthLimit);

        if(stolen == null){
            return "No card to steal";
        }

        attackTree.root = deleteCard(attackTree.root , stolen);
        totalDeckSize--;

        return "The Stranger stole the card: " + stolen.name;
    }

    private CardNode findCardToSteal(AVLHealthTree attackNode , int attackLimit , int healthLimit){
        /**
         * Finds the best steal candidate, first checks the current node and the left subtree.
         * Prefers the left subtree if both current node and left subtree returns a result.
         * Checks the right subtree in case both left subtree and the current node does not return a candidate.
         */

        if(attackNode==null){return null;}

        CardNode best = null;

        if(attackNode.commonAttackValue > attackLimit){
            CardNode candidate = findMinHealthInQueue(attackNode.root , healthLimit);
            if(candidate != null) best = candidate;

            CardNode leftBest = findCardToSteal(attackNode.left , attackLimit, healthLimit);
            if(leftBest != null && (best == null || isBetterStealChoice(leftBest, best))){
                best = leftBest;
            }
        }
        //Checking the right subtree in case the current node and the left subtree do not have a candidate
        CardNode rightBest = findCardToSteal(attackNode.right , attackLimit , healthLimit);
        if(rightBest != null && (best == null || isBetterStealChoice(rightBest, best))){
            best = rightBest;
        }

        return best;

    }

    //Helper method for stealing mechanism
    private CardNode findMinHealthInQueue(CardQueue healthNode , int healthLimit){
        if(healthNode == null) return null;

        if(healthNode.healthValue > healthLimit){

            CardNode leftResult = findMinHealthInQueue(healthNode.left , healthLimit);
            if(leftResult != null){
                return leftResult;
            }

            return healthNode.peek();
        }else{
            return findMinHealthInQueue(healthNode.right, healthLimit);
        }
    }

    //Helper method for keeping the if checks simple
    private boolean isBetterStealChoice(CardNode candidate, CardNode current){
        if(candidate.attackCurrent < current.attackCurrent) return true;
        if(candidate.attackCurrent == current.attackCurrent){
            return candidate.healthCurrent < current.healthCurrent;
        }
        return false;
    }


}
