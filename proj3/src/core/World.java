package core;

import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedurally generates a tile-based world with rooms, hallways, walls,
 * and outdoor terrain. The algorithm creates a roguelike-style dungeon
 * surrounded by grass, trees, water, and mountains.
 *
 * Generation steps:
 *   1. Fill the world with NOTHING.
 *   2. Place non-overlapping rectangular rooms (FLOOR).
 *   3. Connect rooms with L-shaped hallways.
 *   4. Surround all FLOOR with WALL tiles.
 *   5. Fill remaining NOTHING with outdoor terrain (grass, trees, water, etc.).
 *   6. Add indoor decorations (trees, flowers in larger rooms).
 */
public class World {

    /* ---------- Constants ---------- */

    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;

    private static final int MIN_ROOM_SIZE = 4;
    private static final int MAX_ROOM_SIZE = 10;
    private static final int MAX_ROOM_ATTEMPTS = 25;
    private static final int ROOM_BUFFER = 2;      // empty tiles between rooms (for walls)
    private static final int MIN_HALLWAY_WIDTH = 1;
    private static final int MAX_HALLWAY_WIDTH = 2; // inclusive

    /* ---------- Custom tiles ---------- */

    /** Dirt path tile for outdoor roads. */
    public static final TETile DIRT = new TETile(
            '▓', new Color(160, 140, 100), Color.black, "dirt path", 20);

    /** Deep water tile for ponds. */
    public static final TETile DEEP_WATER = new TETile(
            '≈', new Color(30, 80, 180), new Color(20, 30, 80), "deep water", 21);

    /** Snow-capped peak for mountain ranges. */
    public static final TETile PEAK = new TETile(
            '^', Color.white, Color.black, "snowy peak", 22);

    /* ---------- Fields ---------- */

    private final TETile[][] tiles;
    private final Random random;
    private final long seed;

    /* ---------- Helper: room rectangle ---------- */

    private static class Rect {
        final int x, y;            // bottom-left corner
        final int width, height;

        Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        int centerX() {
            return x + width / 2;
        }

        int centerY() {
            return y + height / 2;
        }

