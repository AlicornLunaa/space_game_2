package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;

public abstract class AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };

    // Variables
    public final Element element;
    public final State state;
    public float temperature = 0.f; // In Kelvin
    public float mass = 0.f; // In kilograms
    public boolean isUpdated = false; // If this tile was updated this frame or not

    // Constructor
    public AbstractTile(Element element, State state){
        this.element = element;
        this.state = state;
    }

    // Functions
    protected boolean checkTileSwap(Simulation simulation, int fx, int fy, int tx, int ty){
        AbstractTile target = simulation.getTile(tx, ty);

        if(simulation.inBounds(tx, ty) && (target == null || target.state != State.SOLID)){
            simulation.swap(fx, fy, tx, ty);
            return true;
        }

        return false;
    }

    public void update(Simulation simulation, int currX, int currY){
        // Basic functionality of every cell, like heat
        AbstractTile current = simulation.getTile(currX, currY);
        current.isUpdated = true;
    }
}
