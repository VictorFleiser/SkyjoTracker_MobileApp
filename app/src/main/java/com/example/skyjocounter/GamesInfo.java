package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;

public class GamesInfo implements Serializable {
    private static final long serialVersionUID = 1L; // Add this for version control
    private ArrayList<Integer> gameIds;
    private Tuple<ArrayList<String>, ArrayList<Scores>> scores;
    public GamesInfo(ArrayList<String> playerNames) {
        gameIds = new ArrayList<>();
        scores = new Tuple<>(playerNames, null);
    }

    public void addGameId(int gameId) {
        gameIds.add(gameId);
    }

    public ArrayList<Integer> getGameIds() {
        return gameIds;
    }

    public void setScores(Tuple<ArrayList<String>, ArrayList<Scores>> scores) {
        this.scores = scores;
        }

    public Tuple<ArrayList<String>, ArrayList<Scores>> getScores() {
        return scores;
    }
}
