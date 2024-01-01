package com.alicornlunaa.space_game.scenes;

import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.Grid;
import com.alicornlunaa.space_game.util.Constants;
import com.alicornlunaa.space_game.util.Vector2i;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisSplitPane;

public class GridEditor extends BaseScene {
    // Variables
    private @Null Screen previousScreen = null;
    private Engine engine = new Engine();
    private Stage mInterface;

    private OrthographicCamera editorCamera = new OrthographicCamera(1280 / Constants.PPM, 720 / Constants.PPM);
    private ShapeRenderer batch = App.instance.shapeRenderer;
    private Vector2 panningVector = null;
    private Vector2i currentCell = new Vector2i();
    private Grid testGrid = new Grid();

    // Constructor
    public GridEditor(){
        super();
        initInterface();
        initControls();

        inputs.addProcessor(0, mInterface);
        previousScreen = App.instance.getScreen();
    }

    // Functions
    private void initInterface(){
        // Create the gui
        Skin skin = App.instance.skin;

        mInterface = new Stage(new ScreenViewport());
        mInterface.setDebugAll(Constants.DEBUG);

        Table root = new Table(skin);
        root.setFillParent(true);
        mInterface.addActor(root);

        // Top bar controls
        Table topBarTbl = new Table();
        root.add(topBarTbl).expandX().fillX().row();

        TextField nameTextField = new TextField("Ship Name Here", skin);
        nameTextField.setName("ship_name");
        topBarTbl.add(nameTextField).expand().fill().left().pad(10, 10, 10, 300);

        TextButton saveBtn = new TextButton("Save", skin);
        saveBtn.setColor(Color.BLUE);
        saveBtn.addListener(new ChangeListener(){
            TextField nameTextField = mInterface.getRoot().findActor("ship_name");
            
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Save file to ./saves/ships/name.ship
                if(nameTextField.getText().length() > 0){
                }
            }
        });
        topBarTbl.add(saveBtn).right().pad(10);

        TextButton loadBtn = new TextButton("Load", skin);
        loadBtn.setColor(Color.BLUE);
        loadBtn.addListener(new ChangeListener(){
            TextField nameTextField = mInterface.getRoot().findActor("ship_name");
            
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Save file to ./saves/ships/name.ship
                if(nameTextField.getText().length() > 0){
                }
            }
        });
        topBarTbl.add(loadBtn).right().pad(10);

        TextButton quitBtn = new TextButton("Quit", skin);
        quitBtn.setColor(Color.RED);
        quitBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                App.instance.setScreen(previousScreen);
            }
        });
        topBarTbl.add(quitBtn).right().pad(10);

        // Parts tab
        Table partsTbl = new Table(skin);

        VerticalGroup categories = new VerticalGroup();
        VerticalGroup parts = new VerticalGroup();
        partsTbl.add(categories).expand().fill();
        partsTbl.add(parts).expand().fill();
        // populateCategories();

        // Split pane creation
        VisSplitPane splitPane = new VisSplitPane(partsTbl, new Table(), false);
        splitPane.setName("editor_pane");
        splitPane.setSplitAmount(0.3f);
        root.add(splitPane).expand().fill().row();
    }

    private void initControls(){
        inputs.addProcessor(new InputAdapter(){
            Vector3 rawPos = new Vector3();

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(button == Buttons.MIDDLE){
                    panningVector = new Vector2(screenX, screenY);
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(panningVector != null){
                    panningVector = null;
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(panningVector != null){
                    panningVector.sub(screenX, screenY).scl(editorCamera.zoom / 128.f);
                    editorCamera.position.add(panningVector.x, -panningVector.y, 0);
                    editorCamera.update();
                    panningVector.set(screenX, screenY);
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                editorCamera.zoom = Math.min(Math.max(editorCamera.zoom + amountY * 0.05f, 0.01f), 50);
                editorCamera.update();
                return true;
            }

            @Override
            public boolean mouseMoved (int screenX, int screenY) {
                rawPos.set(screenX, screenY, 0);
                rawPos.set(editorCamera.unproject(rawPos));
                rawPos.set((int)(rawPos.x / Constants.TILE_SIZE - (rawPos.x < 0 ? 1 : 0)), (int)(rawPos.y / Constants.TILE_SIZE - (rawPos.y < 0 ? 1 : 0)), 0);
                currentCell.set((int)rawPos.x, (int)rawPos.y);
                return false;
            }
        });
    }
    
    @Override
    public void render(float delta) {
        super.render(delta);
        engine.update(delta);
        mInterface.act();
        mInterface.draw();

        batch.setProjectionMatrix(editorCamera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.setAutoShapeType(true);
        batch.begin();

        batch.circle(0, 0, 1, 50);

        // Cursor
        int placeWidth = 0;
        batch.set(ShapeType.Line);
        batch.setColor(Color.CYAN);
        batch.rect(
            currentCell.x * Constants.TILE_SIZE - Constants.TILE_SIZE * placeWidth,
            currentCell.y * Constants.TILE_SIZE - Constants.TILE_SIZE * placeWidth,
            Constants.TILE_SIZE * placeWidth * 2 + Constants.TILE_SIZE,
            Constants.TILE_SIZE * placeWidth * 2 + Constants.TILE_SIZE
        );

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        mInterface.getViewport().update(width, height, true);
    }
}
