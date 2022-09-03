package com.alicornlunaa.spacegame.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Ship extends Actor {
    private TextureRegion texture;
    private Body body;
    private PolygonShape shape;

    public Ship(World world, float x, float y, float rotation){
        texture = new TextureRegion(new Texture("badlogic.jpg"));

        BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		def.position.set(x, y);
        def.angle = rotation;
		body = world.createBody(def);

        shape = new PolygonShape();
        shape.setAsBox(texture.getRegionWidth() / 2, texture.getRegionHeight() / 2);
        body.createFixture(shape, 0.5f);
        
        setBounds(texture.getRegionX(), texture.getRegionY(), texture.getRegionWidth(), texture.getRegionHeight());
        setOrigin(texture.getRegionWidth() / 2, texture.getRegionHeight() / 2);
        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));
    }

    @Override
    public void act(float delta){
        super.act(delta);

        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        batch.draw(texture, getX() - getWidth() / 2, getY() - getHeight() / 2, getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }

    @Override
    public boolean remove(){
        shape.dispose();
        return super.remove();
    }
}
