package com.alicornlunaa.spacegame;

import com.alicornlunaa.spacegame.engine.phys.Simulation;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Biome;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.scenes.space_scene.SpaceScene;
import com.alicornlunaa.spacegame.scenes.transitions.LoadingScene;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
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

	public Simulation simulation;
	public Universe universe;
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
				Biome.register("Desert", Color.YELLOW, 0.5f, 0.0f, 0.2f, 100, 0.2f);
				Biome.register("Forest", Color.GREEN, 0.4f, 0.4f, 0.2f, 40, 0.8f);
				Biome.register("Grassland", Color.LIME, 0.3f, 0.5f, 0.2f, 40, 0.8f);
				Biome.register("Jungle", Color.OLIVE, 0.62f, 0.5f, 0.3f, 40, 0.8f);
				Biome.register("Mountains", Color.GRAY, 0.0f, 0.0f, 0.5f, 40, 0.8f);
				Biome.register("Ocean", Color.BLUE, 0.0f, 0.0f, 0.0f, 1, 0.05f);
				Biome.register("Tundra", Color.CYAN, 0.0f, 0.0f, 0.2f, 1, 0.05f);

				// Initialize VisUI for dev screens
				VisUI.load();
				FileChooser.setDefaultPrefsName("com.alicornlunaa.spacegame2");

				// Start new scene
				simulation = new Simulation();
				universe = new Universe(this);
				player = new Player(this, universe.getUniversalWorld(), -50, 0, Constants.PPM);
				spaceScene = new SpaceScene(this);
				this.setScreen(spaceScene);
				
				Planet p = ((Planet)universe.getCelestial(3));
				p.addEntityWorld(spaceScene.spacePanel.ship);
				spaceScene.spacePanel.ship.setPosition(500, 3.9f * p.getWorld().getPhysScale());
				spaceScene.spacePanel.ship.setRotation(0);
				// p.addEntityWorld(player);
				// player.setPosition(1, 2.5f * p.getWorld().getPhysScale());
				// this.setScreen(new PlanetScene(this, p));

				// this.setScreen(new MapScene(this, spaceScene, player));
				// this.setScreen(new PhysicsEditor(this));
				// this.setScreen(new ShaderScene(this));
				// this.setScreen(new TestScreen(this));
				// this.setScreen(new OrbitTest(this));
				// this.setScreen(new PlanetEditor(this));
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
