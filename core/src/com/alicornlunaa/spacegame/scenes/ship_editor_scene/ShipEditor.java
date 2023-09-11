package com.alicornlunaa.spacegame.scenes.ship_editor_scene;

import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.scenes.BaseScene;
import com.alicornlunaa.selene_engine.systems.RenderSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship2.Ship;
import com.alicornlunaa.spacegame.objects.ship2.parts.Part;
import com.alicornlunaa.spacegame.objects.ship2.parts.Part.Node;
import com.alicornlunaa.spacegame.systems.CustomRenderSystem;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisSplitPane;

public class ShipEditor extends BaseScene {
    // Interfaces
    private static interface RecursiveNodeInterface {
        abstract void run(Part part);
        abstract Part.Node getNode();
    }

    // Variables
    private Stage root;
    private Stage editorStage;
    private OrthographicCamera editorCamera;
    private @Null Vector2 panningVector = null;
    private Vector2 camOffset = new Vector2();
    private VisSplitPane splitPane;

    private VerticalGroup categories;
    private VerticalGroup parts;
    private TextField nameField;

    private float snapDistance = 4;
    private @Null String selectedCategory = null;
    private @Null Part selectedPart = null;

    private Registry registry;
    private PhysWorld world;
    private Ship ship;
    
    // Private functions
    private Part.Node closestNode(final Vector2 pos, Part part){
        RecursiveNodeInterface lambda = new RecursiveNodeInterface(){
            private Part.Node res = null;
            private float distance = Float.MAX_VALUE;

			@Override
			public void run(Part currentPart) {
                for(Part.Node node : currentPart.getAttachments()){
                    float curDistance = currentPart.getPosition().cpy().add(node.point).dst(pos);
    
                    if(curDistance < distance){
                        distance = curDistance;
                        res = node;
                    }

                    if(node.next != null){
                        run(node.next.part);
                    }
                }
            }

			@Override
			public Node getNode() {
                return res;
			}
        };

        lambda.run(part);
        return lambda.getNode();
    }

    private void populateParts(){
        parts.clear();

        if(selectedCategory == null) return;

        for(final String partID : game.partManager.getPartsList().get(selectedCategory).keySet()){
            TextureRegionDrawable texture = new TextureRegionDrawable(game.atlas.findRegion("parts/" + partID.toLowerCase()));
            texture.setMinSize(64 * ((float)texture.getRegion().getRegionWidth() / (float)texture.getRegion().getRegionHeight()), 64);

            ImageButton btn = new ImageButton(texture);
            btn.addListener(new ChangeListener(){
                @Override
                public void changed(ChangeEvent e, Actor a){
                    selectedPart = new Part(game, ship, game.partManager.get(selectedCategory, partID));

                    if(ship.getRootPart() == null){
                        ship.setRootPart(selectedPart);
                        selectedPart = null;
                    }
                }
            });
            parts.addActor(btn);
        }
    }

