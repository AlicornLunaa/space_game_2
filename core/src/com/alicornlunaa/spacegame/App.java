package com.alicornlunaa.spacegame;

import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.systems.CameraSystem;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.selene_engine.systems.ScriptSystem;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.ILoader;
import com.alicornlunaa.selene_engine.vfx.VfxManager;
import com.alicornlunaa.spacegame.objects.planet.Biome;
import com.alicornlunaa.spacegame.scenes.game_scene.GameplayScene;
import com.alicornlunaa.spacegame.scenes.transitions.LoadingScene;
import com.alicornlunaa.spacegame.systems.CustomRenderSystem;
import com.alicornlunaa.spacegame.systems.OrbitSystem;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.alicornlunaa.spacegame.util.state_management.SaveManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
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
	public VfxManager vfxManager = new VfxManager();
	public Skin skin;

	public LoadingScene loadingScene;
	public GameplayScene gameScene;
	public Screen activeSpaceScreen;

	public Registry registry;
	public PhysicsSystem simulation;
	public OrbitSystem orbitSystem;

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

		manager = new Assets(new ILoader() {
			@Override
			public void loadAssets(Assets manager) {
				manager.load("shaders/star", ShaderProgram.class);
				manager.load("shaders/planet", ShaderProgram.class);
				manager.load("shaders/starfield", ShaderProgram.class);
				manager.load("shaders/atmosphere", ShaderProgram.class);
				manager.load("shaders/cartesian_atmosphere", ShaderProgram.class);
				manager.load("shaders/shadow_map", ShaderProgram.class);
				manager.load("shaders/light", ShaderProgram.class);
				manager.load("textures_packed/textures.atlas", TextureAtlas.class);
				manager.load("particles_packed/particles.atlas", TextureAtlas.class);
				manager.load("effects/rcs", ParticleEffectPool.class);
				manager.load("effects/rocket", ParticleEffectPool.class);
				manager.load("textures/test_image.png", Texture.class);
			}
		});
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
				loadingScene.progressBar.setValue(1);

				atlas = manager.get("textures_packed/textures.atlas", TextureAtlas.class);
				particleAtlas = manager.get("particles_packed/particles.atlas", TextureAtlas.class);
				loaded = true;
				Gdx.app.log("Asset Manager", "Loaded");

				// Get all the particle effects
				Biome.register("Desert", Color.YELLOW, 0.5f, 0.0f, 0.2f, 100, 0.2f);
				Biome.register("Forest", Color.GREEN, 0.4f, 0.4f, 0.2f, 40, 0.8f);
				Biome.register("Grassland", Color.LIME, 0.3f, 0.5f, 0.2f, 40, 0.8f);
				Biome.register("Jungle", Color.OLIVE, 0.62f, 0.5f, 0.3f, 40, 0.8f);
				Biome.register("Mountains", Color.GRAY, 0.0f, 0.0f, 0.5f, 40, 0.8f);
				Biome.register("Ocean", Color.BLUE, 0.0f, 0.0f, 0.0f, 1, 0.05f);
				Biome.register("Tundra", Color.CYAN, 0.0f, 0.0f, 0.2f, 1, 0.05f);
				Gdx.app.log("Biome Manager", "Loaded");

				// Initialize VisUI for dev screens
				VisUI.load();
				FileChooser.setDefaultPrefsName("com.alicornlunaa.spacegame");
				SaveManager.init(this);

				// Start new scene
				registry = new Registry();
				registry.registerSystem(new CameraSystem(this));
				simulation = registry.registerSystem(new PhysicsSystem());
				registry.registerSystem(new CustomRenderSystem(this));
				registry.registerSystem(new ScriptSystem());
				orbitSystem = registry.registerSystem(new OrbitSystem(this));
				
				// SaveManager.load(this, "dev_world");
				// SaveManager.save(this, "dev_world");

				gameScene = new GameplayScene(this);
				gameScene.init();
				this.setScreen(gameScene);

				// this.setScreen(new MapScene(this, spaceScene, player));
				// this.setScreen(new PhysicsEditor(this));
				// this.setScreen(new ShaderScene(this));
				// this.setScreen(new EditorScene(this));
				// this.setScreen(new TestScreen(this));
				// this.setScreen(new OrbitTest(this));
				// this.setScreen(new PlanetEditor(this));
			} else {
				// Loading is not complete, update progress bar
				loadingScene.progressBar.setValue(manager.getProgress());
			}

			return;
		}
	}
	
	@Override
	public void dispose(){
		// spaceScene.dispose();
		loadingScene.dispose();
		manager.dispose();
		VisUI.dispose();
	}
}
