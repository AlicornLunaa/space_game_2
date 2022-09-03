package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.parts.ShipPart;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Ship extends Actor {
    private TextureRegion texture;
    private Body body;
    private ArrayList<ShipPart> parts;

    public Ship(World world, float x, float y, float rotation){
        texture = new TextureRegion(new Texture("badlogic.jpg"));

        BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		def.position.set(x, y);
        def.angle = rotation;
		body = world.createBody(def);
        
        setBounds(texture.getRegionX(), texture.getRegionY(), texture.getRegionWidth(), texture.getRegionHeight());
        setOrigin(texture.getRegionWidth() / 2, texture.getRegionHeight() / 2);
        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));

        this.parts = new ArrayList<ShipPart>();
        parts.add(new ShipPart(body, new Texture("tip1.png"), new Vector2(64, 64), new Vector2(0, 64), 0.f));
        parts.add(new ShipPart(body, new Texture("fuselage.png"), new Vector2(64, 64), new Vector2(0, 0), 0.f));
        parts.add(new ShipPart(body, new Texture("rocket.png"), new Vector2(64, 64), new Vector2(0, -64), 0.f));
    }

    @Override
    public void act(float delta){
        super.act(delta);

        for(ShipPart p : parts){
            p.act(delta);
        }

        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle() * (float)(180.f / Math.PI));
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Color c = getColor();
        
        for(ShipPart p : parts){
            p.draw(batch, c.a * parentAlpha);
        }
    }

    @Override
    public boolean remove(){
        for(ShipPart p : parts){
            p.remove();
        }

        return super.remove();
    }
}
