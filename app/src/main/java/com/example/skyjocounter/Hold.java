package com.example.skyjocounter;

import java.io.Serializable;

public class Hold implements Serializable {
    private static final long serialVersionUID = 1L;
    private Card card;

    public Hold() {
        card = new Card("empty", -4, 0);
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    // Display and onClick methods will be integrated with Android UI
}
