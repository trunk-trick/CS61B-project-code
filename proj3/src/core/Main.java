package core;

import tileengine.TERenderer;

/**
 * Entry point for the tile-based world generator.
 *
 * Usage:  java core.Main [seed]
 *
 * If no seed is given, seed 137 is used (a nod to CS61B tradition).
 * The world is rendered in a window using TERenderer.
 */
public class Main {
    public static void main(String[] args) {
        long seed = 137;
        if (args.length > 0) {
            try {
                seed = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed '" + args[0] + "'. Using default: " + seed);
            }
        }

        System.out.println("Generating world with seed: " + seed);
        World world = new World(seed);

        TERenderer ter = new TERenderer();
        ter.initialize(World.WIDTH, World.HEIGHT);
        ter.renderFrame(world.getTiles());

        System.out.println("World generation complete. Close the window to exit.");
        System.out.println("World dimensions: " + world.getWidth() + "x" + world.getHeight());
    }
}
