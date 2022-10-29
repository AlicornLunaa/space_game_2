package com.alicornlunaa.spacegame;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.panels.GameStage;
import com.alicornlunaa.spacegame.panels.OptionsPanel;
import com.alicornlunaa.spacegame.panels.ShipEditor;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class App extends ApplicationAdapter {
	private Assets manager;
	private PartManager partManager;
	private ArrayList<Stage> stages;
	private GameStage gameStage;
	private InputMultiplexer inputs;

	private Skin skin;
	private Table pauseMenu;
	private Table loadScreen;

	private boolean loaded;
	private boolean paused;
	private boolean debug;

	public void init(){
		// Called after the manager has loaded
		gameStage = new GameStage(manager, stages, skin, partManager);
		gameStage.addActor(pauseMenu);
		stages.add(gameStage);
		inputs.addProcessor(gameStage);
	}
	
	@Override
	public void create(){
		ControlSchema.fromFile("spacegame_controls.json");
		manager = new Assets();
		partManager = new PartManager();
		partManager.load("parts/aero.json");
		partManager.load("parts/structural.json");
		partManager.load("parts/thrusters.json");
		stages = new ArrayList<Stage>();

		skin = new Skin(Gdx.files.internal("skins/default/uiskin.json"));
		pauseMenu = new Table();
		loadScreen = new Table();

		loaded = false;
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
					pauseMenu.setDebug(debug);
					return true;

				case Keys.R:
					gameStage.rcs = !gameStage.rcs;
					return true;

				case Keys.T:
					gameStage.sas = !gameStage.sas;
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
		Gdx.input.setInputProcessor(inputs);

		Stage load = new Stage();
		loadScreen.setFillParent(true);
		loadScreen.row().expand().fillX().center();
		Label l = new Label("Space Game 2", skin);
		l.setAlignment(Align.center);
		loadScreen.add(l);
		loadScreen.row().expand().fillX().center().pad(60);
		loadScreen.add(new ProgressBar(0.f, 1.f, 0.1f, false, skin));
		load.addActor(loadScreen);
		stages.add(0, load);

		pauseMenu.setFillParent(true);
		pauseMenu.setDebug(true);
		pauseMenu.add(new Label("PAUSED", skin));
		pauseMenu.row();
		TextButton quitButton = new TextButton("QUIT", skin);
		quitButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor){
				Gdx.app.exit();
			}
		});
		pauseMenu.add(quitButton).width(100).height(40);
		pauseMenu.row();
		TextButton optionsButton = new TextButton("OPTIONS", skin);
		optionsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor){
				stages.add(new OptionsPanel(manager, stages, skin));
			}
		});
		pauseMenu.add(optionsButton).width(100).height(40);
	}

	@Override
	public void resize(int width, int height){
		for(Stage s : stages){
			s.getViewport().update(width, height, true);
		}
	}

	@Override
	public void render(){
		// Prep screen
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.f);

		if(manager.update(17)){
			// Loading completed, start the game
			if(!loaded){
				// Finish loading by removing the loading screen
				loaded = true;
				stages.get(0).dispose();
				stages.remove(0);
				
				this.init();
			}

			pauseMenu.setVisible(paused);
			gameStage.setPaused(paused);
			gameStage.setDebugAll(debug);

			Stage curStage = stages.get(stages.size() - 1);
			curStage.act(Gdx.graphics.getDeltaTime());
			curStage.draw();
		} else {
			// Loading is not complete, show a progress bar
			Stage curStage = stages.get(0);

			((ProgressBar)((Table)curStage.getActors().get(0)).getChild(1)).setValue(manager.getProgress());

			curStage.act(Gdx.graphics.getDeltaTime());
			curStage.draw();
		}
	}
	
	@Override
	public void dispose(){
		for(Stage s : stages){
			s.dispose();
		}

		skin.dispose();
		manager.dispose();
	}
}
