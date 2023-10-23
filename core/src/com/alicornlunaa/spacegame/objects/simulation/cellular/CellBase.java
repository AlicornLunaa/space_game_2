package com.alicornlunaa.spacegame.objects.simulation.cellular;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

// Base cell for automata simulation
public class CellBase {
    // Variables
    protected TextureRegion texture;

    private int ix, iy; // Integer position
    private Vector2 position = new Vector2(); // "Real" float position
    private Vector2 velocity = new Vector2(); // Velocity

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
    public int getX(){ return ix; }

    public int getY(){ return iy; }

    public void setPosition(float x, float y){
        position.set(x, y);
        ix = (int)position.x; iy = (int)position.y;
    }

    public Vector2 getPosition(){ return position; }

    public void setVelocity(float vx, float vy){
        velocity.set(vx, vy);
    }

    public Vector2 getVelocity(){ return velocity; }

    public void applyForce(float vx, float vy){
        velocity.add(vx, vy);
    }

    public void step(CellWorld world, Array<Action> changes){
    }
}