        /** Returns true if the interior point (px, py) is inside this room. */
        boolean contains(int px, int py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    /* ---------- Constructors ---------- */

    /**
     * Creates a world generated from the given seed.
     * Identical seeds produce identical worlds.
     */
    public World(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.tiles = new TETile[WIDTH][HEIGHT];
        generate();
    }

    /** Creates a world with a randomly chosen seed. */
    public World() {
        this(new Random().nextLong());
    }

    /* ---------- Public accessors ---------- */

    /** Returns the 2D tile array representing the world. */
    public TETile[][] getTiles() {
        return tiles;
    }

    /** Returns the world width in tiles. */
    public int getWidth() {
        return WIDTH;
    }

    /** Returns the world height in tiles. */
    public int getHeight() {
        return HEIGHT;
    }

    /** Returns the seed that generated this world. */
    public long getSeed() {
        return seed;
    }

    /* ================================================================
     *  GENERATION PIPELINE
     * ================================================================ */

    private void generate() {
        fillNothing();
        List<Rect> rooms = placeRooms();
        connectRooms(rooms);
        placeWalls();
        decorateOutdoors(rooms);
        decorateIndoors(rooms);
    }

    /** Fills every tile with NOTHING. */
    private void fillNothing() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /* ---------- Step 2: Room placement ---------- */

    /**
     * Tries to place up to MAX_ROOM_ATTEMPTS rooms.  Each candidate is
     * a randomly sized, randomly positioned rectangle.  It is accepted
     * only if its buffered bounds do not overlap any already-placed room.
     *
     * @return the list of successfully placed rooms
     */
    private List<Rect> placeRooms() {
        List<Rect> rooms = new ArrayList<>();
        for (int attempt = 0; attempt < MAX_ROOM_ATTEMPTS; attempt++) {
            int w = RandomUtils.uniform(random, MIN_ROOM_SIZE, MAX_ROOM_SIZE + 1);
            int h = RandomUtils.uniform(random, MIN_ROOM_SIZE, MAX_ROOM_SIZE + 1);
            int x = RandomUtils.uniform(random, 2, WIDTH - w - 2);
            int y = RandomUtils.uniform(random, 2, HEIGHT - h - 2);

            Rect candidate = new Rect(x, y, w, h);
            if (!overlaps(candidate, rooms)) {
                carveFloor(candidate);
                rooms.add(candidate);
            }
        }
        return rooms;
    }

    /** Returns true if candidate's buffered rectangle overlaps any existing room. */
    private boolean overlaps(Rect candidate, List<Rect> rooms) {
        int bx = candidate.x - ROOM_BUFFER;
        int by = candidate.y - ROOM_BUFFER;
        int bw = candidate.width + 2 * ROOM_BUFFER;
        int bh = candidate.height + 2 * ROOM_BUFFER;

        for (Rect r : rooms) {
            if (bx < r.x + r.width
                    && bx + bw > r.x
                    && by < r.y + r.height
                    && by + bh > r.y) {
                return true;
            }
        }
        return false;
    }

    /** Fills the given rectangle with FLOOR tiles. */
    private void carveFloor(Rect r) {
        for (int x = r.x; x < r.x + r.width; x++) {
            for (int y = r.y; y < r.y + r.height; y++) {
                tiles[x][y] = Tileset.FLOOR;
            }
        }
    }

    /* ---------- Step 3: Hallway connections ---------- */

    /**
     * Sorts rooms by horizontal position, then connects each consecutive
     * pair with an L-shaped hallway carved out of FLOOR.
     */
    private void connectRooms(List<Rect> rooms) {
        if (rooms.size() < 2) {
            return;
        }
        // Sort by centre x to produce a left-to-right sequence.
        rooms.sort((a, b) -> Integer.compare(a.centerX(), b.centerX()));

        for (int i = 0; i < rooms.size() - 1; i++) {
            Rect a = rooms.get(i);
            Rect b = rooms.get(i + 1);
            carveHallway(a.centerX(), a.centerY(), b.centerX(), b.centerY());
        }
    }

    /**
     * Carves an L-shaped hallway between (x1, y1) and (x2, y2).
     * Randomly chooses horizontal-first or vertical-first.
     */
    private void carveHallway(int x1, int y1, int x2, int y2) {
        if (RandomUtils.bernoulli(random)) {
            // Horizontal segment first, then vertical.
            carveHorizontalSegment(x1, x2, y1);
            carveVerticalSegment(y1, y2, x2);
        } else {
            // Vertical segment first, then horizontal.
            carveVerticalSegment(y1, y2, x1);
            carveHorizontalSegment(x1, x2, y2);
        }
    }

    /** Carves a horizontal corridor at row y between columns x1 and x2. */
    private void carveHorizontalSegment(int x1, int x2, int y) {
        int startX = Math.min(x1, x2);
        int endX = Math.max(x1, x2);
        int hw = RandomUtils.uniform(random, MIN_HALLWAY_WIDTH, MAX_HALLWAY_WIDTH + 1);
        int loY = clamp(y - hw / 2, 0, HEIGHT - 1);
        int hiY = clamp(loY + hw, 0, HEIGHT);

        for (int x = startX; x <= endX; x++) {
            if (x < 0 || x >= WIDTH) {
                continue;
            }
            for (int cy = loY; cy < hiY; cy++) {
                if (cy >= 0 && cy < HEIGHT && tiles[x][cy] == Tileset.NOTHING) {
                    tiles[x][cy] = Tileset.FLOOR;
                }
            }
        }
    }

    /** Carves a vertical corridor at column x between rows y1 and y2. */
    private void carveVerticalSegment(int y1, int y2, int x) {
        int startY = Math.min(y1, y2);
        int endY = Math.max(y1, y2);
        int hw = RandomUtils.uniform(random, MIN_HALLWAY_WIDTH, MAX_HALLWAY_WIDTH + 1);
        int loX = clamp(x - hw / 2, 0, WIDTH - 1);
        int hiX = clamp(loX + hw, 0, WIDTH);

        for (int y = startY; y <= endY; y++) {
            if (y < 0 || y >= HEIGHT) {
                continue;
            }
            for (int cx = loX; cx < hiX; cx++) {
                if (cx >= 0 && cx < WIDTH && tiles[cx][y] == Tileset.NOTHING) {
                    tiles[cx][y] = Tileset.FLOOR;
                }
            }
        }
    }

    /* ---------- Step 4: Wall placement ---------- */

    /**
     * After rooms and hallways are carved, surrounds every FLOOR tile
     * with WALL.  Works from a snapshot so newly placed walls do not
     * cascade outward.
     */
    private void placeWalls() {
        TETile[][] snapshot = TETile.copyOf(tiles);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (snapshot[x][y] == Tileset.FLOOR) {
                    // Check all 8 neighbours so diagonal corners are covered.
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (inBounds(nx, ny) && snapshot[nx][ny] == Tileset.NOTHING) {
                                tiles[nx][ny] = Tileset.WALL;
                            }
                        }
                    }
                }
            }
        }
    }

    /* ---------- Step 5: Outdoor terrain ---------- */

    /**
     * Converts all remaining NOTHING tiles into outdoor terrain.
     * Mountain ranges appear near the world edges.  The interior outdoor
     * space gets grass, trees, flowers, dirt paths, sand, and water pools.
     */
    private void decorateOutdoors(List<Rect> rooms) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (tiles[x][y] != Tileset.NOTHING) {
                    continue;
                }

                // Distance from each edge (0 = at edge, 1 = far from edge).
                double edgeDist = Math.min(
                        Math.min((double) x / (WIDTH * 0.12),
                                 (double) (WIDTH - 1 - x) / (WIDTH * 0.12)),
                        Math.min((double) y / (HEIGHT * 0.12),
                                 (double) (HEIGHT - 1 - y) / (HEIGHT * 0.12)));
                edgeDist = Math.min(1.0, edgeDist);

                double roll = RandomUtils.uniform(random);

                if (edgeDist < 0.5) {
                    // --- Edge region: mountains + sparse grass ---
                    if (roll < 0.30) {
                        tiles[x][y] = Tileset.MOUNTAIN;
                    } else if (roll < 0.36) {
                        tiles[x][y] = PEAK;
                    } else if (roll < 0.55) {
                        tiles[x][y] = Tileset.GRASS;
                    } else if (roll < 0.65) {
                        tiles[x][y] = Tileset.TREE;
                    } else {
                        tiles[x][y] = Tileset.GRASS;
                    }
                } else {
                    // --- Interior outdoors ---
                    if (roll < 0.50) {
                        tiles[x][y] = Tileset.GRASS;
                    } else if (roll < 0.60) {
                        tiles[x][y] = DIRT;
                    } else if (roll < 0.67) {
                        tiles[x][y] = Tileset.TREE;
                    } else if (roll < 0.70) {
                        tiles[x][y] = Tileset.FLOWER;
                    } else if (roll < 0.72) {
                        tiles[x][y] = Tileset.SAND;
                    } else {
                        tiles[x][y] = Tileset.GRASS;
                    }
                }
            }
        }

        // Place a few water pools (clustered lakes).
        placeWaterPools(rooms);
        // Place a winding dirt road across the map.
        placeDirtRoad();
    }

    /**
     * Places small clustered water pools in outdoor areas using a
     * cellular-automaton-inspired approach: seed a few water cells, then
     * grow them outward a few steps so they form natural-looking ponds.
     */
    private void placeWaterPools(List<Rect> rooms) {
        int numPools = RandomUtils.uniform(random, 2, 5);
        for (int p = 0; p < numPools; p++) {
            int cx = RandomUtils.uniform(random, 5, WIDTH - 5);
            int cy = RandomUtils.uniform(random, 5, HEIGHT - 5);
            int radius = RandomUtils.uniform(random, 2, 5);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (dx * dx + dy * dy > radius * radius) {
                        continue;
                    }
                    int wx = cx + dx;
                    int wy = cy + dy;
                    if (!inBounds(wx, wy)) {
                        continue;
                    }
                    // Only overwrite outdoor tiles, never dungeon.
                    if (isOutdoor(wx, wy)) {
                        if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
                            tiles[wx][wy] = DEEP_WATER;
                        } else if (RandomUtils.bernoulli(random, 0.7)) {
                            tiles[wx][wy] = Tileset.WATER;
                        }
                    }
                }
            }
        }
    }

    /**
     * Places a meandering dirt road that runs roughly horizontally
     * across the world, with slight vertical wobble.
     */
    private void placeDirtRoad() {
        int roadY = HEIGHT / 2 + RandomUtils.uniform(random, -5, 6);
        for (int x = 0; x < WIDTH; x++) {
            // Gently vary the road's vertical position.
            if (x % 3 == 0) {
                roadY += RandomUtils.uniform(random, -1, 2);
                roadY = clamp(roadY, 2, HEIGHT - 3);
            }
            int halfWidth = 1;
            for (int dy = -halfWidth; dy <= halfWidth; dy++) {
                int wy = roadY + dy;
                if (inBounds(x, wy) && isOutdoor(x, wy)) {
                    tiles[x][wy] = DIRT;
                }
            }
        }
    }

    /* ---------- Step 6: Indoor decorations ---------- */

    /**
     * Adds trees and flowers inside larger rooms to make them feel
     * more natural (garden rooms, overgrown chambers, etc.).
     */
    private void decorateIndoors(List<Rect> rooms) {
        for (Rect r : rooms) {
            int area = r.width * r.height;
            if (area < 20) {
                continue; // skip tiny rooms
            }
            // Number of decorations proportional to room area.
            int numDecor = RandomUtils.uniform(random, 0, area / 8);
            for (int d = 0; d < numDecor; d++) {
                int dx = RandomUtils.uniform(random, 1, r.width - 1);
                int dy = RandomUtils.uniform(random, 1, r.height - 1);
                int px = r.x + dx;
                int py = r.y + dy;
                if (tiles[px][py] == Tileset.FLOOR) {
                    if (RandomUtils.bernoulli(random, 0.6)) {
                        tiles[px][py] = Tileset.TREE;
                    } else {
                        tiles[px][py] = Tileset.FLOWER;
                    }
                }
            }
        }
    }

    /* ---------- Utility helpers ---------- */

    /** Returns true if (x, y) is within the world bounds. */
    private boolean inBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    /**
     * Returns true if the tile at (x, y) is outdoor (not FLOOR, not WALL,
     * not NOTHING -- i.e. it has already been decorated as terrain).
     */
    private boolean isOutdoor(int x, int y) {
        TETile t = tiles[x][y];
        return t != Tileset.FLOOR
                && t != Tileset.WALL
                && t != Tileset.NOTHING;
    }

    /** Clamps value between min (inclusive) and max (exclusive). */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max - 1, value));
    }
}
