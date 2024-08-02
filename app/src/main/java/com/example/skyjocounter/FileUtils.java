package com.example.skyjocounter;

import android.Manifest;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileUtils {
    public static void writeJsonToFile(Context context, String fileName, JSONObject jsonObject) {
        // writes a JSON file to the app's internal storage
        File path = context.getApplicationContext().getFilesDir();
//        Log.d("FileUtils", "Writing JSON to file: " + path.getAbsolutePath());
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(jsonObject.toString(4).getBytes());
            writer.close();
            Toast.makeText(context.getApplicationContext(), "File Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writeJsonToFile(Context context, String fileName, JSONArray jsonArray) {
        // writes a JSON file to the app's internal storage
        File path = context.getApplicationContext().getFilesDir();
//        Log.d("FileUtils", "Writing JSON to file: " + path.getAbsolutePath());
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(jsonArray.toString(4).getBytes());
            writer.close();
            Toast.makeText(context.getApplicationContext(), "File Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writeJsonToFile(Context context, String fileName, String stringObject) {
        // writes a JSON file to the app's internal storage
        File path = context.getApplicationContext().getFilesDir();
//        Log.d("FileUtils", "Writing JSON to file: " + path.getAbsolutePath());
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(stringObject.getBytes());
            writer.close();
            Toast.makeText(context.getApplicationContext(), "File Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String readTextFromFile(Context context, String fileName) {
        // reads a text file from the app's internal storage
        File path = context.getApplicationContext().getFilesDir();
        File readFrom = new File(path, fileName);
        byte[] content = new byte[(int) readFrom.length()];
        try {
            FileInputStream stream = new FileInputStream(readFrom);
            stream.read(content);
            return new String (content);
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static void downloadFile(Context context, String fileName) {
        // downloads a file from the app's internal storage to the external storage (Download/)

        // Check if WRITE_EXTERNAL_STORAGE permission is granted
        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED) {
//            Log.d("FileUtils", "Requesting WRITE_EXTERNAL_STORAGE permission");
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ((android.app.Activity) context).requestPermissions(permissions, 1000);
//            Log.d("FileUtils", "WRITE_EXTERNAL_STORAGE permission requested");
        }
        else {

            // start downloading
            // Get source and destination files
            File sourceFile = new File(context.getFilesDir(), fileName);
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File destinationFile = new File(downloadDir, fileName);

            try (FileInputStream input = new FileInputStream(sourceFile);
                 FileOutputStream output = new FileOutputStream(destinationFile)) {

                // Copy the file
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                Toast.makeText(context.getApplicationContext(), "File Downloaded", Toast.LENGTH_SHORT).show();
//                Log.d("FileUtils", "File downloaded to: " + destinationFile.getAbsolutePath());

            } catch (IOException e) {
                Log.e("FileUtils", "Error downloading file", e);
                // Handle error appropriately
            }
        }
    }

    private static String getAppStats(Context context) {
        File path = context.getApplicationContext().getFilesDir();
        File statsFile = new File(path, "stats.json");
        // Check if the file exists
        if (!statsFile.exists()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("numberOfSessions", 0);
                jsonObject.put("numberOfGames", 0);
                writeJsonToFile(context, "stats.json", jsonObject);
                return readTextFromFile(context, "stats.json");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        // Read the file
        return readTextFromFile(context, "stats.json");
    }

    public static int getNumberOfSessions(Context context) {
        String stats = getAppStats(context);
        try {
            JSONObject jsonObject = new JSONObject(stats);
            return jsonObject.getInt("numberOfSessions");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getNumberOfGames(Context context) {
//        return 1;
        if (context == null) {
            Log.e("FileUtils", "Context is null");
        }
        String stats = getAppStats(context);
        try {
            JSONObject jsonObject = new JSONObject(stats);
            return jsonObject.getInt("numberOfGames");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateStats(Context context, int numberOfSessions, int numberOfGames) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("numberOfSessions", numberOfSessions);
            jsonObject.put("numberOfGames", numberOfGames);
            writeJsonToFile(context, "stats.json", jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void incrementNumberOfGames(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(getAppStats(context));
            jsonObject.put("numberOfGames", jsonObject.getInt("numberOfGames") + 1);
            writeJsonToFile(context, "stats.json", jsonObject);
//            Log.d("FileUtils", "Number of games incremented to: " + jsonObject.getInt("numberOfGames"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void incrementNumberOfSessions(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(getAppStats(context));
            jsonObject.put("numberOfSessions", jsonObject.getInt("numberOfSessions") + 1);
            writeJsonToFile(context, "stats.json", jsonObject);
//            Log.d("FileUtils", "Number of sessions incremented to: " + jsonObject.getInt("numberOfSessions"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject getPlayerData(Context context, String playerName) {
        File path = context.getApplicationContext().getFilesDir();
        File playersFile = new File(path, "players.json");
        if (!playersFile.exists()) {
            JSONArray jsonArray = new JSONArray();
            writeJsonToFile(context, "players.json", jsonArray);
        }
        try {
            JSONArray players = new JSONArray(readTextFromFile(context, "players.json"));
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                if (player.getString("name").equals(playerName)) {
                    return player;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static void modifyPlayerData(Context context, String playerName, JSONObject playerData) {
        JSONArray players;
        try {
            players = new JSONArray(readTextFromFile(context, "players.json"));
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                if (player.getString("name").equals(playerName)) {
                    players.put(i, playerData);
                    break;
                }
            }
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
        writeJsonToFile(context, "players.json", players);
    }
    public static void deletePlayerData(Context context, String playerName) {
        JSONArray players;
        try {
            players = new JSONArray(readTextFromFile(context, "players.json"));
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                if (player.getString("name").equals(playerName)) {
                    players.remove(i);
                    break;
                }
            }
            writeJsonToFile(context, "players.json", players);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private static void addPlayerData(Context context, JSONObject playerData) {
        JSONArray players;
        try {
            players = new JSONArray(readTextFromFile(context, "players.json"));
            players.put(playerData);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
        writeJsonToFile(context, "players.json", players);
    }

    public static void updatePlayerData(Context context, String playerName, JSONObject playerData) {
        if (getPlayerData(context, playerName) == null) {
            addPlayerData(context, playerData);
        }
        else {
            modifyPlayerData(context, playerName, playerData);
        }
    }

    public static JSONArray getPlayers(Context context) {
        File path = context.getApplicationContext().getFilesDir();
        File playersFile = new File(path, "players.json");
        if (!playersFile.exists()) {
            JSONArray jsonArray = new JSONArray();
            writeJsonToFile(context, "players.json", jsonArray);
        }
        try {
            JSONArray players = new JSONArray(readTextFromFile(context, "players.json"));
            return players;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteFile(Context context, String fileName) {
        File path = context.getApplicationContext().getFilesDir();
        File file = new File(path, fileName);
        if (file.exists()) {
            file.delete();
        }
        Toast.makeText(context.getApplicationContext(), "File Deleted", Toast.LENGTH_SHORT).show();
    }

    public static ArrayList<String> getFileNames(Context context) {
        File path = context.getApplicationContext().getFilesDir();
        ArrayList<String> fileNames = new ArrayList<>();
        for (File file : path.listFiles()) {
            fileNames.add(file.getName());
        }
        return fileNames;
    }
}