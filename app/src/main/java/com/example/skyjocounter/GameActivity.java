package com.example.skyjocounter;

import static java.lang.Math.min;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.AlertDialog;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
//    private Tuple<ArrayList<String>, ArrayList<Scores>> newGameData;
    private GamesInfo gamesInfo;
//    private ArrayList<String> playerNames;
    private Game game;
    private TextView activePlayerNameTextView;
    private GridLayout deckGridLayout;
    private LinearLayout discardPile1Layout;
    private LinearLayout drawPileLayout;
    private LinearLayout discardPile2Layout;
    private LinearLayout holdLayout;

    private int finishedFirst = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        // Initialize game instance with player names and past scores from intent
        Intent intent = getIntent();
        gamesInfo = (GamesInfo) intent.getSerializableExtra("newGameData");
//        if (gamesInfo == null) {
//            Log.d("gamesInfo", "null");
//        }

//        newGameData = (Tuple<ArrayList<String>, ArrayList<Scores>>) intent.getSerializableExtra("newGameData");
//        if (newGameData == null) {
//            // throw error: TODO
//            return;
//        }

//        playerNames = newGameData.first;

//        ArrayList<Scores> pastScores = newGameData.getSecond();
//        playerNames = getIntent().getStringArrayListExtra("PLAYER_NAMES");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize and set up the game with the given player names
        game = new Game(gamesInfo);

        // Set up layout references
        activePlayerNameTextView = findViewById(R.id.active_player_text);
        deckGridLayout = findViewById(R.id.deck_grid);
        discardPile1Layout = findViewById(R.id.discard_pile1_layout);
        drawPileLayout = findViewById(R.id.draw_pile_layout);
        discardPile2Layout = findViewById(R.id.discard_pile2_layout);
        holdLayout = findViewById(R.id.hold_layout);

        // Set up click listeners for discard piles and draw pile
        discardPile1Layout.setOnClickListener(v -> onDiscardPileClick(1));
        discardPile2Layout.setOnClickListener(v -> onDiscardPileClick(2));
        drawPileLayout.setOnClickListener(v -> onDrawPileClick());

        // Set the active player's name
        updateActivePlayerName();

        // Display the active player's deck
        displayActivePlayerDeck();

        // Display the piles
        displayPiles();

        // Set up buttons for changing order of discard piles
        Button changeOrderButton1 = findViewById(R.id.change_order_button1);
        Button changeOrderButton2 = findViewById(R.id.change_order_button2);
        changeOrderButton1.setOnClickListener(v -> showChangeOrderDialog(game.getPiles().getDiscardPile1().getCards(), 1));
        changeOrderButton2.setOnClickListener(v -> showChangeOrderDialog(game.getPiles().getDiscardPile2().getCards(), 2));

        // Display number of cards left in draw pile
        TextView cardsLeftText = findViewById(R.id.cards_left_text);
        cardsLeftText.setText("Cards left: " + game.getPiles().getDrawPile().getCards().size());

        // Display the Card Selector
        displayCardSelector();

        // Set up button actions
        Button previousTurnButton = findViewById(R.id.previous_turn_button);
        Button nextTurnButton = findViewById(R.id.next_turn_button);
        Button endGameButton = findViewById(R.id.end_game_button);
        Button playersInfoButton = findViewById(R.id.players_info_button);
        Button cardsInfoButton = findViewById(R.id.cards_info_button);
        playersInfoButton.setOnClickListener(v -> showPlayersInfoDialog());
        cardsInfoButton.setOnClickListener(v -> showCardsInfoDialog());

        previousTurnButton.setOnClickListener(v -> {
            checkIfFinished();
            game.previousTurn();
            updateActivePlayerAndDeck();
        });

        nextTurnButton.setOnClickListener(v -> {
            checkIfFinished();
            game.nextTurn();
            updateActivePlayerAndDeck();
        });

        endGameButton.setOnClickListener(v -> showEndGameDialog());

