package com.example.demo.snake;

public class SnakeRollMessage {
    private int diceStatus;
    private int steps;
    private int playerIndex;

    // Getters and Setters
    public int getDiceStatus() {
        return diceStatus;
    }

    public void setDiceStatus(int diceStatus) {
        this.diceStatus = diceStatus;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public void setPlayerIndex(int playerIndex) {
        this.playerIndex = playerIndex;
    }
}
