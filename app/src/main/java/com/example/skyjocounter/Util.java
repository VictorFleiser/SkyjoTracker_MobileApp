package com.example.skyjocounter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Util {
    public static String getOrdinal(int rank) {
        switch (rank) {
            case 1:
                return "<underline><c#D7AE451st</c></underline>";
            case 2:
                return "<c#AAAAAC2nd</c>";
            case 3:
                return "<c#CD91633rd</c>";
            default:
                return "<c#6C6C6A" + rank + "th</c>";
        }
    }

    public static SpannableString getStyledText(String text) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(text);

        // Apply styles
        applyStyle(spannableBuilder, "<bold>", "</bold>", new StyleSpan(Typeface.BOLD));
        applyStyle(spannableBuilder, "<underline>", "</underline>", new UnderlineSpan());
        applyColorStyle(spannableBuilder, "<c#", "</c>");

        return new SpannableString(spannableBuilder);
    }

    private static void applyStyle(SpannableStringBuilder spannableBuilder, String startDelimiter, String endDelimiter, Object span) {
        int start = spannableBuilder.toString().indexOf(startDelimiter);
        while (start >= 0) {
            int end = spannableBuilder.toString().indexOf(endDelimiter, start + startDelimiter.length());
            if (end > start) {
                int spanStart = start + startDelimiter.length();
                int spanEnd = end;

                // Create a new instance of the span (needs a new instance, because if the same style is applied multiple times, only the last one will be applied)
                Object newSpan = createNewSpan(span);

                // Apply span
                spannableBuilder.setSpan(newSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Remove delimiters
                spannableBuilder.delete(end, end + endDelimiter.length());
                spannableBuilder.delete(start, start + startDelimiter.length());
                // Update start position for next search
                start = spannableBuilder.toString().indexOf(startDelimiter);
            } else {
                start = -1;
            }
        }
    }

    private static void applyColorStyle(SpannableStringBuilder spannableBuilder, String startDelimiter, String endDelimiter) {
        int start = spannableBuilder.toString().indexOf(startDelimiter);
        while (start >= 0) {
            int end = spannableBuilder.toString().indexOf(endDelimiter, start + startDelimiter.length() + 6);
            if (end > start) {
                String color = spannableBuilder.toString().substring(start + startDelimiter.length() - 1, start + startDelimiter.length() + 6);
                int spanStart = start + startDelimiter.length() + 6;
                int spanEnd = end;

                // Apply color span
                spannableBuilder.setSpan(new ForegroundColorSpan(Color.parseColor(color)), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Remove delimiters and color code
                spannableBuilder.delete(end, end + endDelimiter.length());
                spannableBuilder.delete(start, start + startDelimiter.length() + 6);

                // Update start position for next search
                start = spannableBuilder.toString().indexOf(startDelimiter, start);
            } else {
                start = -1;
            }
        }
    }

    private static Object createNewSpan(Object span) {
        if (span instanceof StyleSpan) {
            return new StyleSpan(((StyleSpan) span).getStyle());
        } else if (span instanceof UnderlineSpan) {
            return new UnderlineSpan();
        }
        return span; // For other types of spans
    }

    public static Object[] parseSessionJson(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            int sessionID = jsonObject.getInt("sessionId");
            boolean tracked = jsonObject.getBoolean("tracked");
            int numberOfGames = jsonObject.getInt("numberOfGames");
            ArrayList<Integer> gameIds = new ArrayList<>();
            JSONArray gameArray = jsonObject.getJSONArray("gameIds");
            for (int i = 0; i < gameArray.length(); i++) {
                gameIds.add(gameArray.getInt(i));
            }
            ArrayList<JSONObject> players = new ArrayList<>();
            JSONArray playersArray = jsonObject.getJSONArray("players");
            for (int i = 0; i < playersArray.length(); i++) {
                players.add(playersArray.getJSONObject(i));
            }
            return new Object[]{sessionID, tracked, numberOfGames, gameIds, players};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static ArrayList<JSONObject> parsePlayersJSONArray(JSONObject playersJSONArray) {
//        ArrayList<JSONObject> players = new ArrayList<>();
//        try {
//            for (int i = 0; i < playersJSONArray.length(); i++) {
//                players.add(playersJSONArray.getJSONObject(i));
//            }
//            return players;
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static Object[] parsePlayerJSONObject(JSONObject playerJSONObject) {
        try {
            String playerName = playerJSONObject.getString("name");
            int scoreSession = playerJSONObject.getInt("scoreSession");
            int rankSession = playerJSONObject.getInt("rankSession");
            ArrayList<JSONObject> games = new ArrayList<>();
            JSONArray gamesJSONArray = playerJSONObject.getJSONArray("games");
            for (int i = 0; i < gamesJSONArray.length(); i++) {
                games.add(gamesJSONArray.getJSONObject(i));
            }
            return new Object[]{playerName, scoreSession, rankSession, games};
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object[] parseGameJSONObject(JSONObject gameJSONObject) {
        try {
            int gameId = gameJSONObject.getInt("gameId");
            int score = gameJSONObject.getInt("score");
            int rank = gameJSONObject.getInt("rank");
            boolean firstFinisher = gameJSONObject.getBoolean("firstFinisher");
            boolean doubleScore = gameJSONObject.getBoolean("doubleScore");
            return new Object[]{gameId, score, rank, firstFinisher, doubleScore};
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
