package com.alicornlunaa.spacegame.objects.simulation.cellular;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.MoveAction;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.Vector2i;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

// Base cell for automata simulation
public class CellBase {
    // Variables
    protected TextureRegion texture;

    private Vector2i idp = new Vector2i(); // Integer position
    private Vector2 position = new Vector2(); // "Real" float position
    private Vector2 velocity = new Vector2(); // Velocity

    // Private functions
    protected Array<Vector2i> getLine(int startX, int startY, int endX, int endY){
        Array<Vector2i> arr = new Array<>();

        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        int hStep = (startX < endX) ? 1 : -1;
        int vStep = (startY < endY) ? 1 : -1;
        int diff = dx - dy;

        while(true){
            int doubleDiff = diff * 2;

            if(doubleDiff > -dy){
                diff -= dy;
                startX += hStep;
            }

            if(doubleDiff < dx){
                diff += dx;
                startY += vStep;
            }

            if(startX == endX && startY == endY)
                break;

            arr.add(new Vector2i(startX, startY));
        }

        arr.add(new Vector2i(endX, endY));
        return arr;
    }

    protected boolean iteratePossiblePositions(CellWorld world, Array<Action> changes, Array<Vector2i> possiblePositions){
        @Null Vector2i validTarget = null;
        
        for(Vector2i possiblePosition : possiblePositions){
            if(world.getTile(possiblePosition.x, possiblePosition.y) != null)
                break;

            validTarget = possiblePosition;
        }
        if(validTarget != null){
            // Swap positions to the last valid target position
            changes.add(new MoveAction(this, validTarget.x, validTarget.y));
            return true;
        }

        return false;
    }

    protected void move(float x, float y){
        position.add(x, y);
    }

    protected boolean checkAndSwap(CellWorld world, Array<Action> changes, int toX, int toY){
        int targetX = getX() + toX;
        int targetY = getY() + toY;

        if(world.getTile(targetX, targetY) == null){
            changes.add(new MoveAction(this, targetX, targetY));
            return true;
        }

        return false;
    }
    
    protected boolean isEmpty(CellWorld world, int toX, int toY){
        return (world.getTile(getX() + toX, getY() + toY) == null);
    }

    // Constructor
    public CellBase(String textureName){
        texture = App.instance.atlas.findRegion("tiles/" + textureName);
        setPosition(0, 0);
    }

    public CellBase(String textureName, int x, int y){
        this(textureName);
        setPosition(0, 0);
    }

    // Functions
    public int getX(){ return idp.x; }

    public int getY(){ return idp.y; }

    public void setPosition(int x, int y){
        position.set(x + 0.5f, y + 0.5f);
        idp.x = x; idp.y = y;
    }

    public Vector2 getPosition(){ return position; }

    public void setVelocity(float vx, float vy){ velocity.set(vx, vy); }

    public Vector2 getVelocity(){ return velocity; }

    public void applyForce(float vx, float vy){ velocity.add(vx, vy); }

    public void step(CellWorld world, Array<Action> changes){
    }

    public void draw(Batch batch){
        batch.setColor(1, 1, 1, 1);
        batch.draw(
            texture,
            getX() * Constants.TILE_SIZE, getY() * Constants.TILE_SIZE,
            0, 0,
            Constants.TILE_SIZE, Constants.TILE_SIZE,
            1, 1,
            0
        );
    }
}
