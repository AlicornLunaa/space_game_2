package com.alicornlunaa.spacegame.scenes.PartEditor;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogAdapter;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;

/**
 * The part editor has four modes so far. The exterior controls the shape given to the universe
 * world. It is a dynamic shape. The interior editor controls the shape while inside the ship,
 * the static body. The Attachment editor changes the attachment point positions and the
 * animator sets different states for different icons to be shown.
 */
public class PartEditor implements Screen {

    // Variables
    private final App game;
    private Stage ui;
    private InputMultiplexer inputs = new InputMultiplexer();

    // UI variables
    private VisTable root;
    private FileChooser fileChooser;
    private TabbedPane tabs;
    private VisSplitPane splitPane;
    private VisTable partSettings;

    // Editor variables
    public enum EditType { SHAPE_EDITOR_WORLD, SHAPE_EDITOR_INTERIOR, ATTACHMENT_EDITOR, ANIMATOR };
    public EditType mode = EditType.ATTACHMENT_EDITOR;
    public Vector2 cursor = new Vector2();
    public HashMap<EditType, EditorPanel> panels = new HashMap<>();
    public JSONArray partList;
    public JSONObject partMetadata;
    public TextureRegion externalTexture;
    public TextureRegion internalTexture;
    public Array<PhysShape> externalShape = new Array<>();
    public Array<PhysShape> internalShape = new Array<>();
    public Array<Vector2> attachmentPoints = new Array<>();
    public String selectedType = "";
    public int selectedPartIndex = -1;
    public boolean renderExternal = true;
    public Vector2 center = new Vector2();
    public float partSize = 256;

    // Private functions
    private void saveParts(FileHandle handle){
        if(selectedPartIndex != -1){
            JSONObject part = partList.getJSONObject(selectedPartIndex);
            JSONArray attachments = new JSONArray();
            for(Vector2 a : attachmentPoints){
                JSONObject v = new JSONObject();
                v.put("x", a.x);
                v.put("y", a.y);
                attachments.put(v);
            }
            part.put("attachmentPoints", attachments);

            JSONArray external = new JSONArray();
            for(PhysShape s : externalShape){
                external.put(s.serialize());
            }
            part.put("externalShape", external);

            JSONArray internal = new JSONArray();
            for(PhysShape s : internalShape){
                internal.put(s.serialize());
            }
            part.put("internalShape", internal);
        }

        handle.writeString(partList.toString(2), false);
    }

    private void loadParts(FileHandle handle){
        partList = new JSONArray(handle.readString());
        selectedType = handle.nameWithoutExtension().toUpperCase();
        tabs.removeAll();

        for(int i = 0; i < partList.length(); i++){
            JSONObject obj = partList.getJSONObject(i);
            tabs.add(new PartTab(obj));
        }
    }

    private void selectPart(String id){
        // Save old info
        if(selectedPartIndex != -1){
            JSONObject part = partList.getJSONObject(selectedPartIndex);
            JSONArray attachments = new JSONArray();
            for(Vector2 a : attachmentPoints){
                JSONObject v = new JSONObject();
                v.put("x", a.x);
                v.put("y", a.y);
                attachments.put(v);
            }
            part.put("attachmentPoints", attachments);

            JSONArray external = new JSONArray();
            for(PhysShape s : externalShape){
                external.put(s.serialize());
            }
            part.put("externalShape", external);

            JSONArray internal = new JSONArray();
            for(PhysShape s : internalShape){
                internal.put(s.serialize());
            }
            part.put("internalShape", internal);
        }

        // Load the part into the UI
        for(int i = 0; i < partList.length(); i++){
            JSONObject obj = partList.getJSONObject(i);
            
            if(obj.getString("id").equals(id)){
                selectedPartIndex = i;
                partMetadata = obj.getJSONObject("metadata");
                ((VisTextField)ui.getRoot().findActor("part_id")).setText(id);
                ((VisTextField)ui.getRoot().findActor("part_name")).setText(obj.getString("name"));
                ((VisTextField)ui.getRoot().findActor("part_desc")).setText(obj.getString("desc"));
                ((VisValidatableTextField)ui.getRoot().findActor("part_density")).setText(String.valueOf(obj.getFloat("density")));
                ((VisValidatableTextField)ui.getRoot().findActor("part_scale")).setText(String.valueOf(obj.getFloat("scale")));

                externalTexture = game.atlas.findRegion("parts/" + id.toLowerCase());
                internalTexture = game.atlas.findRegion("parts/" + id.toLowerCase());

                attachmentPoints.clear();
                for(int j = 0; j < obj.getJSONArray("attachmentPoints").length(); j++){
                    JSONObject v = obj.getJSONArray("attachmentPoints").getJSONObject(j);
                    attachmentPoints.add(new Vector2(v.getFloat("x"), v.getFloat("y")));
                }

                externalShape.clear();
                for(int j = 0; j < obj.getJSONArray("externalShape").length(); j++){
                    externalShape.add(PhysShape.unserialize(game.shapeRenderer, obj.getJSONArray("externalShape").getJSONArray(j)));
                }

                internalShape.clear();
                for(int j = 0; j < obj.getJSONArray("internalShape").length(); j++){
                    internalShape.add(PhysShape.unserialize(game.shapeRenderer, obj.getJSONArray("internalShape").getJSONArray(j)));
                }
                return;
            }
        }
    }

