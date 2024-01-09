package com.alicornlunaa.space_game.scenes;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.PhysicsSystem;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.selene_engine.util.AutoScrollPane;
import com.alicornlunaa.selene_engine.util.asset_manager.AsepriteSheet;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.grid.Grid;
import com.alicornlunaa.space_game.grid.Grid.GridIterator;
import com.alicornlunaa.space_game.grid.TileManager.PickableTile;
import com.alicornlunaa.space_game.grid.TileManager.TileCategory;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.util.Constants;
import com.alicornlunaa.space_game.util.Vector2i;
import com.alicornlunaa.space_game.widgets.HoverLabel;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisSplitPane;

public class GridEditor extends BaseScene {
    // Inner classes
    private static class DrawableTile implements Drawable {
        // Variables
        private final PickableTile tile;
        private Matrix4 matrix = new Matrix4();

        // Constructor
        public DrawableTile(PickableTile tile){
            this.tile = tile;
        }

        // Functions
        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
            float aspect = 1.f / Math.max(tile.tile.width, tile.tile.height);
            int beforeX = tile.tile.x;
            int beforeY = tile.tile.y;
            int beforeR = tile.tile.rotation;

            matrix.idt();
            matrix.translate(x + width, y, 0);
            matrix.scl(1 / Constants.TILE_SIZE * width);
            matrix.scl(aspect, aspect, 1);

            batch.setTransformMatrix(matrix);
            tile.tile.x = 0;
            tile.tile.y = 0;
            tile.tile.rotation = 0;
            tile.tile.render(batch, 0);
            tile.tile.x = beforeX;
            tile.tile.y = beforeY;
            tile.tile.rotation = beforeR;
        }

        @Override
        public float getLeftWidth() { return 0; }

        @Override
        public void setLeftWidth(float leftWidth) {  }

        @Override
        public float getRightWidth() { return 0;  }

        @Override
        public void setRightWidth(float rightWidth) {  }

        @Override
        public float getTopHeight() { return 0;  }

        @Override
        public void setTopHeight(float topHeight) {  }

        @Override
        public float getBottomHeight() { return 0;  }

        @Override
        public void setBottomHeight(float bottomHeight) {  }

        @Override
        public float getMinWidth() { return 64.f; }

        @Override
        public void setMinWidth(float minWidth) {  }

        @Override
        public float getMinHeight() { return 64.f;  }

