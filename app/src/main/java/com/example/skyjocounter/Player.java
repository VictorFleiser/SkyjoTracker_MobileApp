package com.example.skyjocounter;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private Deck deck;
    private String name;
    private Scores scores;
    public Boolean finishedFirst = false;

    public Player(String name, Scores previousScores, int playerIndex) {
        this.name = name;
        this.deck = new Deck(playerIndex);
        this.scores = new Scores(previousScores);
    }

    public Deck getDeck() {
        return deck;
    }

    public String getName() {
        return name;
    }

    public Scores getScores() {
        return scores;
    }

    public void addScore(int score, boolean finishedFirst, boolean doubleScore) {
        scores.add(score, finishedFirst, doubleScore);
    }

    public int getRevealedScore() {
        int total = 0;
        for (Card card : deck.getCards()) {
            if ("flipped".equals(card.getType())) {
                continue;
            }
            total += (int) Cards.getCardValue(card.getType());
        }
        return total;
    }

    public float getEstimatedScore() {
        float total = 0;
        for (Card card : deck.getCards()) {
//            Log.d("Player", "Card type: " + card.getType());
            Object cardValue = Cards.getCardValue(card.getType());
            if (cardValue instanceof Integer) {
                cardValue = ((Integer) cardValue).floatValue();
            }
            total += (float) cardValue;
        }
        return (float) Math.round(total * 100) / 100;  // round to 2 decimal places;
    }

    public String getTotalScoresString() {
        // returns the String like such : "12 + *1* + *3x2* + 5 = 24", "**" means the player ended first in that round, "x2" means the score was doubled that round
        StringBuilder sb = new StringBuilder();
        int total = 0;
        for (Scores.Node node : scores.getScores()) {
            // test if the player finished first
            if (node.finishedFirst) {
                sb.append("<underline>");
            }
            // add the score achieved that round
            sb.append(node.score);
            // test if the player finished first
            if (node.finishedFirst) {
                sb.append("</underline>");
            }
            // test if the score was doubled
            if (node.doubleScore) {
                sb.append("<c#FF0000x2</c>");
            }
            // add the " + " separator if not the last node
            if (node != scores.getScores().get(scores.getScores().size() - 1)) {
                sb.append(" + ");
            }
            // add the score to the total
            if (node.doubleScore) {
                total += node.score * 2;
            } else {
                total += node.score;
            }
        }
        if (!scores.getScores().isEmpty()) {
            sb.append(" = ");
            sb.append(total);
            return sb.toString();
        } else {
            return "no previous matches";
        }
    }

    public int getTotalScore() {
        int total = 0;
        for (Scores.Node node : scores.getScores()) {
            if (node.doubleScore) {
                total += node.score * 2;
            } else {
                total += node.score;
                }
        }
        return total;
    }
}
