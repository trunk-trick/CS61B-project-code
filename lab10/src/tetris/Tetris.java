package tetris;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TETile;
import tileengine.TERenderer;
import tileengine.Tileset;

import java.awt.event.KeyEvent;
import java.util.*;

/**
 *  Provides the logic for Tetris.
 *
 *  @author Erik Nelson, Omar Yu, Noah Adhikari, Jasmine Lin
 */

public class Tetris {

    private static int WIDTH = 10;
    private static int HEIGHT = 20;

    // Tetrominoes spawn above the area we display, so we'll have our Tetris board have a
    // greater height than what is displayed.
    private static int GAME_HEIGHT = 25;

    // Contains the tiles for the board.
    private TETile[][] board;

    // Helps handle movement of pieces.
    private Movement movement;

    // Checks for if the game is over.
    private boolean isGameOver;

    // The current Tetromino that can be controlled by the player.
    private Tetromino currentTetromino;

    // The current game's score.
    private int score;

    // Time between auto-drops (in milliseconds)
    private static final int DROP_INTERVAL = 1000;

    /**
     * Checks for if the game is over based on the isGameOver parameter.
     * @return boolean representing whether the game is over or not
     */
    private boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Renders the game board and score to the screen.
     */
    private void renderBoard() {
        ter.drawTiles(board);
        renderScore();
        StdDraw.show();

        if (auxFilled) {
            auxToBoard();
        } else {
            fillBoard(Tileset.NOTHING);
        }
    }

    /**
     * Creates a new Tetromino and updates the instance variable
     * accordingly. Flags the game to end if the top of the board
     * is filled and the new piece cannot be spawned.
     */
    private void spawnPiece() {
        // The game ends if this tile is filled
        if (board[4][19] != Tileset.NOTHING) {
            isGameOver = true;
        }

        // Otherwise, spawn a new piece and set its position to the spawn point
        currentTetromino = Tetromino.values()[bagRandom.getValue()];
        currentTetromino.reset();
    }

