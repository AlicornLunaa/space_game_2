package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Thruster extends ShipPart {
    // Variables
    private String name;
    private String description;
    private float power;
    private float coneAngle;
    private float coneSpeed;
    private float fuelUsage;
    private float rotationOffset;

    private float currentAngle;

    // Constructor
    public Thruster(Body parent, ShipState stateRef, TextureRegion region, float scale, Vector2 posOffset, float rotOffset, ArrayList<Vector2> attachmentPoints, String name, String description, float density, float power, float coneAngle, float fuelUsage){
        super(parent, stateRef, region, scale, posOffset, rotOffset, attachmentPoints);

        this.name = name;
        this.description = description;
        this.power = power;
        this.coneAngle = coneAngle;
        this.coneSpeed = 1.5f;
        this.fuelUsage = fuelUsage;
        this.rotationOffset = rotOffset;

        currentAngle = 0.f;
    }

    // Functions
    public void setTargetAngle(float angle){
        currentAngle += Math.min(Math.max((angle * coneAngle) - currentAngle, -coneSpeed), coneSpeed);
        setRotation(rotationOffset + currentAngle);
    }

    public void thrust(float delta, float throttle){
        if(throttle == 0) return;

        Vector2 dir = new Vector2(
            (float)Math.cos((currentAngle - 90) * (Math.PI / 180.f) + parent.getAngle()),
            (float)Math.sin((currentAngle - 90) * (Math.PI / 180.f) + parent.getAngle())
        );
        parent.applyForce(dir.scl(power * -2.f * throttle).add(parent.getPosition()), parent.getWorldPoint(new Vector2(getX(), getY())), true);
    }

    @Override
    public void act(float delta){
        super.act(delta);

        float compRoll = Math.min(Math.max(stateRef.roll + stateRef.artifRoll, -1), 1);

        this.setTargetAngle(compRoll);
        this.thrust(delta, stateRef.throttle);
    }

    @Override
    public String toString(){
        String str = "Name: " + name;
        str += "\nDesc: " + description;
        str += "\nPower: " + String.valueOf(power);
        str += "\nConeAngle: " + String.valueOf(coneAngle);
        str += "\nFuelUsage: " + String.valueOf(fuelUsage);
        str += "\n";
        return str;
    }
}
