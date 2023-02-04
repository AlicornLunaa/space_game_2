package com.alicornlunaa.spacegame;

import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.scenes.SpaceScene.SpaceScene;
import com.alicornlunaa.spacegame.scenes.Transitions.LoadingScene;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;

public class App extends Game {

	// Variables
	public Assets manager;
	public PartManager partManager = new PartManager();
	public Skin skin;

	public LoadingScene loadingScene;
	public SpaceScene spaceScene;
	public Player player;

	public boolean loaded = false;
	
	public TextureAtlas atlas;
	public TextureAtlas particleAtlas;
	public SpriteBatch spriteBatch;
	public ShapeRenderer shapeRenderer;
	public Box2DDebugRenderer debug;
	
	// Functions
	@Override
	public void create(){
		// Load files and settings
		ControlSchema.fromFile("./saves/settings/controls.json");

		manager = new Assets();
		skin = manager.get("skins/spacecadet/spacecadet.json");

		partManager.load("parts/aero.json");
		partManager.load("parts/structural.json");
		partManager.load("parts/thrusters.json");
		partManager.load("parts/rcsport.json");

		spriteBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		debug = new Box2DDebugRenderer();
		
        ShaderProgram.pedantic = false;

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

				// Initialize VisUI for dev screens
				VisUI.load();
				FileChooser.setDefaultPrefsName("com.alicornlunaa.spacegame2");

				// Start new scene
				player = new Player(this, -50, 0, Constants.PPM);
				spaceScene = new SpaceScene(this);
				this.setScreen(spaceScene);
				// this.setScreen(new MapScene(this, spaceScene, player));
				// this.setScreen(new PhysicsEditor(this));
				// this.setScreen(new ShaderScene(this));
				// this.setScreen(new TestScreen(this));
				// this.setScreen(new OrbitTest(this));
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
		// spaceScene.dispose();
		loadingScene.dispose();
		manager.dispose();
		VisUI.dispose();
	}
}
