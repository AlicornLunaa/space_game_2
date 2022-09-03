package com.alicornlunaa.spacegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.ScreenUtils;

public class App extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	World world;
	Body body;
	
	@Override
	public void create(){
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");

		world = new World(new Vector2(0, -600.f), true);

		BodyDef def = new BodyDef();
		def.type = BodyType.DynamicBody;
		def.position.set(20, 20);
		body = world.createBody(def);
	}

	@Override
	public void render(){
		ScreenUtils.clear(1.f, 0.4f, 0.f, 1.f);

		batch.begin();
		batch.draw(img, body.getPosition().x, body.getPosition().y);
		batch.end();

		world.step(1/60.f, 6, 2);
	}
	
	@Override
	public void dispose(){
		batch.dispose();
		img.dispose();
	}
}
