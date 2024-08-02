package com.example.skyjocounter;

import static java.lang.Math.abs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GameStats extends AppCompatActivity implements Serializable {
    private static final long serialVersionUID = 1L; // Add this for version control
    private Game game;
    private int firstPlayerIndex = -1;
    private Boolean doubleScore = false;
    private ArrayList<Tuple<Integer, Integer>> orderedPlayerIndexAndScoreTuples;
    private ArrayList<Tuple<Integer, Integer>> orderedPlayerIndexAndScoreSessionTuples;
    private ArrayList<String> currentGameStatsStrings;
    private ArrayList<String> sessionStatsStrings;
    private ArrayList<String> allTimeStatsStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_stats);

        // Retrieve the Game object from the intent
        game = (Game) getIntent().getSerializableExtra("game");
        game.addGameId(this);

        // Use the game object to display the stats
        displayGameStats();
    }
    private void displayGameStats() {
        verifyIfFirstPlayerDoubleScore();
        addScoresToPlayers();
        rankPlayers();
        getCurrentGameStatsStrings();
        getSessionStatsStrings();
        getAllTimeStatsStrings();

        // Get the sections container
        LinearLayout sectionsContainer = findViewById(R.id.sections_container);

        // Add sections dynamically
        addSection(sectionsContainer, currentGameStatsStrings);
        addSection(sectionsContainer, sessionStatsStrings);
        addSection(sectionsContainer, allTimeStatsStrings);

//        TextView statsTextView = findViewById(R.id.stats_text_view);
//        String stats = "Game Stats: " + game.getPlayers().get(1).getName() + " Score: " + game.getPlayers().get(1).getScores();
//        statsTextView.setText(stats);
    }

    private void addSection(LinearLayout container, ArrayList<String> sectionStrings) {
        if (sectionStrings == null || sectionStrings.isEmpty()) {
            return;
        }

        // Add section title
        TextView titleTextView = new TextView(this);
        titleTextView.setText(sectionStrings.get(0));
        titleTextView.setTextSize(20);
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setPadding(0, 16, 0, 8);
        container.addView(titleTextView);

        // Add section content
        for (int i = 1; i < sectionStrings.size(); i++) {
            TextView contentTextView = new TextView(this);
            contentTextView.setText(Util.getStyledText(sectionStrings.get(i)));
            contentTextView.setPadding(0, 0, 0, 8);
            container.addView(contentTextView);
        }

        // Add a delimiter line
        View delimiter = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        params.setMargins(0, 16, 0, 16);
        delimiter.setLayoutParams(params);
        delimiter.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        container.addView(delimiter);
    }



    // Handle the Continue button click
    public void continueToNextActivity(View view) {
        Intent intent = new Intent(GameStats.this, BetweenGame.class);
        // Put itself in the intent
        intent.putExtra("gameStats", this);
        startActivity(intent);
        finish();


    }

    private void verifyIfFirstPlayerDoubleScore () {
        int lowestScoreNotFirstFinish = 141;    // highest possible score is 140
        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            Player player = game.getPlayers().get(playerIndex);
            int score = player.getRevealedScore();

            if (player.finishedFirst) {
                // Player who finished first
                firstPlayerIndex = playerIndex;
            }
            else {
                // Player who did not finish first
                if (score < lowestScoreNotFirstFinish) {
                    // new lowest score for player who did not finish first
                    lowestScoreNotFirstFinish = score;
                }
            }
        }
