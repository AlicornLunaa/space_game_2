package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.alicornlunaa.space_game.util.Vector2i;
import com.badlogic.gdx.math.Vector2;

public abstract class AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };

    // Variables
    public final Element element;
    public final State state;
    public float temperature = 0.f; // In Kelvin
    public float mass = 0.f; // In kilograms
    public Vector2 floatingPosition = new Vector2(0.5f, 0.5f); // Keeps the decimals of the current position to allow small movements
    public Vector2 velocity = new Vector2();
    public boolean isUpdated = false; // If this tile was updated this frame or not

    // Constructor
    public AbstractTile(Element element, State state){
        this.element = element;
        this.state = state;
    }

    // Functions
    public boolean update(Simulation simulation, int currX, int currY){
        // Basic functionality of every cell, like heat
        AbstractTile current = simulation.getTile(currX, currY);
        current.isUpdated = true;
        
        // Gravity rule
        if(element.falling)
            velocity.add(simulation.gravity);

        floatingPosition.add(velocity);

        if(Math.abs(floatingPosition.x) >= 1 || Math.abs(floatingPosition.y) >= 1){
            // Move based on velocity
            int newX = (int)(currX + floatingPosition.x);
            int newY = (int)(currY + floatingPosition.y);
            int targetX = currX;
            int targetY = currY;
            floatingPosition.set(0.5f, 0.5f); // Reset the position offset to the middle

            // Check every tile in the way
            for(Vector2i v : Simulation.getLine(currX, currY, newX, newY)){
                AbstractTile target = simulation.getTile(v.x, v.y);

                if(simulation.inBounds(v.x, v.y) && target == null){
                    // The target is a valid spot to move, update new position
                    targetX = v.x;
                    targetY = v.y;
                    continue;
                }

                break;
            }

            if(targetX != currX || targetY != currY){
                // Move if the position is different
                simulation.tiles[simulation.getIndex(targetX, targetY)] = current;
                simulation.tiles[simulation.getIndex(currX, currY)] = null;
                return false;
            } else {
                // Nowhere to move, reset velocity
                velocity.set(0, 0);
            }
        }

        // Finish up
        return true; // Continue the update
    }
}