    private void populateCategories(){
        for(final String entry : game.partManager.getPartsList().keySet()){
            TextButton btn = new TextButton(entry, game.skin);
            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    selectedCategory = entry;
                    populateParts();
                }
            });
            categories.addActor(btn);
        }
    }

    private void initializeRoot(){
        // Root creation
        root = new Stage(new ScreenViewport());
        
        Table ui = new Table(game.skin);
        ui.setFillParent(true);
        root.addActor(ui);

        // Top bar creation
        Table topBar = new Table();

        nameField = new TextField("TEST", game.skin);
        topBar.add(nameField).expand().fill().left().pad(10, 10, 10, 300);

        TextButton saveBtn = new TextButton("Save", game.skin);
        saveBtn.setColor(Color.BLUE);
        saveBtn.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                // Save file to ./saves/ships/name.ship
                if(nameField.getText().length() > 0){
                    ship.save("./saves/ships/" + nameField.getText() + ".ship");
                }
            }
        });
        topBar.add(saveBtn).right().pad(10);

        TextButton loadBtn = new TextButton("Load", game.skin);
        loadBtn.setColor(Color.BLUE);
        topBar.add(loadBtn).right().pad(10);

        TextButton quitBtn = new TextButton("Quit", game.skin);
        quitBtn.setColor(Color.RED);
        topBar.add(quitBtn).right().pad(10);

        ui.add(topBar).expandX().fillX().row();

        // Parts tab creation
        Table partsTbl = new Table(game.skin);

        categories = new VerticalGroup();
        partsTbl.add(categories).expand().fill();
        parts = new VerticalGroup();
        partsTbl.add(parts).expand().fill();
        populateCategories();

        // Split pane creation
        splitPane = new VisSplitPane(partsTbl, new Table(), false);
        splitPane.setSplitAmount(0.3f);
        ui.add(splitPane).expand().fill().row();
    }

    private void initializeEditor(){
        editorStage = new Stage(new ScreenViewport());

        editorCamera = (OrthographicCamera)editorStage.getCamera();
        editorCamera.position.set(0, 0, 0);
        editorCamera.zoom = 0.1f;
        editorCamera.update();
        game.camera = editorCamera;

        Table t = new Table();
        t.setFillParent(true);
        Image i = new Image(new Texture("./assets/textures/dev_texture_32.png"));
        i.setPosition(i.getWidth() / -2, i.getHeight() / -2);
        // t.add(i).row();
        editorStage.addActor(t);

        world = new PhysWorld(128);
        ship = new Ship(game, world, 0, 0, 0);
        ship.getTransform().position.set(1280 / 2, 720 / 2);

        registry = new Registry();
        registry.registerSystem(new RenderSystem(game));
        registry.registerSystem(new CustomRenderSystem(game));
        registry.addEntity(ship);
    }

    private void initializeControls(){
        inputs.setProcessors(root, editorStage);
        
        inputs.addProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(button == Buttons.MIDDLE){
                    panningVector = new Vector2(screenX, screenY);
                    return true;
                }

                if(button == Buttons.LEFT){
                    if(selectedPart != null){
                        // Trying to place something
                        selectedPart = null;
                    } else {
                        // Trying to pick something up?
                        Part rootPart = ship.getRootPart();
                        Vector2 localCenter = ship.getBody().body.getLocalCenter().cpy().scl(ship.getBody().world.getPhysScale());
                        Rectangle bounds = new Rectangle(rootPart.getWidth() / -2.f - localCenter.x, rootPart.getHeight() / -2.f - localCenter.y, rootPart.getWidth(), rootPart.getHeight());
                        Vector2 cursorPos = editorStage.screenToStageCoordinates(new Vector2(screenX, screenY)).sub(1280 / 2.f, 720 / 2.f);
                        System.out.println(bounds.contains(cursorPos.x, cursorPos.y));
                    }
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
                    panningVector.sub(screenX, screenY).scl(editorCamera.zoom);
                    camOffset.add(panningVector.x, -panningVector.y);
                    editorCamera.update();
                    panningVector.set(screenX, screenY);
                }

                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                editorCamera.zoom = Math.min(Math.max(editorCamera.zoom + amountY * 0.01f, 0.01f), 50);
                return true;
            }
        });
    }

    // Constructor
    public ShipEditor(App game) {
        super(game);
        initializeRoot();
        initializeEditor();
        initializeControls();
        
        backgroundColor.set(0.1f, 0.1f, 0.1f, 1.0f);
        
        root.setDebugAll(true);
        editorStage.setDebugAll(true);
    }
    
    // Functions
    @Override
    public void render(float delta) {
        super.render(delta);
        
        float ratio = root.getWidth() / root.getHeight();
        Rectangle bounds = splitPane.getSecondWidgetBounds();
        editorCamera.position.set(1280 / 2 + bounds.getX() * editorCamera.zoom - 16 + camOffset.x, 720 / 2 + camOffset.y, 0);
        editorCamera.update();
        editorStage.getViewport().setScreenBounds((int)bounds.getX(), (int)bounds.getY(), (int)(bounds.getHeight() * ratio), (int)bounds.getHeight());
        editorStage.getViewport().setWorldSize(1280, 720);
        editorStage.getViewport().apply(false);
        editorStage.act(delta);
        editorStage.draw();

        registry.update(delta);
        registry.render();
        
        Vector2 pos = editorStage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        // pos.set((int)(pos.x / snapDistance) * snapDistance, (int)(pos.y / snapDistance) * snapDistance);

        if(selectedPart != null){
            Batch batch = editorStage.getBatch();
            batch.setProjectionMatrix(editorCamera.combined);
            batch.setColor(1.0f, 0.8f, 0.8f, 0.4f);
            batch.begin();
            selectedPart.draw(batch, new Matrix4().translate(pos.x, pos.y, 0.0f));
            batch.end();
        }

        if(selectedPart != null){
            game.shapeRenderer.setProjectionMatrix(editorCamera.combined);
            game.shapeRenderer.setTransformMatrix(new Matrix4());
            game.shapeRenderer.begin(ShapeType.Filled);
            selectedPart.drawAttachmentPoints(game.shapeRenderer, new Matrix4().translate(pos.x, pos.y, 0.0f));

            // Test closest
            game.shapeRenderer.setProjectionMatrix(editorCamera.combined);
            game.shapeRenderer.setTransformMatrix(new Matrix4());
            game.shapeRenderer.setColor(Color.MAGENTA);
            Part.Node node = this.closestNode(pos.cpy().sub(1280/2, 720/2), ship.getRootPart());
            if(node != null){
                Vector2 p = node.part.getPosition().cpy().add(node.point);
                game.shapeRenderer.circle(p.x + 1280/2, p.y + 720/2, 1.5f, 16);
                
                node = this.closestNode(p, selectedPart);
                if(node != null){
                    p.set(node.part.getPosition().cpy().add(node.point));
                    game.shapeRenderer.circle(p.x + 1280/2, p.y + 720/2, 1.5f, 16);
                }
            }
            
            game.shapeRenderer.setTransformMatrix(new Matrix4());
            game.shapeRenderer.setColor(0.2f, 0.2f, 0.9f, 1.0f);
            game.shapeRenderer.circle(pos.x, pos.y, 0.7f, 16);

            game.shapeRenderer.end();
        }

        root.getViewport().apply();
        root.act(delta);
        root.draw();
    }

    @Override
    public void resize(int width, int height) {
        root.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {}
}
