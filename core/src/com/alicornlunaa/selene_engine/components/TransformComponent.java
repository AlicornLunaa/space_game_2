package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public class TransformComponent implements IComponent{
    
    public Vector2 dp = new Vector2();
    public Vector2 dv = new Vector2();
    public float dr = 0.0f;

    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public float rotation = 0.0f;

    public Matrix3 getMatrix(){
        Matrix3 matrix = new Matrix3();
        matrix.idt();
        matrix.translate(position);
        matrix.rotateRad(rotation);
        return matrix;
    }
    
}
