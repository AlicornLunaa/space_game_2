package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/*
 * Mono propellant/RCS ports are decided to be fired depending on the button pressed
 * 
 * Roll can be calculated by getting the angle of the port to the ship's center of
 * mass and checking if its tangent value is positive or negative, just like on a
 * unit circle.
 */

public class RCSPort extends ShipPart {

    // Variables
    private String name;
    private String description;
    private float power;
    private float fuelUsage;
    

    // Constructors
    protected RCSPort(String name, String description, float density, float power, float fuelUsage){
        super();

        this.name = name;
        this.description = description;
        this.power = power;
        this.fuelUsage = fuelUsage;
    }

    public RCSPort(Body parent, ShipState stateRef, TextureRegion region, Vector2 size, Vector2 pos, float rot, ArrayList<Vector2> attachmentPoints, String name, String description, float power, float fuelUsage){
        super(parent, stateRef, region, size, pos, rot, attachmentPoints);

        this.name = name;
        this.description = description;
        this.power = power;
        this.fuelUsage = fuelUsage;
    }

    // Functions
    public int getSignAtPos(){
        Vector2 center = parent.getLocalCenter();
        Vector2 posOfCenter = new Vector2(getX() - center.x, getY() - center.y);
        float tanVal = (float)Math.atan(posOfCenter.y / posOfCenter.x);
        return Math.round(Math.abs(tanVal) / tanVal);
    }

    public void thrust(float delta, float direction){
        if(direction <= 0) return; // Cant thrust negative
    
        Vector2 portPos = new Vector2(getX(), getY()).rotateDeg((float)Math.toDegrees(parent.getAngle()) + getRotation()).add(parent.getPosition());
        Vector2 dir = new Vector2(1, 0).rotateDeg((float)Math.toDegrees((parent.getAngle()) + getRotation()) * getScaleX()).scl(-getScaleX(), -getScaleY());
        
        parent.applyForce(dir.scl(power * 2.f).add(portPos), portPos, true);
    }

    @Override
    public void act(float delta){
        super.act(delta);

        if(stateRef.rcs){
            thrust(delta, getSignAtPos() * stateRef.roll);
        }
    }

    @Override
    public String toString(){
        String str = "Name: " + name;
        str += "\nDesc: " + description;
        str += "\nPower: " + String.valueOf(power);
        str += "\nFuel usage: " + String.valueOf(fuelUsage);
        str += "\n";
        return str;
    }
    
}
