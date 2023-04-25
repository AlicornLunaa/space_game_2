package com.alicornlunaa.spacegame.engine.scenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class GameScene<C extends Stage, U extends Stage> extends BaseScene {

    // Variables
    private C content;
    private U ui;

    // Constructor
    public GameScene(App game, C content, U ui) {
        super(game);

        this.content = content;
        this.ui = ui;

        inputs.addProcessor(ui);
        inputs.addProcessor(content);

        ui.setDebugAll(Constants.DEBUG);
        content.setDebugAll(Constants.DEBUG);
    }

    // Functions
    public C getContent(){ return content; }
    public U getUI(){ return ui; }

    @Override
    public void render(float delta) {
        super.render(delta);

        ui.act(delta);
        content.act(delta);

        content.draw();
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        content.getViewport().update(width, height, true);
        ui.getViewport().update(width, height, true);
    }

    @Override
    public void dispose(){
        content.dispose();
        ui.dispose();
    }
    
}
