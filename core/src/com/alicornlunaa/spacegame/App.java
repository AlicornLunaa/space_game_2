package com.alicornlunaa.spacegame;

import com.alicornlunaa.spacegame.scenes.EditorScene;
import com.alicornlunaa.spacegame.scenes.GameScene;
import com.alicornlunaa.spacegame.scenes.LoadingScene;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class App extends Game {

	// Variables
	public Assets manager = new Assets();
	public PartManager partManager = new PartManager();
	public Skin skin;

	public LoadingScene loadingScene;
	public GameScene gameScene;
	public EditorScene editorScene;

	public boolean loaded = false;
	
	public ShapeRenderer shapeRenderer;
	
	// Functions
	@Override
	public void create(){
		// Load files and settings
		ControlSchema.fromFile("./saves/settings/spacegame_controls.json");

		manager = new Assets();

		partManager.load("parts/aero.json");
		partManager.load("parts/structural.json");
		partManager.load("parts/thrusters.json");

		skin = new Skin(Gdx.files.internal("skins/default/uiskin.json"));

		shapeRenderer = new ShapeRenderer();

		loadingScene = new LoadingScene(this);
		this.setScreen(loadingScene);
	}

	@Override
	public void render(){
		super.render();

		if(!loaded){
			if(manager.update(17)){
				// Finish loading by removing the loading screen
				((LoadingScene)this.getScreen()).progressBar.setValue(1);

				loaded = true;
				System.out.println("Assets loaded");

				// Start new scene
				gameScene = new GameScene(this);
				editorScene = new EditorScene(this);
				this.setScreen(gameScene);
			} else {
				// Loading is not complete, update progress bar
				((LoadingScene)this.getScreen()).progressBar.setValue(manager.getProgress());
			}

			return;
		}
		
		// Loading completed
	}
	
	@Override
	public void dispose(){
		gameScene.dispose();
		loadingScene.dispose();

		skin.dispose();
		manager.dispose();
	}
}
