package com.alicornlunaa.spacegame;

import com.alicornlunaa.spacegame.scenes.EditorScene;
import com.alicornlunaa.spacegame.scenes.SpaceScene;
import com.alicornlunaa.spacegame.scenes.LoadingScene;
import com.alicornlunaa.spacegame.scenes.PlanetScene;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class App extends Game {

	// Variables
	public Assets manager = new Assets();
	public PartManager partManager = new PartManager();
	public Skin skin;

	public LoadingScene loadingScene;
	public SpaceScene gameScene;
	public EditorScene editorScene;

	public boolean loaded = false;
	
	public TextureAtlas atlas;
	public TextureAtlas particleAtlas;
	public SpriteBatch spriteBatch;
	public ShapeRenderer shapeRenderer;
	
	// Functions
	@Override
	public void create(){
		// Load files and settings
		ControlSchema.fromFile("./saves/settings/controls.json");

		manager = new Assets();

		partManager.load("parts/aero.json");
		partManager.load("parts/structural.json");
		partManager.load("parts/thrusters.json");
		partManager.load("parts/rcsport.json");

		skin = new Skin(Gdx.files.internal("skins/default/uiskin.json"));

		spriteBatch = new SpriteBatch();
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

				atlas = manager.get("textures_packed/textures.atlas", TextureAtlas.class);
				particleAtlas = manager.get("particles_packed/particles.atlas", TextureAtlas.class);
				loaded = true;
				System.out.println("Assets loaded");

				// Get all the particle effects
				manager.initEffects(this);

				// Start new scene
				gameScene = new SpaceScene(this);
				editorScene = new EditorScene(this);
				this.setScreen(gameScene);
				// this.setScreen(new PlanetScene(this));
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