//        if (indexLowestScoreNotFirstFinish != -1) // should only happen if there is only 1 player

        if (lowestScoreNotFirstFinish <= game.getPlayers().get(firstPlayerIndex).getRevealedScore()) {
            // a player got equal or lower score to the first finisher player
            doubleScore = true;
        }
    }

    private void addScoresToPlayers() {
        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            Player player = game.getPlayers().get(playerIndex);
            int score = player.getRevealedScore();
            player.addScore(score, playerIndex == firstPlayerIndex, (playerIndex == firstPlayerIndex) ? doubleScore : false);
        }
    }

    private void rankPlayers() {
        // sort players by their scores :
        orderedPlayerIndexAndScoreTuples = new ArrayList<>();
        // Creating the Tuples :
        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            // get the score, doubled if the player finished first and is not the first player (bool doubleScore)
            Integer score = (doubleScore && (playerIndex == firstPlayerIndex))
                    ? game.getPlayers().get(playerIndex).getRevealedScore() * 2
                    : game.getPlayers().get(playerIndex).getRevealedScore();
            Tuple<Integer, Integer> playerScore = new Tuple<>(playerIndex, score);
//            Log.d("playerScore", playerScore.first.toString() + " " + playerScore.second.toString());
            orderedPlayerIndexAndScoreTuples.add(playerScore);
        }
        // Sorting based on the second element (score) in ascending order
        orderedPlayerIndexAndScoreTuples.sort(Comparator.comparing(o -> o.second));

//        for (Tuple<Integer, Integer> tuple : orderedPlayerIndexAndScoreTuples) {
//            Log.d("orderedPlayerIndexAndScoreTuples", tuple.first.toString() + " " + tuple.second.toString());
//        }

        // add rank
        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            int rank = getRankOfPlayer(playerIndex);
//            Log.d("rank", "" + rank);
            game.getPlayers().get(playerIndex).getScores().addRank(rank);
