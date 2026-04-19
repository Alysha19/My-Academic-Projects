

public class CardNode {

    int height, attackBase , healthBase , attackCurrent , healthCurrent, discardPileID;
    String name;

    int revivalProgress;

    CardNode(String name , int attackBase , int healthBase){
        this.name = name;
        this.attackBase = attackBase;
        this.healthBase = healthBase;
        attackCurrent = this.attackBase;
        healthCurrent = this.healthBase;
        height = 1;
    }

    public static void attack(CardNode card , int attackCurrent){

        card.healthCurrent = Integer.max(0, card.healthCurrent - attackCurrent);

    }

    public void updateAttack(){
        if(this.healthBase > 0){
            this.attackCurrent = Math.max(1 , (int)Math.floor((double)this.attackBase * this.healthCurrent / this.healthBase));
        }
    }
}