package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Holds and manages the cards currently owned by a player.
public class Hand  {

    private final List<Card> cards = new ArrayList<>();

    public Hand() {
    }

    public Hand(List<Card> initialCards) {
        if (initialCards != null) cards.addAll(initialCards);
    }

    public void add(Card card) {
        if (card != null) cards.add(card);
    }

    public void addAll(List<Card> list) {
        if (list != null) {
            for (Card c : list) add(c);
        }
    }

    public Card removeAt(int index) {
        if (index < 0 || index >= cards.size()) return null;
        return cards.remove(index);
    }

    public boolean remove(Card card) {
        if (card == null) return false;
        return cards.remove(card);
    }

    public Card get(int index) {
        if (index < 0 || index >= cards.size()) return null;
        return cards.get(index);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void clear() {
        cards.clear();
    }

    public List<Card> asList() {
        return Collections.unmodifiableList(cards);
    }

    public int indexOf(Card card) {
        if (card == null) return -1;
        return cards.indexOf(card);
    }
}
