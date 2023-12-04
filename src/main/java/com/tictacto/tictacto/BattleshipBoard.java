package com.tictacto.tictacto;

import java.util.Scanner;

public class BattleshipBoard {
    private static final char WATER = '-';
    private static final char HIT = 'X';
    private static final char EMPTY = 'O';
    private static final char SHIP = 'S';
    private Server server;
    private String playerName;

    private char[] board;
    private char[] oppBoard;
    private int[] remainingShips = {1, 1, 1, 1}; // One ship of each size: 2, 3, 4, 6
    private int boardWidth = 8;

    public BattleshipBoard(Server server, String playerName) {
        this.playerName = playerName;
        this.server = server;
        createBoard();
    }

    public void createBoard(){
        board = new char[boardWidth * boardWidth];
        oppBoard = new char[boardWidth * boardWidth];
        
        //Fill board with water
        for (int i = 0; i < board.length; i++) {
            board[i] = WATER;
        }
        for (int i = 0; i < oppBoard.length; i++) {
            oppBoard[i] = WATER;
        }
    }

    //reset game board
    public void resetBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i] = WATER;
        }
        for (int i = 0; i < oppBoard.length; i++) {
            oppBoard[i] = WATER;
        }
    }

    public void placeShips() {
        Scanner scanner = new Scanner(System.in);
        int start = 0;
        int end = 0;
    
        for (int i = 1; i <= remainingShips.length; i++) {
            int shipSize;
            if(i == 4){
                shipSize = i + 2;
            }
            else {
                shipSize = i + 1;
            }
            System.out.println("Debug: i=" + i + ", shipSize=" + shipSize);
        
            boolean invalidPlacement = true;
        
            while (invalidPlacement && remainingShips[i - 1] > 0) {
                System.out.println("Now placing 1x" + shipSize + " ship\n");
                System.out.println("Enter the start index where the ship should be placed: ");
                start = scanner.nextInt();
                System.out.println("\nEnter the end index where the ship should be placed: ");
                end = scanner.nextInt();
        
                if (checkPlacement(start, end, shipSize)) {
                    invalidPlacement = false;
                    remainingShips[i - 1] = 0;  // Set count for this ship size to 0
                } else {
                    System.out.println("Invalid ship placement, please try again");
                }
            }
        
            placeSingleShip(start, end);
            System.out.println(this.toString());
        }
    }
    

    private boolean checkPlacement(int start, int end, int shipSize) {
        // Check if ship placement is within the boundaries
        if (start < 0 || start >= board.length || end < 0 || end >= board.length) {
            return false;
        }
    
        // Check if the ship placement is horizontal or vertical
        boolean isHorizontal = start / boardWidth == end / boardWidth;
        boolean isVertical = start % boardWidth == end % boardWidth;
    
        if (!isHorizontal && !isVertical) {
            return false;  // Not a valid placement if neither horizontal nor vertical
        }
    
        // Check for adjacent ships horizontally
        if (isHorizontal) {
            for (int i = start; i <= end; i++) {
                int row = i / boardWidth;
                int col = i % boardWidth;
                if (hasAdjacentShip(row, col)) {
                    return false; // Adjacent ship found
                }
            }
        }
    
        // Check for adjacent ships vertically
        if (isVertical) {
            for (int i = start; i <= end; i += boardWidth) {
                int row = i / boardWidth;
                int col = i % boardWidth;
                if (hasAdjacentShip(row, col)) {
                    return false; // Adjacent ship found
                }
            }
        }
    
        // Check if the difference between start and end indices matches the expected ship size
        int indexDifference = Math.abs(end - start);
    
        // Check if the ship placement is valid based on direction
        if (isHorizontal && indexDifference == shipSize - 1) {
            return true;
        } else return isVertical && indexDifference == (shipSize - 1) * boardWidth;
    }
    
    private boolean hasAdjacentShip(int row, int col) {
        // Check for adjacent ships in all directions
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < boardWidth && newCol >= 0 && newCol < boardWidth) {
                    if (board[newRow * boardWidth + newCol] == SHIP) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void placeSingleShip(int start, int end) {
        String request = String.format("place [%d, %d]", start, end);
        // Place ship based on start and end indices
        int direction = (start < end) ? 1 : -1;
        int step = (start / boardWidth == end / boardWidth) ? direction : direction * boardWidth;
        
        for (int i = start; (direction > 0) ? i <= end : i >= end; i += step) {
            board[i] = SHIP;
        }
    }

    //Only for inccoming hits from server(opponent)
    public void incomingShot(int index) {
        if(board[index] == 'S') {
            System.out.println("A ship has been hit");
            board[index] = HIT;
        }
        else {
            System.out.println("The shot missed");
        }
        System.out.println(this.toString());
    }

    public void makeShot(int index) {
        //Send request to server
        String request = String.format("move %d", index);
        server.SendCommand(request);
    }

    //update opponent board based on received server response
    public void updateBoards(int index, String player, String result) {
        if (player.equals(playerName)) {
            if ("PLONS".equals(result)) {
                oppBoard[index] = HIT;
            } else {
                oppBoard[index] = EMPTY;
            }
        } else if (!"unknown".equals(player)) {
            if ("PLONS".equals(result)) {
                board[index] = HIT;
            } else {
                board[index] = EMPTY;
            }
        }
        System.out.println(this.oppToString());
    }

    public String oppToString(){
        StringBuilder oppBoardString = new StringBuilder();

        for (int i = 0; i < oppBoard.length; i++) {
            oppBoardString.append(oppBoard[i]);
            if ((i + 1) % boardWidth == 0) {
                oppBoardString.append("\n");
            }
        }
        return oppBoardString.toString();
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            boardString.append(board[i]);
            if ((i + 1) % boardWidth == 0) {
                boardString.append("\n");
            }
        }
        return boardString.toString();
    }
}
