package com.alicornlunaa.space_game;

import com.alicornlunaa.selene_engine.scenes.GameScene;
import com.alicornlunaa.selene_engine.util.asset_manager.AsepriteSheet;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets.ILoader;
import com.alicornlunaa.selene_engine.vfx.VfxManager;
import com.alicornlunaa.space_game.grid.TileManager;
import com.alicornlunaa.space_game.scenes.GridEditor;
import com.alicornlunaa.space_game.scenes.LoadingScene;
import com.alicornlunaa.space_game.scenes.space_scene.SpaceScene;
import com.alicornlunaa.space_game.util.ControlSchema;
import com.alicornlunaa.space_game.util.PartManager;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Null;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;

public class App extends Game {
	// Static variable
	public static App instance;

	// Variables
	public Assets manager;
	public PartManager partManager = new PartManager();
	public TileManager tileManager;
	public VfxManager vfxManager = new VfxManager();
	public Skin skin;

	public LoadingScene loadingScene;
	public OrthographicCamera camera;
	public Entity playerEntity;

	public boolean loaded = false;
	
	public TextureAtlas atlas;
	public TextureAtlas particleAtlas;
	public SpriteBatch spriteBatch;
	public ShapeRenderer shapeRenderer;
	public Box2DDebugRenderer debug;

	// Functions
	public @Null GameScene getScene(){
		if(getScreen() instanceof GameScene)
			return (GameScene)getScreen();

		return null;
	}

	@Override
	public void create(){
		// Load files and settings
		ControlSchema.fromFile("./saves/settings/controls.json");

		manager = new Assets(new ILoader() {
			@Override
			public void loadAssets(Assets manager) {
				manager.load("shaders/star", ShaderProgram.class);
				manager.load("shaders/planet", ShaderProgram.class);
				manager.load("shaders/terrain", ShaderProgram.class);
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
				manager.load("textures/dev_texture.png", Texture.class);
				manager.load("textures/ui/categories.json", AsepriteSheet.class);
				manager.load("textures/ui/buttons.json", AsepriteSheet.class);
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
		
		App.instance = this;
        ShaderProgram.pedantic = false;

		loadingScene = new LoadingScene();
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
				tileManager = new TileManager();
				loaded = true;
				Gdx.app.log("Asset Manager", "Loaded");

				// Initialize VisUI for development screens
				VisUI.load();
				FileChooser.setDefaultPrefsName("com.alicornlunaa.spacegame");

				this.setScreen(new SpaceScene());
				// this.setScreen(new GridEditor());
			} else {
				// Loading is not complete, update progress bar
				loadingScene.progressBar.setValue(manager.getProgress());
			}

			return;
		}
	}
	
	@Override
	public void dispose(){
		loadingScene.dispose();
		manager.dispose();
		VisUI.dispose();
		App.instance = null;
	}
}
