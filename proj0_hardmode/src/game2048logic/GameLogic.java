package game2048logic;

import game2048rendering.Side;
import static game2048logic.MatrixUtils.rotateLeft;
import static game2048logic.MatrixUtils.rotateRight;

/**
 * @author  Josh Hug
 */
public class GameLogic {
    /** Moves the given tile up as far as possible, subject to the minR constraint.
     *
     * @param board the current state of the board
     * @param r     the row number of the tile to move up
     * @param c -   the column number of the tile to move up
     * @param minR  the minimum row number that the tile can land in, e.g.
     *              if minR is 2, the moving tile should move no higher than row 2.
     * @return      if there is a merge, returns the 1 + the row number where the merge occurred.
     *              if no merge occurs, then return 0.
     */
    public static int moveTileUpAsFarAsPossible(int[][] board, int r, int c, int minR) {
        int value = board[r][c];
        if (value == 0) {
            return 0;
        }

        // Find the farthest position this tile can move to
        int targetR = r - 1;
        while (targetR >= minR && board[targetR][c] == 0) {
            targetR--;
        }

        // Check if we can merge with the tile at targetR
        if (targetR >= minR && board[targetR][c] == value) {
            // Merge
            board[targetR][c] *= 2;
            board[r][c] = 0;
            return targetR + 1;
        }

        // Move the tile to the farthest empty position (just below the blocking tile or minR)
        int newR = targetR + 1;
        if (newR != r) {
            board[newR][c] = value;
            board[r][c] = 0;
        }

        return 0;
    }

    /**
     * Modifies the board to simulate the process of tilting column c
     * upwards.
     *
     * @param board     the current state of the board
     * @param c         the column to tilt up.
     */
    public static void tiltColumn(int[][] board, int c) {
        int minR = 0;
        // Process tiles from bottom to top
        for (int r = 1; r < board.length; r++) {
            if (board[r][c] != 0) {
                int merge = moveTileUpAsFarAsPossible(board, r, c, minR);
                if (merge != 0) {
                    minR = merge;
                }
            }
        }
    }

    /**
     * Modifies the board to simulate tilting all columns upwards.
     *
     * @param board     the current state of the board.
     */
    public static void tiltUp(int[][] board) {
        for (int c = 0; c < board[0].length; c++) {
            tiltColumn(board, c);
        }
    }

    /**
     * Modifies the board to simulate tilting the entire board to
     * the given side.
     *
     * @param board the current state of the board
     * @param side  the direction to tilt
     */
    public static void tilt(int[][] board, Side side) {
        if (side == Side.NORTH) {
            tiltUp(board);
        } else if (side == Side.EAST) {
            rotateLeft(board);
            tiltUp(board);
            rotateRight(board);
        } else if (side == Side.WEST) {
            rotateRight(board);
            tiltUp(board);
            rotateLeft(board);
        } else if (side == Side.SOUTH) {
            rotateRight(board);
            rotateRight(board);
            tiltUp(board);
            rotateRight(board);
            rotateRight(board);
        }
    }
}
