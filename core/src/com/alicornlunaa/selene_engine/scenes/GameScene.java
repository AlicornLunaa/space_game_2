package com.alicornlunaa.selene_engine.scenes;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class GameScene extends BaseScene {
    // Variables
    private Engine engine;
    private Stage userInterface;

    // Constructor
    public GameScene(){
        // Initialize stuff
        engine = new Engine();
        userInterface = new Stage(new ScreenViewport());
    }

    // Functions
    public Engine getEngine(){
        return engine;
    }

    public Stage getInterface(){
        return userInterface;
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        engine.update(deltaTime);
        userInterface.act(deltaTime);
        userInterface.draw();
    }

    @Override
    public void resize(int width, int height) {
        userInterface.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        userInterface.dispose();
    }
}
