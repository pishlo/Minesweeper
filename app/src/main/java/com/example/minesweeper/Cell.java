package com.example.minesweeper;

public class Cell {
    public boolean hasMine = false;
    public boolean isRevealed = false;
    public boolean isFlagged = false;
    public int adjacentMines = 0;
}
