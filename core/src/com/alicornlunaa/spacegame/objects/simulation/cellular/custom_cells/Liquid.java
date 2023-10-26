package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.CreateAction;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.DeleteAction;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.Vector2i;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class Liquid extends CellBase {
    // Variables
    private final int SPREAD_FACTOR = 5;
    private final float VISCOSITY = 0.5f;
    private final double MIN_FLUID_LEVEL = 1e-5;

    private float fluidLevel = 1.f;
    private boolean falling = false;

    // Private functions
    private void scanPossibleCells(CellWorld world, Array<Action> changes, Array<Liquid> flowingCells, Array<Vector2i> positions){
        for(Vector2i v : positions){
            CellBase possibleCell = world.getTile(v.x, v.y);

            if(possibleCell == null){
                // No cell exists here, create it
                CreateAction<Liquid> action = new CreateAction<>(v.x, v.y, Liquid.class);
                changes.add(action);

                Liquid cell = action.getCell();
                cell.fluidLevel = 0.0f;
                possibleCell = cell;
            } else if(!(possibleCell instanceof Liquid)) {
                // Flow is blocked
                break;
            }
            
            // Cast to liquid
            Liquid possibleLiquidCell = (Liquid)possibleCell;

            // Add this cell to the possible cells because its a liquid and valid for transfer
            if(possibleLiquidCell.fluidLevel < fluidLevel)
                flowingCells.add(possibleLiquidCell);

            // Aditional rule checking
            CellBase additionalCellCheck = world.getTile(v.x, v.y - 1);
            if(additionalCellCheck == null || (additionalCellCheck != null && additionalCellCheck instanceof Liquid && ((Liquid)additionalCellCheck).fluidLevel < 1))
                break;
        }
    }

    private void balanceFluidLevel(CellWorld world, Array<Action> changes){
        // Get cells to transfer to
        Array<Vector2i> leftCellPositions = getLine(getX(), getY(), getX() - SPREAD_FACTOR, getY());
        Array<Vector2i> rightCellPositions = getLine(getX(), getY(), getX() + SPREAD_FACTOR, getY());

        // Get possible cells to transfer to
        Array<Liquid> flowingCells = new Array<>();
        scanPossibleCells(world, changes, flowingCells, leftCellPositions);
        scanPossibleCells(world, changes, flowingCells, rightCellPositions);

        // Calculate flow between all possible cells
        float averageFluidLevel = fluidLevel;
        
        for(Liquid cell : flowingCells){
            averageFluidLevel += cell.fluidLevel;
        }

        averageFluidLevel /= (flowingCells.size + 1);
        
        // Execute the flow
        for(Liquid cell : flowingCells)
            cell.fluidLevel = averageFluidLevel;

        fluidLevel = averageFluidLevel;
    }

    // Constructor
    public Liquid() {
        super("water");
    }
    
    // Functions
    public void step(CellWorld world, Array<Action> changes){
        // Remove water with nothing in it
        falling = (world.getTile(getX(), getY() + 1) instanceof Liquid);

        if(fluidLevel <= MIN_FLUID_LEVEL){
            changes.add(new DeleteAction(this));
            return;
        }
        
        // Flow down
        CellBase below = world.getTile(getX(), getY() - 1);
        if(below == null){
            CreateAction<Liquid> action = new CreateAction<>(getX(), getY() - 1, Liquid.class);
            changes.add(action);

            below = action.getCell();
            action.getCell().falling = true;
            action.getCell().fluidLevel = 0.f;
        }
        if(below instanceof Liquid){
            Liquid w = (Liquid)below;

            if(w.fluidLevel < 1){
                // Move higher-pressure to lower-pressure
                w.fluidLevel += (VISCOSITY * fluidLevel);
                fluidLevel -= (VISCOSITY * fluidLevel);
                return;
            }
        }
        
        // Simulate water pressure
        balanceFluidLevel(world, changes);
    }

    public void draw(Batch batch){
        batch.setColor(1, 1, 1, 0.75f);
        batch.draw(
            texture,
            getX() * Constants.TILE_SIZE, getY() * Constants.TILE_SIZE,
            0, 0,
            Constants.TILE_SIZE, Constants.TILE_SIZE,
            1, falling ? 1 : Math.min(fluidLevel, 1),
            0
        );
    }
}
