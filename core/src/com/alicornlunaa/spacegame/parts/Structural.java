package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Structural extends ShipPart {
    // Variables
    private String name;
    private String description;
    private float fuelCapacity;
    private float batteryCapacity;

    // Constructor
    public Structural(Body parent, ShipState stateRef, TextureRegion region, float scale, Vector2 posOffset, float rotOffset, ArrayList<Vector2> attachmentPoints, String name, String description, float density, float fuelCapacity, float batteryCapacity){
        super(parent, stateRef, region, scale, posOffset, rotOffset, attachmentPoints);

        this.name = name;
        this.description = description;
        this.fuelCapacity = fuelCapacity;
        this.batteryCapacity = batteryCapacity;
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
        str += "\nFuel: " + String.valueOf(fuelCapacity);
        str += "\nBattery: " + String.valueOf(batteryCapacity);
        str += "\n";
        return str;
    }
}
