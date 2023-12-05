package com.alicornlunaa.space_game.scenes;

import com.alicornlunaa.space_game.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Loading scene is the scene used to show the rendering bar
 * while the game loads items.
 */

public class LoadingScene implements Screen {
    // Variables
    public Stage stage;
    public ProgressBar progressBar;

    // Constructor
    public LoadingScene(){
        // Initialize stage graphics
        stage = new Stage(new ScreenViewport());

        Table tbl = new Table(App.instance.skin);
        tbl.setFillParent(true);
        tbl.row().expand().fillX().center();

        Label lbl = new Label("Space Game 2", App.instance.skin);
        lbl.setStyle(App.instance.skin.get("title", LabelStyle.class));
        lbl.setAlignment(Align.center);
        tbl.add(lbl);

        tbl.row().expand().fillX().center().pad(60);
        progressBar = new ProgressBar(0.0f, 1.0f, 0.1f, false, App.instance.skin);
        tbl.add(progressBar);

        stage.addActor(tbl);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
