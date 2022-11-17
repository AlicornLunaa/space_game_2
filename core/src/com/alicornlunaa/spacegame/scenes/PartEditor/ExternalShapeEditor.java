package com.alicornlunaa.spacegame.scenes.PartEditor;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;

public class ExternalShapeEditor extends EditorPanel {

    // Variables
    private Array<PhysShape> shapes = new Array<>();

    // Constructor
    public ExternalShapeEditor(final App game, final PartEditor editor){
        super(game, editor);

        controls = new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                return false;
            }

            @Override
            public boolean keyDown(InputEvent e, int keycode){
                return false;
            }
        };
    }

    // Functions
    @Override
    public void render(final Rectangle bounds, JSONObject part, Vector2 corner, Vector2 cursor){
        super.render(bounds, part, corner, cursor);
        render.begin(ShapeType.Filled);
        
        // shape.draw(batch, alpha);
        render.circle(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, 2.0f);

        render.end();
    }
    
}
