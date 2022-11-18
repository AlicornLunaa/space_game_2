package com.alicornlunaa.spacegame.scenes.Dev.PartEditor;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class ExternalShapeEditor extends EditorPanel {

    // Variables
    private int selectedShape = 0;

    // Constructor
    public ExternalShapeEditor(final App game, final PartEditor editor, final VisTable content){
        super(game, editor, content);

        add(new VisTextButton("New Shape")).pad(10).top().row();

        controls = new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button){
                if(editor.externalShape.size == 0) return false;
                
                float snapX = (int)(cursor.x * 2) / 2.0f;
                float snapY = (int)(cursor.y * 2) / 2.0f;

                if(button == Buttons.LEFT){
                    editor.externalShape.get(selectedShape).vertices.add(new Vector2(snapX - editor.center.x, snapY - editor.center.y));
                } else if(button == Buttons.RIGHT){
                    editor.externalShape.get(selectedShape).vertices.removeValue(new Vector2(snapX - editor.center.x, snapY - editor.center.y), false);
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode){
                return false;
            }
        };
    }

    // Functions
    @Override
    public void updateContent(){
        editor.renderExternal = true;
        selectedShape = 0;

        super.updateContent();
        content.add(new VisLabel("Shapes")).top().left().pad(10);
        
        VisTextButton newBtn = new VisTextButton("New Shape");
        newBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                editor.externalShape.add(new PhysShape(render));
                selectedShape = editor.externalShape.size - 1;
                
                final int id = editor.externalShape.size - 1;
                VisTextButton selectBtn = new VisTextButton("Shape " + id);
                selectBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent e, Actor a){
                        selectedShape = id;
                    }
                });
                selectBtn.setName("select" + id);
                content.add(selectBtn).expand().top().left().pad(10).colspan(3).row();
            }
        });
        content.add(newBtn).top().left().pad(10);
        
        VisTextButton delBtn = new VisTextButton("Remove Shape");
        delBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                if(editor.externalShape.size == 0) return;
                editor.externalShape.removeIndex(selectedShape);
                content.removeActor(content.findActor("select" + selectedShape));
                selectedShape = 0;
            }
        });
        content.add(delBtn).top().left().pad(10).expand().row();
        
        for(int i = 0; i < editor.externalShape.size; i++){
            final int id = i;
            VisTextButton selectBtn = new VisTextButton("Shape " + id);
            selectBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent e, Actor a){
                    selectedShape = id;
                }
            });
            selectBtn.setName("select" + id);
            content.add(selectBtn).expand().top().left().pad(10).colspan(3).row();
        }
    }

    @Override
    public void render(final Rectangle bounds, JSONObject part, Vector2 corner, Vector2 cursor){
        super.render(bounds, part, corner, cursor);
        render.begin(ShapeType.Filled);
        
        Vector2 screenScale;
        if(editor.renderExternal){
            float ratio = ((float)editor.externalTexture.getRegionHeight() / editor.externalTexture.getRegionWidth());
            screenScale = new Vector2(1.0f / editor.externalTexture.getRegionWidth(), 1.0f / (editor.externalTexture.getRegionWidth() * ratio)).scl(editor.partSize, editor.partSize * ratio);
        } else {
            float ratio = ((float)editor.internalTexture.getRegionHeight() / editor.internalTexture.getRegionWidth());
            screenScale = new Vector2(1.0f / editor.internalTexture.getRegionWidth(), 1.0f / (editor.internalTexture.getRegionWidth() * ratio)).scl(editor.partSize, editor.partSize * ratio);
        }
        
        render.setColor(Color.CYAN);
        float snapX = (int)(cursor.x * 2) / 2.0f;
        float snapY = (int)(cursor.y * 2) / 2.0f;
        render.circle(corner.x + snapX * screenScale.x, corner.y + snapY * screenScale.y, 4.0f);
        
        for(PhysShape shape : editor.externalShape){
            render.setColor((shape == editor.externalShape.get(selectedShape)) ? Color.GREEN : Color.RED);
            Array<Vector2> arr = shape.calculateHull();

            for(int i = 0; i < arr.size; i++){
                Vector2 v1 = arr.get(i);
                Vector2 v2 = arr.get((i + 1) % arr.size);
                render.rectLine(
                    corner.x + (v1.x + editor.center.x) * screenScale.x,
                    corner.y + (v1.y + editor.center.y) * screenScale.y,
                    corner.x + (v2.x + editor.center.x) * screenScale.x,
                    corner.y + (v2.y + editor.center.y) * screenScale.y,
                    2.0f
                );
            }

            for(Vector2 v : shape.vertices){
                render.circle(
                    corner.x + (v.x + editor.center.x) * screenScale.x,
                    corner.y + (v.y + editor.center.y) * screenScale.y,
                    4.0f
                );
            }
        }

        render.end();
    }
    
}
