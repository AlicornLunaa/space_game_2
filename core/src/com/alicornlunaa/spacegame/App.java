package com.alicornlunaa.spacegame;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.objects.Ground;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.panels.ShipEditor;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class App extends ApplicationAdapter {
	private World world;
	private Stage gameStage;
	private ArrayList<Stage> stages;
	private Box2DDebugRenderer debugRenderer;
	private InputMultiplexer inputs;
	private float accumulator;

	// https://libgdx.com/wiki/graphics/2d/scene2d/table
	private Skin skin;
	private Table ui;
	private Table pauseMenu;

	private TextButton rcsButton;
	private TextButton sasButton;

	private boolean paused;
	private boolean debug;
	private boolean rcs;
	private boolean sas;
	
	@Override
	public void create(){
		world = new World(new Vector2(0, 0), true);
		gameStage = new Stage(new ScreenViewport());
		stages = new ArrayList<Stage>();
		stages.add(gameStage);
		debugRenderer = new Box2DDebugRenderer();
		accumulator = 0.f;
		
		ControlSchema.fromFile("spacegame_controls.json");

		skin = new Skin(Gdx.files.internal("skins/default/skin/uiskin.json"));
		ui = new Table();
		pauseMenu = new Table();

		paused = false;
		debug = true;
		rcs = false;
		sas = false;

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

				case Keys.R:
					rcs = !rcs;
					rcsButton.setColor(rcs ? Color.GREEN : Color.RED);
					return true;

				case Keys.T:
					sas = !sas;
					sasButton.setColor(sas ? Color.GREEN : Color.RED);
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
		rcsButton = new TextButton("RCS", skin);
		sasButton = new TextButton("SAS", skin);
		rcsButton.setPosition(640 - 128, 480 - 32);
		rcsButton.setSize(64, 32);
		rcsButton.setColor(Color.RED);
		gameStage.addActor(rcsButton);
		sasButton.setPosition(640 - 64, 480 - 32);
		sasButton.setSize(64, 32);
		sasButton.setColor(Color.RED);
		gameStage.addActor(sasButton);
		gameStage.addActor(ui);

		pauseMenu.setFillParent(true);
		pauseMenu.setDebug(true);
		pauseMenu.add(new Label("PAUSED", skin));
		pauseMenu.row();
		TextButton button = new TextButton("QUIT", skin);
		button.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor){
				Gdx.app.exit();
			}
		});
		pauseMenu.add(button).width(100).height(40);
		gameStage.addActor(pauseMenu);

		gameStage.addActor(new Ship(world, 640/2, 250, 0));
		gameStage.addActor(new Ground(world, 640/2, 20, 500, 15));
		
		// stages.add(new ShipEditor(inputs));
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

		Stage curStage = stages.get(stages.size() - 1);
		curStage.act(delta);
		curStage.draw();

		if(rcs){

		}
		
		if(sas){

		}

		if(debug){
			debugRenderer.render(world, gameStage.getCamera().combined);
		}

		if(!paused){
			// Physics fixed timestep
			accumulator += Math.min(delta, 0.25f);;
			while(accumulator >= Constants.TIME_STEP){
				world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
				accumulator -= Constants.TIME_STEP;
			}
		}
	}
	
	@Override
	public void dispose(){
		gameStage.dispose();
	}
}
