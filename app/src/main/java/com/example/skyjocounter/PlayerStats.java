package com.example.skyjocounter;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PlayerStats extends AppCompatActivity {

    private LinearLayout playersContainer;
    private ArrayList<String> playerNames;
    private JSONArray players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_stats);

        // Set title
        TextView titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText("Players");

        // Get players and display their stats
        playersContainer = findViewById(R.id.players_container);
        updatePlayerList();

    }

    private void updatePlayerList() {
        players = FileUtils.getPlayers(this);
        playerNames = new ArrayList<>();
        try {
            for (int i = 0; i < players.length(); i++) {
                playerNames.add(players.getJSONObject(i).getString("name"));
                LinearLayout playerSection = createPlayerSection(i);
                playersContainer.addView(playerSection);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void deletePlayer(String playerName) {
        FileUtils.deletePlayerData(this, playerName);
    }

    private LinearLayout createPlayerSection(int playerIndex) {
        LinearLayout playerSection = new LinearLayout(this);
        playerSection.setOrientation(LinearLayout.VERTICAL);
        playerSection.setPadding(16, 16, 16, 16);

        // Add player name
        TextView playerNameTextView = new TextView(this);
        playerNameTextView.setText(playerNames.get(playerIndex));
        playerNameTextView.setTextSize(18);
        playerSection.addView(playerNameTextView);

        // Add player stats
        List<String> playerStats = generatePlayerStats(playerIndex);
        for (String stat : playerStats) {
            TextView statTextView = new TextView(this);
            statTextView.setText(stat);
            playerSection.addView(statTextView);
        }

        // Add delete button
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> {
            deletePlayer(playerNames.get(playerIndex));
            updatePlayerList();
        });
        playerSection.addView(deleteButton);

        return playerSection;
    }

    private List<String> generatePlayerStats(int playerIndex) {
        List<String> playerStats = new ArrayList<>();

        try {
            JSONArray games = players.getJSONObject(playerIndex).getJSONArray("games");
            ArrayList<JSONArray> sessions = getSessions(games);
            ArrayList<String> statsScore = getStatsScore(games);
            float averageRank = (float) Math.round(getAverageRank(games) * 100) / 100;  // round to 2 decimal places
//            float averageRankSessions = (float) Math.round(getAverageRankSessions(games) * 100) / 100;  // round to 2 decimal places
            Object[] highestLowestAverageRankSessions = getHighestLowestAverageRankSessions(games);
            float highestAverageRankSession = (float) Math.round((float) highestLowestAverageRankSessions[0] * 100) / 100;  // round to 2 decimal places
            float lowestAverageRankSession = (float) Math.round((float) highestLowestAverageRankSessions[1] * 100) / 100;  // round to 2 decimal places
            JSONArray sessionStats = players.getJSONObject(playerIndex).getJSONArray("sessionStats");
            Object[] highestLowestRankSessions = getHighestLowestRankSessions(sessionStats);
            float highestRankSession = (float) Math.round((float) highestLowestRankSessions[0] * 100) / 100;  // round to 2 decimal places)
            float lowestRankSession = (float) Math.round((float) highestLowestRankSessions[1] * 100) / 100;  // round to 2 decimal places)
            float averageRankSessions = (float) Math.round((float) highestLowestRankSessions[2] * 100) / 100;  // round to 2 decimal places

            playerStats.add("Number of Games : " + games.length());
            playerStats.add("Number of Sessions : " + sessions.size());
            playerStats.add("Average Game Rank : " + averageRank);
            playerStats.add("Average Session Rank : " + averageRankSessions);
            playerStats.add("Highest Session Rank : " + highestRankSession);
            playerStats.add("Lowest Session Rank : " + lowestRankSession);
            playerStats.add("Highest Average Rank : " + highestAverageRankSession);
            playerStats.add("Lowest Average Rank : " + lowestAverageRankSession);
            playerStats.add("Average Score : " + statsScore.get(0));
            playerStats.add("Median Score : " + statsScore.get(1));
            playerStats.add("Highest Score : " + statsScore.get(2));
            playerStats.add("Lowest Score : " + statsScore.get(3));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return playerStats;
    }

    private ArrayList<String> getStatsScore(JSONArray games) {
        int totalScore = 0;
        int minScore = 143;
        int maxScore = -143;
        ArrayList<Integer> scores = new ArrayList<>();
        for (int i = 0; i < games.length(); i++) {
            try {
                int score = games.getJSONObject(i).getInt("score");
                if (games.getJSONObject(i).getBoolean("firstFinisher") && games.getJSONObject(i).getBoolean("doubleScore")) {
                    score *= 2;
                }
                scores.add(score);
                totalScore += score;
                minScore = Math.min(minScore, score);
                maxScore = Math.max(maxScore, score);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        float averageScore = (float) totalScore / games.length();
        averageScore = (float) Math.round(averageScore * 100) / 100;  // round to 2 decimal places
        int medianScore = getMedianScore(scores);

        ArrayList<String> statsScore = new ArrayList();
        statsScore.add(String.valueOf(averageScore));
        statsScore.add(String.valueOf(medianScore));
        statsScore.add(String.valueOf(maxScore));
        statsScore.add(String.valueOf(minScore));

        return statsScore;
    }

    public float getAverageRank(JSONArray games) {
        ArrayList<Float> rankRatios = new ArrayList<>();
        for (int i = 0; i < games.length(); i++) {
            try {
                int rank =  games.getJSONObject(i).getInt("rank");
                int numberOfPlayers = games.getJSONObject(i).getInt("numberOfPlayers");
                float ratio = (float) (rank - 1) / (numberOfPlayers - 1);
                rankRatios.add(ratio);
            } catch (JSONException e) {
                throw new RuntimeException(e);
                }
        }
        float sum = 0;
        for (float ratio : rankRatios) {
            sum += ratio;
        }
        return sum / rankRatios.size();
    }

//    public float getAverageRankSessions(JSONArray games) {
//      // NOT INTERESTING, SEE getHighestLowestRankSessions instead
//        ArrayList<Float> rankRatios = new ArrayList<>();
//        ArrayList<JSONArray> sessions = getSessions(games);
//        for (int i = 0; i < sessions.size(); i++) {
//            float ratio = getAverageRank(sessions.get(i));
//            rankRatios.add(ratio);
//        }
//        float sum = 0;
//        for (float ratio : rankRatios) {
//            sum += ratio;
//        }
//        return sum / rankRatios.size();
//    }

    public Object[] getHighestLowestAverageRankSessions(JSONArray games) {
        ArrayList<JSONArray> sessions = getSessions(games);
        ArrayList<Float> rankRatios = new ArrayList<>();
        for (int i = 0; i < sessions.size(); i++) {
            float ratio = getAverageRank(sessions.get(i));
            rankRatios.add(ratio);
        }
        float highest = 0;
        float lowest = 1;
        for (int i = 0; i < rankRatios.size(); i++) {
            if (rankRatios.get(i) > highest) {
                highest = rankRatios.get(i);
            }
            if (rankRatios.get(i) < lowest) {
                lowest = rankRatios.get(i);
            }
        }
        return new Object[] {highest, lowest};
    }

    public Object[] getHighestLowestRankSessions(JSONArray sessionStats) {
        ArrayList<Float> rankRatios = new ArrayList<>();
        for (int i = 0; i < sessionStats.length(); i++) {
            try {
                int rank = sessionStats.getJSONObject(i).getInt("rank");
                int numberOfPlayers = sessionStats.getJSONObject(i).getInt("numberOfPlayers");
                float ratio = (float) (rank - 1) / (numberOfPlayers - 1);
                rankRatios.add(ratio);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        float highest = 0;
        float lowest = 1;
        float sum = 0;
        for (int i = 0; i < rankRatios.size(); i++) {
            if (rankRatios.get(i) > highest) {
                highest = rankRatios.get(i);
            }
            if (rankRatios.get(i) < lowest) {
                lowest = rankRatios.get(i);
            }
            sum += rankRatios.get(i);
        }
        float average = sum / rankRatios.size();
        return new Object[] {highest, lowest, average};
    }

    private int getMedianScore(ArrayList<Integer> scores) {
        scores.sort(null);
        int middleIndex = scores.size() / 2;
        if (scores.size() % 2 == 0) {
            return (scores.get(middleIndex - 1) + scores.get(middleIndex)) / 2;
        } else {
            return scores.get(middleIndex);
        }
    }

    private ArrayList<JSONArray> getSessions (JSONArray games) {
        ArrayList<JSONArray> sessions = new ArrayList<>();
        JSONArray currentSession = new JSONArray();
        if (games.length() == 0) {
            return null;
        }
        try {
            int lastSessionId = games.getJSONObject(0).getInt("sessionId");

            for (int i = 0; i < games.length(); i++) {
                int sessionId = games.getJSONObject(i).getInt("sessionId");
                if (sessionId == lastSessionId) {
                    currentSession.put(games.getJSONObject(i));
                } else {
                    sessions.add(currentSession);
                    currentSession = new JSONArray();
                    currentSession.put(games.getJSONObject(i));
                    lastSessionId = sessionId;
                }
            }
            sessions.add(currentSession);
            return sessions;

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}