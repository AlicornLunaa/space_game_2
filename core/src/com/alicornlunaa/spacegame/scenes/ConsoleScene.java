package com.alicornlunaa.spacegame.scenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ConsoleScene implements Screen {

    // Variables
    final App game;

    private FrameBuffer fbo;
    private Texture texture;
    private TextureRegion region;

    public Stage stage;

    // private static Label[] console_log = new Label[12]; // Circular buffer
    // private static int console_log_length = 0;

    // // Static functions
    // static private void addLog(App game, Table tbl, String txt){
    //     if(console_log_length < console_log.length){
    //         // Append like normal
    //         console_log[console_log_length] = new Label(txt, game.skin);
    //         tbl.add(console_log[console_log_length]);
    //         console_log_length++;
    //         return;
    //     }
    // }

    // Constructor
    public ConsoleScene(final App game, final Screen previousScreen, int width, int height){
        this.game = game;

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
        tbl.row().expand().fillX().bottom().maxHeight(640);

        // TODO: Add console history here with text area

        tbl.row().expand().fillX().bottom().maxHeight(640);
        TextField cmdLine = new TextField("COMMAND", game.skin);
        tbl.add(cmdLine).bottom().colspan(14);

        TextButton sendCmdBtn = new TextButton("Execute", game.skin);
        tbl.add(sendCmdBtn).bottom().colspan(1);

        stage.addActor(tbl);

        // Inputs
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.CONSOLE_OPEN){
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
    }
    
}