package com.alicornlunaa.spacegame.objects;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Ground extends Entity {

    private final App game;

    private Body body;
    private PolygonShape shape;

    public Ground(App game, World world, float x, float y, float w, float h){
        this.game = game;

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
    }

    @Override
    public void act(float delta){
        super.act(delta);
        setPosition(body.getPosition().x - getOriginX(), body.getPosition().y - getOriginY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        batch.end();
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        game.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        game.shapeRenderer.translate(getX(), getY(), 0);
        game.shapeRenderer.setColor(Color.BLUE);
        game.shapeRenderer.rect(0, 0, getWidth(), getHeight());
        game.shapeRenderer.end();
        batch.begin();
    }

    @Override
    public boolean remove(){
        shape.dispose();
        return super.remove();
    }

}
