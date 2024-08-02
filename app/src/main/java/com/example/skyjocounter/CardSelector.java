package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CardSelector implements Serializable {
    private static final long serialVersionUID = 1L;
    // Component with a button for each card type (18 types in a 6 by 3 grid)
    private ArrayList<Card> cardTypes;

    public CardSelector() {
        cardTypes = new ArrayList<>();
        String[] types = {"1", "2", "3", "4", "m2", "m1", "5", "6", "7", "8", "joker", "0", "9", "10", "11", "12", "flipped"};  //"empty"
        for (String type : types) {
            cardTypes.add(new Card(type, -5, 0));
        }
    }

    public ArrayList<Card> getCardTypes() {
        return cardTypes;
    }
    // Display and onClick methods will be integrated with Android UI
}
