package com.alicornlunaa.selene_engine.ecs;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public class TransformComponent implements Component {
    // Variables
    protected Vector2 dp = new Vector2();
    protected float dr = 0.0f;

    public Vector2 position = new Vector2();
    public float rotation = 0.0f;

    // Constructors
    public TransformComponent(){}

    public TransformComponent(float x, float y, float rot){
        this();
        
        position.set(x, y);
        rotation = rot;
    }

    // Functions
    public Matrix3 getMatrix(){
        Matrix3 matrix = new Matrix3();
        matrix.idt();
        matrix.translate(position);
        matrix.rotateRad(rotation);
        return matrix;
    }
}
