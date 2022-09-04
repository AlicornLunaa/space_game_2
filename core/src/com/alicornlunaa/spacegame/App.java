package com.alicornlunaa.spacegame;

import com.alicornlunaa.spacegame.objects.Ground;
import com.alicornlunaa.spacegame.objects.Ship;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class App extends ApplicationAdapter {
	private World world;
	private Stage gameStage;
	private Box2DDebugRenderer debugRenderer;
	private InputMultiplexer inputs;

	// https://libgdx.com/wiki/graphics/2d/scene2d/table
	private Skin skin;
	private Table ui;
	private Table pauseMenu;

	private boolean paused;
	private boolean debug;
	
	@Override
	public void create(){
		world = new World(new Vector2(0, -600.f), true);
		gameStage = new Stage(new ScreenViewport());
		debugRenderer = new Box2DDebugRenderer();

		skin = new Skin(Gdx.files.internal("skins/default/skin/uiskin.json"));
		ui = new Table();
		pauseMenu = new Table();

		paused = false;
		debug = true;

		inputs = new InputMultiplexer();
		inputs.addProcessor(new InputAdapter(){
			@Override
			public boolean keyDown(int keyCode){
				switch(keyCode){
				case Keys.ESCAPE:
					paused = !paused;
					return true;

				case Keys.F3:
					debug = !debug;
					ui.setDebug(debug);
					pauseMenu.setDebug(debug);
					return true;
				}

				return false;
			}

			@Override
			public boolean scrolled(float x, float y){
				OrthographicCamera cam = ((OrthographicCamera)gameStage.getCamera());
				cam.zoom = Math.min(Math.max(cam.zoom + y / 25.f, 0.25f), 2);
				return cam.zoom != 0.25f || cam.zoom != 2;
			}
		});
		inputs.addProcessor(gameStage);
		Gdx.input.setInputProcessor(inputs);

		ui.setFillParent(true);
		ui.setDebug(true);
		gameStage.addActor(ui);

		pauseMenu.setFillParent(true);
		pauseMenu.setDebug(true);
		pauseMenu.add(new Label("PAUSED", skin));
		pauseMenu.row();
		pauseMenu.add(new TextButton("QUIT", skin)).width(100).height(40);
		gameStage.addActor(pauseMenu);

		gameStage.addActor(new Ship(world, 640/2, 250, 0));
		gameStage.addActor(new Ground(world, 640/2, 20, 500, 15));
	}

	@Override
	public void resize(int width, int height){
		gameStage.getViewport().update(width, height, true);
	}

	@Override
	public void render(){
		float delta = Gdx.graphics.getDeltaTime();

		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.f);

		pauseMenu.setVisible(paused);

		gameStage.act(delta);
		gameStage.draw();

		if(debug){
			debugRenderer.render(world, gameStage.getCamera().combined);
		}

		if(!paused){
			world.step(1/60.f, 6, 2);
		}
	}
	
	@Override
	public void dispose(){
		gameStage.dispose();
	}
}
