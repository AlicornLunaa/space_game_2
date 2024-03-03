package com.alicornlunaa.space_game.grid.entities;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.scenes.GameScene;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Null;

public class ControlSeatTile extends TileEntity {
    // Variables
    private ComponentMapper<TransformComponent> transformMapper = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bodyMapper = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<CameraComponent> cameraMapper = ComponentMapper.getFor(CameraComponent.class);
    private ComponentMapper<ShipComponent> shipMapper = ComponentMapper.getFor(ShipComponent.class);
    private @Null Entity driver = null;
    private TextureRegion texture;

    // Constructor
    public ControlSeatTile(int rotation) {
        super("controlseat", 1, 2, rotation);
        texture = App.instance.atlas.findRegion("parts/control_seat");
    }
    
    // Functions
    @Override
    public void render(Batch batch, float deltaTime){
        batch.draw(
            texture,
            x * Constants.TILE_SIZE,
            y * Constants.TILE_SIZE,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE / 2.f,
            Constants.TILE_SIZE * width,
            Constants.TILE_SIZE * height,
            1,
            1,
            rotation * -90
        );
    }

    @Override
    public void update(Entity entity, float deltaTime) {
    }

    @Override
    public boolean click(Entity entity, Entity interactor, int button){
        // Check to make sure a game scene is active
        GameScene scene = App.instance.getScene();

        if(scene == null)
            return false;

        // Get components needed
        CameraComponent cameraComp = cameraMapper.get(entity);
        ShipComponent shipComp = shipMapper.get(entity);

        // Handle interaction
        switch(button){
            case Buttons.LEFT:
                if(driver == null){
                    driver = interactor;
                    scene.getEngine().removeEntity(interactor);
                } else {
                    TransformComponent entityTransform = transformMapper.get(entity);
                    TransformComponent interactorTransform = transformMapper.get(driver);
                    BodyComponent entityBodyComp = bodyMapper.get(entity);
                    BodyComponent interactorBodyComp = bodyMapper.get(driver);

                    if(entityTransform != null && interactorTransform != null){
                        Vector2 localExitPos = new Vector2(x * Constants.TILE_SIZE - Constants.TILE_SIZE, y * Constants.TILE_SIZE + Constants.TILE_SIZE);
                        localExitPos.rotateRad(entityTransform.rotation);

                        interactorTransform.position.set(entityTransform.position);
                        interactorTransform.position.add(localExitPos);
                        interactorTransform.rotation = entityTransform.rotation;
                    }

                    if(entityBodyComp != null && interactorBodyComp != null && entityBodyComp.body != null){
                        interactorBodyComp.bodyDef.linearVelocity.set(entityBodyComp.body.getLinearVelocity());
                        interactorBodyComp.bodyDef.angularVelocity = entityBodyComp.body.getAngularVelocity();
                    }

                    scene.getEngine().addEntity(driver);
                    driver = null;
                }

                if(cameraComp != null)
                    cameraComp.active = (driver != null);
                
                if(shipComp != null)
                    shipComp.controlEnabled = (driver != null);

                return true;
        }

        return false;
    }
}
