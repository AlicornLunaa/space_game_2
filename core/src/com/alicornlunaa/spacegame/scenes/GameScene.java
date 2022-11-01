package com.alicornlunaa.spacegame.scenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.panels.GamePanel;
import com.alicornlunaa.spacegame.panels.GameUIPanel;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScene implements Screen {

    // Variables
    final App game;

    public GamePanel gamePanel;
    public GameUIPanel uiPanel;

    // Constructor
    public GameScene(final App game){
        this.game = game;
        
        gamePanel = new GamePanel(game);
        uiPanel = new GameUIPanel(game);
        
        gamePanel.setDebugAll(true);
        uiPanel.setDebugAll(true);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        gamePanel.act(delta);
        uiPanel.act(delta);

        gamePanel.draw();
        uiPanel.draw();
    }

    @Override
    public void resize(int width, int height) {
        gamePanel.getViewport().update(width, height, true);
        uiPanel.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        gamePanel.dispose();
        uiPanel.dispose();
    }
    
}
