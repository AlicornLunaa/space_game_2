package com.alicornlunaa.spacegame.scenes.PartEditor;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.kotcrab.vis.ui.widget.VisTable;

public class AttachmentEditor extends VisTable {

    // Variables
    private final ShapeRenderer render;

    // Constructor
    public AttachmentEditor(final App game){
        super();
        this.render = game.shapeRenderer;

        setFillParent(true);
        add(new PhysShape(game.shapeRenderer));
    }

    // Functions
    @Override
    public void draw(Batch batch, float alpha){
        batch.end();
        render.begin(ShapeType.Filled);
        
        super.draw(batch, alpha);

        render.end();
        batch.begin();
    }
    
}
