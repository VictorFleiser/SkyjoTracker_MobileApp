package com.example.skyjocounter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class PlayerNamesActivity extends AppCompatActivity {

    private int numberOfPlayers;
    private ArrayList<EditText> playerNameEditTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_names);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        numberOfPlayers = Integer.parseInt(intent.getStringExtra("NUMBER_OF_PLAYERS"));

        LinearLayout layout = findViewById(R.id.names_layout);
        playerNameEditTexts = new ArrayList<>();

        for (int i = 0; i < numberOfPlayers; i++) {
            EditText playerNameEditText = new EditText(this);
            playerNameEditText.setHint("Player " + (i + 1) + " Name");
            layout.addView(playerNameEditText);
            playerNameEditTexts.add(playerNameEditText);
        }

        Button nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gather player names and proceed to the Game activity
//                Intent gameIntent = new Intent(PlayerNamesActivity.this, GameActivity.class);

                // Create an ArrayList of player names
                ArrayList<String> playerNames = new ArrayList<>();
                for (EditText playerNameEditText : playerNameEditTexts) {
                    playerNames.add(playerNameEditText.getText().toString());
                }

                // Pass the player names to the Game activity

                Intent intent = new Intent(PlayerNamesActivity.this, GameActivity.class);
                GamesInfo gamesInfo = new GamesInfo(playerNames);
//                Tuple<ArrayList<String>, ArrayList<Scores>> data = new Tuple<>(playerNames, null);
                intent.putExtra("newGameData", gamesInfo);
                startActivity(intent);

//                gameIntent.putStringArrayListExtra("PLAYER_NAMES", playerNames);
//                startActivity(gameIntent);
            }
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayerNamesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}