//        Button menuButton = findViewById(R.id.menu_button);
//        menuButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(GameActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private void displayCardSelector() {
        GridLayout cardSelectorGrid = findViewById(R.id.card_selector_grid);
        cardSelectorGrid.removeAllViews();

        ArrayList<Card> cardTypes = game.getCardSelector().getCardTypes();
        int columnCount = 6;

        for (Card cardType : cardTypes) {
            ImageView cardImageView = new ImageView(this);
            String cardTypeString = cardType.getType();
            int resourceId = getResources().getIdentifier("card_1x1_" + cardTypeString, "drawable", getPackageName());
            cardImageView.setImageResource(resourceId);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = getResources().getDisplayMetrics().widthPixels / columnCount - 4;
            params.height = params.width * 1/1; // Card aspect ratio of 100px:100px
            params.setMargins(2, 2, 2, 2);
            cardImageView.setLayoutParams(params);

            cardImageView.setOnClickListener(v -> onCardSelectorClick(cardType.getType()));

            cardSelectorGrid.addView(cardImageView);
        }
    }

    private void showChangeOrderDialog(ArrayList<Card> cards, int discardPileNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_dialog_change_order, null);
        builder.setView(dialogView);

        GridLayout cardGrid = dialogView.findViewById(R.id.card_grid);
        Button closeButton = dialogView.findViewById(R.id.close_button);

        // Close button to dismiss the dialog
        AlertDialog dialog = builder.create();
        closeButton.setOnClickListener(v -> dialog.dismiss());

        final int[] firstClickedIndex = { -1 }; // Array to keep track of the first clicked card index

        // Display the top 15 cards or all cards if they are less than 15
