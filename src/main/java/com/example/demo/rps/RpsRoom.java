package com.example.demo.rps;

import java.util.Map;

public class RpsRoom {
    public String roomId;
    public String player1;
    public String player2;
    public String p1Choice = "";
    public String p2Choice = "";
    public int p1Score = 0;
    public int p2Score = 0;
    public String status = "waiting";
    public String lastResult = "";

    public RpsRoom(String roomId, String player1) {
        this.roomId = roomId;
        this.player1 = player1;
    }

    public void applyChoice(int playerNum, String choice) {
        if (playerNum == 1) {
            p1Choice = choice;
        } else {
            p2Choice = choice;
        }

        // If both chosen, determine winner
        if (!p1Choice.isEmpty() && !p2Choice.isEmpty()) {
            calculateResult();
        }
    }

    private void calculateResult() {
        if (p1Choice.equals(p2Choice)) {
            lastResult = "draw";
        } else if (
            (p1Choice.equals("Rock") && p2Choice.equals("Scissors")) ||
            (p1Choice.equals("Paper") && p2Choice.equals("Rock")) ||
            (p1Choice.equals("Scissors") && p2Choice.equals("Paper"))
        ) {
            lastResult = "p1";
            p1Score++;
        } else {
            lastResult = "p2";
            p2Score++;
        }
    }

    public void nextRound() {
        p1Choice = "";
        p2Choice = "";
        lastResult = "";
    }

    public Map<String, Object> toStateMap() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("roomId", roomId);
        map.put("player1", player1 != null ? player1 : "");
        map.put("player2", player2 != null ? player2 : "");
        map.put("p1Choice", p1Choice);
        map.put("p2Choice", p2Choice);
        map.put("p1Score", p1Score);
        map.put("p2Score", p2Score);
        map.put("status", status);
        map.put("lastResult", lastResult);
        return map;
    }

    // Version of state that hides choices if only one player has chosen
    public Map<String, Object> toHiddenStateMap() {
        boolean bothChosen = !p1Choice.isEmpty() && !p2Choice.isEmpty();
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("roomId", roomId);
        map.put("player1", player1 != null ? player1 : "");
        map.put("player2", player2 != null ? player2 : "");
        map.put("p1Chosen", !p1Choice.isEmpty());
        map.put("p2Chosen", !p2Choice.isEmpty());
        map.put("p1Choice", bothChosen ? p1Choice : "");
        map.put("p2Choice", bothChosen ? p2Choice : "");
        map.put("p1Score", p1Score);
        map.put("p2Score", p2Score);
        map.put("status", status);
        map.put("lastResult", lastResult);
        return map;
    }
}
