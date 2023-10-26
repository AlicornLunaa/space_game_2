package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.DeleteAction;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.EmplaceAction;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.SwapAction;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.Vector2i;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public abstract class Liquid extends CellBase {
    // Variables
    private final double MIN_FLUID_LEVEL = 1e-5;

    private int spreadFactor = 5;
    private float viscosity = 0.5f;
    private float density = 1.f;
    private float fluidLevel = 1.f;
    private boolean falling = false;

    // Private functions
    private void scanPossibleCells(CellWorld world, Array<Action> changes, Array<Liquid> flowingCells, Array<Vector2i> positions){
        for(Vector2i v : positions){
            CellBase possibleCell = world.getTile(v.x, v.y);

            if(possibleCell == null){
                // No cell exists here, create it
                Liquid cell = this.cpy();
                cell.fluidLevel = 0.0f;
                possibleCell = cell;
                changes.add(new EmplaceAction(cell, v.x, v.y));
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
        Array<Vector2i> leftCellPositions = getLine(getX(), getY(), getX() - spreadFactor, getY());
        Array<Vector2i> rightCellPositions = getLine(getX(), getY(), getX() + spreadFactor, getY());

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

    private boolean flowDown(CellWorld world, Array<Action> changes){
        // Flow down
        CellBase possibleCell = world.getTile(getX(), getY() - 1);

        if(possibleCell == null){
            Liquid cell = this.cpy();
            cell.falling = true;
            cell.fluidLevel = 0.f;
            possibleCell = cell;
            changes.add(new EmplaceAction(cell, getX(), getY() - 1));
        } else if(possibleCell instanceof Liquid){
            // Some liquid, check if its less dense
            Liquid liquidCell = (Liquid)possibleCell;

            if(liquidCell.density < density){
                // Swap them.
                changes.add(new SwapAction(this, getX(), getY() - 1));
                return true;
            }
        }

        if(possibleCell instanceof Liquid){
            Liquid cell = (Liquid)possibleCell;

            if(cell.fluidLevel < 1){
                // Move higher-pressure to lower-pressure
                cell.fluidLevel += (viscosity * fluidLevel);
                fluidLevel -= (viscosity * fluidLevel);
                return true;
            }
        }

        return false;
    }

    // Constructor
    public Liquid(String textureName, int spreadFactor, float viscosity, float density, float fluidLevel) {
        super(textureName);
        this.spreadFactor = spreadFactor;
        this.viscosity = viscosity;
        this.density = density;
        this.fluidLevel = fluidLevel;
    }
    
    // Functions
    public abstract Liquid cpy();

    public void step(CellWorld world, Array<Action> changes){
        // Remove water with nothing in it
        falling = (world.getTile(getX(), getY() + 1) instanceof Liquid);

        // Prevent stupidly small water cells
        if(fluidLevel <= MIN_FLUID_LEVEL){
            changes.add(new DeleteAction(this));
            return;
        }
        
        // Simulate water pressure
        if(!flowDown(world, changes))
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
