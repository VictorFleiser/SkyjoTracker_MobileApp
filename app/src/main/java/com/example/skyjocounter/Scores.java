package com.example.skyjocounter;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Scores implements Serializable {
    private static final long serialVersionUID = 1L;
    public static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        public int score;
        public boolean finishedFirst;
        public boolean doubleScore;
        public int rank;
    }
    private final ArrayList<Node> nodes;

    public Scores(Scores scores) {
        if (scores == null) {
            this.nodes = new ArrayList<>();
        }
        else {
            this.nodes = scores.getScores();
        }
    }

    public void add(int score, boolean finishedFirst, boolean doubleScore) {
//        Log.d("Scores", "Scores added \nScores added \nScores added \nScores added \nScores added ");
        Node node = new Node();
        node.score = score;
        node.finishedFirst = finishedFirst;
        node.doubleScore = doubleScore;
        nodes.add(node);
    }

    public void addRank(int rank) {
        nodes.get(nodes.size() - 1).rank = rank;
    }

    public ArrayList<Node> getScores () {
        return nodes;
    }
}
