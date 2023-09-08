package com.alicornlunaa.spacegame.objects.ship.parts;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.phys.PhysicsCollider;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.ship.ShipState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

/**
 * A Part is an object that gets attached to a ship. It should
 * contain a collider of its self, attachment points, physics
 * information, and a reference to the ship's state.
 * 
 * Drawing this shape is always in ship-local coordinates
 * as the batch will use a transformation matrix at the ship
 */
public class Part implements Comparable<Part> {

    // Variables
    protected Body parent;
    protected ShipState stateRef;
    protected float physScale;

    private TextureRegion texture;
    private PhysicsCollider collider;
    private Array<Vector2> attachmentPoints = new Array<>();

    private String type;
    private String id;
    private String name;
    private String description;
    private int interiorSize;
    private boolean freeform = false;
    private boolean flipX = false;
    private boolean flipY = false;
    private Vector2 position = new Vector2();
    private Vector2 size = new Vector2();
    private float rotation = 0.0f;

    // Constructors
    public Part(final Ship ship, final TextureRegion texture, String type, String id, String name, String desc){
        parent = ship.getComponent(BodyComponent.class).body;
        stateRef = ship.state;

        this.type = type;
        this.id = id;
        this.name = name;
        this.description = desc;

        this.texture = texture;
        size.set(texture.getRegionWidth(), texture.getRegionHeight());

        collider = PhysicsCollider.box(Vector2.Zero.cpy(), size.cpy(), 0.0f);
    }
    
    public Part(final App game, final Ship ship, JSONObject obj){
        parent = ship.getComponent(BodyComponent.class).body;
        stateRef = ship.state;

        type = obj.getString("type");
        id = obj.getString("id");
        name = obj.getString("name");
        description = obj.getString("desc");
        interiorSize = obj.getInt("interiorSize");
        freeform = obj.optBoolean("freeform", false);

        texture = game.atlas.findRegion("parts/" + id.toLowerCase());
        size.set(texture.getRegionWidth(), texture.getRegionHeight());

        collider = new PhysicsCollider(new JSONArray(Gdx.files.internal("colliders/parts/" + id.toLowerCase() + ".json").readString()));
        
        for(int i = 0; i < obj.getJSONArray("attachmentPoints").length(); i++){
            JSONObject vec = obj.getJSONArray("attachmentPoints").getJSONObject(i);
            attachmentPoints.add(new Vector2(vec.getFloat("x"), vec.getFloat("y")));
        }
    }

    // Functions
    public void setParent(Body b, float physScale){
        parent = b;
        collider.setScale(1 / physScale);
        collider.setPosition(position.cpy().scl(1 / physScale));
        collider.setRotation(rotation);
        collider.attachCollider(b);
        this.physScale = physScale;
    }

    public void draw(Batch batch, float delta){
        drawEffectsBelow(batch, delta);
        batch.draw(
            texture,
            position.x - size.x / 2, position.y - size.y / 2,
            size.x / 2, size.y / 2,
            size.x, size.y,
            flipX ? -1 : 1, flipY ? -1 : 1,
            rotation
        );
        drawEffectsAbove(batch, delta);
    }

    public boolean hit(Vector2 p){
        // Convert point into part-space coordinates
        Matrix3 trans = new Matrix3().translate(getX(), getY()).rotate(getRotation()).scale(flipX ? -1 : 1, flipY ? -1 : 1).inv();
        Vector2 localPoint = p.cpy().mul(trans);
        return ((localPoint.x < size.x / 2 && localPoint.x > size.x / -2) && (localPoint.y < size.y / 2 && localPoint.y > size.y / -2));
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
    public void update(float delta){}
    public void dispose(){}


    // Getters & setters
    public boolean getFreeform(){ return freeform; }
    public int getInteriorSize(){ return interiorSize; }
    public void setFlipX(){ flipX = !flipX; }
    public void setFlipY(){ flipY = !flipY; }
    public boolean getFlipX(){ return flipX; }
    public boolean getFlipY(){ return flipY; }
    public void setX(float x){ position.x = x; }
    public void setY(float y){ position.y = y; }
    public float getX(){ return position.x; }
    public float getY(){ return position.y; }
    public float getWidth(){ return size.x; }
    public float getHeight(){ return size.y; }
    public void setRotation(float rot){ rotation = rot; }
    public float getRotation(){ return rotation; }
    public String getName(){ return name; }
    public String getDescription(){ return description; }
    public Array<Vector2> getAttachmentPoints(){ return attachmentPoints; }

    @Override
    public int compareTo(Part o) {
        if(o.getX() == getX() && o.getY() == getY()) return 0;
        if(o.getX() + o.getY() > getX() + getY()) return 1;
        if(o.getX() + o.getY() < getX() + getY()) return -1;
        return 0;
    }

    // Static functions
    public static Part spawn(final App game, final Ship ship, String type, String id){
        // Load part information from the json object
        switch(type){
            case "AERO":
                return new Aero(game, ship, game.partManager.get(type, id));
                
            case "STRUCTURAL":
                return new Structural(game, ship, game.partManager.get(type, id));
                
            case "THRUSTER":
                return new Thruster(game, ship, game.partManager.get(type, id));
                
            case "RCSPORT":
                return new RCSPort(game, ship, game.partManager.get(type, id));

            default:
                return null;
        }
    }

    public static Part unserialize(final App game, final Ship ship, JSONObject obj){
        String type = obj.getString("type");
        String id = obj.getString("id");
        float x = obj.getFloat("x");
        float y = obj.getFloat("y");
        float rotation = obj.getFloat("rotation");
        boolean flipX = obj.getBoolean("flipX");
        boolean flipY = obj.getBoolean("flipY");

        Part newPart = Part.spawn(game, ship, type, id);
        newPart.position.set(x, y);
        newPart.rotation = rotation;
        newPart.flipX = flipX;
        newPart.flipY = flipY;

        return newPart;
    }
    
}