//            game.getGamesInfo().getScores().second.get(playerIndex).getScores().get(size - 1).rank = rank;
        }


        // rank the players by total score
        orderedPlayerIndexAndScoreSessionTuples = new ArrayList<>();
        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            Tuple<Integer, Integer> playerScore = new Tuple<>(playerIndex, game.getPlayers().get(playerIndex).getTotalScore());
            orderedPlayerIndexAndScoreSessionTuples.add(playerIndex, playerScore);
        }
        // Sorting based on the second element (total score) in ascending order
        orderedPlayerIndexAndScoreSessionTuples.sort(Comparator.comparing(o -> o.second));
    }

    public int getRankOfPlayer(int playerIndex) {
        // accounts for draws
        int actualRank = 0;
        int previousScore = -142;
        int currentDrawNumber = 0;
        for (int index = 0; index < orderedPlayerIndexAndScoreTuples.size(); index++) {
            int score = orderedPlayerIndexAndScoreTuples.get(index).second;
            if (score == previousScore) {
                currentDrawNumber += 1;
            }
            else {
                actualRank += 1 + currentDrawNumber;
                currentDrawNumber = 0;
            }
            previousScore = score;
            if (orderedPlayerIndexAndScoreTuples.get(index).first == playerIndex) {
//                Log.d("actualRank", "" + index + playerIndex + actualRank);
//                Log.d("actualRank", "" + orderedPlayerIndexAndScoreTuples.get(index).first + orderedPlayerIndexAndScoreTuples.get(index).second);
                return actualRank;
            }
        }
        return -1;
    }

    private void getCurrentGameStatsStrings() {
        // returns a list of Strings that represent the current game stats ("rank - name : score (indication if finished first)")
        currentGameStatsStrings = new ArrayList<>();
        currentGameStatsStrings.add("Current Game");
        for (int index = 0; index < orderedPlayerIndexAndScoreTuples.size(); index++) {
            Tuple<Integer, Integer> playerScore = orderedPlayerIndexAndScoreTuples.get(index);
            int rank = getRankOfPlayer(playerScore.first);
            String s = "";
            s += Util.getOrdinal(rank) + " - " + game.getPlayers().get(playerScore.first).getName() + " : ";
            s += (playerScore.first == firstPlayerIndex ?
                    ("<underline>"+ (doubleScore ?
                            (((int) (playerScore.second / 2) + "</underline>") + "<c#FF0000x2</c>")
                            : (playerScore.second + "</underline>"))
                            )
                    : playerScore.second)
            + " points";
            currentGameStatsStrings.add(s);
            // ex : 1st - player1 : 3 points (finished first)   // finished the game first with no penalty
            // ex : 2nd - player2 : 3x2 points (finished first) // finished the game first with the penalty
            // ex : 1st - player3 : 3 points                    // didn't finish the game first
        }
    }

    public Game getGame() {
        return game;
    }
    public Boolean getDoubleScore() {
        return doubleScore;
    }
    public int getFirstPlayerIndex() {
        return firstPlayerIndex;
    }
    public ArrayList<Tuple<Integer, Integer>> getOrderedPlayerIndexAndScoreTuples() {
        return orderedPlayerIndexAndScoreTuples;
    }

    private void getSessionStatsStrings() {
        // returns a list of Strings that represent the session stats ("rank - name : scores (average)")
        sessionStatsStrings = new ArrayList<>();
        sessionStatsStrings.add("Session Total");
        for (int index = 0; index < orderedPlayerIndexAndScoreSessionTuples.size(); index++) {
            Tuple<Integer, Integer> playerScore = orderedPlayerIndexAndScoreSessionTuples.get(index);
            int rank = getPlayerRankSession(playerScore.first);
            float averageScore = (float) playerScore.second / game.getPlayers().get(playerScore.first).getScores().getScores().size();
            averageScore = (float) Math.round(averageScore * 100) / 100;
            sessionStatsStrings.add(Util.getOrdinal(rank) + " - " + game.getPlayers().get(playerScore.first).getName() + " : " + game.getPlayers().get(playerScore.first).getTotalScoresString() + " points (average: " + averageScore + ")");
            // ex : 1st - player1 : *3* *2x2* 5 points (average: 4)
        }
    }

    private void getAllTimeStatsStrings() {
        // returns a list of Strings that represent the all time stats ("[name] ([number of games] games) : \n Average Score : [average score prior] [^ or v] [new average] \n Average Rank : [average rank prior] [^ or v] [new average]
        allTimeStatsStrings = new ArrayList<>();
        allTimeStatsStrings.add("All Time");
        for (int playerIndex = 0; playerIndex < game.getPlayers().size(); playerIndex++) {
            String playerName = game.getPlayers().get(playerIndex).getName();
            int score = (doubleScore && (playerIndex == firstPlayerIndex))
                    ? game.getPlayers().get(playerIndex).getRevealedScore() * 2
                    : game.getPlayers().get(playerIndex).getRevealedScore();
            float rankRatio = (float) (getRankOfPlayer(playerIndex) - 1) / (game.getPlayers().size() - 1);
            Object[] averageScoreRankAndNumberOfGames = getPlayerAverageScoreRank(playerName);
            if (averageScoreRankAndNumberOfGames == null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(playerName).append(" (1 game) : \n");
                stringBuilder.append("Average Score : ").append(score);
                stringBuilder.append(" = ").append("\n");
                stringBuilder.append("Average Rank : ").append(rankRatio);
                stringBuilder.append(" = ").append("\n");
                allTimeStatsStrings.add(stringBuilder.toString());
                continue;
            }
            float averageScore = (float) Math.round((float) averageScoreRankAndNumberOfGames[0] * 100) / 100;   // round to 2 decimal places;
            float averageRankRatio = (float) Math.round((float) averageScoreRankAndNumberOfGames[1] * 100) / 100;   // round to 2 decimal places;
            int numberOfGames = (int) averageScoreRankAndNumberOfGames[2];
            float newAverageScore = (float) Math.round(((averageScore * (numberOfGames) + score) / (numberOfGames + 1)) * 100) / 100;   // round to 2 decimal places;
            float newAverageRankRatio = (float) Math.round(((averageRankRatio * (numberOfGames) + rankRatio) / (numberOfGames + 1)) * 100) / 100;   // round to 2 decimal places;
            float scoreDiff = (float) Math.round((newAverageScore - averageScore) * 100) / 100;   // round to 2 decimal places;
            float rankRatioDiff = (float) Math.round((newAverageRankRatio - averageRankRatio) * 100) / 100;   // round to 2 decimal places;
            // creating the Strings for the player
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(playerName).append(" (").append(numberOfGames + 1).append(" games) : \n");
            stringBuilder.append("Average Score : ").append(newAverageScore);
            if (scoreDiff > 0) {
                stringBuilder.append(" <c#FF0000^ ").append(abs(scoreDiff)).append("</c>\n");
            }
            else if (scoreDiff < 0) {
                stringBuilder.append(" <c#00FF00v ").append(abs(scoreDiff)).append("</c>\n");
            }
            else {
                stringBuilder.append(" = ").append("\n");
            }
            stringBuilder.append("Average Rank : ").append(newAverageRankRatio);
            if (rankRatioDiff > 0) {
                stringBuilder.append(" <c#FF0000^ ").append(abs(rankRatioDiff)).append("</c>\n");
            }
            else if (rankRatioDiff < 0) {
                stringBuilder.append(" <c#00FF00v ").append(abs(rankRatioDiff)).append("</c>\n");
            }
            else {
                stringBuilder.append(" = ").append("\n");
            }
            allTimeStatsStrings.add(stringBuilder.toString());
        }
    }

    private Object[] getPlayerAverageScoreRank(String playerName) {
        JSONArray players = FileUtils.getPlayers(this);
        int playerIndex = -1;
        JSONArray games = null;
        try {
            for (int i = 0; i < players.length(); i++) {
                String name = players.getJSONObject(i).getString("name");
                if (name.equals(playerName)) {
                    playerIndex = i;
                    break;
                }
            }
            if (playerIndex == -1) {
                return null;
            }
            games = players.getJSONObject(playerIndex).getJSONArray("games");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // calculate average score/rank
        float averageScore = 0;
        float averageRank = 0;
        int numberOfGames = games.length();

        ArrayList<Float> rankRatios = new ArrayList<>();
        int sumScores = 0;
        for (int i = 0; i < numberOfGames; i++) {
            try {
                int score = games.getJSONObject(i).getInt("score");
                sumScores += score;
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
        averageScore = (float) sumScores / numberOfGames;
        averageScore = (float) Math.round(averageScore * 100) / 100;
        averageRank =  sum / numberOfGames;
        averageRank = (float) Math.round(averageRank * 100) / 100;
        return new Object[]{averageScore, averageRank, numberOfGames};
    }

//    public int getPlayerRank (int playerIndex) {
//        for (int rank = 0; rank < orderedPlayerIndexAndScoreTuples.size(); rank++) {
//            if (orderedPlayerIndexAndScoreTuples.get(rank).first == playerIndex) {
//                return rank + 1;
//            }
//        }
//        return -1;
//    }

    public int getPlayerRankSession (int playerIndex) {
        int actualRank = 0;
        int previousScore = -142;
        int currentDrawNumber = 0;
        for (int index = 0; index < orderedPlayerIndexAndScoreSessionTuples.size(); index++) {
            int score = orderedPlayerIndexAndScoreSessionTuples.get(index).second;
            if (score == previousScore) {
                currentDrawNumber += 1;
            } else {
                actualRank += 1 + currentDrawNumber;
                currentDrawNumber = 0;
            }
            previousScore = score;
            if (orderedPlayerIndexAndScoreSessionTuples.get(index).first == playerIndex) {
                return actualRank;
            }
        }
        return -1;
    }

    public int getNumberOfGames() {
        return game.getPlayers().get(0).getScores().getScores().size();
    }

    public int getScorePlayerDouble (int playerIndex) {
        if (doubleScore && (playerIndex == firstPlayerIndex)) {
            return game.getPlayers().get(playerIndex).getRevealedScore() * 2;
        }
        return game.getPlayers().get(playerIndex).getRevealedScore();

    }
}