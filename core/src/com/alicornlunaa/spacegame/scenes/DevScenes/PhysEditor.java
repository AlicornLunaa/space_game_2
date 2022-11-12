package com.alicornlunaa.spacegame.scenes.DevScenes;

import org.json.JSONArray;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;

/**
 * This scene will edit physics bodies and output a JSON file
 */
public class PhysEditor implements Screen {

    // Classes
    private static class PhysShape {
        private Array<Vector2> vertices = new Array<>();
        private PhysShape(){}
        private PhysShape(PhysShape ps){
            for(Vector2 v : ps.vertices){
                vertices.add(v.cpy());
            }
        }

        private JSONArray serialize(){
            JSONArray o = new JSONArray();
            
            for(Vector2 v : vertices){
                o.put(v.x);
                o.put(v.y);
            }

            return o;
        }
    }

    // Variables
    private final App game;
    private Stage stage;
    private VisTable root;
    private FileChooser fileChooser;
    private InputMultiplexer inputs;

    private float zoomAmount = 1.0f;
    private Texture referenceImage;
    private Array<PhysShape> shapes = new Array<>();

    private boolean lineStarted = false;
    private Vector2 cursor = new Vector2();
    private Vector2 start = new Vector2();
    private Vector2 point1 = new Vector2();
    private PhysShape currentShape = new PhysShape();

    // Private functions
    private void saveBody(FileHandle handle){
        JSONArray o = new JSONArray();
        for(PhysShape s : shapes){
            o.put(s.serialize());
        }
        
        handle.writeString(o.toString(), false);
    }

    // Constructor
    public PhysEditor(final App game){
        // Initialize components
        this.game = game;
        referenceImage = new Texture(Gdx.files.internal("textures/parts/aero/med_cmd_pod.png"));
        // referenceImage = new Texture(16, 16, Format.RGBA8888);

        // Initialize UI
        stage = new Stage(new ScreenViewport());
        root = new VisTable();
        root.setFillParent(true);
        stage.addActor(root);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        menuBar.addMenu(fileMenu);
        root.add(menuBar.getTable()).fillX().expandX().top().row();
        root.add().fill().expand().row();

        fileChooser = new FileChooser("Reference Image", Mode.OPEN);
        fileChooser.setSelectionMode(SelectionMode.FILES);

        fileMenu.addItem(new MenuItem("Load image", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load an image with a file chooser
                fileChooser.setMode(Mode.OPEN);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle imgHandle = files.first();
                        referenceImage.dispose();
                        referenceImage = new Texture(imgHandle);
                    }
                });

                stage.addActor(fileChooser);
            }
        }));

        fileMenu.addItem(new MenuItem("Save body", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load an image with a file chooser
                fileChooser.setMode(Mode.SAVE);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle bodyHandle = files.first();
                        saveBody(bodyHandle);
                    }
                });
                
                stage.addActor(fileChooser);
            }
        }));

        // Initialize controls
        inputs = new InputMultiplexer();
        inputs.addProcessor(stage);
        inputs.addProcessor(new InputAdapter(){
            @Override
            public boolean scrolled(float amountX, float amountY){
                zoomAmount = Math.min(Math.max(zoomAmount + (amountY / 30), 0.05f), 3.0f);
                return true;
            }

            @Override
            public boolean touchDown(int x, int y, int pointer, int button){
                if(button != Buttons.LEFT) return false;

                if(!lineStarted){
                    // Create new line
                    point1.set(cursor);
                    start.set(point1);

                    currentShape.vertices.clear();
                    currentShape.vertices.add(point1.cpy());

                    lineStarted = true;
                } else {
                    // End line
                    currentShape.vertices.add(cursor.cpy());
                    point1.set(cursor);

                    if(point1.equals(start)){
                        // End the shape
                        lineStarted = false;
                        shapes.add(new PhysShape(currentShape));
                    }
                }

                return true;
            }
        });
    }

    // Functions
    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
        OrthographicCamera cam = (OrthographicCamera)stage.getCamera();

        cursor = stage.screenToStageCoordinates(cursor.set(Gdx.input.getX(), Gdx.input.getY())).sub(stage.getWidth() / 2, stage.getHeight() / 2).scl(zoomAmount);
        cursor.set(Math.round(cursor.x), Math.round(cursor.y));

        cam.zoom = zoomAmount;
        cam.update();
        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.setTransformMatrix(new Matrix4().translate(stage.getWidth() / 2, stage.getHeight() / 2, 0));
        batch.begin();
        batch.draw(referenceImage, referenceImage.getWidth() / -2, referenceImage.getHeight() / -2);
        batch.end();

        ShapeRenderer s = game.shapeRenderer;
        s.setProjectionMatrix(stage.getCamera().combined);
        s.setTransformMatrix(batch.getTransformMatrix());
        s.begin(ShapeType.Filled);
        
        if(lineStarted){
            s.setColor(Color.RED);
            s.circle(point1.x, point1.y, 0.5f);
            s.rectLine(point1, cursor, 0.4f);
        } else {
            s.setColor(Color.CYAN);
            s.circle(cursor.x, cursor.y, 0.5f);
        }

        for(int i = 0; i < currentShape.vertices.size; i++){
            if((i + 1) % currentShape.vertices.size == 0) continue;
            Vector2 p1 = currentShape.vertices.get(i);
            Vector2 p2 = currentShape.vertices.get((i + 1) % currentShape.vertices.size);

            s.setColor(Color.YELLOW);
            s.circle(p1.x, p1.y, 0.5f);
            s.rectLine(p1, p2, 0.4f);
        }

        for(PhysShape shape : shapes){
            for(int i = 0; i < shape.vertices.size; i++){
                Vector2 p1 = shape.vertices.get(i);
                Vector2 p2 = shape.vertices.get((i + 1) % shape.vertices.size);

                s.setColor(Color.GREEN);
                s.circle(p1.x, p1.y, 0.5f);
                s.rectLine(p1, p2, 0.4f);
            }
        }
        
        s.end();

        cam.zoom = 1;
        cam.update();
        stage.getViewport().apply();
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
    public void dispose() {
        stage.dispose();
    }

    // Static
    public static Body loadShape(Body body, Vector2 offset, FileHandle handle){
        JSONArray arr = new JSONArray(handle.readString());

        for(int i = 0; i < arr.length(); i++){
            PolygonShape shape = new PolygonShape();
            JSONArray vertexData = arr.getJSONArray(i);
            Vector2[] vertices = new Vector2[vertexData.length() / 2];

            for(int j = 0; j < vertexData.length(); j += 2){
                vertices[j / 2] = new Vector2(vertexData.getFloat(j), vertexData.getFloat(j + 1));
            }

            shape.set(vertices);

            FixtureDef def = new FixtureDef();
            def.shape = shape;
            body.createFixture(def);

            shape.dispose();
        }

        return body;
    }
    
}
