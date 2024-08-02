package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;

    // 0+ : Player
    // -1 : DrawPile
    // -2 : DiscardPile1
    // -3 : DiscardPile2
    // -4 : HoldPile
    // -5 : CardSelector
    private final ArrayList<Integer> owners;

    // 0+ : Pile
    // -1 to -12 : Deck
    private final ArrayList<Integer> positions;

    public Card(String type, int owner, int position) {
        this.type = type;
        owners = new ArrayList<>();
        positions = new ArrayList<>();

        owners.add(owner);
        positions.add(position);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Integer> getOwners() {
        return owners;
    }

    public void addOwner(int owner) {
        owners.add(owner);
    }

    public ArrayList<Integer> getPositions() {
        return positions;
    }

    public void addPosition(int position) {
        positions.add(position);
    }
}
