package com.example.skyjocounter;

import java.io.Serializable;

public class Piles implements Serializable {
    private static final long serialVersionUID = 1L;
    private final DrawPile drawPile;
    private final DiscardPile discardPile1;
    private final DiscardPile discardPile2;
    private final Hold hold;

    public Piles(int drawPileAmount) {
        this.drawPile = new DrawPile(drawPileAmount);
        this.discardPile1 = new DiscardPile();
        this.discardPile2 = new DiscardPile();
        this.hold = new Hold();
    }

    public DrawPile getDrawPile() {
        return drawPile;
    }

    public DiscardPile getDiscardPile1() {
        return discardPile1;
    }

    public DiscardPile getDiscardPile2() {
        return discardPile2;
    }

    public Hold getHold() {
        return hold;
    }

    // Display method will be integrated with Android UI
}
