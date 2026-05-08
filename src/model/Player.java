package model;

import java.util.List;
import java.util.Objects;

//Represents a player with a hand of cards, score, and energy.
public class Player {

    private final String name;
    private final Hand hand = new Hand();

    private int score;
    private int energy;

    public Player(String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.score = 0;
        this.energy = 0;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand.asList();
    }

    public int handSize() {
        return hand.size();
    }

    public boolean hasCards() {
        return !hand.isEmpty();
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void addCards(List<Card> cards) {
        hand.addAll(cards);
    }

    public void clearHand() {
        hand.clear();
    }

    public Card playCard() {
        return hand.removeAt(0);
    }

    public Card playCardAt(int index) {
        return hand.removeAt(index);
    }

    public Card removeCard(Card card) {
        if (card == null) return null;
        boolean removed = hand.remove(card);
        return removed ? card : null;
    }

    public Card drawFrom(Deck deck) {
        if (deck == null) return null;
        Card c = deck.draw();
        if (c != null) hand.add(c);
        return c;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public void addScore(int delta) {
        if (delta <= 0) return;
        this.score += delta;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, energy);
    }

    public void addEnergy(int delta) {
        if (delta <= 0) return;
        this.energy += delta;
    }

    public boolean canAfford(Card card) {
        if (card == null) return false;
        return energy >= card.getCost();
    }

    public boolean spendEnergy(int amount) {
        if (amount < 0) return false;
        if (energy < amount) return false;
        energy -= amount;
        return true;
    }
}
