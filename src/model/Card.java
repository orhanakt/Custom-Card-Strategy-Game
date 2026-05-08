package model;

import java.util.Objects;

//Represents a playable card with stats such as attack, defense, and energy cost.
public class Card {

    private final String id;
    private final String name;
    private final CardType type;

    private final int attack;
    private final int defense;
    private final int cost;

    private final String imagePath;

    public Card(String id, String name, CardType type, int attack, int defense, int cost, String imagePath) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");

        if (attack < 0 || defense < 0 || cost < 0) {
            throw new IllegalArgumentException("attack/defense/cost cannot be negative");
        }

        this.attack = attack;
        this.defense = defense;
        this.cost = cost;

        this.imagePath = imagePath;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CardType getType() {
        return type;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getCost() {
        return cost;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int combatScore() {
        return attack + defense;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", atk=" + attack +
                ", def=" + defense +
                ", cost=" + cost +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
