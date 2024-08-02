package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscardPile extends Pile implements Serializable {
    private static final long serialVersionUID = 1L;
    public DiscardPile() {
        super();
    }

    // Display and onClick methods will be integrated with Android UI
    // has a "change order" button to change the order of the top N cards
}
