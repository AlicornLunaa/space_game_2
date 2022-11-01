package com.alicornlunaa.spacegame;

import com.alicornlunaa.spacegame.scenes.LoadingScene;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class App extends Game {

	// Variables
	public Assets manager = new Assets();
	public PartManager partManager = new PartManager();
	public Skin skin;

	public boolean loaded = false;
	
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

		this.setScreen(new LoadingScene(this));
	}

	@Override
	public void render(){
		super.render();

		if(manager.update(17)){
			// Loading completed, start the game
			if(!loaded){
				// Finish loading by removing the loading screen
				loaded = true;
				((LoadingScene)this.getScreen()).progressBar.setValue(1);
				System.out.println("Assets loaded");
			}
		} else {
			// Loading is not complete, update progress bar
			((LoadingScene)this.getScreen()).progressBar.setValue(manager.getProgress());
		}
	}
	
	@Override
	public void dispose(){
		skin.dispose();
		manager.dispose();
	}
}
