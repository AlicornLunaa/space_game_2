package com.alicornlunaa.spacegame;

import com.alicornlunaa.spacegame.objects.Ground;
import com.alicornlunaa.spacegame.objects.Ship;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class App extends ApplicationAdapter {
	World world;
	Stage stage;
	Body body;

	Box2DDebugRenderer debug;
	
	@Override
	public void create(){
		world = new World(new Vector2(0, -600.f), true);
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		debug = new Box2DDebugRenderer();

		stage.addActor(new Ship(world, 640/2, 250, 15));
		stage.addActor(new Ground(world, 640/2, 20, 500, 15));
	}

	@Override
	public void resize(int width, int height){
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render(){
		float delta = Gdx.graphics.getDeltaTime();

		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.f);
		stage.act(delta);
		stage.draw();

		debug.render(world, stage.getCamera().combined);

		world.step(1/60.f, 6, 2);
	}
	
	@Override
	public void dispose(){
		stage.dispose();
	}
}
