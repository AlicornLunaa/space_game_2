package com.alicornlunaa.spacegame.objects.ship2;

import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.objects.ship2.parts.Part;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

// Ship is a tree of parts, doublely linked
public class Ship extends BaseEntity {
    // Variables
    private final App game;
    private Part rootPart;

    // Constructor
    public Ship(final App game){
        this.game = game;

        rootPart = new Part(game, this, game.partManager.get("STRUCTURAL", "BSC_FUSELAGE"));
        rootPart.attach(3, 1, new Part(game, this, game.partManager.get("STRUCTURAL", "BSC_FUSELAGE")));
        
        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                rootPart.draw(batch, new Matrix4());
                
                batch.end();
                game.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                game.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
                game.shapeRenderer.begin(ShapeType.Filled);

                rootPart.drawAttachmentPoints(game.shapeRenderer, new Matrix4());

                game.shapeRenderer.end();
                batch.begin();
            }
        });
        addComponent(new CameraComponent(1280, 720)).camera.zoom = 0.1f;
    }

    // Functions
    public void setRootPart(Part p){
        this.rootPart = p;
    }
}
