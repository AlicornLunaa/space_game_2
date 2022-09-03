package com.alicornlunaa.spacegame.parts;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Aero extends ShipPart {
    // Variables
    private String name;
    private String description;
    private float drag;
    private float lift;

    // Constructor
    public Aero(Body parent, Texture texture, Vector2 size, Vector2 posOffset, float rotOffset, String name, String description, float density, float drag, float lift){
        super(parent, texture, size, posOffset, rotOffset);

        this.name = name;
        this.description = description;
        this.drag = drag;
        this.lift = lift;
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
        str += "\nDrag: " + String.valueOf(drag);
        str += "\nLift: " + String.valueOf(lift);
        str += "\n";
        return str;
    }
}
