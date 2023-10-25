package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.CreateAction;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.DeleteAction;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class Water extends CellBase {
    // Variables
    private final float VISCOSITY = 0.5f;
    private float fluidLevel = 1.f;
    private boolean full = false;

    // Constructor
    public Water() {
        super("water");
    }
    
    // Functions
    public void step(CellWorld world, Array<Action> changes){
        // Remove water with nothing in it
        // float amount = Math.max(VISCOSITY * fluidLevel, VISCOSITY);
        full = (world.getTile(getX(), getY() + 1) instanceof Water);

        if(fluidLevel <= 1e-5){
            changes.add(new DeleteAction(this));
            return;
        }
        
        // Flow down
        CellBase below = world.getTile(getX(), getY() - 1);
        if(below == null){
            CreateAction<Water> action = new CreateAction<>(getX(), getY() - 1, Water.class);
            changes.add(action);

            below = action.getCell();
            action.getCell().full = true;
            action.getCell().fluidLevel = 0.f;
        }
        if(below instanceof Water){
            Water w = (Water)below;

            if(w.fluidLevel < 1){
                // Move higher-pressure to lower-pressure
                w.fluidLevel += (VISCOSITY * fluidLevel);
                fluidLevel -= (VISCOSITY * fluidLevel);
                return;
            }
        }
        
        // Getting cells
        CellBase left = world.getTile(getX() - 1, getY());
        if(left == null){
            CreateAction<Water> action = new CreateAction<>(getX() - 1, getY(), Water.class);
            changes.add(action);
            left = action.getCell();
            action.getCell().fluidLevel = 0.f;
        }
        CellBase right = world.getTile(getX() + 1, getY());
        if(right == null){
            CreateAction<Water> action = new CreateAction<>(getX() + 1, getY(), Water.class);
            changes.add(action);
            right = action.getCell();
            action.getCell().fluidLevel = 0.f;
        }

        float averageFluidLevel = fluidLevel;
        int flowCount = 1;

        if(left instanceof Water && ((Water)left).fluidLevel < fluidLevel){
            averageFluidLevel += ((Water)left).fluidLevel;
            flowCount++;
        }
        if(right instanceof Water && ((Water)right).fluidLevel < fluidLevel){
            averageFluidLevel += ((Water)right).fluidLevel;
            flowCount++;
        }

        averageFluidLevel /= flowCount;

        // Balancing
        if(left instanceof Water){
            Water w = (Water)left;

            if(w.fluidLevel < fluidLevel){
                // Move higher-pressure to lower-pressure
                w.fluidLevel = averageFluidLevel;
            }
        }

        if(right instanceof Water){
            Water w = (Water)right;

            if(w.fluidLevel < fluidLevel){
                // Move higher-pressure to lower-pressure
                w.fluidLevel = averageFluidLevel;
            }
        }

        if(flowCount > 1)
            fluidLevel = averageFluidLevel;
    }

    public void draw(Batch batch){
        batch.setColor(1, 1, 1, 1);
        batch.draw(
            texture,
            getX() * Constants.TILE_SIZE, getY() * Constants.TILE_SIZE,
            0, 0,
            Constants.TILE_SIZE, Constants.TILE_SIZE,
            // 1, fluidLevel,
            1, full ? 1 : Math.min(fluidLevel, 1),
            0
        );
    }
}
