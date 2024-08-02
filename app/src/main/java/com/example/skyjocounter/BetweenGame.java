package com.example.skyjocounter;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BetweenGame extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private GameStats gameStats;
//    private HashMap<String, Object> statsInMemory; // Use your actual stats class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_between_game);

//        checkPermissions();

        // Get game object from intent
        Intent intent = getIntent();
        gameStats = (GameStats) intent.getSerializableExtra("gameStats");
        if (gameStats == null) {
            Log.e("BetweenGame", "Game object not found in intent");
        }

        updateGamesInfo();

        // Set up buttons
        Button playNewGameButton = findViewById(R.id.play_new_game_button);
        Button saveGameJsonButton = findViewById(R.id.save_game_json_button);
        Button saveSessionJsonButton = findViewById(R.id.save_session_json_button);
        Button menuButton = findViewById(R.id.menu_button);
        Button saveGameToStatsButton = findViewById(R.id.save_game_to_stats_button);
        Button saveSessionToStatsButton = findViewById(R.id.save_session_to_stats_button);
//        Button endSessionButton = findViewById(R.id.end_session_button);

        playNewGameButton.setOnClickListener(v -> playNewGame());
        saveGameJsonButton.setOnClickListener(v -> saveGameJson());
        saveSessionJsonButton.setOnClickListener(v -> saveSessionJson());
        menuButton.setOnClickListener(v -> goToMenu());
        saveGameToStatsButton.setOnClickListener(v -> saveGameToStats());
        saveSessionToStatsButton.setOnClickListener(v -> saveSessionToStats());
//        endSessionButton.setOnClickListener(v -> endSession());
    }

