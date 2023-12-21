package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.alicornlunaa.space_game.cell_simulation.actions.CreateAction;
import com.alicornlunaa.space_game.cell_simulation.actions.DeleteAction;
import com.alicornlunaa.space_game.util.Constants;

public class LiquidTile extends AbstractTile {
    // Variables
    public int spreadFactor = 1;
    public float viscosity = 0.5f;
    public boolean falling = false;

    // Constructor
    public LiquidTile(Element element) {
        super(element, State.LIQUID);
        mass = element.density;
    }

    // Functions
    @Override
    public void update(Simulation simulation, int currX, int currY){
        // Rendering flags
        falling = simulation.getTile(currX, currY + 1) instanceof LiquidTile;

        // Remove water with nothing in it
        if(mass <= Constants.MIN_FLUID_LEVEL){
            simulation.actionStack.add(new DeleteAction(currX, currY));
            return;
        }

        // Simulate water flow
        AbstractTile target = simulation.getTile(currX, currY - 1);

        if(simulation.inBounds(currX, currY - 1) && target == null){
            // Create new liquid cell
            LiquidTile tile = new LiquidTile(element);
            tile.falling = true;
            simulation.actionStack.add(new CreateAction(tile, currX, currY - 1));
        }

        if(target != null && target.element == element && target.mass < target.element.density){
            // Move higher to lower water
            // TODO: Convert to actions
            target.mass += (viscosity * mass);
            mass -= (viscosity * mass);
            return;
        }
    }
}
