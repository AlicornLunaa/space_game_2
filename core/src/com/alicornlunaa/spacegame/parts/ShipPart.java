package com.alicornlunaa.spacegame.parts;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.spacegame.util.Assets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ShipPart extends Actor {
    // Classes
    public static class Attachment {
        private Vector2 position = new Vector2(0, 0);
        private ShipPart parent = null; // The current part with the attachment
        private ShipPart child = null; // The child part being attached
        private int childAttachmentPoint = 0; // Which attachment on the child is being used
        private boolean inUse = false; // True when this part is parented to something using this attachment

        Attachment(ShipPart parent, Vector2 position){
            this.parent = parent;
            this.position = position;
        }

        Attachment(ShipPart parent, Vector2 position, ShipPart child){
            this(parent, position);
            this.child = child;
        }

        public Vector2 getPos(){ return position; }
        public ShipPart getParent(){ return parent; }
        public ShipPart getChild(){ return child; }
    };

    // Variables
    protected Body parent;
    private TextureRegion region;
    private PolygonShape shape = new PolygonShape();
    private ArrayList<Attachment> attachments = new ArrayList<Attachment>();
    
    private boolean drawAttachPoints = true;

    private static final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Constructor
    public ShipPart(Body parent, TextureRegion texture, Vector2 size, Vector2 posOffset, float rotOffset, ArrayList<Vector2> attachmentPoints){
        this.parent = parent;
        region = texture;

        for(Vector2 p : attachmentPoints){
            attachments.add(new Attachment(this, p));
        }

        setSize(size.x, size.y);
        setOrigin(size.x / 2.f - posOffset.x, size.y / 2.f - posOffset.y);
        setPosition(posOffset.x, posOffset.y);
        setRotation(rotOffset);

        shape.setAsBox(size.x / 2.f, size.y / 2.f, posOffset, rotOffset * (float)(Math.PI / 180.f));
    }

    // Functions
    public Attachment getClosestAttachment(Vector2 point, float radius){
        // Returns the closest point to the mouse
        if(attachments.size() <= 0) return null;

        Attachment closestAttach = attachments.get(0);
        float minDist = (closestAttach.position.dst2(point));
        for(int i = 1; i < attachments.size(); i++){
            Vector2 curPoint = attachments.get(i).position;
            float curDist = (curPoint.dst2(point));
            
            if(curDist < minDist){
                closestAttach = attachments.get(i);
                minDist = curDist;
            }
        }

        if(minDist < (radius * radius)){
            return closestAttach;
        }

        return null;
    }

    public ShipPart attachPart(ShipPart target, int targetAttachment, int thisAttachment){
        /** Attaches TARGET to THIS. */
        Attachment a = attachments.get(thisAttachment);
        
        if(a.child == null){
            a.child = target;
            a.childAttachmentPoint = targetAttachment;
            target.attachments.get(targetAttachment).inUse = true;

            target.setPosition(
                getX() + a.position.x - target.attachments.get(targetAttachment).position.x,
                getY() + a.position.y - target.attachments.get(targetAttachment).position.y
            );

            ((PolygonShape)target.getShape()).setAsBox(
                getWidth() / 2.f,
                getHeight() / 2.f,
                new Vector2(
                    getX() + a.position.x - target.attachments.get(targetAttachment).position.x,
                    getY() + a.position.y - target.attachments.get(targetAttachment).position.y
                ),
                getRotation() * (float)(Math.PI / 180.f)
            );

            return target;
        }

        return null;
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
        float rot = (parent.getAngle() * (float)(180.f / Math.PI)) + getRotation();
        Vector2 pos = new Vector2(getX(), getY()).rotateDeg(rot);
        float x = parent.getPosition().x + pos.x - getWidth() / 2;
        float y = parent.getPosition().y + pos.y - getHeight() / 2;

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        batch.draw(
            region,
            x,
            y,
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX(),
            getScaleY(),
            rot
        );
        
        for(Attachment attachment : getAttachments()){
            if(attachment.getChild() != null){
                attachment.getChild().draw(batch, parentAlpha);
            }
        }

        if(drawAttachPoints){
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

            for(Attachment a : attachments){
                Vector2 local = new Vector2(a.position.x, a.position.y).rotateDeg(parent.getAngle());

                shapeRenderer.setColor(a.child == null && !a.inUse ? Color.GREEN : Color.RED);
                shapeRenderer.circle(parent.getPosition().x + getX() + local.x, parent.getPosition().y + getY() + local.y, 2);
            }

            shapeRenderer.end();
            batch.begin();
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

    // Static methods
    public static ShipPart fromJSON(Assets manager, JSONObject data, Body parent, Vector2 posOffset, float rotOffset){
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
}