//    private void checkPermissions() {
//        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
//        Log.d("BetweenGame", "Permission granted: " + hasPermission);
//        if (!hasPermission) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
//        }
//    }
    private void playNewGame() {
        Intent intent = new Intent(this, GameActivity.class);
//        Tuple<ArrayList<String>, ArrayList<Scores>> data = generateNewGameData();

        GamesInfo data = gameStats.getGame().getGamesInfo();
        intent.putExtra("newGameData", data);
        startActivity(intent);
    }

    private void updateGamesInfo() {
        Tuple<ArrayList<String>, ArrayList<Scores>> data = new Tuple<>(new ArrayList<>(), new ArrayList<>());
        for (int indexPlayer = 0; indexPlayer < gameStats.getGame().getPlayers().size(); indexPlayer++) {
            Player player = gameStats.getGame().getPlayers().get(indexPlayer);
            data.first.add(player.getName());
            data.second.add(new Scores(player.getScores()));
        }
        gameStats.getGame().getGamesInfo().setScores(data);

    }

    private void saveGameJson() {
        // get date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String formattedDate = sdf.format(new Date());
//        String date = new java.util.Date().toString().replace(" ", "_").replace(":", "_");
        String fileName = "game_" + formattedDate + ".json";
//        Log.d("BetweenGame", "fileName : " + fileName);
        JSONObject jsonObject = new JSONObject();
        try {
            // int "gameId" is the id of the game
            jsonObject.put("gameId", gameStats.getGame().getGameId());

            // boolean "tracked" = true means that the game was tracked using SkyjoTracker
            jsonObject.put("tracked", true);

            // boolean "doubleScore" precises if the first player to finish got penalized
            jsonObject.put("doubleScore", gameStats.getDoubleScore());

            // int "firstPlayerIndex" is the index of the first player to finish
            jsonObject.put("firstPlayerIndex", gameStats.getFirstPlayerIndex());

            // array of all the cards
            JSONArray cards = new JSONArray();
            // combine all the cards into one list
            ArrayList<Card> cardsList = new ArrayList<>();
            cardsList.addAll(gameStats.getGame().getPiles().getDrawPile().getCards());
            cardsList.addAll(gameStats.getGame().getPiles().getDiscardPile1().getCards());
            cardsList.addAll(gameStats.getGame().getPiles().getDiscardPile2().getCards());
            for (Player player : gameStats.getGame().getPlayers()) {
                cardsList.addAll(player.getDeck().getCards());
            }
            // create a json object for each card and add it to the JSONArray
            for (Card card : cardsList) {
                JSONObject cardJson = new JSONObject();
                // String "type" is the type of the card
                cardJson.put("type", card.getType());
                // Array<int> "owners" is the list of owners of the card over the course of the game
                JSONArray owners = new JSONArray(card.getOwners());
                cardJson.put("owners", owners);
                // Array<int> "positions" is the list of positions of the card over the course of the game
                JSONArray positions = new JSONArray(card.getPositions());
                cardJson.put("positions", positions);
                // add the card to the JSONArray
                cards.put(cardJson);
            }
            // add the JSONArray to the JSONObject
            jsonObject.put("cards", cards);

            // array of all the players
            JSONArray players = new JSONArray();
            // create a json object for each player and add it to the JSONArray
            for (int indexPlayer = 0; indexPlayer < gameStats.getGame().getPlayers().size(); indexPlayer++) {
                Player player = gameStats.getGame().getPlayers().get(indexPlayer);
                JSONObject playerJson = new JSONObject();
                // String "name" is the name of the player
                playerJson.put("name", player.getName());
                // int "score" is the score of the player
                playerJson.put("score", player.getRevealedScore());
                // int "rank" is the rank of the player
                playerJson.put("rank", gameStats.getRankOfPlayer(indexPlayer));
                // add the player to the JSONArray
                players.put(playerJson);
            }
            // add the JSONArray to the JSONObject
            jsonObject.put("players", players);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        try {
//            Log.d("BetweenGame", "JSON object created: " + jsonObject.toString(4));
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
        FileUtils.writeJsonToFile(this, fileName, jsonObject);
//        Log.d("BetweenGame", "JSON saved to file: " + fileName);
        FileUtils.downloadFile(this, fileName);
//        Log.d("BetweenGame", "File downloaded: " + fileName);
    }


    private void saveSessionJson() {
        // get date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String formattedDate = sdf.format(new Date());
//        String date = new java.util.Date().toString().replace(" ", "_").replace(":", "_");
        String fileName = "session_" + formattedDate + ".json";
        JSONObject jsonObject = new JSONObject();
        try {
            // int "sessionId" is the id of the session
            jsonObject.put("sessionId", FileUtils.getNumberOfSessions(this));

            // boolean "tracked" = true means that the game was tracked using SkyjoTracker
            jsonObject.put("tracked", true);

            // int "numberOfGames" is the number of games played
            jsonObject.put("numberOfGames", gameStats.getNumberOfGames());

            // array of all the gameIds
            JSONArray gameIds = new JSONArray(gameStats.getGame().getGamesInfo().getGameIds());
            jsonObject.put("gameIds", gameIds);

            // array of all the players
            JSONArray players = new JSONArray();
            // create a json object for each player and add it to the JSONArray
//            Log.d("BetweenGame", "Number of players: " + gameStats.getGame().getPlayers().size());
            for (int indexPlayer = 0; indexPlayer < gameStats.getGame().getPlayers().size(); indexPlayer++) {
                Player player = gameStats.getGame().getPlayers().get(indexPlayer);
                JSONObject playerJson = new JSONObject();
                // String "name" is the name of the player
                playerJson.put("name", player.getName());

                // int "scoreSession" is the total score of the player
                playerJson.put("scoreSession", gameStats.getGame().getPlayers().get(indexPlayer).getTotalScore());

                // int "rankSession" is the total rank of the player
                playerJson.put("rankSession", gameStats.getPlayerRankSession(indexPlayer));

                // array of all the games
                JSONArray games = new JSONArray();
                // create a json object for each game and add it to the JSONArray
                for (int indexGame = 0; indexGame < gameStats.getGame().getGamesInfo().getGameIds().size(); indexGame++) {
                    JSONObject gameJson = new JSONObject();
                    // String "gameId" is the id of the game
                    gameJson.put("gameId", gameStats.getGame().getGamesInfo().getGameIds().get(indexGame));
                    // int "score" is the score of the game
                    gameJson.put("score", gameStats.getGame().getPlayers().get(indexPlayer).getScores().getScores().get(indexGame).score);
                    // int "rank" is the rank of the game
                    gameJson.put("rank", gameStats.getGame().getGamesInfo().getScores().second.get(indexPlayer).getScores().get(indexGame).rank);
                    // boolean "firstFinisher"
                    gameJson.put("firstFinisher", gameStats.getGame().getGamesInfo().getScores().second.get(indexPlayer).getScores().get(indexGame).finishedFirst);
                    // boolean "doubleScore"
                    gameJson.put("doubleScore", gameStats.getGame().getGamesInfo().getScores().second.get(indexPlayer).getScores().get(indexGame).doubleScore);
                    // add the game to the JSONArray
                    games.put(gameJson);
                }
                // add the games to the player
                playerJson.put("games", games);
                // add the player to the JSONArray
                players.put(playerJson);
            }
            // add the JSONArray to the JSONObject
            jsonObject.put("players", players);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        try {
//            Log.d("BetweenGame", "JSON object created: " + jsonObject.toString(4));
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
        FileUtils.writeJsonToFile(this, fileName, jsonObject);
//        Log.d("BetweenGame", "JSON saved to file: " + fileName);
        FileUtils.downloadFile(this, fileName);
//        Log.d("BetweenGame", "File downloaded: " + fileName);
    }

    private void goToMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void saveGameToStats() {
        FileUtils.incrementNumberOfGames(this);
        // update player stats
        for (int indexPlayer = 0; indexPlayer < gameStats.getGame().getPlayers().size(); indexPlayer++) {
            Player player = gameStats.getGame().getPlayers().get(indexPlayer);

            // get the stats of the player
            JSONObject playerStats = FileUtils.getPlayerData(this, player.getName());
            if (playerStats == null) {
                playerStats = new JSONObject();
                try {
                    playerStats.put("name", player.getName());
                    playerStats.put("sessionStats", new JSONArray());
                    playerStats.put("games", new JSONArray());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                // creating a new JSONObject for the new game to add
                JSONObject game = new JSONObject();
                game.put("sessionId", FileUtils.getNumberOfSessions(this));
                game.put("gameId", gameStats.getGame().getGameId());
                game.put("score", player.getRevealedScore());
                game.put("rank", gameStats.getRankOfPlayer(indexPlayer));
                game.put("numberOfPlayers", gameStats.getGame().getPlayers().size());
                game.put("firstFinisher", (gameStats.getFirstPlayerIndex() == indexPlayer));
                game.put("doubleScore", (gameStats.getFirstPlayerIndex() == indexPlayer && gameStats.getDoubleScore()));
                playerStats.put("games", playerStats.getJSONArray("games").put(game));
//                Log.d("BetweenGame", "JSON object created: " + playerStats.toString(4));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            FileUtils.updatePlayerData(this, player.getName(), playerStats);
        }

    }

    private void saveSessionToStats() {

        // update player stats
        for (int indexPlayer = 0; indexPlayer < gameStats.getGame().getPlayers().size(); indexPlayer++) {
            Player player = gameStats.getGame().getPlayers().get(indexPlayer);

            // get the stats of the player
            JSONObject playerStats = FileUtils.getPlayerData(this, player.getName());
            if (playerStats == null) {
                playerStats = new JSONObject();
                try {
                    playerStats.put("name", player.getName());
                    playerStats.put("sessionStats", new JSONArray());
                    playerStats.put("games", new JSONArray());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                // creating a new JSONObject for the new sessionRank to add
                JSONObject sessionRank = new JSONObject();
                sessionRank.put("sessionId", FileUtils.getNumberOfSessions(this));
                sessionRank.put("rank", gameStats.getPlayerRankSession(indexPlayer));
                sessionRank.put("score", player.getTotalScore());
                sessionRank.put("numberOfGames", gameStats.getNumberOfGames());
                sessionRank.put("numberOfPlayers", gameStats.getGame().getPlayers().size());
                playerStats.put("sessionStats", playerStats.getJSONArray("sessionStats").put(sessionRank));
//                Log.d("BetweenGame", "JSON object created: " + playerStats.toString(4));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            FileUtils.updatePlayerData(this, player.getName(), playerStats);
        }
        FileUtils.incrementNumberOfSessions(this);
    }

//            private void endSession() {
////        FileUtils.incrementNumberOfSessions(this);
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//    }



}