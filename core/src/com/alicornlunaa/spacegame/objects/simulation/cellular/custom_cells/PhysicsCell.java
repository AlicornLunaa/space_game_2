package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.MoveAction;
import com.alicornlunaa.spacegame.util.Vector2i;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class PhysicsCell extends CellBase {
    public PhysicsCell(String type) {
        super(type);
    }
    
    public void step(CellWorld world, Array<Action> changes){
        // Continuous physics
        Vector2 projectedPosition = getPosition().cpy().add(getVelocity());
        
        if((int)projectedPosition.x != getX() || (int)projectedPosition.y != getY()){
            // Position has changed, fix it
            // Newtonian physics check for swapping position
            Array<Vector2i> path = this.getLine(getX(), getY(), (int)projectedPosition.x, (int)projectedPosition.y);
            @Null Vector2i validTarget = null;
    
            // Find next possible position
            for(Vector2i possiblePosition : path){
                if(world.getTile(possiblePosition.x, possiblePosition.y) != null){
                    // Collision with some cell, run a calculation for a response
                    float dx = Math.signum(possiblePosition.x - getX());
                    float dy = Math.signum(possiblePosition.y - getY());

                    dx = (dx == 0) ? 1 : dx;
                    dy = (dy == 0) ? 1 : dy;
                    
                    setVelocity(getVelocity().x * dx * 0.1f, getVelocity().y * dy * 0.1f);
                    break;
                }
    
                validTarget = possiblePosition;
            }
    
            if(validTarget != null && world.getTile(validTarget.x, validTarget.y) == null){
                // Swap positions to the last valid target position
                changes.add(new MoveAction(this, validTarget.x, validTarget.y));
            }
        }

        // Position is a small-floating point change, update it with euler integration
        getPosition().set(projectedPosition);

        // Gravity
        applyForce(0, -0.001f);
    }
}
