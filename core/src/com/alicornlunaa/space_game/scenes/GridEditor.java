package com.alicornlunaa.space_game.scenes;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.PhysicsSystem;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.Grid;
import com.alicornlunaa.space_game.grid.Grid.GridIterator;
import com.alicornlunaa.space_game.grid.TileManager.PickableTile;
import com.alicornlunaa.space_game.grid.TileManager.TileCategory;
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

    private VerticalGroup partsGroup;
    private TileCategory selectedGategory = TileCategory.CONSTRUCTION;
    private @Null PickableTile selectedTile = null;

    // Constructor
    public GridEditor(){
        super();
        initInterface();
        initControls();
        initEngine();

        gridEntity.add(new TransformComponent());
        gridEntity.add(new BodyComponent());
        testGrid.setTile(1, 0, new CustomTile(1));
        testGrid.setTile(0, 1, new SolidTile(Element.STEEL));
        testGrid.setTile(0, 0, new SolidTile(Element.STEEL));
        testGrid.setTile(0, -1, new SolidTile(Element.STEEL));
        testGrid.setTile(0, -2, new SolidTile(Element.STEEL));
        testGrid.setTile(0, -3, new SolidTile(Element.STEEL));
        testGrid.setTile(0, -4, new SolidTile(Element.STEEL));
        testGrid.setTile(3, 1, new SolidTile(Element.STEEL));
        testGrid.setTile(3, 0, new SolidTile(Element.STEEL));
        testGrid.setTile(3, -1, new SolidTile(Element.STEEL));
        testGrid.setTile(3, -2, new SolidTile(Element.STEEL));
        testGrid.setTile(3, -3, new SolidTile(Element.STEEL));
        testGrid.setTile(3, -4, new SolidTile(Element.STEEL));
        testGrid.assemble(gridEntity.getComponent(BodyComponent.class));
        engine.addEntity(gridEntity);

        inputs.addProcessor(0, mInterface);
        previousScreen = App.instance.getScreen();
        App.instance.camera = editorCamera;
    }

    // Functions
    private void selectCategory(TileCategory category){
        selectedGategory = category;
        partsGroup.clear();

        for(final PickableTile tile : App.instance.tileManager.getTilesInCategory(category)){
            // TextureRegionDrawable texture = new TextureRegionDrawable(game.atlas.findRegion("parts/" + partID.toLowerCase()));
            // texture.setMinSize(64 * ((float)texture.getRegion().getRegionWidth() / (float)texture.getRegion().getRegionHeight()), 64);

            TextButton btn = new TextButton(tile.tile.tileID, App.instance.skin);
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent e, Actor a){
                    selectedTile = tile;
                }
            });
            partsGroup.addActor(btn);
        }
    }

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
        partsGroup = new VerticalGroup();
        partsTbl.add(categories).expand().fill();
        partsTbl.add(partsGroup).expand().fill();

        // Split pane creation
        VisSplitPane splitPane = new VisSplitPane(partsTbl, new Table(), false);
        splitPane.setName("editor_pane");
        splitPane.setSplitAmount(0.3f);
        root.add(splitPane).expand().fill().row();

        // Populate parts
        for(final TileCategory entry : App.instance.tileManager.getTileMap().keySet()){
            TextButton btn = new TextButton(entry.name, skin);

            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    selectCategory(entry);
                }
            });

            categories.addActor(btn);
        }
    }

    private void initControls(){
        inputs.addProcessor(new InputAdapter(){
            Vector3 rawPos = new Vector3();

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                switch(button){
                    case Buttons.LEFT:
                        if(selectedTile != null){
                            testGrid.setTile(currentCell.x, currentCell.y, selectedTile.spawn());
                            testGrid.assemble(gridEntity.getComponent(BodyComponent.class));
                        }
                        return true;
                        
                    case Buttons.RIGHT:
                        testGrid.removeTile(currentCell.x, currentCell.y);
                        testGrid.assemble(gridEntity.getComponent(BodyComponent.class));
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
                    case Keys.R:
                        if(selectedTile != null)
                            selectedTile.tile.rotation = Math.floorMod(selectedTile.tile.rotation + (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ? -1 : 1), 4);
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

        // Cursor
        if(selectedTile != null){
            int placeWidth = 0;
            batch.set(ShapeType.Line);
            batch.setColor(testGrid.isOccupied(currentCell.x, currentCell.y) ? Color.RED : Color.CYAN);
            batch.rect(
                currentCell.x * Constants.TILE_SIZE - Constants.TILE_SIZE * placeWidth,
                currentCell.y * Constants.TILE_SIZE - Constants.TILE_SIZE * placeWidth,
                Constants.TILE_SIZE * placeWidth * 2 + Constants.TILE_SIZE,
                Constants.TILE_SIZE * placeWidth * 2 + Constants.TILE_SIZE
            );
        }

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

        if(selectedTile != null){
            selectedTile.tile.x = currentCell.x;
            selectedTile.tile.y = currentCell.y;
            selectedTile.tile.render(spriteBatch, delta);
        }

        spriteBatch.end();


        mInterface.act();
        mInterface.draw();
    }

    @Override
    public void resize(int width, int height) {
        mInterface.getViewport().update(width, height, true);
    }
}
