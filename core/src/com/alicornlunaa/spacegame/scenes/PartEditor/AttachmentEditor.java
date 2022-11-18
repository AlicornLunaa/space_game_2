package com.alicornlunaa.spacegame.scenes.PartEditor;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kotcrab.vis.ui.widget.VisTable;

public class AttachmentEditor extends EditorPanel {

    // Constructor
    public AttachmentEditor(final App game, final PartEditor editor, final VisTable content){
        super(game, editor, content);

        controls = new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button){
                float snapX = (int)(cursor.x * 2) / 2.0f;
                float snapY = (int)(cursor.y * 2) / 2.0f;

                if(button == Buttons.LEFT){
                    editor.attachmentPoints.add(new Vector2(snapX - editor.center.x, snapY - editor.center.y));
                } else if(button == Buttons.RIGHT){
                    editor.attachmentPoints.removeValue(new Vector2(snapX - editor.center.x, snapY - editor.center.y), false);
                }

                return true;
            }
        };
    }

    // Functions
    @Override
    public void updateContent(){
        editor.renderExternal = true;
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
        
        render.setColor(Color.ORANGE);
        for(Vector2 point : editor.attachmentPoints){
            render.circle(corner.x + (point.x + editor.center.x) * screenScale.x, corner.y + (point.y + editor.center.y) * screenScale.y, 4.0f);
        }

        render.end();
    }
    
}
