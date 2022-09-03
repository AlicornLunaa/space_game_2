package com.alicornlunaa.spacegame.parts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ShipPart extends Actor {
    // Variables
    private Body parent;
    private PolygonShape shape;
    private TextureRegion region;

    // Constructor
    public ShipPart(Body parent, Texture texture, Vector2 size, Vector2 posOffset, float rotOffset){
        this.parent = parent;
        region = new TextureRegion(texture);
        shape = new PolygonShape();

        setSize(size.x, size.y);
        setOrigin(size.x / 2.f - posOffset.x, size.y / 2.f - posOffset.y);
        setPosition(posOffset.x, posOffset.y);
        setRotation(rotOffset);

        shape.setAsBox(size.x / 2.f, size.y / 2.f, posOffset, rotOffset * (float)(Math.PI / 180.f));
        parent.createFixture(shape, 0.5f);
    }

    // Functions
    @Override
    public void setRotation(float r){
        super.setRotation(r);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void setPosition(float x, float y){
        super.setPosition(x, y);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void setSize(float w, float h){
        super.setSize(w, h);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void act(float delta){
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        batch.draw(
            region,
            parent.getPosition().x + getX() - getWidth() / 2,
            parent.getPosition().y + getY() - getHeight() / 2,
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX(),
            getScaleY(),
            (parent.getAngle() * (float)(180.f / Math.PI)) + getRotation()
        );
    }

    @Override
    public boolean remove(){
        shape.dispose();
        return super.remove();
    }

    // Static methods
    // public static ShipPart fromFile(String filename){

    // }
}
