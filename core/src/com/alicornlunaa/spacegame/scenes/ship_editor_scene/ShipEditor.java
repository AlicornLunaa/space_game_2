package com.alicornlunaa.spacegame.scenes.ship_editor_scene;

import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.stripe.scenecomposer.SceneComposerStageBuilder;

public class ShipEditor extends BaseScene {
    // Variables
    private Stage root;
    private Stage userInterface;
    
    // Private functions
    private void initializeRoot(){
        root = new Stage(new ScreenViewport());
        root.setDebugAll(true);
        inputs.addProcessor(root);
    }

    private void initializeInterface(){
        SceneComposerStageBuilder builder = new SceneComposerStageBuilder();
        userInterface = new Stage(new ScreenViewport());
        userInterface.setDebugAll(true);
        builder.build(userInterface, game.skin, Gdx.files.internal("layouts/editor_hud.json"));
    }

    private void initializeControls(){
        inputs.setProcessors(userInterface, root);
    }

    // Constructor
    public ShipEditor(App game) {
        super(game);
        initializeInterface();
        initializeRoot();
        initializeControls();
    }
    
    // Functions
    @Override
    public void render(float delta) {
        super.render(delta);

        root.getViewport().apply();
        root.act(delta);
        root.draw();

        userInterface.getViewport().apply();
        userInterface.act(delta);
        userInterface.draw();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void dispose() {}
}
