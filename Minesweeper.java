import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Minesweeper {
    
    private static final String DISPLAY_SPOT_UNKNOWN    = "? ";
    private static final String DISPLAY_SPOT_CLEARED    = "_ ";
    private static final String DISPLAY_SPOT_MINED      = "* ";
    private static final String DISPLAY_SPOT_FLAGGED    = "F ";
    private static final boolean isDebug = false;
    private static final long randomSeed = 0L;
    
    private static int boardLength = -1;
    private static int boardWidth = -1;
    private static int mines = -1;
    
    
    private static class BoardSpace {
        public boolean isMined = false;
        public boolean isFlagToggle = false;
        public String display = DISPLAY_SPOT_UNKNOWN;
        public List<BoardSpace> neighbors = new ArrayList<>();
    }
    
    private static StringBuilder displayBoard(BoardSpace[][] board) {
        StringBuilder display = new StringBuilder();
        int rowCount = boardLength;
        for (BoardSpace[] boardRow : board) {
            for (BoardSpace point : boardRow) {
                display.append(point.display);
            }
            display.append("  < " + rowCount--);
            display.append('\n');
        }
        display.append('\n');
        for (int i=0;i<boardWidth;i++) {
            display.append("^ ");
        }
        display.append('\n');
        for (int i=1;i<=boardWidth;i++) {
            if (i < 10) {
                display.append(i + " ");
            } else if (i >= 10 && i < 100) {
                display.append((int)(Math.floor(i/10.0) * 10)/10 + " ");
            }
        }
        if (boardWidth >= 10) {
            display.append('\n');
            for (int i=1;i<=boardWidth;i++) {
                if (i < 10) {
                    display.append("  ");
                } else if (i >= 10 && i < 100) {
                    display.append((int)(i - Math.floor(i/10.0) * 10) + " ");
                }
            }
        }
        display.append('\n');
        return display;
    }

    private static StringBuilder displayDebugBoard(BoardSpace[][] board) {
        StringBuilder display = new StringBuilder();
        for (BoardSpace[] boardRow : board) {
            for (BoardSpace point : boardRow) {
                if (point.isMined) {
                    display.append(DISPLAY_SPOT_MINED);
                } else {
                    display.append(DISPLAY_SPOT_CLEARED);
                }
            }
            display.append('\n');
        }
        return display;
    }
    
    private static BoardSpace[][] buildBoard(int rows, int columns) {
        BoardSpace[][] board = new BoardSpace[rows][columns];
        for (int i=0;i<rows;i++) {
            for (int j=0;j<columns;j++) {
                board[i][j] = new BoardSpace();
            }
        }
        
        // link neighbors
        for (int i=0;i<rows;i++) {
            for (int j=0;j<columns;j++) {
                
                // e.g. consider a middle BoardSpace
                if (i - 1 >= 0 && j - 1 >= 0) {
                    board[i][j].neighbors.add(board[i-1][j-1]);
                }
                if (i - 1 >= 0) {
                    board[i][j].neighbors.add(board[i-1][j]);
                }
                if (i - 1 >= 0 && j + 1 < columns) {
                    board[i][j].neighbors.add(board[i-1][j+1]);
                }
                if (j - 1 >= 0) {
                    board[i][j].neighbors.add(board[i][j-1]);
                }
                if (j + 1 < columns) {
                    board[i][j].neighbors.add(board[i][j+1]);
                }
                if (i + 1 < rows && j - 1 >= 0) {
                    board[i][j].neighbors.add(board[i+1][j-1]);
                }
                if (i + 1 < rows) {
                    board[i][j].neighbors.add(board[i+1][j]);
                }
                if (i + 1 < rows && j + 1 < columns) {
                    board[i][j].neighbors.add(board[i+1][j+1]);
                }
            }
        }
        return board;
    }

    private static boolean isBlowup(BoardSpace space) {
        
        boolean isBlowup = false;
        if (space.isMined && !space.isFlagToggle && space.display != DISPLAY_SPOT_FLAGGED) {
            space.display = DISPLAY_SPOT_MINED;
            isBlowup = true;
        }
        return isBlowup;
    }

    private static boolean isFlagOperation(BoardSpace space) {
        boolean answer = space.isFlagToggle;
        space.isFlagToggle = false;
        return answer;
    }

    // returns 'true' if spot is cleared
    private static boolean isClearAndUpdateSpace(BoardSpace space) {
        
        int countMines = 0;
        for (BoardSpace neighbor : space.neighbors) {
            if (neighbor.isMined) countMines++;
        }
        
        if (countMines == 0) {
            space.display = DISPLAY_SPOT_CLEARED;
        } else {
            space.display = countMines + " ";
        }
        
        return space.display == DISPLAY_SPOT_CLEARED;
    }
    
    private static BoardSpace nextMove(Scanner scanner, BoardSpace[][] board, boolean isTip) {
        StringBuilder display = displayBoard(board);
        boolean isFlag = false;
        
        StringBuilder debugDisplay = null;
        if (isDebug) debugDisplay = displayDebugBoard(board);
        
        int row = -1;
        int column = -1;
        while (row < 0 || column < 0) {
            System.out.println(display);
            if (isDebug) {
                System.out.println("");
                System.out.println("Debug:");
                System.out.println(debugDisplay);
            }

            if (isTip) {
                System.out.println("Tip: Input of \"1,2\" means first column and second row over - don't use \"\" characters, please.");
                System.out.println("Tip: To flag a mine use 'F' - e.g. F1,2");
            }
            
            String input = scanner.nextLine().trim();
            try {
                String[] commandArr = input.split(",");
                isFlag = commandArr[0].startsWith("F");

                if (isFlag) {
                    column = Integer.parseInt(commandArr[0].substring(1)) - 1;      // normalize to 0 based
                } else column = Integer.parseInt(commandArr[0]) - 1;      // normalize to 0 based
                row = Integer.parseInt(commandArr[1]) - 1;   // normalize to 0 based

                if (row > boardLength || column > boardWidth || row < 0 || column < 0) {
                    row = -1;
                    column = -1;
                    System.out.println("That's not a valid row and column! Try again.");
                }
            } catch (Exception e) {
                System.out.println("I didn't understand... try again.");
            }   
        }

        BoardSpace selected = board[boardLength - row - 1][column];
        selected.isFlagToggle = isFlag;

        // Flag
        if (isFlag && (selected.display == DISPLAY_SPOT_UNKNOWN || selected.display == DISPLAY_SPOT_FLAGGED)) {
            if (selected.display == DISPLAY_SPOT_FLAGGED) {
                selected.display = DISPLAY_SPOT_UNKNOWN;
            } else {
                selected.display = DISPLAY_SPOT_FLAGGED;
            }
        }
        
        return selected;
    }

    private static boolean areNeighborsCleared(BoardSpace space) {
        for (BoardSpace n : space.neighbors) {
            if (n.display == DISPLAY_SPOT_CLEARED) return true;
        }
        return false;
    }
    
    // First time we add mines after the user has selected the first "always cleared" spot
    private static void addMines(BoardSpace[][] board) {

        // Create mines
        // After the first input create the mines... (anywhere but round the first input) - distributed fairly equally...
        float chanceOfMine = (float)mines / ((float)boardLength * (float)boardWidth) * 100;
        System.out.println("NOTE: Every spot has a " + Math.round(chanceOfMine) + "% chance of a mine! Watch out!");
        
        int mineCount = mines;
        Random random = isDebug ? new Random(randomSeed) : new Random();
        int safeFail = 0;
        done: while (mineCount > 0) {
            for (int i=0;i<board.length;i++) {
                for (int j=0;j<board[i].length;j++) {
                    if (mineCount == 0) {
                        break done;

                    // Which spots are allowed to be mines?
                    } else if (
                        !board[i][j].isMined && // No mine already
                        board[i][j].display == DISPLAY_SPOT_UNKNOWN && // Currently unknown (ie. not the user's first selection)
                        !areNeighborsCleared(board[i][j]) // Neighbors are not cleared (ie. always give the user a bigger initial start)
                    ) {
                        board[i][j].isMined = random.nextDouble()*100 < chanceOfMine;
                        if (board[i][j].isMined) {
                            mineCount--;
                        }
                    }
                }
            }
            
            // Just in case...
            safeFail++;
            if (safeFail > 9) {
                System.out.println("NOTE: hmmm... all mines weren't added to the board, but we are continuing anyway!\n");
                break done;
            }
        }
    }
    
    private static boolean isAliveAndUpdateBoard(BoardSpace space) {

        if (isFlagOperation(space)) {
            return true; // Flag operations do not reveal/clear other spaces
        }
        
        if (isBlowup(space)) { 
            return false; 
        }
        
        // Clear safe discovered spaces and display
        List<BoardSpace> queue = new ArrayList<>();
        Set<BoardSpace> processed = new HashSet<>();

        if (isClearAndUpdateSpace(space)) {
            processed.add(space);
            queue.add(space);
        }
        
        while (!queue.isEmpty()) {
            BoardSpace next = queue.remove(0);
            for (BoardSpace neighbor : next.neighbors) {
                if (isClearAndUpdateSpace(neighbor)) {
                    if (!processed.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                    processed.add(neighbor);
                }
            }
        }
        
        return space.display != DISPLAY_SPOT_MINED;
    }

    private static boolean isWin(BoardSpace[][] board) {
        // Case 1
            // Board has no "?" and "F" are only over mines
        // Case 2
            // Board had "?" left where there are mines only and "F" are only over mines
        int countVictory = 0;
        int countUnknownMines = 0;
        int countUnknownSpots = 0;
        for (BoardSpace[] row : board) {
            for (BoardSpace space : row) {
                if (space.display == DISPLAY_SPOT_UNKNOWN) {
                    countUnknownSpots++;
                }

                if (space.isMined && space.display == DISPLAY_SPOT_FLAGGED) {
                    countVictory++;
                } else if (space.isMined && space.display == DISPLAY_SPOT_UNKNOWN) {
                    countUnknownMines++;
                }
            }
        }

        return countUnknownSpots == countUnknownMines &&
            countVictory + countUnknownMines == mines;
    }
    
    private static void playGame() {
        
        
        Scanner scanner = new Scanner(System. in);
        boolean keepPlaying = true;
        
        while (keepPlaying) {
        
            // Setup
            String gameType = null;
            String easy = "e";
            String medium = "m";
            String hard = "h";
            
            // Game size?
            while (gameType == null) {
                System.out.println("What size board do you want to play?");
                System.out.println("easy (e), medium (m), or hard (h)?");
                String input = scanner.nextLine().trim();
                if (easy.equals(input) || medium.equals(input) || hard.equals(input)) {
                    gameType = input;
                } else {
                    System.out.println("I didn't understand... try again.");
                }
            }
            
            boardLength = -1;
            boardWidth = -1;
            if (gameType.equals(easy)) {
                boardLength = 9;
                boardWidth = 9;
                mines = 10;
            } else if (gameType.equals(medium)) {
                boardLength = 16;
                boardWidth = 16;
                mines = 40;
            } else {
                boardLength = 16;
                boardWidth = 30;
                mines = 99;
            }
            
            // init board
            BoardSpace[][] board = buildBoard(boardLength, boardWidth);
            
            // First move
            BoardSpace first = nextMove(scanner, board, true);
            first.display = DISPLAY_SPOT_CLEARED;
            addMines(board);
            isAliveAndUpdateBoard(first);
            
            // Now the game can be lost if user hits mine
            boolean isGameActive = true;
            boolean isWin = false;
            while (isGameActive) {
                isGameActive = isAliveAndUpdateBoard(
                        nextMove(scanner, board, false) // User's move
                );
                isWin = isWin(board);
                if (isWin) {
                    isGameActive = false;
                }
            }
            
            System.out.println(displayBoard(board));
            if (isWin) {
                System.out.println("YOU WON!!!");
            } else {
                System.out.println("You blew up... Sorry.");
            }
            
            
            String isDone = null;
            String yes = "y";
            String no = "n";
            
            // Game size?
            while (isDone == null) {
                System.out.println("Do you want to play again?");
                System.out.println("Yes (y) or No (n)?");
                String input = scanner.nextLine().trim();
                if (yes.equals(input) || no.equals(input)) {
                    isDone = input;
                } else {
                    System.out.println("I didn't understand... try again.");
                }
            }
            
            
            if (no.equals(isDone)) {
                keepPlaying = false;
            }
        }

        //TODO: Now the user can clear with use of flags       
        
        scanner.close();
    }
    
    public static void main(String[] args) {
        Minesweeper.playGame();
    }
}