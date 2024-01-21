package nl.picobello.basecamp.battleship;

import nl.picobello.basecamp.shared.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class BattleshipBoard {
    private static final char WATER = '-';
    private static final char HIT = 'X';
    private static final char EMPTY = 'O';
    private static final char SHIP = 'S';
    private Server server;
    private String playerName;

    private char[] board;
    private char[] oppBoard;
    private int[] remainingShips = {1, 1, 1, 1}; // One ship of each size: 2, 3, 4, 6 (For ship placement)
    private int[] opponentShips = {1, 1, 1, 1}; //For tracking opponent ships by AI
    private int[] playerShips = {1, 1, 1, 1}; //For tracking player ships
    private boolean shipsPlaced = false;
    private Map<Integer, int[]> shipPositions = new HashMap<>();
    private int boardWidth = 8;
    private int AIState = 0; //0 is random, 1 is targeted
    private int lastHit = 0;

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

    public boolean shipsPlaced() {
        return shipsPlaced;
    }

    public String getPlayerName() {
        return playerName;
    }

    public char[] getBoard() {
        return this.board;
    }

    public void editCell(int index, char symbol) {
        this.board[index] = symbol;
    }

    public Map<Integer, int[]> getShipPositions() {
        return this.shipPositions;
    }

    public String getOppShips() {
        StringBuilder string = new StringBuilder();
        for(int i=0; i<opponentShips.length; i++) {
            string.append(opponentShips[i]);
        }
        return string.toString();
    }

    //reset game board
    public void resetBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i] = WATER;
        }
        for (int i = 0; i < oppBoard.length; i++) {
            oppBoard[i] = WATER;
        }
        //Reset opponent ship counter
        for(int i = 0; i < opponentShips.length; i++){
            opponentShips[i] = 1;
        }
        shipsPlaced = false;
    }

    public boolean gameOver() {
        if(playerShips[0] == 0 && playerShips[1] == 0 && playerShips[2] == 0 && playerShips[3] == 0) {
            return true;
        }
        return false;
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
            //System.out.println("Debug: i=" + i + ", shipSize=" + shipSize);
        
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
            
            shipPositions.put(shipSize, new int[]{start, end});
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
        String request = String.format("place %d %d", start, end);
        // Place ship based on start and end indices
        int direction = (start < end) ? 1 : -1;
        int step = (start / boardWidth == end / boardWidth) ? direction : direction * boardWidth;
        
        for (int i = start; (direction > 0) ? i <= end : i >= end; i += step) {
            board[i] = SHIP;
        }
    }

    public void makeShot(int index) {
        //Send request to server
        String request = String.format("move %d", index);
        //server.SendCommand(request);
    }

    //update boards based on received server response
    public void updateBoards(int index, String player, String result, int length) {
        int shipSize = 0;
        if(length == 6) {
            shipSize = 3;
        } else if(length < 6) {
            shipSize = length - 2;
        }

        if (player.equals(playerName)) {
            if ("BOEM".equals(result)) {
                oppBoard[index] = HIT;
                AIState = 1;
                lastHit = index;
            } else if ("GEZONKEN".equals(result)) {
                oppBoard[index] = HIT;
                opponentShips[shipSize] = 0;
                AIState = 0;
                lastHit = index;
            } else {
                oppBoard[index] = EMPTY;
            }
        } else if (!"unknown".equals(player)) {
            if ("BOEM".equals(result)) {
                board[index] = HIT;
            } else if ("GEZONKEN".equals(result)) {
                board[index] = HIT;
                playerShips[shipSize] = 0;
            } else {
                board[index] = EMPTY;
            }
        }
    }

    //Get symbol at given board index
    public char getSymbol(int index){
        return this.board[index];
    }

    //Determine ship placement for AI, for now randomized
    public int[] aiPlaceShips(){
        Random random = new Random();
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
            //System.out.println("Debug: i=" + i + ", shipSize=" + shipSize);
        
            boolean invalidPlacement = true;
        
            while (invalidPlacement && remainingShips[i - 1] > 0) {
                //System.out.println("Now placing 1x" + shipSize + " ship\n");
                start = random.nextInt(board.length);

                boolean horizontal = random.nextBoolean();
                if (horizontal) {
                    end = start + shipSize - 1;
                } else {
                    end = start + (shipSize - 1) * boardWidth;
                }
        
                if (checkPlacement(start, end, shipSize)) {
                    shipPositions.put(shipSize, new int[]{start, end});
                    placeSingleShip(start, end);
                    invalidPlacement = false;
                    remainingShips[i - 1] = 0;  // Set count for this ship size to 0
                    server.sendCommand("place " + start + " " + end);
                    return new int[] {start, end};
                }
            }
        }
        shipsPlaced = true;
        return new int[]{0, 0};
    }

    //Determine best move for AI. Basic AI for now
    public int aiMove(){
        Random random = new Random();
        int move = 99;
        int badMoves = 0;

        if(AIState == 0) {
            while(!validMove(move)) {
                move = random.nextInt(64);
            }
        } else {
            int potentialCells[] = {lastHit + 1, lastHit - 1, lastHit - 8, lastHit + 8};
            for(int i = 0; i < potentialCells.length; i++) {
                if(validMove(potentialCells[i])) {
                    move = potentialCells[i];  
                }
                else {
                    badMoves += 1;
                }
            }
            if(badMoves == 4) {
                while(!validMove(move)) {
                    move = random.nextInt(64);
                }
            } 
        }
        return move;
    }

    //Heatmap generator WIP
    private Map<Integer, Integer> evaluateBoard() {
        Map<Integer, Integer> cellScores = new HashMap<>();

        if(AIState == 0) {
            for (int i = 0; i < 64; i++) {
                if(oppBoard[i] == 'X' || oppBoard[i] == 'O') { //If hit or empty, never pick this cell
                    cellScores.put(i, 0); 
                } else { //Firing randomly
                    cellScores.put(i, 50);
                }
            }
        } else {
            for (int i = 0; i < 64; i++) {
                if(oppBoard[i] == 'X' || oppBoard[i] == 'O') { //If hit or empty, never pick this cell
                    cellScores.put(i, 0);
                } else {
                    //cellScores.put(i, targetModeProbability(i));
                }
            }
        }
        return cellScores;
    }

    //WIP
    //private int targetModeProbability(int move) {

    //}

    //Determine validity of a move
    private boolean validMove(int move) {
        if(move > board.length - 1 || move < 0) {
            return false;
        }
        if(oppBoard[move] == 'X' || oppBoard[move] == 'O') {
            return false;
        }
        return true;
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
