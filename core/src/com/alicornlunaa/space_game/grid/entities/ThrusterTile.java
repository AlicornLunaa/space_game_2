package com.alicornlunaa.space_game.grid.entities;

import org.json.JSONArray;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class ThrusterTile extends TileEntity {
    // Variables
    private ComponentMapper<TransformComponent> transMapper = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bodyMapper = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<ShipComponent> shipMapper = ComponentMapper.getFor(ShipComponent.class);
    private TextureRegion texture;

    // Constructor
    public ThrusterTile(int rotation) {
        super("thruster", 1, 1, rotation);

        collider = new Collider(new JSONArray(Gdx.files.internal("assets/colliders/parts/bsc_thruster.json").readString()));
        texture = App.instance.atlas.findRegion("parts/bsc_thruster");
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
        TransformComponent transform = transMapper.get(entity);
        BodyComponent bodyComp = bodyMapper.get(entity);
        ShipComponent shipComp = shipMapper.get(entity);

        if(shipComp != null && bodyComp != null){
            Vector2 forward = new Vector2(-(float)Math.sin(transform.rotation + rotation * -Math.PI / 2), (float)Math.cos(transform.rotation + rotation * -Math.PI / 2));
            bodyComp.body.applyForceToCenter(forward.x * shipComp.throttle * 0.1f, forward.y * shipComp.throttle * 0.1f, true);
        }
    }
}
