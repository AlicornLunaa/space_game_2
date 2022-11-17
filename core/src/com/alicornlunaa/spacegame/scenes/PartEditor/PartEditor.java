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
import com.badlogic.gdx.utils.viewport.FillViewport;
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
    private Stage editor;
    private InputMultiplexer inputs = new InputMultiplexer();

    // UI variables
    private VisTable root;
    private FileChooser fileChooser;
    private TabbedPane tabs;
    private VisSplitPane splitPane;
    private VisTable partSettings;
    private boolean renderExternal = true;

    // Editor variables
    private enum EditType { SHAPE_EDITOR_WORLD, SHAPE_EDITOR_INTERIOR, ATTACHMENT_EDITOR, ANIMATOR };
    private EditType mode = EditType.ATTACHMENT_EDITOR;
    private Vector2 cursor = new Vector2();
    private HashMap<EditType, EditorPanel> panels = new HashMap<>();
    private JSONArray partList;
    private JSONObject partMetadata;
    private TextureRegion externalTexture;
    private TextureRegion internalTexture;
    private String selectedType = "";
    private int selectedPartIndex = 0;

    // Private functions
    private void saveParts(FileHandle handle){
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

    private void changeMode(EditType type){ mode = type; }

    private void initUI(){
        // Initialize UI
        ui = new Stage(new ScreenViewport());
        root = new VisTable();
        root.setFillParent(true);
        ui.addActor(root);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu modeMenu = new Menu("Mode");
        Menu settingsMenu = new Menu("Settings");
        menuBar.addMenu(fileMenu);
        menuBar.addMenu(modeMenu);
        menuBar.addMenu(settingsMenu);
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
        panels.put(EditType.ATTACHMENT_EDITOR, new AttachmentEditor(game));

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

        // Initialize controls
        inputs.addProcessor(ui);
    }

    private void initEditor(){
        // Create editor variables
        editor = new Stage(new FillViewport(640, 360));
    }

    // Constructor
    public PartEditor(final App game){
        // Initialize components
        this.game = game;
        initUI();
        initEditor();
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
                float size = 128;
                Vector2 corner = new Vector2(rect.x + rect.width / 2 - size / 2, rect.y + rect.height / 2 - size / 2);
                Vector2 cursorOnPart;

                Batch batch = ui.getBatch();
                batch.begin();
                if(renderExternal){
                    cursorOnPart = cursor.cpy().sub(corner).scl(1 / size).scl(externalTexture.getRegionWidth());
                    batch.draw(externalTexture, corner.x, corner.y, size, size);
                } else {
                    cursorOnPart = cursor.cpy().sub(corner).scl(1 / size).scl(externalTexture.getRegionWidth());
                    batch.draw(internalTexture, corner.x, corner.y, size, size);
                }
                batch.end();
                
                panels.get(mode).render(splitPane.getSecondWidgetBounds(), ((PartTab)tabs.getActiveTab()).getData(), cursorOnPart);
            }
        }

        ui.getViewport().apply();
        ui.act(delta);
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        ui.getViewport().update(width, height, true);
        editor.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        editor.dispose();
        ui.dispose();
    }
    
}
