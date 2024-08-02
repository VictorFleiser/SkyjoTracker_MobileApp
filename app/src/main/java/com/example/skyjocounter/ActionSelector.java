package com.example.skyjocounter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActionSelector {
    private List<String> actions;

    public ActionSelector() {
        actions = new ArrayList<>();
        actions.add("Undo");    // maybe too hard to implement
        actions.add("Previous Turn");
        actions.add("Next Turn");
        actions.add("End Game");
        // Add more actions as needed
    }

    public List<String> getActions() {
        return actions;
    }

    // Display and onClick methods will be integrated with Android UI
    // 1 button for each action
}
