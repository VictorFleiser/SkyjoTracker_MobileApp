package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<Card> cards;

    public Deck(int playerIndex) {
        cards = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            int position = -(i + 1);
            String[] types = {"1", "2", "3", "4", "m2", "m1", "5", "6", "7", "8", "joker", "0", "9", "10", "11", "12", "empty"};
//            cards.add(new Card(types[(int) (Math.random() * types.length)], playerIndex, position));
//            cards.add(new Card("0", playerIndex, position));
            cards.add(new Card("flipped", playerIndex, position));
        }
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public void setCard(int index, Card card) {
        cards.set(index, card);
    }

    public int getConfirmedValue() {
        int sum = 0;
        for (Card card : cards) {
            if (Objects.equals(card.getType(), "flipped")) {
                continue;
            }
            sum += (int) Cards.getCardValue(card.getType());
        }
        return sum;
    }

    public float getEstimatedValue() {
        float sum = 0;
        for (Card card : cards) {
            sum += (float) Cards.getCardValue(card.getType());
            }
        sum = (float) Math.round(sum * 100) / 100;  // round to 2 decimal places
        return sum;
    }
    // Display method will be integrated with Android UI
}
