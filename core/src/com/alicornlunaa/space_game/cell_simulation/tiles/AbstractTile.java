package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.alicornlunaa.space_game.cell_simulation.actions.SwapAction;

public abstract class AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };

    // Variables
    public final State state;
    public float temperature = 0.f; // In Kelvin
    public float mass = 0.f; // In kilograms

    // Constructor
    public AbstractTile(State state){
        this.state = state;
    }

    // Functions
    private boolean checkTile(Simulation simulation, int fx, int fy, int tx, int ty){
        AbstractTile target = simulation.getTile(tx, ty);

        if(simulation.inBounds(tx, ty) && (target == null || target.state != State.SOLID)){
            simulation.actionStack.add(new SwapAction(fx, fy, tx, ty));
            return true;
        }

        return false;
    }

    public void update(Simulation simulation, int currX, int currY){
        // Basic functionality of every cell, like heat
        if(checkTile(simulation, currX, currY, currX, currY - 1)) return;
        if(checkTile(simulation, currX, currY, currX - 1, currY - 1)) return;
        if(checkTile(simulation, currX, currY, currX + 1, currY - 1)) return;
    }
}
