package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pile implements Serializable {
    // class represents a pile of card
    ArrayList<Card> cards;
    public Pile() {
        cards = new ArrayList<>();
    }
    public void addCard(Card card) {
        cards.add(card);
    }
    public Card removeCard() {
        return cards.remove(cards.size() - 1);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void swapCards(int cardA, int cardB) {
        Card temp = cards.get(cardA);
        cards.set(cardA, cards.get(cardB));
        cards.set(cardB, temp);
    }
}
