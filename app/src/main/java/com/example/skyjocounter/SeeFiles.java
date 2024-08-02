package com.example.skyjocounter;

import static java.lang.Math.max;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class SeeFiles extends AppCompatActivity {

    private LinearLayout filesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_files);

        TextView titleTextView = findViewById(R.id.title_text_view);
        titleTextView.setText("Files");

        filesContainer = findViewById(R.id.files_container);

        displayFiles();
    }

    private void displayFiles() {
        filesContainer.removeAllViews();

        addSectionTitle("General Files");
        addFileSection("stats.json");
        addFileSection("players.json");
        addDelimiter();

        addSectionTitle("Previous Sessions");
        ArrayList<String> fileNames = FileUtils.getFileNames(this);
//        Log.d("SeeFiles", "File names: " + fileNames);
        // filter files that start with "game_" or "session_"
        ArrayList<String> sessionFiles = new ArrayList<>();
        for (String fileName : fileNames) {
            if (fileName.startsWith("game_") || fileName.startsWith("session_")) {
                sessionFiles.add(fileName);
            }
//            Log.d("SeeFiles", "File name: " + fileName);
        }
        for (String file : sessionFiles) {
            addFileSection(file);
        }
        addDelimiter();
        Button addGameButton = new Button(this);
        addGameButton.setText("Add Session File");
        addGameButton.setOnClickListener(v -> addGame());
        filesContainer.addView(addGameButton);
    }

    private void addSectionTitle(String title) {
        TextView titleTextView = new TextView(this);
        titleTextView.setText(title);
        titleTextView.setTextSize(20);
        titleTextView.setPadding(16, 16, 16, 16);
        filesContainer.addView(titleTextView);
    }

    private void addFileSection(String fileName) {
        LinearLayout fileSection = new LinearLayout(this);
        fileSection.setOrientation(LinearLayout.VERTICAL);
        fileSection.setPadding(16, 16, 16, 16);

        TextView fileNameTextView = new TextView(this);
        fileNameTextView.setText(fileName);
        fileSection.addView(fileNameTextView);

        // See File Button
        Button seeFileButton = new Button(this);
        seeFileButton.setText("See File");
        seeFileButton.setOnClickListener(v -> seeFile(fileName));
        fileSection.addView(seeFileButton);

        // Download File Button
        Button downloadFileButton = new Button(this);
        downloadFileButton.setText("Download File");
        downloadFileButton.setOnClickListener(v -> FileUtils.downloadFile(this, fileName));
        fileSection.addView(downloadFileButton);

        // Delete File Button
        Button deleteFileButton = new Button(this);
        deleteFileButton.setText("Delete File");
        deleteFileButton.setOnClickListener(v -> {
            FileUtils.deleteFile(this, fileName);
            displayFiles();
        });
        fileSection.addView(deleteFileButton);

        // Replace File Button
        Button replaceFileButton = new Button(this);
        replaceFileButton.setText("Replace File");
        replaceFileButton.setOnClickListener(v -> replaceFile(fileName));
        fileSection.addView(replaceFileButton);

        filesContainer.addView(fileSection);
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
        filesContainer.addView(delimiter);
    }

    private void seeFile(String fileName) {
        String fileContents = FileUtils.readTextFromFile(this, fileName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_dialog_view_file, null);
        builder.setView(dialogView);

        TextView fileContentTextView = dialogView.findViewById(R.id.file_content_text_view);
        fileContentTextView.setText(fileContents);

        Button closeButton = dialogView.findViewById(R.id.close_button);
        AlertDialog dialog = builder.create();
        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void replaceFile(String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Replace File: " + fileName);

        final EditText input = new EditText(this);
        input.setHint("Paste file contents here");
        builder.setView(input);

        builder.setPositiveButton("Replace", (dialog, which) -> {
            String newContent = input.getText().toString();
//            newContent = hardCodedReplace();    // read from hard coded file "hardCoded.json" in assets
            FileUtils.writeJsonToFile(this, fileName, newContent);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Session File");

        final EditText input = new EditText(this);
        input.setHint("Paste file contents here\n (first line will be the file name, you must include the extension)");
        builder.setView(input);

        builder.setPositiveButton("Add Session File", (dialog, which) -> {
            String newContent = input.getText().toString();
            String fileName = newContent.split("\n")[0];
            newContent = newContent.substring(newContent.indexOf("\n") + 1);
//            Log.d("SeeFiles", "File name: " + fileName);
//            Log.d("SeeFiles", "File contents: " + newContent);
            FileUtils.writeJsonToFile(this, fileName, newContent);
            updatePlayerStats(newContent);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updatePlayerStats(String sessionJSON) {
        // update the players.json file and the stats.json file with the new data from the session file

        // parse the session file
        Object[] sessionObjects = Util.parseSessionJson(sessionJSON);
        int sessionID = (int) sessionObjects[0];
        boolean tracked = (boolean) sessionObjects[1];
        int numberOfGames = (int) sessionObjects[2];
        ArrayList<Integer> gameIds = (ArrayList<Integer>) sessionObjects[3];
        ArrayList<JSONObject> players = (ArrayList<JSONObject>) sessionObjects[4];

        // update stats.json for sessionId and gameId
        int sessionIdStats = FileUtils.getNumberOfSessions(this);
        int gameIdStats = FileUtils.getNumberOfGames(this);
        FileUtils.updateStats(this, max(sessionIdStats, sessionID), max(gameIdStats, Collections.max(gameIds)));

        // update players.json
        for (int i = 0; i < players.size(); i++) {
            // parse the player object
            JSONObject player = players.get(i);
            Object[] playerObjects = Util.parsePlayerJSONObject(player);
            String playerName = (String) playerObjects[0];
            int scoreSession = (int) playerObjects[1];
            int rankSession = (int) playerObjects[2];
            ArrayList<JSONObject> games = (ArrayList<JSONObject>) playerObjects[3];

            // get the stats of the player from players.json
            JSONObject playerStats = FileUtils.getPlayerData(this, playerName);
            if (playerStats == null) {
                // player does not exist in players.json, create a new entry
                playerStats = new JSONObject();
                try {
                    playerStats.put("name", playerName);
                    playerStats.put("games", new JSONArray());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            // add all the new games to the player's games array
            for (int j = 0; j < games.size(); j++) {
                // parse the game object
                JSONObject game = games.get(j);
                Object[] gameObjects = Util.parseGameJSONObject(game);
                int gameId = (int) gameObjects[0];
                int score = (int) gameObjects[1];
                int rank = (int) gameObjects[2];
                boolean firstFinisher = (boolean) gameObjects[3];
                boolean doubleScore = (boolean) gameObjects[4];

                // create a new JSONObject for the new game
                try {
                    // creating a new JSONObject for the new game to add
                    JSONObject newGame = new JSONObject();
                    newGame.put("sessionId", sessionID);
                    newGame.put("gameId", gameId);
                    newGame.put("score", score);
                    newGame.put("rank", rank);
                    newGame.put("numberOfPlayers", players.size());
                    newGame.put("firstFinisher", firstFinisher);
                    newGame.put("doubleScore", doubleScore);
                    // add the new game to the player's games array
                    playerStats.put("games", playerStats.getJSONArray("games").put(newGame));
//                    Log.d("BetweenGame", "JSON object created: " + playerStats.toString(4));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            // update players.json for the current player
            FileUtils.updatePlayerData(this, playerName, playerStats);
        }
    }

    private String hardCodedReplace() {
        String jsonString = null;
        try {
            InputStream is = this.getAssets().open("hardCoded.json");
//            Log.d("SeeFiles", "is: " + is);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonString;
    }
}