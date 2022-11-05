package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.states.ShipState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShipPart extends Entity {

    // Classes
    public static class Attachment {
        private Vector2 position = new Vector2(0, 0);
        private ShipPart parent = null; // The current part with the attachment
        private ShipPart child = null; // The child part being attached
        private int thisAttachmentPoint = 0;
        private int childAttachmentPoint = 0; // Which attachment on the child is being used
        private boolean inUse = false; // True when this part is parented to something using this attachment

        Attachment(ShipPart parent, Vector2 position, int thisAttachmentPoint){
            this.parent = parent;
            this.position = position;
            this.thisAttachmentPoint = thisAttachmentPoint;
        }

        Attachment(ShipPart parent, Vector2 position, int thisAttachmentPoint, ShipPart child){
            this(parent, position, thisAttachmentPoint);
            this.child = child;
        }

        public Vector2 getPos(){ return position; }
        public ShipPart getParent(){ return parent; }
        public ShipPart getChild(){ return child; }
        public boolean getInUse(){ return inUse; }
        public int getThisId(){ return thisAttachmentPoint; }

        public Vector2 getGlobalPos(){
            return parent.localToWorld(new Vector2(position).scl(parent.getFlipX() ? -1 : 1, parent.getFlipY() ? -1 : 1));
        }

        public JSONObject serialize(){
            JSONObject data = new JSONObject();
            data.put("x", position.x);
            data.put("y", position.y);
            data.put("thisAttachmentPoint", thisAttachmentPoint);
            data.put("childAttachmentPoint", childAttachmentPoint);
            data.put("inUse", inUse);
            return data;
        }

        public static Attachment unserialize(ShipPart parent, JSONObject data){
            Attachment a = new Attachment(
                parent,
                new Vector2(
                    data.getFloat("x"),
                    data.getFloat("y")
                ),
                data.getInt("thisAttachmentPoint")
            );

            a.childAttachmentPoint = data.getInt("childAttachmentPoint");
            a.inUse = data.getBoolean("inUse");

            return a;
        }
    };

    // Variables
    protected Body parent;
    protected ShipState stateRef;
    private TextureRegion region;
    private PolygonShape shape = new PolygonShape();
    private ArrayList<Attachment> attachments = new ArrayList<Attachment>();

    private String partType;
    private String partId;
    private boolean partFlipX = false;
    private boolean partFlipY = false;

    private boolean drawAttachPoints = false;
    protected static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Constructor
    public ShipPart(Body parent, ShipState stateRef, TextureRegion region, float scale, Vector2 pos, float rot, ArrayList<Vector2> attachmentPoints){
        this.parent = parent;
        this.stateRef = stateRef;
        this.region = region;

        for(Vector2 p : attachmentPoints){
            attachments.add(new Attachment(this, p, attachments.size()));
        }

        setSize(region.getRegionWidth() * scale, region.getRegionHeight() * scale);
        setOrigin(getWidth() / 2.f - pos.x, getHeight() / 2.f - pos.y);
        setPosition(pos.x, pos.y);
        setRotation(rot);

        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, pos, rot * (float)(Math.PI / 180.f));
    }

    // Functions
    public Attachment getClosestAttachment(Vector2 point){
        // Returns the closest attachment to the point
        if(attachments.size() <= 0) return null;
        
        Attachment closestAttach = attachments.get(0);
        float minDist = point.dst2(closestAttach.getGlobalPos());

        for(int i = 1; i < attachments.size(); i++){
            Vector2 curPoint = attachments.get(i).getGlobalPos();
            float curDist = point.dst2(curPoint);
            
            if(curDist < minDist){
                closestAttach = attachments.get(i);
                minDist = curDist;
            }
        }

        return closestAttach;
    }

    public ShipPart attachPart(ShipPart target, int targetAttachment, int thisAttachment){
        /** Attaches TARGET to THIS. */
        Attachment a = attachments.get(thisAttachment);
        Attachment t = target.attachments.get(targetAttachment);
        Vector2 aPosition = new Vector2(a.position).scl(a.getParent().getFlipX() ? -1 : 1, a.getParent().getFlipY() ? -1 : 1).rotateDeg(a.getParent().getRotation());
        Vector2 tPosition = new Vector2(t.position).scl(t.getParent().getFlipX() ? -1 : 1, t.getParent().getFlipY() ? -1 : 1).rotateDeg(t.getParent().getRotation());
        
        a.child = target;
        a.childAttachmentPoint = targetAttachment;
        target.attachments.get(targetAttachment).inUse = true;

        target.setPosition(
            getX() + aPosition.x - tPosition.x,
            getY() + aPosition.y - tPosition.y
        );

        ((PolygonShape)target.getShape()).setAsBox(
            target.getWidth() / 2.f,
            target.getHeight() / 2.f,
            new Vector2(
                (getX() + aPosition.x - tPosition.x),
                (getY() + aPosition.y - tPosition.y)
            ),
            t.getParent().getRotation() * (float)(Math.PI / 180.f)
        );

        target.parent = parent;

        return target;
    }

    public boolean detachPart(int attachment){
        Attachment a = attachments.get(attachment);

        if(a.child != null){
            a.child.attachments.get(a.childAttachmentPoint).inUse = false;
            a.child = null;
            return true;
        }

        return false;
    }

    public ArrayList<Attachment> getAttachments(){
        return attachments;
    }

    public Shape getShape(){ return shape; }

    public void drawPoints(boolean draw){ this.drawAttachPoints = draw; }

    public void flipX(){
        this.partFlipX = !this.partFlipX;
        setScaleX(getScaleX() * -1);
    }

    public void flipY(){
        this.partFlipY = !this.partFlipY;
        setScaleY(getScaleY() * -1);
    }

    public boolean getFlipX(){ return partFlipX; }
    
    public boolean getFlipY(){ return partFlipY; }

    public ShipPart hit(Vector2 pos, Matrix3 transform){
        Vector2 local = new Vector2(pos).mul(new Matrix3(transform).mul(getTransform()).inv());
        ShipPart part = (ShipPart)hit(local.x + getOriginX(), local.y + getOriginY(), false);

        // This part was not hit
        if(part == null){
            for(Attachment a : attachments){
                if(a.getChild() != null){
                    // Check children for a hit
                    part = (ShipPart)a.getChild().hit(pos, transform);

                    if(part != null){
                        // Child was hit, end loop
                        break;
                    }
                }
            }
        }

        return part;
    }

    protected void drawEffects(Batch batch, float deltaTime){}
    protected void drawEffectsUnder(Batch batch, float deltaTime){}

    @Override
    public void act(float delta){
        super.act(delta);

        for(Attachment attachment : getAttachments()){
            if(attachment.getChild() != null){
                attachment.getChild().act(delta);
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Matrix4 batchMatrix = new Matrix4(batch.getTransformMatrix());
        Matrix3 transform = getTransform();
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform)));

        Color bc = batch.getColor();
        Color c = this.getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        drawEffectsUnder(batch, Gdx.graphics.getDeltaTime());
        batch.draw(region, -getOriginX(), -getOriginY());
        drawEffects(batch, Gdx.graphics.getDeltaTime());

        batch.setColor(bc);

        if(drawAttachPoints){
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

            for(Attachment a : attachments){
                shapeRenderer.setColor(a.child == null && !a.inUse ? Color.GREEN : Color.RED);
                shapeRenderer.circle(a.position.x, a.position.y, 1);
            }

            shapeRenderer.end();
            batch.begin();
        }
        
        batch.setTransformMatrix(batchMatrix);
        
        for(Attachment attachment : getAttachments()){
            if(attachment.getChild() != null){
                attachment.getChild().draw(batch, parentAlpha);
            }
        }
    }

    @Override
    public void setColor(float r, float g, float b, float a){
        super.setColor(r, g, b, a);

        for(Attachment attachment : getAttachments()){
            if(attachment.getChild() != null){
                attachment.getChild().setColor(r, g, b, a);
            }
        }
    }

    @Override
    public boolean remove(){
        for(Attachment attachment : getAttachments()){
            if(attachment.getChild() != null){
                attachment.getChild().remove();
            }
        }

        shape.dispose();
        return super.remove();
    }

    public JSONObject serialize(){
        JSONObject data = new JSONObject();
        data.put("type", partType);
        data.put("id", partId);
        data.put("x", getX());
        data.put("y", getY());
        data.put("originX", getOriginX());
        data.put("originY", getOriginY());
        data.put("rotation", getRotation());
        data.put("flipX", getFlipX());
        data.put("flipY", getFlipY());

        // Recursively serialize every additional part
        JSONArray attachments = new JSONArray();

        for(Attachment a : this.attachments){
            if(a.getChild() != null){
                JSONObject attachment = new JSONObject();
                JSONObject attachData = a.serialize();
                JSONObject partData = a.getChild().serialize();
                attachment.put("attachData", attachData);
                attachment.put("partData", partData);
                attachments.put(attachment);
            }
        }

        data.put("attachments", attachments);

        return data;
    }

    // Static methods
    private static ShipPart fromJSON(final App game, JSONObject data, Body parent, ShipState stateRef, Vector2 posOffset, float rotOffset){
        // Part information is in parts_layout.md
        try {
            // Load part information from the json object
            String type = data.getString("type");
            String id = data.getString("id");
            String name = data.getString("name");
            String desc = data.getString("desc");
            float scale = data.getFloat("scale");
            float density = data.getFloat("density");
            TextureRegion region = game.atlas.findRegion(String.format("parts/%s/%s", type.toLowerCase(), id.toLowerCase()));

            ArrayList<Vector2> attachmentPoints = new ArrayList<Vector2>();
            JSONArray points = data.getJSONArray("attachmentPoints");
            for(int i = 0; i < points.length(); i++){
                JSONObject o = points.getJSONObject(i);
                attachmentPoints.add(new Vector2(o.getFloat("x"), o.getFloat("y")));
            }

            JSONObject metadata = data.getJSONObject("metadata");
            switch(type){
                case "AERO":
                    float drag = metadata.getFloat("drag");
                    float lift = metadata.getFloat("lift");
                    return new Aero(
                        parent,
                        stateRef,
                        region,
                        scale,
                        posOffset,
                        rotOffset,
                        attachmentPoints,
                        name,
                        desc,
                        density,
                        drag,
                        lift
                    );
                    
                case "STRUCTURAL":
                    float fuel = metadata.getFloat("fuelCapacity");
                    float battery = metadata.getFloat("batteryCapacity");
                    return new Structural(
                        parent,
                        stateRef,
                        region,
                        scale,
                        posOffset,
                        rotOffset,
                        attachmentPoints,
                        name,
                        desc,
                        density,
                        fuel,
                        battery
                    );
                    
                case "THRUSTER":
                    float power = metadata.getFloat("power");
                    float cone = metadata.getFloat("cone");
                    float usage = metadata.getFloat("fuelUsage");
                    String effect = metadata.getString("effect");
                    return new Thruster(
                        game,
                        parent,
                        stateRef,
                        region,
                        scale,
                        posOffset,
                        rotOffset,
                        attachmentPoints,
                        name,
                        desc,
                        density,
                        power,
                        cone,
                        usage,
                        effect
                    );
                    
                case "RCSPORT":
                    float rcspower = metadata.getFloat("power");
                    float rcsusage = metadata.getFloat("fuelUsage");
                    return new RCSPort(
                        game,
                        parent,
                        stateRef,
                        region,
                        scale,
                        posOffset,
                        rotOffset,
                        attachmentPoints,
                        name,
                        desc,
                        density,
                        rcspower,
                        rcsusage
                    );
            }
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error reading the part data");
            e.printStackTrace();
        }

        return null;
    }
    
    public static ShipPart spawn(final App game, String type, String id, Body parent, ShipState stateRef, Vector2 posOffset, float rotOffset){
        ShipPart p = ShipPart.fromJSON(game, game.partManager.get(type, id), parent, stateRef, posOffset, rotOffset);
        p.partType = type;
        p.partId = id;
        return p;
    }

    public static ShipPart unserialize(final App game, Body b, ShipState stateRef, JSONObject data){
        // Recursively builds the object from serialized data
        ShipPart p = ShipPart.spawn(
            game,
            data.getString("type"),
            data.getString("id"),
            b,
            stateRef,
            new Vector2(
                data.getFloat("x"),
                data.getFloat("y")
            ),
            data.getFloat("rotation")
        );

        p.setOrigin(data.getFloat("originX"), data.getFloat("originY"));

        if(data.getBoolean("flipX")) p.flipX();
        if(data.getBoolean("flipY")) p.flipY();

        // Load attachments
        JSONArray attachments = data.getJSONArray("attachments");

        for(int i = 0; i < attachments.length(); i++){
            JSONObject partData = attachments.getJSONObject(i).getJSONObject("partData");
            Attachment a = Attachment.unserialize(p, attachments.getJSONObject(i).getJSONObject("attachData"));
            a.child = ShipPart.unserialize(game, b, stateRef, partData);

            p.attachPart(a.child, a.childAttachmentPoint, a.thisAttachmentPoint);
        }

        return p;
    }

}
