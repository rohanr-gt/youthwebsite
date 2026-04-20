package com.example.demo.ludo;

import java.util.*;

public class LudoRoom {
    public String roomId;
    public List<LudoPlayer> players = new ArrayList<>();
    public int currentPlayerIndex = 0;
    public int diceValue = 1;
    public boolean diceRolled = false;
    public String status = "waiting";

    public LudoRoom(String roomId, String playerName) {
        this.roomId = roomId;
        this.players.add(new LudoPlayer(playerName, "RED", 0));
        // Initialize other slots as empty
        this.players.add(new LudoPlayer("", "BLUE", 1));
        this.players.add(new LudoPlayer("", "GREEN", 2));
        this.players.add(new LudoPlayer("", "YELLOW", 3));
    }

    public static class LudoPlayer {
        public String name;
        public String color;
        public int index;
        public int[] pieces = {-1, -1, -1, -1};

        public LudoPlayer(String name, String color, int index) {
            this.name = name;
            this.color = color;
            this.index = index;
        }
    }

    public Map<String, Object> toStateMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", roomId);
        map.put("players", players);
        map.put("currentPlayer", currentPlayerIndex);
        map.put("diceValue", diceValue);
        map.put("diceRolled", diceRolled);
        map.put("status", status);
        return map;
    }

    public void applyRoll(int val) {
        this.diceValue = val;
        this.diceRolled = true;
    }

    public void applyMove(int playerIdx, int pieceIdx, int newPos) {
        players.get(playerIdx).pieces[pieceIdx] = newPos;
        diceRolled = false;
        
        // Handle next turn if not a 6
        if (diceValue != 6) {
            nextTurn();
        }
    }

    private void nextTurn() {
        int original = currentPlayerIndex;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % 4;
        } while (players.get(currentPlayerIndex).name.isEmpty() && currentPlayerIndex != original);
    }
}