    private void newPart(){
        Dialogs.showInputDialog(ui, "New part", "ID: ", new InputDialogAdapter() {
            @Override
            public void finished(String input){
                JSONObject part = new JSONObject();
                part.put("type", selectedType);
                part.put("id", input);
                part.put("name", "NAME HERE");
                part.put("desc", "DESCRIPTION HERE");
                part.put("density", 0.1f);
                part.put("scale", 1.0f);
                part.put("attachmentPoints", new JSONArray());
                part.put("externalShape", new JSONArray());
                part.put("internalShape", new JSONArray());
                part.put("metadata", new JSONObject());
                partList.put(part);
                tabs.add(new PartTab(part));
                selectPart(input);
            }
        });
    }

    private void changeMode(EditType type){
        inputs.removeProcessor(panels.get(mode).getInputListener());
        inputs.addProcessor(panels.get(type).getInputListener());
        mode = type;
    }

    private void initUI(){
        // Initialize UI
        ui = new Stage(new ScreenViewport());
        root = new VisTable();
        root.setFillParent(true);
        ui.addActor(root);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu modeMenu = new Menu("Mode");
        menuBar.addMenu(fileMenu);
        menuBar.addMenu(modeMenu);
        root.add(menuBar.getTable()).fillX().expandX().top().row();

        // Properties menu
        VisTextField idField = new VisTextField("PART_ID"); idField.setName("part_id");
        VisTextField nameField = new VisTextField("PART_NAME"); nameField.setName("part_name");
        VisTextField descField = new VisTextField("PART_DESC"); descField.setName("part_desc");
        VisValidatableTextField densityField = new VisValidatableTextField(Validators.FLOATS); densityField.setName("part_density");
        VisValidatableTextField scaleField = new VisValidatableTextField(Validators.FLOATS); scaleField.setName("part_scale");
        
        partSettings = new VisTable();
        partSettings.top();
        partSettings.add(new VisLabel("ID")).pad(10).right();
        partSettings.add(idField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Name")).pad(10).right();
        partSettings.add(nameField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Description")).pad(10).right();
        partSettings.add(descField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Density")).pad(10).right();
        partSettings.add(densityField).expandX().fillX().padRight(10).row();
        partSettings.add(new VisLabel("Scale")).pad(10).right();
        partSettings.add(scaleField).expandX().fillX().padRight(10).row();

        partSettings.add(new VisLabel("Metadata")).pad(10).center().colspan(2).row();
        // TODO: Metadata list here

        ChangeListener listener = new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                String key = a.getName().substring(5);
                partList.getJSONObject(selectedPartIndex).put(key, ((VisTextField)a).getText());
            }
        };
        idField.addListener(listener);
        nameField.addListener(listener);
        descField.addListener(listener);
        densityField.addListener(listener);
        scaleField.addListener(listener);

        // Part selection
        tabs = new TabbedPane();
        tabs.addListener(new TabbedPaneAdapter(){
            @Override
            public void switchedTab(Tab tab){
                selectPart(tab.getTabTitle());
            }
        });
        root.add(tabs.getTable()).fillX().expandX().top().row();

        VisTable placeholder = new VisTable();
        placeholder.setFillParent(true);
        placeholder.add().expand().fill();
        panels.put(EditType.ATTACHMENT_EDITOR, new AttachmentEditor(game, this));
        panels.put(EditType.SHAPE_EDITOR_WORLD, new ExternalShapeEditor(game, this));
        inputs.addProcessor(panels.get(EditType.ATTACHMENT_EDITOR).getInputListener());

        splitPane = new VisSplitPane(partSettings, placeholder, false);
        splitPane.setSplitAmount(0.25f);
        root.add(splitPane).fill().expand().row();

        fileChooser = new FileChooser(Gdx.files.internal("./assets/parts/"), Mode.OPEN);
        fileChooser.setSelectionMode(SelectionMode.FILES);

        fileMenu.addItem(new MenuItem("New", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Add new template object
                newPart();
            }
        }));

