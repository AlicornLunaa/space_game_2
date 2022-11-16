package com.alicornlunaa.spacegame.scenes.PartEditor;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.kotcrab.vis.ui.widget.VisTable;

public class AttachmentEditor extends VisTable {

    // Variables
    private final ShapeRenderer render;
    private PhysShape shape;

    // Constructor
    public AttachmentEditor(final App game){
        super();
        this.render = game.shapeRenderer;
        setFillParent(true);
        add().expand().fill();

        shape = new PhysShape(game.shapeRenderer);
    }

    // Functions
    public void render(final Rectangle bounds){
        render.begin(ShapeType.Filled);
        
        // shape.draw(batch, alpha);
        render.circle(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, 10.0f);

        render.end();
    }
    
}
