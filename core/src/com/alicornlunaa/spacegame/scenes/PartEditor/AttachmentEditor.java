package com.alicornlunaa.spacegame.scenes.PartEditor;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class AttachmentEditor extends EditorPanel {

    // Variables
    private PhysShape shape;

    // Constructor
    public AttachmentEditor(final App game){
        super(game);
        shape = new PhysShape(game.shapeRenderer);
    }

    // Functions
    @Override
    public void render(final Rectangle bounds, JSONObject part, Vector2 cursor){
        render.begin(ShapeType.Filled);
        
        // shape.draw(batch, alpha);
        render.circle(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, 10.0f);

        render.end();
    }
    
}
