package com.example.skyjocounter;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Game extends AppCompatActivity implements Serializable {
    private static final long serialVersionUID = 1L; // Add this for version control
    private static Game instance;   // single instance of Game class to be accessible from any other classes easily
    private GamesInfo gamesInfo;
    private int gameId;
    private List<Player> players;
    private int currentPlayerIndex;
    private int numberOfPlayers;
    private Piles piles;
//    private ActionSelector actionSelector;
    private CardSelector cardSelector;

    private Cards cards;

//    public Game(ArrayList<String> playerNames, ArrayList<Scores> scores) {
    public Game(GamesInfo gamesInfo) {
        this.gamesInfo = gamesInfo;
        instance = this;
//        addGameId();
        players = new ArrayList<>();
//        numberOfPlayers = playerNamesAndScores.first.size();
        numberOfPlayers = gamesInfo.getScores().first.size();
        cards = new Cards();
        piles = new Piles(150-12*numberOfPlayers);
//        actionSelector = new ActionSelector();
        cardSelector = new CardSelector();
        // Add players to the game with their scores
        for (int i = 0; i < numberOfPlayers; i++) {
            players.add(new Player(gamesInfo.getScores().first.get(i), (gamesInfo.getScores().second == null) ? null : gamesInfo.getScores().second.get(i), i ));
        }
        currentPlayerIndex = 0;
    }

    public void addGameId(Context context) {
        gameId =  FileUtils.getNumberOfGames(context);
//        Log.d("Game", "GameId: " + gameId);
//        if (gamesInfo == null) {
//            Log.d("Game", "gamesInfo is null");
//        }
        gamesInfo.addGameId(gameId);
    }
    public List<Player> getPlayers() {
        return players;
    }

    public Piles getPiles() {
        return piles;
    }

//    public ActionSelector getActionSelector() {
//        return actionSelector;
//    }

    public CardSelector getCardSelector() {
        return cardSelector;
    }

    public Cards getCards() {
        return cards;
    }

    // More methods for game logic and state management
    public static Game getInstance() {
        return instance;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % numberOfPlayers;
    }

    public void previousTurn() {
        currentPlayerIndex = (currentPlayerIndex - 1 + numberOfPlayers) % numberOfPlayers;
    }

    public int getGameId () {
        return gameId;
    }

    public GamesInfo getGamesInfo() {
        return gamesInfo;
    }

    // Display method will be integrated with Android UI

    // Game logic :
    // I) general information :
    // 1) the possible cards are : "flipped", "m2", "m1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "joker", "empty"
    // II) interactions when pressing on a card :
    // 1) When a non-empty pile is pressed, if the Hold has an empty card, then the top card is moved from the pile to the Hold and replaces the empty card
    // otherwise (ie the hold has a non-empty card), the top card of the pile is switched with the card in the Hold
    // 2) When a card (empty or not) from the deck is pressed, it is switched with the card in the hold (empty or not)
    // 3) when a card from the CardSelector is pressed, it edits the value of the card in the Hold with the value of the card pressed
    // III) interactions when pressing on a button :
    // 1) buttons from the action selector :
    // - "Undo" : undo the last action (not planned to be implemented yet)
    // - "Next Turn" : switch to the next player
    // - "Previous Turn" : switch to the previous player
    // - "End Game" : end the game after a "confirm"/"go back" dialog box, will save the results of the game (to be implemented later) and switches to a new page showing the results of the game
    // 2) MiniMenu button : brings a side bar over the screen with information about the state of the game, and a button to close it
    // 3) "Change Order" button under discard piles : brings a box with the top N cards of the discard pile, pressing 2 cards in a row will swap the cards, also has a button to close it
    // IV) flow of information/actions between classes :
    // 1) Game is the highest level class and contains all the other classes directly or indirectly, Game is a singleton (can be accessed through the static method "getInstance" of the Game class) so it is accessible from any other class easily
    // 2) Cards handles keeping track of how many card of each type are in the game, and therefore can provide the various statistics concerning the cards to the other classes who might need it, when cards are modified/discovered, whichever class handles this change should notify Cards of the corresponding change (accessible through Game.getCards()))
}
