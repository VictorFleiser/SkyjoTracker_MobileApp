package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DrawPile extends Pile implements Serializable {
    private static final long serialVersionUID = 1L;

    public DrawPile(int amountOfCards) {
        super();
        for (int i = 0; i < amountOfCards; i++) {
            cards.add(new Card("flipped", -1, i));
        }
    }

    // Display and onClick methods will be integrated with Android UI
    // has a label with the number of cards left in the pile
}
