package com.alicornlunaa.spacegame.objects.ship2;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.DriveableEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.objects.ship2.parts.Part;
import com.alicornlunaa.spacegame.objects.ship2.parts.Thruster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

// Ship is a tree of parts, doublely linked
public class Ship extends DriveableEntity {
    // Variables
    private TransformComponent transform = getComponent(TransformComponent.class);
    private BodyComponent bodyComponent;
    private Part rootPart;

    // Private functions
    private void generateExterior(PhysWorld world){
        // Create exterior body for the real-world scenes
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        
        bodyComponent = addComponent(new BodyComponent(world, def){
            @Override
            public void afterWorldChange(PhysWorld world){
                Ship.super.afterWorldChange(world);

                if(rootPart != null){
                    rootPart.setParent(Ship.this, 0, 0);
                }
            }
        });
        
        bodyComponent.setWorld(world);
        bodyComponent.body.setTransform(-1, 0, 3.14f / 4 * 0);
    }

    // Constructor
    public Ship(final App game, PhysWorld world, float x, float y, float rotation){
        super(game);
        generateExterior(world);

        rootPart = new Part(game, this, game.partManager.get("STRUCTURAL", "BSC_FUSELAGE"));
        rootPart.attach(1, 3, new Part(game, this, game.partManager.get("STRUCTURAL", "BSC_DEBUG_STRUCT")))
            .attach(1, 0, new Thruster(game, this, game.partManager.get("THRUSTER", "BSC_THRUSTER")));
        rootPart.setParent(this, 0, 0);
        
        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                Vector2 localCenter = bodyComponent.body.getLocalCenter().cpy().scl(bodyComponent.world.getPhysScale());
                Matrix4 trans = batch.getTransformMatrix().cpy().translate(-localCenter.x, -localCenter.y, 0);
                rootPart.draw(batch, trans.cpy());
                
                batch.end();
                game.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                game.shapeRenderer.setTransformMatrix(trans);
                game.shapeRenderer.begin(ShapeType.Filled);

                rootPart.drawDebug(game.shapeRenderer);
                rootPart.drawAttachmentPoints(game.shapeRenderer, trans.cpy());

                game.shapeRenderer.end();
                batch.begin();
            }
        });
        addComponent(new IScriptComponent() {
            @Override
            public void update() {
                if(rootPart != null)
                    rootPart.tick(Gdx.graphics.getDeltaTime());
            }

            @Override
            public void render() {}
        });
        addComponent(new CameraComponent(1280, 720)).camera.zoom = 0.4f;
    }

    // Functions
    public void setRootPart(Part p){
        this.rootPart = p;
    }

    public TransformComponent getTransform(){
        return transform;
    }

    public BodyComponent getBody(){
        return bodyComponent;
    }
}