//        int amountOfCardsToShow = min(cards.size(), 15);
        int amountOfCardsToShow = cards.size();
        for (int i = 0; i < amountOfCardsToShow; i++) {
            ImageView cardImageView = new ImageView(this);
            int cardIndex = i; // Use current index
            String cardType = cards.get(cardIndex).getType();
            int resourceId = getResources().getIdentifier("card_1x1_" + cardType, "drawable", getPackageName());
            cardImageView.setImageResource(resourceId);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(2, 2, 2, 2);
            cardImageView.setLayoutParams(params);

            cardImageView.setOnClickListener(v -> {
                if (firstClickedIndex[0] == -1) {
                    // First card clicked
                    firstClickedIndex[0] = cardIndex;
                } else {
                    // Second card clicked, swap the cards
                    int secondClickedIndex = cardIndex;
                    DiscardPile dp = discardPileNumber == 1 ? game.getPiles().getDiscardPile1() : game.getPiles().getDiscardPile2();
                    Card firstCard = dp.getCards().get(firstClickedIndex[0]);
                    Card secondCard = dp.getCards().get(secondClickedIndex);

//                    firstCard.addOwner(firstCard.getOwners().get(firstCard.getOwners().size()-1));
//                    firstCard.addPosition(secondClickedIndex);
//                    secondCard.addOwner(secondCard.getOwners().get(secondCard.getOwners().size()-1));
//                    secondCard.addPosition(firstClickedIndex[0]);

                    dp.getCards().set(firstClickedIndex[0], secondCard);
                    dp.getCards().set(secondClickedIndex, firstCard);
//                    Collections.swap(cards, firstClickedIndex[0], secondClickedIndex);
                    firstClickedIndex[0] = -1; // Reset the first clicked index

                    // Refresh the dialog to show the swapped cards
                    dialog.dismiss();
                    displayPiles();
                    showChangeOrderDialog(cards, discardPileNumber);
                }
            });

            cardGrid.addView(cardImageView);
        }

        dialog.show();
    }

    private void updateActivePlayerName() {
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        String activePlayerName = game.getPlayers().get(currentPlayerIndex).getName();
        activePlayerNameTextView.setText("Player: " + activePlayerName);
    }

    private void displayActivePlayerDeck() {
        Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
        ArrayList<Card> deck = currentPlayer.getDeck().getCards();

        deckGridLayout.removeAllViews();
        for (int i = 0; i < deck.size(); i++) {
            Card card = deck.get(i);
            ImageView cardImageView = new ImageView(this);
            String cardType = card.getType();
            int resourceId = getResources().getIdentifier("card_1x1_" + cardType, "drawable", getPackageName());
            cardImageView.setImageResource(resourceId);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(1, -4, 1, -4);

            cardImageView.setLayoutParams(params);

            int cardIndex = i;  // Capture the index for use in the onClickListener
            cardImageView.setOnClickListener(v -> onDeckCardClick(cardIndex));

            deckGridLayout.addView(cardImageView);
        }
    }

    private void displayPiles() {
        displayPile(discardPile1Layout, game.getPiles().getDiscardPile1().getCards());
        displayPile(drawPileLayout, game.getPiles().getDrawPile().getCards());
        displayPile(discardPile2Layout, game.getPiles().getDiscardPile2().getCards());
        displaySingleCard(holdLayout, game.getPiles().getHold().getCard());
        updateCardsLeftText();
    }

    private void displayPile(LinearLayout layout, ArrayList<Card> cards) {
        layout.removeAllViews();
        int amountOfCardsToShow = 3;
        for (int i = 0; i < amountOfCardsToShow; i++) {
            ImageView cardImageView = new ImageView(this);
            int cardIndex = cards.size()-amountOfCardsToShow+i;
//            Log.d("cardIndex", cardIndex+"");
            if (cardIndex >= 0) {
                String cardType = cards.get(cardIndex).getType();
                int resourceId = getResources().getIdentifier("card_1x1_" + cardType, "drawable", getPackageName());
//                Log.d("cardType", cardType+"");
                cardImageView.setImageResource(resourceId);
            } else {
                int resourceId = getResources().getIdentifier("card_1x1_empty", "drawable", getPackageName());
//                Log.d("cardType", "empty");
                cardImageView.setImageResource(resourceId);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i == 0) {
                params.setMargins(0, 0, 0, 0);
            }
            else {
                params.setMargins(0, -150, 0, 0);  // Adjust the vertical margin for overlap
            }
            cardImageView.setLayoutParams(params);

            layout.addView(cardImageView);
        }
    }

    private void displaySingleCard(LinearLayout layout, Card card) {
        layout.removeAllViews();
        ImageView cardImageView = new ImageView(this);
        if (card != null) {
            String cardType = card.getType();
            int resourceId = getResources().getIdentifier("card_1x1_" + cardType, "drawable", getPackageName());
            cardImageView.setImageResource(resourceId);
        } else {
            int resourceId = getResources().getIdentifier("card_1x1_empty", "drawable", getPackageName());
            cardImageView.setImageResource(resourceId);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardImageView.setLayoutParams(params);

        layout.addView(cardImageView);
    }

    private void showEndGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to end the game?")
                .setPositiveButton("Confirm", (dialog, id) -> endGame())
                .setNegativeButton("Go Back", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void endGame() {
        // Save the results of the game (to be implemented later)

        // Switch to a new page showing the results of the game
        Intent intent = new Intent(GameActivity.this, GameStats.class);
        // Put the Game object in the intent
        intent.putExtra("game", game);
        startActivity(intent);
    }

    private void updateActivePlayerAndDeck() {
        displayActivePlayerDeck();
        updateActivePlayerName();
    }

    private void showPlayersInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.activity_dialog_players_info, null);
        LinearLayout playersInfoContainer = dialogView.findViewById(R.id.players_info_container);

        for (Player player : game.getPlayers()) {
            View playerInfoView = getLayoutInflater().inflate(R.layout.item_player_info, null);
            TextView playerNameTextView = playerInfoView.findViewById(R.id.player_name);
            TextView revealedScoreTextView = playerInfoView.findViewById(R.id.revealed_score);
            TextView estimatedScoreTextView = playerInfoView.findViewById(R.id.estimated_score);
            TextView totalScoreTextView = playerInfoView.findViewById(R.id.total_score);

            playerNameTextView.setText(Util.getStyledText(player.getName()));
            revealedScoreTextView.setText(Util.getStyledText("Revealed Score: " + player.getRevealedScore()));
            estimatedScoreTextView.setText(Util.getStyledText("Estimated Score: " + player.getEstimatedScore()));
            totalScoreTextView.setText(Util.getStyledText("Total Score: " + player.getTotalScoresString()));

            playersInfoContainer.addView(playerInfoView);
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button closeButton = dialogView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCardsInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.activity_dialog_cards_info, null);
        LinearLayout cardsInfoContainer = dialogView.findViewById(R.id.cards_info_container);
        TextView averageFlippedValueTextView = dialogView.findViewById(R.id.average_flipped_value);

        String[] cardTypes = {"flipped", "m2", "m1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "joker"};
        for (String cardType : cardTypes) {
            View cardInfoView = getLayoutInflater().inflate(R.layout.item_card_info, null);
            ImageView cardImageView = cardInfoView.findViewById(R.id.card_image);
            TextView cardAmountTextView = cardInfoView.findViewById(R.id.card_amount);

            int resourceId = getResources().getIdentifier("card_1x1_" + cardType, "drawable", getPackageName());
            cardImageView.setImageResource(resourceId);
            if (cardType.equals("flipped")) {
                cardAmountTextView.setText("   " + game.getCards().getCardAmount(cardType) + " / 150 not revealed yet");
            }
            else {
                cardAmountTextView.setText("   " + game.getCards().getCardAmount(cardType) + " / " + game.getCards().getAmountOfCards().get(cardType) + " revealed");
            }

            cardsInfoContainer.addView(cardInfoView);
        }

        averageFlippedValueTextView.setText("Average value of flipped cards: " + game.getCards().getAverageValueFlipped());

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button closeButton = dialogView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void onDiscardPileClick(int pileNumber) {
        ArrayList<Card> discardPile = pileNumber == 1 ? game.getPiles().getDiscardPile1().getCards() : game.getPiles().getDiscardPile2().getCards();
        Card holdCard = game.getPiles().getHold().getCard();

        if (holdCard.getType().equals("empty")) {
            if (!discardPile.isEmpty()) {
                Card topCard = discardPile.remove(discardPile.size() - 1);

                game.getPiles().getHold().setCard(topCard);
            }
        } else {
            holdCard.addOwner(-(pileNumber+1));
            holdCard.addPosition(discardPile.size());

            discardPile.add(holdCard);
            game.getPiles().getHold().setCard(new Card("empty", -4, 0));
        }

        displayPiles();
        displaySingleCard(holdLayout, game.getPiles().getHold().getCard());
    }

    private void onDrawPileClick() {
        ArrayList<Card> drawPile = game.getPiles().getDrawPile().getCards();
        Card holdCard = game.getPiles().getHold().getCard();

        if (holdCard.getType().equals("empty") && !drawPile.isEmpty()) {
            Card topCard = drawPile.remove(drawPile.size() - 1);
            game.getPiles().getHold().setCard(topCard);
        }

        displayPiles();
        displaySingleCard(holdLayout, game.getPiles().getHold().getCard());
        updateCardsLeftText();
    }

    private void updateCardsLeftText() {
        TextView cardsLeftText = findViewById(R.id.cards_left_text);
        cardsLeftText.setText("Cards left: " + game.getPiles().getDrawPile().getCards().size());
    }

    private void onCardSelectorClick(String newType) {
        Card holdCard = game.getPiles().getHold().getCard();
        if (!holdCard.getType().equals("empty")) {
            String oldType = holdCard.getType();
            holdCard.setType(newType);
            game.getCards().changeCardType(oldType, newType);
            displaySingleCard(holdLayout, holdCard);
        }
    }

    private void onDeckCardClick(int cardIndex) {
        Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
        ArrayList<Card> deck = currentPlayer.getDeck().getCards();
        Card holdCard = game.getPiles().getHold().getCard();

        // Swap the clicked deck card with the hold card
        Card clickedCard = deck.get(cardIndex);

        holdCard.addOwner(game.getCurrentPlayerIndex());
        holdCard.addPosition(-(cardIndex+1));

        deck.set(cardIndex, holdCard);
        game.getPiles().getHold().setCard(clickedCard);

        // Refresh the display
        displayActivePlayerDeck();
        displaySingleCard(holdLayout, game.getPiles().getHold().getCard());
    }

    // verifies if the current player has revealed all their cards
    private void checkIfFinished() {
        // check if the game already has a first finisher
        if (finishedFirst != -1) {
//            Log.d("finishedFirst", finishedFirst+"");
            return; // a player has already finished
        }
        // check if the current player has revealed all their cards
        Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
        ArrayList<Card> deck = currentPlayer.getDeck().getCards();
        for (Card card : deck) {
            if (card.getType().equals("flipped")) {
                return; // not all cards are revealed
            }
        }
        askIfFinished(game.getCurrentPlayerIndex());
    }
    // creates a dialog box to ask if the current player has finished 1st
    private void askIfFinished(int currentPlayerIndex) {
        // Get the current player's name
        String currentPlayerName = game.getPlayers().get(currentPlayerIndex).getName();

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Did player " + currentPlayerName + " finish first?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Set the finishedFirst to the current player index
//                        Log.d("finishedFirst", currentPlayerIndex+"");
                        finishedFirst = currentPlayerIndex;
                        game.getPlayers().get(finishedFirst).finishedFirst = true;
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Just dismiss the dialog
                        dialog.dismiss();
                    }
                });

        // Create and show the dialog
        AlertDialog alert = builder.create();
        alert.show();
    }
}