        fileMenu.addItem(new MenuItem("Export", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load an image with a file chooser
                fileChooser.setMode(Mode.SAVE);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle bodyHandle = files.first();
                        saveParts(bodyHandle);
                    }
                });
                
                ui.addActor(fileChooser);
            }
        }));

        fileMenu.addItem(new MenuItem("Import", new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor a){
                // Load an image with a file chooser
                fileChooser.setMode(Mode.OPEN);
                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle bodyHandle = files.first();
                        loadParts(bodyHandle);
                    }
                });
                
                ui.addActor(fileChooser);
            }
        }));

        modeMenu.addItem(new MenuItem("Exterior Shape", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.SHAPE_EDITOR_WORLD);
            }
        }));

        modeMenu.addItem(new MenuItem("Interior Shape", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.SHAPE_EDITOR_INTERIOR);
            }
        }));

        modeMenu.addItem(new MenuItem("Attachment Points", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.ATTACHMENT_EDITOR);
            }
        }));

        modeMenu.addItem(new MenuItem("Animation", new ChangeListener(){
            @Override
            public void changed(ChangeEvent e, Actor a){
                changeMode(EditType.ANIMATOR);
            }
        }));
    
        // Controls
        inputs.addProcessor(0, ui);
    }

    // Constructor
    public PartEditor(final App game){
        // Initialize components
        this.game = game;
        initUI();

        loadParts(Gdx.files.internal("assets/parts/aero.json"));
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

        cursor = ui.screenToStageCoordinates(cursor.set(Gdx.input.getX(), Gdx.input.getY()));
        cursor.set(Math.round(cursor.x), Math.round(cursor.y)); // Snap points

        if(tabs.getActiveTab() != null){
            if(internalTexture != null && externalTexture != null){
                Rectangle rect = splitPane.getSecondWidgetBounds();
                Vector2 corner = new Vector2(rect.x + rect.width / 2 - partSize / 2, rect.y + rect.height / 2 - partSize / 2);
                Vector2 cursorOnPart;

                Batch batch = ui.getBatch();
                batch.begin();
                if(renderExternal){
                    float ratio = ((float)externalTexture.getRegionHeight() / externalTexture.getRegionWidth());
                    center.set(externalTexture.getRegionWidth() / 2.0f, externalTexture.getRegionHeight() / 2.0f);
                    cursorOnPart = cursor.cpy().sub(corner).scl(1 / partSize, 1 / (partSize * ratio)).scl(externalTexture.getRegionWidth(), externalTexture.getRegionWidth() * ratio);
                    batch.draw(externalTexture, corner.x, corner.y, partSize, partSize * ratio);
                } else {
                    float ratio = ((float)internalTexture.getRegionHeight() / internalTexture.getRegionWidth());
                    center.set(internalTexture.getRegionWidth() / 2.0f, internalTexture.getRegionHeight() / 2.0f);
                    cursorOnPart = cursor.cpy().sub(corner).scl(1 / partSize, 1 / (partSize * ratio)).scl(internalTexture.getRegionWidth(), internalTexture.getRegionWidth() * ratio);
                    batch.draw(internalTexture, corner.x, corner.y, partSize, partSize * ratio);
                }
                batch.end();
                
                panels.get(mode).render(splitPane.getSecondWidgetBounds(), ((PartTab)tabs.getActiveTab()).getData(), corner, cursorOnPart);
            }
        }

        ui.getViewport().apply();
        ui.act(delta);
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        ui.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        ui.dispose();
    }
    
}
