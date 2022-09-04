package com.alicornlunaa.spacegame;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.panels.GameStage;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;

public class App extends ApplicationAdapter {
	private ArrayList<Stage> stages;
	private GameStage gameStage;
	private InputMultiplexer inputs;

	// https://libgdx.com/wiki/graphics/2d/scene2d/table
	private Skin skin;
	private Table pauseMenu;

	private boolean paused;
	private boolean debug;
	
	@Override
	public void create(){
		ControlSchema.fromFile("spacegame_controls.json");

		skin = new Skin(Gdx.files.internal("skins/default/skin/uiskin.json"));
		pauseMenu = new Table();

		stages = new ArrayList<Stage>();
		gameStage = new GameStage(stages, skin);
		stages.add(gameStage);

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
		inputs.addProcessor(gameStage);
		Gdx.input.setInputProcessor(inputs);

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
	}

	@Override
	public void resize(int width, int height){
		for(Stage s : stages){
			s.getViewport().update(width, height, true);
		}
	}

	@Override
	public void render(){
		float delta = Gdx.graphics.getDeltaTime();

		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.f);

		pauseMenu.setVisible(paused);
		gameStage.setPaused(paused);
		gameStage.setDebugAll(debug);

		Stage curStage = stages.get(stages.size() - 1);
		curStage.act(delta);
		curStage.draw();
	}
	
	@Override
	public void dispose(){
		for(Stage s : stages){
			s.dispose();
		}

		skin.dispose();
	}
}
