package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//Manages the collection of cards, including shuffling and drawing.
public class Deck 
{

    private final List<Card> cards = new ArrayList<>();
    private final Random rng;

    public Deck() {
        this(new Random());
    }

    public Deck(Random rng) {
        this.rng = (rng == null) ? new Random() : rng;
    }

    public void reset(List<Card> newCards) {
        cards.clear();
        if (newCards != null) cards.addAll(newCards);
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards, rng);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public Card draw() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    public List<Card> peekAll() {
        return Collections.unmodifiableList(cards);
    }

    public void addToBottom(Card card) {
        if (card != null) cards.add(0, card);
    }

    public void addToTop(Card card) {
        if (card != null) cards.add(card);
    }

    public void clear() {
        cards.clear();
    }
}
