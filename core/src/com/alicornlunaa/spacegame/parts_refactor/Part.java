package com.alicornlunaa.spacegame.parts_refactor;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.PhysicsCollider;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.states.ShipState;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * A Part is an object that gets attached to a ship. It should
 * contain a collider of its self, attachment points, physics
 * information, and a reference to the ship's state.
 * 
 * Drawing this shape is always in ship-local coordinates
 * as the batch will use a transformation matrix at the ship
 */
public class Part {

    // Variables
    protected Body parent;
    protected ShipState stateRef;

    private TextureRegion texture;
    private PhysicsCollider externalCollider;
    private PhysicsCollider internalCollider;

    private String type;
    private String id;
    private String name;
    private String description;
    private boolean flipX = false;
    private boolean flipY = false;
    private Vector2 position = new Vector2();
    private Vector2 size = new Vector2();
    private float rotation = 0.0f;

    // Constructors
    public Part(final Ship ship, final TextureRegion texture, String type, String id, String name, String desc){
        parent = ship.getBody();
        stateRef = ship.state;

        this.type = type;
        this.id = id;
        this.name = name;
        this.description = desc;

        this.texture = texture;
        size.set(texture.getRegionWidth(), texture.getRegionHeight());

        externalCollider = PhysicsCollider.box(Vector2.Zero.cpy(), size.cpy(), 0.0f);
        internalCollider = PhysicsCollider.box(Vector2.Zero.cpy(), size.cpy(), 0.0f);
    }
    
    public Part(final Ship ship, JSONObject obj){
        // TODO: Implement JSONObject constructor
    }

    // Functions
    public void setParent(Body b, float physScale){
        parent = b;
        externalCollider.setScale(1 / physScale);
        externalCollider.attachCollider(b);
    }

    public void draw(Batch batch, float delta){
        drawEffectsBelow(batch, delta);
        batch.draw(
            texture,
            position.x, position.y,
            size.x / 2, size.y / 2,
            size.x, size.y,
            flipX ? -1 : 1, flipY ? -1 : 1,
            rotation
        );
        drawEffectsAbove(batch, delta);
    }

    public JSONObject serialize(){
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("id", id);
        obj.put("x", position.x);
        obj.put("y", position.y);
        obj.put("rotation", rotation);
        obj.put("flipX", flipX);
        obj.put("flipY", flipY);

        return obj;
    }

    protected void drawEffectsAbove(Batch batch, float delta){}
    protected void drawEffectsBelow(Batch batch, float delta){}

    // Getters & setters
    public PhysicsCollider getInternalCollider(){ return internalCollider; }
    public void setFlipX(){ flipX = !flipX; }
    public void setFlipY(){ flipY = !flipY; }
    public boolean getFlipX(){ return flipX; }
    public boolean getFlipY(){ return flipY; }

    // Static functions
    public static Part spawn(final App game, String type, String id){
        // TODO: Implement part spawning
        return null;
    }

    public static Part unserialize(final App game, final Ship ship, JSONObject obj){
        String type = obj.getString("type");
        String id = obj.getString("type");
        float x = obj.getFloat("x");
        float y = obj.getFloat("y");
        float rotation = obj.getFloat("rotation");
        boolean flipX = obj.getBoolean("flipX");
        boolean flipY = obj.getBoolean("flipY");

        Part newPart = Part.spawn(game, type, id);
        newPart.position.set(x, y);
        newPart.rotation = rotation;
        newPart.flipX = flipX;
        newPart.flipY = flipY;

        return newPart;
    }
    
}
