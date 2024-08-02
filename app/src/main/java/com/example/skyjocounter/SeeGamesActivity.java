package com.example.skyjocounter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

public class SeeGamesActivity extends AppCompatActivity {

    private LinearLayout sessionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_games);

        TextView titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText("List of Games");

        sessionsContainer = findViewById(R.id.sessions_container);

        displaySessions();

    }

    private void displaySessions() {
        ArrayList<ArrayList<String>> sessions = getSessionsStrings();

        for (ArrayList<String> session : sessions) {
            if (session.size() > 0) {
                addSessionTitle(session.get(0));
                for (int i = 1; i < session.size(); i++) {
                    addSessionDetail(session.get(i));
                }
                addDelimiter();
            }
        }
    }

    private void addSessionTitle(String title) {
        TextView titleTextView = new TextView(this);
        titleTextView.setText(Util.getStyledText(title));
        titleTextView.setTextSize(20);
        titleTextView.setPadding(16, 16, 16, 16);
        sessionsContainer.addView(titleTextView);
    }

    private void addSessionDetail(String detail) {
        TextView detailTextView = new TextView(this);
        detailTextView.setText(Util.getStyledText(detail));
        detailTextView.setPadding(16, 8, 16, 8);
        sessionsContainer.addView(detailTextView);
    }

    private void addDelimiter() {
        // Add a delimiter line
        View delimiter = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        params.setMargins(0, 16, 0, 16);
        delimiter.setLayoutParams(params);
        delimiter.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        sessionsContainer.addView(delimiter);

    }

    private ArrayList<ArrayList<String>> getSessionsStrings() {
        // get all the sessions from the database
        ArrayList<String> fileNames = FileUtils.getFileNames(this);
//        Log.d("SeeFiles", "File names: " + fileNames);
        // filter files that start with "session_"
        ArrayList<String> sessionFiles = new ArrayList<>();
        for (String fileName : fileNames) {
            if (fileName.startsWith("session_")) {
                sessionFiles.add(fileName);
            }
        }

        ArrayList<ArrayList<String>> sessionsStrings = new ArrayList<>();
        // for each session file, read the file and add the contents to the sessionsStrings
        for (String fileName : sessionFiles) {
            ArrayList<String> sessionStrings = new ArrayList<>();
            // read the file
            String sessionJSONString = FileUtils.readTextFromFile(this, fileName);
            Object[] sessionObjects = Util.parseSessionJson(sessionJSONString);
            int sessionID = (int) sessionObjects[0];
            ArrayList<JSONObject> players = (ArrayList<JSONObject>) sessionObjects[4];
            // create the session title
            sessionStrings.add("Session " + sessionID + " : " + fileName);
            // create Player2 objects for each player
            ArrayList<Player2> players2 = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                Player2 player2 = new Player2();
                // parse the player JSON object
                JSONObject player = players.get(i);
                Object[] playerObjects = Util.parsePlayerJSONObject(player);
                player2.name = (String) playerObjects[0];
                player2.scoreSession = (int) playerObjects[1];
                player2.rankSession = (int) playerObjects[2];
                ArrayList<JSONObject> games = (ArrayList<JSONObject>) playerObjects[3];
                // create Game2 objects for each game
                player2.games = new ArrayList<>();
                for (int j = 0; j < games.size(); j++) {
                    Game2 game2 = new Game2();
                    // parse the game JSON object
                    JSONObject game = games.get(j);
                    Object[] gameObjects = Util.parseGameJSONObject(game);
                    game2.score = (int) gameObjects[1];
                    game2.rank = (int) gameObjects[2];
                    game2.firstFinisher = (boolean) gameObjects[3];
                    game2.doubledScore = (boolean) gameObjects[4];
                    player2.games.add(game2);
                }
                players2.add(player2);
            }
            // create a ranked list of players by scoreSession
            ArrayList<Player2> rankedPlayers2 = new ArrayList<>(players2);
            rankedPlayers2.sort(Comparator.comparingInt(p -> p.scoreSession));
            // creating the Strings :
            for (int index = 0; index < rankedPlayers2.size(); index++) {
                Player2 player2 = rankedPlayers2.get(index);
                int rank = player2.rankSession;
                float averageScore = (float) player2.scoreSession / player2.games.size();
                averageScore = (float) Math.round(averageScore * 100) / 100;
                // creating the String for the player :
                StringBuilder playerString = new StringBuilder(Util.getOrdinal(rank) + " - " + player2.name + " : ");
                for (int i = 0; i < player2.games.size(); i++) {
                    Game2 game = player2.games.get(i);
                    // test if the player finished first
                    if (game.firstFinisher) {
                        playerString.append("<underline>");
                    }
                    // add the score achieved that round
                    playerString.append(game.score);
                    // test if the player finished first
                    if (game.firstFinisher) {
                        playerString.append("</underline>");
                    }
                    // test if the score was doubled
                    if (game.doubledScore) {
                        playerString.append("<c#FF0000x2</c>");
                    }
                    // add the " + " separator if not the last game
                    if (i != player2.games.size() - 1) {
                        playerString.append(" + ");
                    }
                }
                playerString.append(" = ").append(player2.scoreSession).append(" points (average: ").append(averageScore).append(")");
                sessionStrings.add(playerString.toString());
            }
            // add the Strings of the session to the sessionsStrings
            sessionsStrings.add(sessionStrings);
        }
        return sessionsStrings;
    }

    class Player2 {
        public String name;
        public int scoreSession;
        public int rankSession;
        public ArrayList<Game2> games;
    }

    class Game2 {
        public int score;
        public int rank;
        public boolean firstFinisher;
        public boolean doubledScore;
    }

}