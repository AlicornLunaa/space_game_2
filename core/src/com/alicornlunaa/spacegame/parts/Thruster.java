package com.alicornlunaa.spacegame.parts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Thruster extends ShipPart {
    // Variables
    private String name;
    private String description;
    private float power;
    private float coneAngle;

    // Constructor
    public Thruster(Body parent, Texture texture, Vector2 size, Vector2 posOffset, float rotOffset, String name, String description, float density, float power, float coneAngle){
        super(parent, texture, size, posOffset, rotOffset);

        this.name = name;
        this.description = description;
        this.power = power;
        this.coneAngle = coneAngle;
    }

    // Functions
    @Override
    public void act(float delta){
        super.act(delta);

        if(Gdx.input.isKeyPressed(Keys.SPACE)){
            Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Vector2 curPos = new Vector2(getX(), getY());
            Vector2 dir = mouse.sub(parent.getPosition()).nor();

            parent.applyForce(dir.scl(power * 10000.f), parent.getWorldPoint(curPos), true);
        }
    }

    @Override
    public String toString(){
        String str = "Name: " + name;
        str += "\nDesc: " + description;
        str += "\nPower: " + String.valueOf(power);
        str += "\nConeAngle: " + String.valueOf(coneAngle);
        str += "\n";
        return str;
    }
}
