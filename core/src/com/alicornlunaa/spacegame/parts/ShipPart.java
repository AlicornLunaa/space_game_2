package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.Assets;
import com.alicornlunaa.spacegame.util.PartManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
            return parent.localToWorld(new Vector2(position));
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
    private TextureRegion region;
    private PolygonShape shape = new PolygonShape();
    private ArrayList<Attachment> attachments = new ArrayList<Attachment>();

    private String partType;
    private String partId;

    private boolean drawAttachPoints = false;
    private static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Constructor
    public ShipPart(Body parent, TextureRegion texture, Vector2 size, Vector2 posOffset, float rotOffset, ArrayList<Vector2> attachmentPoints){
        this.parent = parent;
        region = texture;

        for(Vector2 p : attachmentPoints){
            attachments.add(new Attachment(this, p, attachments.size()));
        }

        setSize(size.x, size.y);
        setOrigin(size.x / 2.f - posOffset.x, size.y / 2.f - posOffset.y);
        setPosition(posOffset.x, posOffset.y);
        setRotation(rotOffset);

        shape.setAsBox(size.x / 2.f, size.y / 2.f, posOffset, rotOffset * (float)(Math.PI / 180.f));
    }

    // Functions
    public Attachment getClosestAttachment(Vector2 point){
        // Returns the closest attachment to the point
        if(attachments.size() <= 0) return null;
        
        Attachment closestAttach = attachments.get(0);
        float minDist = point.dst2(attachments.get(0).getGlobalPos());

        System.out.println(attachments.get(0).getGlobalPos());

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
        Vector2 aPosition = new Vector2(a.position).rotateDeg(a.getParent().getRotation());
        Vector2 tPosition = new Vector2(t.position).rotateDeg(t.getParent().getRotation());
        
        a.child = target;
        a.childAttachmentPoint = targetAttachment;
        target.attachments.get(targetAttachment).inUse = true;

        target.setPosition(
            getX() + aPosition.x - tPosition.x,
            getY() + aPosition.y - tPosition.y
        );

        ((PolygonShape)target.getShape()).setAsBox(
            getWidth() / 2.f,
            getHeight() / 2.f,
            new Vector2(
                getX() + aPosition.x - tPosition.x,
                getY() + aPosition.y - tPosition.y
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

    @Override
    public void setRotation(float r){
        super.setRotation(r);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void setPosition(float x, float y){
        super.setPosition(x, y);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

    @Override
    public void setSize(float w, float h){
        super.setSize(w, h);
        shape.setAsBox(getWidth() / 2.f, getHeight() / 2.f, new Vector2(getX(), getY()), getRotation() * (float)(180.f / Math.PI));
    }

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

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        batch.draw(region, -getOriginX(), -getOriginY());

        if(drawAttachPoints){
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

            for(Attachment a : attachments){
                shapeRenderer.setColor(a.child == null && !a.inUse ? Color.GREEN : Color.RED);
                shapeRenderer.circle(a.position.x, a.position.y, 2);
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
    private static ShipPart fromJSON(Assets manager, JSONObject data, Body parent, Vector2 posOffset, float rotOffset){
        // Part information is in parts_layout.md
        try {
            // Load part information from the json object
            String type = data.getString("type");
            String name = data.getString("name");
            String desc = data.getString("desc");
            String texture = data.getString("texture");
            TextureRegion region = new TextureRegion(
                manager.get(texture, Texture.class),
                data.getJSONObject("uv").getInt("x"),
                data.getJSONObject("uv").getInt("y"),
                data.getJSONObject("uv").getInt("width"),
                data.getJSONObject("uv").getInt("height")
            );
            Vector2 size = new Vector2(
                data.getJSONObject("scale").getFloat("width") * data.getJSONObject("scale").getFloat("scale"),
                data.getJSONObject("scale").getFloat("height") * data.getJSONObject("scale").getFloat("scale")
            );
            float density = data.getFloat("density");
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
                    return new Aero(parent, region, size, posOffset, rotOffset, attachmentPoints, name, desc, density, drag, lift);
                    
                case "STRUCTURAL":
                    float fuel = metadata.getFloat("fuelCapacity");
                    float battery = metadata.getFloat("batteryCapacity");
                    return new Structural(parent, region, size, posOffset, rotOffset, attachmentPoints, name, desc, density, fuel, battery);
                    
                case "THRUSTER":
                    float power = metadata.getFloat("power");
                    float cone = metadata.getFloat("cone");
                    float usage = metadata.getFloat("fuelUsage");
                    return new Thruster(parent, region, size, posOffset, rotOffset, attachmentPoints, name, desc, density, power, cone, usage);
            }
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error reading the part data");
            e.printStackTrace();
        }

        return null;
    }
    
    public static ShipPart spawn(Assets manager, PartManager partManager, String type, String id, Body parent, Vector2 posOffset, float rotOffset){
        ShipPart p = ShipPart.fromJSON(manager, partManager.get(type, id), parent, posOffset, rotOffset);
        p.partType = type;
        p.partId = id;
        return p;
    }

    public static ShipPart unserialize(Assets manager, PartManager partManager, Body body, JSONObject data){
        // Recursively builds the object from serialized data
        ShipPart p = ShipPart.spawn(
            manager,
            partManager,
            data.getString("type"),
            data.getString("id"),
            body,
            new Vector2(
                data.getFloat("x"),
                data.getFloat("y")
            ),
            data.getFloat("rotation")
        );

        p.setOrigin(data.getFloat("originX"), data.getFloat("originY"));

        // Load attachments
        JSONArray attachments = data.getJSONArray("attachments");

        for(int i = 0; i < attachments.length(); i++){
            JSONObject partData = attachments.getJSONObject(i).getJSONObject("partData");
            Attachment a = Attachment.unserialize(p, attachments.getJSONObject(i).getJSONObject("attachData"));
            a.child = ShipPart.unserialize(manager, partManager, body, partData);

            p.attachPart(a.child, a.childAttachmentPoint, a.thisAttachmentPoint);
        }

        return p;
    }

}
