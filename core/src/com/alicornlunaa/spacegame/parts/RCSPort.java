package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

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

    public RCSPort(Body parent, TextureRegion region, Vector2 size, Vector2 pos, float rot, ArrayList<Vector2> attachmentPoints, String name, String description, float power, float fuelUsage){
        super(parent, region, size, pos, rot, attachmentPoints);

        this.name = name;
        this.description = description;
        this.power = power;
        this.fuelUsage = fuelUsage;
    }

    // Functions
    @Override
    public void act(float delta){
        super.act(delta);
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
