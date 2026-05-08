package engine;

import model.Card;

//Defines the core combat and scoring rules of the game.
public class GameRules {

    public int effectiveDefense(Card card) {
        if (card == null) return 0;
        return Math.max(0, card.getDefense());
    }

    public int damage(Card attacker, int defenderEffectiveDefense) {
        if (attacker == null) return 0;
        int atk = Math.max(0, attacker.getAttack());
        return Math.max(0, atk - Math.max(0, defenderEffectiveDefense));
    }

    public int basePointsFromDamage(int damage) {
        return Math.max(0, damage);
    }

    public int applyMultiplier(int points) {
        return Math.max(0, points);
    }
}