    /**
     * Updates the board based on the user input. Makes the appropriate moves
     * depending on the user's input.
     */
    private void updateBoard() {
        // Grabs the current piece.
        Tetromino t = currentTetromino;

        // Handle keyboard input
        if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT)) {
            movement.tryMove(-1, 0);
        } else if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT)) {
            movement.tryMove(1, 0);
        } else if (StdDraw.isKeyPressed(KeyEvent.VK_DOWN)) {
            movement.tryMove(0, -1);
        } else if (StdDraw.isKeyPressed(KeyEvent.VK_UP)) {
            movement.rotateRight();
        } else if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
            // Hard drop: keep dropping until it can't move
            while (currentTetromino != null) {
                movement.dropDown();
            }
            return;
        }

        // Auto-drop at interval
        if (actionDeltaTime() > DROP_INTERVAL) {
            movement.dropDown();
            resetActionTimer();
        }

        // If the piece was placed (currentTetromino is null), check for line clears
        if (currentTetromino == null) {
            clearLines(board);
            spawnPiece();
        }

        Tetromino.draw(t, board, t.pos.x, t.pos.y);
    }

    /**
     * Increments the score based on the number of lines that are cleared.
     *
     * @param linesCleared
     */
    private void incrementScore(int linesCleared) {
        // Standard Tetris scoring: 100, 300, 500, 800 for 1, 2, 3, 4 lines
        if (linesCleared == 1) {
            score += 100;
        } else if (linesCleared == 2) {
            score += 300;
        } else if (linesCleared == 3) {
            score += 500;
        } else if (linesCleared == 4) {
            score += 800;
        }
    }

    /**
     * Clears lines/rows on the provided tiles/board that are horizontally filled.
     * Repeats this process for cascading effects and updates score accordingly.
     * @param tiles
     */
    public void clearLines(TETile[][] tiles) {
        // Keeps track of the current number lines cleared
        int linesCleared = 0;

        // Check each row from bottom to top
        for (int y = 0; y < GAME_HEIGHT; y++) {
            boolean rowFull = true;
            for (int x = 0; x < WIDTH; x++) {
                if (tiles[x][y] == Tileset.NOTHING) {
                    rowFull = false;
                    break;
                }
            }

            if (rowFull) {
                linesCleared++;
                // Shift all rows above down by one
                for (int shiftY = y; shiftY < GAME_HEIGHT - 1; shiftY++) {
                    for (int x = 0; x < WIDTH; x++) {
                        tiles[x][shiftY] = tiles[x][shiftY + 1];
                    }
                }
                // Clear the top row
                for (int x = 0; x < WIDTH; x++) {
                    tiles[x][GAME_HEIGHT - 1] = Tileset.NOTHING;
                }
                // Check this row again (since rows above shifted down)
                y--;
            }
        }

        // Increment the score based on the number of lines cleared.
        incrementScore(linesCleared);

        fillAux();
    }

    /**
     * Where the game logic takes place. The game should continue as long as the game isn't
     * over.
     */
    public void runGame() {
        resetActionTimer();

        // Spawn the first piece
        spawnPiece();

        // Game loop: keep running until the game is over
        while (!isGameOver()) {
            renderBoard();
            updateBoard();
        }

        // Game over message
        StdDraw.clear();
        StdDraw.text(0.5, 0.5, "Game Over! Score: " + score);
        StdDraw.show();
    }

    /**
     * Renders the score using the StdDraw library.
     */
    private void renderScore() {
        // Draw score text on the upper-left area of the screen
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.textLeft(0.5, HEIGHT + 1.5, "Score: " + score);
    }

    /**
     * Use this method to run Tetris.
     * @param args
     */
    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : (new Random()).nextLong();
        Tetris tetris = new Tetris(seed);
        tetris.runGame();
    }

    /**
     * Everything below here you don't need to touch.
     */

    // This is our tile rendering engine.
    private final TERenderer ter = new TERenderer();

    // Used for randomizing which pieces are spawned.
    private Random random;
    private BagRandomizer bagRandom;

    private long prevActionTimestamp;
    private long prevFrameTimestamp;

    // The auxiliary board. At each time step, as the piece moves down, the board
    // is cleared and redrawn, so we keep an auxiliary board to track what has been
    // placed so far to help render the current game board as it updates.
    private TETile[][] auxiliary;
    private boolean auxFilled;

    public Tetris() {
        board = new TETile[WIDTH][GAME_HEIGHT];
        auxiliary = new TETile[WIDTH][GAME_HEIGHT];
        random = new Random(new Random().nextLong());
        bagRandom = new BagRandomizer(random, Tetromino.values().length);
        auxFilled = false;
        movement = new Movement(WIDTH, GAME_HEIGHT, this);
        fillBoard(Tileset.NOTHING);
        fillAux();
    }

    public Tetris(long seed) {
        board = new TETile[WIDTH][GAME_HEIGHT];
        auxiliary = new TETile[WIDTH][GAME_HEIGHT];
        random = new Random(seed);
        bagRandom = new BagRandomizer(random, Tetromino.values().length);
        auxFilled = false;
        movement = new Movement(WIDTH, GAME_HEIGHT, this);

        ter.initialize(WIDTH, HEIGHT);
        fillBoard(Tileset.NOTHING);
        fillAux();
    }

    // Setter and getter methods.

    /**
     * Returns the current game board.
     * @return
     */
    public TETile[][] getBoard() {
        return board;
    }

    /**
     * Returns the score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the current auxiliary board.
     * @return
     */
    public TETile[][] getAuxiliary() {
        return auxiliary;
    }


    /**
     * Returns the current Tetromino/piece.
     * @return
     */
    public Tetromino getCurrentTetromino() {
        return currentTetromino;
    }

    /**
     * Sets the current Tetromino to null.
     * @return
     */
    public void setCurrentTetromino() {
        currentTetromino = null;
    }

    /**
     * Sets the boolean auxFilled to true;
     */
    public void setAuxTrue() {
        auxFilled = true;
    }

    /**
     * Fills the entire board with the specific tile that is passed in.
     * @param tile
     */
    private void fillBoard(TETile tile) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j] = tile;
            }
        }
    }

    /**
     * Copies the contents of the src array into the dest array using
     * System.arraycopy.
     * @param src
     * @param dest
     */
    private static void copyArray(TETile[][] src, TETile[][] dest) {
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dest[i], 0, src[0].length);
        }
    }

    /**
     * Copies over the tiles from the game board to the auxiliary board.
     */
    public void fillAux() {
        copyArray(board, auxiliary);
    }

    /**
     * Copies over the tiles from the auxiliary board to the game board.
     */
    private void auxToBoard() {
        copyArray(auxiliary, board);
    }

    /**
     * Calculates the delta time with the previous action.
     * @return the amount of time between the previous Tetromino movement with the present
     */
    private long actionDeltaTime() {
        return System.currentTimeMillis() - prevActionTimestamp;
    }

    /**
     * Resets the action timestamp to the current time in milliseconds.
     */
    private void resetActionTimer() {
        prevActionTimestamp = System.currentTimeMillis();
    }

}
