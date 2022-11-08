package com.alicornlunaa.spacegame.scenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.panels.ShipEditorPanel;
import com.alicornlunaa.spacegame.panels.ShipEditorUIPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class EditorScene implements Screen {

    // Variables
    final App game;

    public ShipEditorPanel editorPanel;
    public ShipEditorUIPanel uiPanel;

    private InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public EditorScene(final App game){
        this.game = game;
        
        editorPanel = new ShipEditorPanel(game);
        uiPanel = new ShipEditorUIPanel(game);

        inputs.addProcessor(uiPanel);
        inputs.addProcessor(editorPanel);
        
        // editorPanel.setDebugAll(true);
        // uiPanel.setDebugAll(true);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        editorPanel.act(delta);
        uiPanel.act(delta);

        editorPanel.draw();
        uiPanel.draw();
    }

    @Override
    public void resize(int width, int height) {
        editorPanel.getViewport().update(width, height, true);
        uiPanel.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        editorPanel.dispose();
        uiPanel.dispose();
    }
    
}
