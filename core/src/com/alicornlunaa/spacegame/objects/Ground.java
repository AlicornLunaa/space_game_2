package com.alicornlunaa.spacegame.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Ground extends Actor {
    private Body body;
    private PolygonShape shape;
    private ShapeRenderer renderer;

    public Ground(World world, float x, float y, float w, float h){
        BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		def.position.set(x, y);
		body = world.createBody(def);

        shape = new PolygonShape();
        shape.setAsBox(w / 2, h / 2);
        body.createFixture(shape, 0.f);
        
        setBounds(0, 0, w, h);
        setOrigin(w / 2, h / 2);
        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation(body.getAngle());

        renderer = new ShapeRenderer();
    }

    @Override
    public void act(float delta){
        super.act(delta);

        setPosition(body.getPosition().x, body.getPosition().y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        batch.end();
        
        renderer.setProjectionMatrix(batch.getProjectionMatrix());
        renderer.setTransformMatrix(batch.getTransformMatrix());
        renderer.translate(getX() - getWidth() / 2.f, getY() - getHeight() / 2.f, 0);

        renderer.begin(ShapeType.Filled);
        renderer.setColor(Color.BLUE);
        renderer.rect(0, 0, getWidth(), getHeight());
        renderer.end();

        batch.begin();
    }

    @Override
    public boolean remove(){
        shape.dispose();
        return super.remove();
    }
}
