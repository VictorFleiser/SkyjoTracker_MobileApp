package com.example.skyjocounter;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cards implements Serializable {
    private static final long serialVersionUID = 1L;
    // class to store the amount of cards known and their value, as well as the average value of all flipped cards

    // hash map of all cards known and their amount (key:card, value:amount)
    // possible card values : "flipped", "m2", "m1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "joker"
    private final Map<String, Integer> knownCards;
    // hash map of all cards in the game and their amount (key:card, value:amount)
    private static final Map<String, Integer> amountOfCards = new HashMap<>();
    static {
        amountOfCards.put("m2", 5);
        amountOfCards.put("m1", 10);
        amountOfCards.put("0", 14);
        for (int i = 1; i <= 12; i++) {
            amountOfCards.put(Integer.toString(i), 10);
        }
        amountOfCards.put("joker", 1);
    }
    // hash map of all cards in the game and their value (key:card, value:value)
    private static final Map<String, Integer> cardValues = new HashMap<>();
    static {
        cardValues.put("m2", -2);
        cardValues.put("joker", -2);
        cardValues.put("m1", -1);
        cardValues.put("0", 0);
        cardValues.put("1", 1);
        cardValues.put("2", 2);
        cardValues.put("3", 3);
        cardValues.put("4", 4);
        cardValues.put("5", 5);
        cardValues.put("6", 6);
        cardValues.put("7", 7);
        cardValues.put("8", 8);
        cardValues.put("9", 9);
        cardValues.put("10", 10);
        cardValues.put("11", 11);
        cardValues.put("12", 12);
        cardValues.put("empty", 0);
    }
    // average value of all flipped cards in the game
    private static float averageValueFlipped;
    // constructor : 150 flipped cards, 0 known cards
    public Cards() {
        knownCards = new HashMap<>();
        knownCards.put("flipped", 150);
        knownCards.put("m2", 0);
        knownCards.put("m1", 0);
        for (int i = 0; i <= 12; i++) {
            knownCards.put(Integer.toString(i), 0);
        }
        knownCards.put("joker", 0);
//        Log.d("Cards", "Cards created");
        calculateAverageValueFlipped();
    }

    // get the value of a card
    public static Object getCardValue(String card) {
        if ("flipped".equals(card)) {
            return (float) Math.round(averageValueFlipped * 100) / 100;  // round to 2 decimal places;
        }
        return cardValues.get(card);
    }
    // add a card to the known cards (removes a flipped card)
    public void addCard(String card) {
        try {
            knownCards.put(card, knownCards.get(card) + 1);
            knownCards.put("flipped", knownCards.get("flipped") - 1);
            calculateAverageValueFlipped();
        }
        catch (NullPointerException e) {
            System.out.println("Error adding card to known cards : " + e.getMessage());
        }
    }
    // change the type of a card, removing the old type and adding the new type
    public void changeCardType(String oldType, String newType) {
        try {
            knownCards.put(oldType, knownCards.get(oldType) - 1);
            knownCards.put(newType, knownCards.get(newType) + 1);
            calculateAverageValueFlipped();
        }
        catch (NullPointerException e) {
            System.out.println("Error changing card type : " + e.getMessage());
        }
    }

    // calculate the average value of all flipped cards
    public void calculateAverageValueFlipped() {
        try {
            int sum = 0;
            int numberOfFlippedCards = 0;
//            Log.d("Cards", "Calculating average value of flipped cards");
            for (Map.Entry<String, Integer> entry : knownCards.entrySet()) {
//                Log.d("Card", entry.getKey() + " : " + entry.getValue());
                if ("flipped".equals(entry.getKey())) {
                    numberOfFlippedCards = entry.getValue();
                    continue;
                }
                int cardValue = cardValues.get(entry.getKey());
                int cardAmountFlipped = amountOfCards.get(entry.getKey()) - entry.getValue();
                sum += cardValue * cardAmountFlipped;
//                Log.d("Card2", cardValue + " : " + cardAmountFlipped);
            }
            if (numberOfFlippedCards == 0) {
                averageValueFlipped = 0;    // no flipped cards left, average value is 0 (arbitrary)
            }
            else {
                averageValueFlipped = (float) sum / numberOfFlippedCards;
                averageValueFlipped = (float) Math.round(averageValueFlipped * 100) / 100;  // round to 2 decimal places
            }
        }
        catch (NullPointerException e) {
            System.out.println("Error calculating average value of flipped cards : " + e.getMessage());
            averageValueFlipped = 0;
        }
    }
    // get average value of all flipped cards
    public float getAverageValueFlipped() {
        calculateAverageValueFlipped();
        return (float) Math.round(averageValueFlipped * 100) / 100;  // round to 2 decimal places;
    }
    // get the amount of cards known for one card
    public int getCardAmount(String card) {
        try {
            return knownCards.get(card);
        }
        catch (NullPointerException e) {
            System.out.println("Error getting card amount : " + e.getMessage());
            return 0;   // if the card is not known, return 0
        }
    }

    // get amountOfCards
    public static Map<String, Integer> getAmountOfCards() {
        return amountOfCards;
    }

}
