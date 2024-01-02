package com.alicornlunaa.space_game.scenes;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.PhysicsSystem;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.Grid;
import com.alicornlunaa.space_game.grid.Grid.GridIterator;
import com.alicornlunaa.space_game.grid.entities.CustomTile;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.grid.tiles.Element;
import com.alicornlunaa.space_game.grid.tiles.SolidTile;
import com.alicornlunaa.space_game.util.Constants;
import com.alicornlunaa.space_game.util.Vector2i;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
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

    private Entity gridEntity = new Entity();
    
    private OrthographicCamera editorCamera = new OrthographicCamera(1280 / Constants.PPM, 720 / Constants.PPM);
    private ShapeRenderer batch = App.instance.shapeRenderer;
    private Batch spriteBatch = App.instance.spriteBatch;
    private Vector2 panningVector = null;
    private Vector2i currentCell = new Vector2i();
    private Grid testGrid = new Grid();

    // Constructor
    public GridEditor(){
        super();
        initInterface();
        initControls();
        initEngine();

        gridEntity.add(new TransformComponent());
        gridEntity.add(new BodyComponent());
        testGrid.setTile(0, 0, new CustomTile(0));
        testGrid.assemble(gridEntity.getComponent(BodyComponent.class));
        engine.addEntity(gridEntity);

        inputs.addProcessor(0, mInterface);
        previousScreen = App.instance.getScreen();
        App.instance.camera = editorCamera;
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
                switch(button){
                    case Buttons.LEFT:
                        testGrid.setTile(currentCell.x, currentCell.y, new SolidTile(Element.STEEL, App.instance.atlas.findRegion("tiles/steel")));
                        return true;
                        
                    case Buttons.RIGHT:
                        testGrid.removeTile(currentCell.x, currentCell.y);
                        return true;

                    case Buttons.MIDDLE:
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
                switch (keycode) {
                    case Keys.SHIFT_LEFT:
                        testGrid.assemble(gridEntity.getComponent(BodyComponent.class));
                        break;
                
                    default:
                        break;
                }

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
    
    private void initEngine(){
        engine.addSystem(new PhysicsSystem(Constants.PPM));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        engine.update(delta);

        batch.setProjectionMatrix(editorCamera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.setAutoShapeType(true);
        batch.begin();

        // Grid
        // batch.set(ShapeType.Line);
        // for(int x = -20; x <= 20; x++) for(int y = -15; y <= 15; y++){
        //     batch.setColor((x == 0 && y == 0) ? Color.WHITE : Color.LIGHT_GRAY);
        //     batch.rect(
        //         x * Constants.TILE_SIZE,
        //         y * Constants.TILE_SIZE,
        //         Constants.TILE_SIZE,
        //         Constants.TILE_SIZE
        //     );
        // }

        // Cursor
        int placeWidth = 0;
        batch.set(ShapeType.Line);
        batch.setColor(testGrid.isOccupied(currentCell.x, currentCell.y) ? Color.RED : Color.CYAN);
        batch.rect(
            currentCell.x * Constants.TILE_SIZE - Constants.TILE_SIZE * placeWidth,
            currentCell.y * Constants.TILE_SIZE - Constants.TILE_SIZE * placeWidth,
            Constants.TILE_SIZE * placeWidth * 2 + Constants.TILE_SIZE,
            Constants.TILE_SIZE * placeWidth * 2 + Constants.TILE_SIZE
        );

        batch.end();

        
        spriteBatch.setProjectionMatrix(editorCamera.combined);
        spriteBatch.setTransformMatrix(new Matrix4());
        spriteBatch.begin();

        testGrid.iterate(new GridIterator() {
            @Override
            public void iterate(AbstractTile tile) {
                tile.render(spriteBatch, Gdx.graphics.getDeltaTime());
            }
        });

        spriteBatch.end();


        mInterface.act();
        mInterface.draw();
    }

    @Override
    public void resize(int width, int height) {
        mInterface.getViewport().update(width, height, true);
    }
}
