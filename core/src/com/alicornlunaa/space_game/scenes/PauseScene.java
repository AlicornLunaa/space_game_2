package com.alicornlunaa.space_game.scenes;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.util.ControlSchema;
import com.alicornlunaa.space_game.util.HexColor;
import com.alicornlunaa.space_game.widgets.ColorWidget;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Pause screen is the window shown when the game gets paused
 */

public class PauseScene implements Screen {
    // Variables
    final App game;

    private FrameBuffer fbo;
    private Texture texture;
    private TextureRegion region;

    public Stage stage;

    // Constructor
    public PauseScene(final App game, int width, int height){
        this.game = game;
        final Screen previousScreen = game.getScreen();

        // Initialize framebuffer
        fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
        fbo.begin();
        previousScreen.render(0);
        fbo.end();

        texture = fbo.getColorBufferTexture();
        region = new TextureRegion(texture);
        region.flip(false, true);
        
        // Initialize stage graphics
        stage = new Stage(new ScreenViewport());

        Table tbl = new Table(game.skin);
        tbl.setFillParent(true);
        tbl.row().expandX().fillX().center().maxWidth(240);

        Label lbl = new Label("Space Game 2", game.skin);
        lbl.setAlignment(Align.center);
        tbl.add(new ColorWidget(lbl, new HexColor("#7681B3")));
        
        tbl.row().expandX().fillX().center().maxWidth(240);
        TextButton optBtn = new TextButton("Options", game.skin);
        optBtn.setColor(new HexColor("#D9F0FF"));
        optBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // game.setScreen(new OptionsScene(app, previousScreen, int width, int height));
            }
        });
        tbl.add(optBtn);
        
        tbl.row().expandX().fillX().center().maxWidth(240);
        TextButton clsBtn = new TextButton("Quit", game.skin);
        clsBtn.setColor(new HexColor("#D9F0FF"));
        clsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        tbl.add(clsBtn);

        stage.addActor(tbl);

        // Inputs
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.PAUSE_GAME){
                    game.setScreen(previousScreen);
                    return true;
                }

                return false;
            }
        });
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        Batch batch = stage.getBatch();
        batch.begin();
        batch.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        batch.draw(region, 0, 0);
        batch.end();
        
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
        texture.dispose();
        fbo.dispose();
    }
}
