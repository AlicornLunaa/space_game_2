package com.alicornlunaa.spacegame.scenes.Misc;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.alicornlunaa.spacegame.objects.Simulation.Star;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ConsoleScene implements Screen {

    // Variables
    final App game;

    private FrameBuffer fbo;
    private Texture texture;
    private TextureRegion region;

    public Stage stage;
    public TextField cmdLine;
    
    // Private functions
    private void handleCmd(String cmd){
        String[] args = cmd.split("\\s+");
        
        if(args[0].equals("load_ship")){
            game.spaceScene.spacePanel.ship.load(args[1]);
        } else if(args[0].equals("set_pos")){
            game.spaceScene.spacePanel.ship.setPosition(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else if(args[0].equals("set_rot")){
            game.spaceScene.spacePanel.ship.setRotation(Integer.parseInt(args[1]));
        } else if(args[0].equals("orbit")){
            game.spaceScene.spacePanel.ship.setPosition(Integer.parseInt(args[1]), 0);
            game.spaceScene.spacePanel.universe.createEntityOrbit(game.spaceScene.spacePanel.ship);
        } else if(args[0].equals("set_timewarp")){
            game.spaceScene.spacePanel.universe.setTimewarp(Float.parseFloat(args[1]));
        } else if(args[0].equals("reload_shaders")){
            ((Star)game.spaceScene.spacePanel.universe.getCelestial(0)).reloadShaders();
        }
    }

    // Constructor
    public ConsoleScene(final App game, int width, int height){
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
        tbl.row().expandX().fillX();

        // TextArea text = new TextArea("CONSOLE LOG HERE", game.skin);
        // text.setDisabled(true);
        // tbl.add(text).colspan(15).bottom().minHeight(320).expand();

        tbl.row().expand().fillX().maxHeight(32);
        cmdLine = new TextField("COMMAND", game.skin);
        tbl.add(cmdLine).bottom().colspan(14);

        TextButton sendCmdBtn = new TextButton("Execute", game.skin);
        tbl.add(sendCmdBtn).bottom().colspan(1);
        stage.addActor(tbl);

        stage.setKeyboardFocus(cmdLine);

        // Inputs
        sendCmdBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                TextField field = ((ConsoleScene)game.getScreen()).cmdLine;
                handleCmd(field.getText());
                field.setText("");
            }
        });

        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.CONSOLE_OPEN){
                    game.setScreen(previousScreen);
                    return true;
                } else if(keycode == Keys.ENTER){
                    TextField field = ((ConsoleScene)game.getScreen()).cmdLine;
                    handleCmd(field.getText());
                    field.setText("");
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