        @Override
        public void setMinHeight(float minHeight) {  }
    };

    // Variables
    private @Null Screen previousScreen = null;
    private Engine engine = new Engine();
    private Stage mInterface;

    private Entity gridEntity = new Entity();
    private Grid testGrid = new Grid();
    
    private OrthographicCamera editorCamera = new OrthographicCamera(1280 / Constants.PPM, 720 / Constants.PPM);
    private ShapeRenderer batch = App.instance.shapeRenderer;
    private Batch spriteBatch = App.instance.spriteBatch;
    private Vector2 panningVector = null;
    private Vector2i currentCell = new Vector2i();

    private Texture topBarBackground;
    private Texture partsBackground;
    private AsepriteSheet categoryIcons;
    private Table partsGroup;
    private @Null PickableTile selectedTile = null;
    private Group partHoverLabels = new Group();

    // Constructor
    public GridEditor(){
        super();
        initTextures();
        initInterface();
        initControls();
        initEngine();

        gridEntity.add(new TransformComponent());
        gridEntity.add(new BodyComponent());
        testGrid.assemble(gridEntity.getComponent(BodyComponent.class));
        engine.addEntity(gridEntity);

        inputs.addProcessor(0, mInterface);
        previousScreen = App.instance.getScreen();
        App.instance.camera = editorCamera;
    }

    // Functions
    private void selectCategory(TileCategory category){
        for(Actor actor : partHoverLabels.getChildren()){
            if(actor instanceof HoverLabel){
                ((HoverLabel)actor).detach();
            }
        }
        
        partHoverLabels.clear();
        partsGroup.clear();
        int index = 0;

        for(final PickableTile tile : App.instance.tileManager.getTilesInCategory(category)){
            ImageButton btn = new ImageButton(new DrawableTile(tile));
            HoverLabel lbl = new HoverLabel(btn, tile.tile.tileID, App.instance.skin, 1.f);
            
            lbl.setAlignment(Align.bottomLeft);
            lbl.setFontScale(0.7f);
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent e, Actor a){
                    selectedTile = tile;
                }
            });

            if(index % 2 == 0)
                partsGroup.row().pad(10).expandX();

            partsGroup.add(btn);
            partHoverLabels.addActor(lbl);

            index++;
        }
    }

    private void initTextures(){
        Pixmap data = new Pixmap(1, 1, Format.RGBA8888);
        data.setColor(0.2f, 0.2f, 0.2f, 1.f);
        data.fill();
        topBarBackground = new Texture(data);

        data.setColor(0.1f, 0.1f, 0.1f, 1.f);
        data.fill();
        partsBackground = new Texture(data);

        categoryIcons = App.instance.manager.get("textures/ui/categories.json", AsepriteSheet.class);
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
        topBarTbl.setBackground(new TextureRegionDrawable(topBarBackground));
        root.add(topBarTbl).expandX().fillX().row();

        TextField nameTextField = new TextField("unnamed_grid", skin);
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
                    testGrid.gridName = nameTextField.getText();

                    FileHandle handle = Gdx.files.local("./saves/grids/" + nameTextField.getText() + ".grid");
                    handle.writeBytes(testGrid.serialize(), false);
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
                    testGrid = Grid.unserialize(Gdx.files.local("./saves/grids/" + nameTextField.getText() + ".grid").readBytes());
                    testGrid.assemble(gridEntity.getComponent(BodyComponent.class));
                }
            }
        });
        topBarTbl.add(loadBtn).right().pad(10);

        ImageButton quitBtn = new ImageButton(new TextureRegionDrawable(App.instance.atlas.findRegion("ui/exit")));
        quitBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                App.instance.setScreen(previousScreen);
            }
        });
        topBarTbl.add(quitBtn).right().pad(10).width(64).height(64);

        // Parts tab
        Table partsTbl = new Table(skin);
        partsTbl.setBackground(new TextureRegionDrawable(partsBackground));

        VerticalGroup categories = new VerticalGroup();
        partsTbl.add(new AutoScrollPane(categories)).fill().width(64);

        partsGroup = new Table();
        partsGroup.align(Align.top);
        partsTbl.add(new AutoScrollPane(partsGroup)).expand().fill().top();
        mInterface.addActor(partHoverLabels);

        // Split pane creation
        VisSplitPane splitPane = new VisSplitPane(partsTbl, new Table(), false);
        splitPane.setName("editor_pane");
        splitPane.setSplitAmount(0.3f);
        root.add(splitPane).expand().fill().row();

        // Populate categories
        for(final TileCategory entry : App.instance.tileManager.getTileMap().keySet()){
            ImageButton btn = new ImageButton(new TextureRegionDrawable(categoryIcons.getRegion(entry.toString().toLowerCase())));
            HoverLabel lbl = new HoverLabel(btn, entry.name + "\n" + entry.description, skin, 0.5f);

            lbl.setAlignment(Align.bottomLeft);
            lbl.setFontScale(0.7f);
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    selectCategory(entry);
                }
            });

            categories.addActor(btn);
            mInterface.addActor(lbl);
        }

        selectCategory(TileCategory.ENERGY);
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

                    case Keys.F5:
                        App.instance.setScreen(new GridEditor());
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
            batch.setColor(testGrid.isRegionOccupied(currentCell.x, currentCell.y, 0, selectedTile.tile.width, selectedTile.tile.height) ? Color.RED : Color.CYAN);
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
                spriteBatch.setColor(1, 1, 1, 1);
                tile.render(spriteBatch, Gdx.graphics.getDeltaTime());
            }
        });

        if(selectedTile != null){
            spriteBatch.setColor(1, 1, 1, 0.2f);
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
