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
import com.ray3k.stripe.FreeTypeSkin;

public class App extends Game {

	// Variables
	public Assets manager = new Assets();
	public PartManager partManager = new PartManager();
	public Skin skin;

	public LoadingScene loadingScene;
	public SpaceScene spaceScene;
	public PlanetScene planetScene;
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
		skin = new FreeTypeSkin(Gdx.files.internal("skins/default/uiskin.json"));

		partManager.load("parts/aero.json");
		partManager.load("parts/structural.json");
		partManager.load("parts/thrusters.json");
		partManager.load("parts/rcsport.json");

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

				skin = manager.get("skins/spacecadet/spacecadet.json");
				atlas = manager.get("textures_packed/textures.atlas", TextureAtlas.class);
				particleAtlas = manager.get("particles_packed/particles.atlas", TextureAtlas.class);
				loaded = true;
				System.out.println("Assets loaded");

				// Get all the particle effects
				manager.initEffects(this);

				// Start new scene
				spaceScene = new SpaceScene(this);
				planetScene = new PlanetScene(this, spaceScene.spacePanel.planet, spaceScene.spacePanel.player);
				editorScene = new EditorScene(this);
				this.setScreen(spaceScene);
            	// spaceScene.spacePanel.planet.addEntity(spaceScene.spacePanel.ship);
				// this.setScreen(planetScene);
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
		spaceScene.dispose();
		loadingScene.dispose();
		manager.dispose();
	}
}
