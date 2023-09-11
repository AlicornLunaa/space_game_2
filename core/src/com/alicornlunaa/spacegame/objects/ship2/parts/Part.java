package com.alicornlunaa.spacegame.objects.ship2.parts;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship2.Ship;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class Part implements Disposable {
    // Inner classes
    public static class Node {
        private @Null Node previous = null;
        private @Null Node next = null;

        private Vector2 point;
        private Part part;

        private Node(Part part, Vector2 point){
            this.part = part;
            this.point = point.cpy();
        }

        private Node(Part part, float x, float y){
            this.part = part;
            this.point = new Vector2(x, y);
        }
    };

    // Variables
    protected Ship parent;
    private TextureRegion texture;
    private Collider collider;
    private Array<Node> attachments = new Array<>();

    private String type;
    private String id;
    private String name;
    private String description;
    private float partScale = 1;
    private int interiorSize;
    private boolean freeform = false;
    private boolean flipX = false;
    private boolean flipY = false;
    private Vector2 pos = new Vector2();
    private float rotation = 0;

    // Constructor
    public Part(final App game, Ship parent, JSONObject data){
        this.parent = parent;

        type = data.optString("type", "STRUCTURAL");
        id = data.optString("id", "BSC_FUSELAGE");
        name = data.optString("name", "Basic Fuselage");
        description = data.optString("desc", "Ol' reliable with its 1000 fuel and battery!");
        interiorSize = data.optInt("interiorSize", 3);
        freeform = data.optBoolean("freeform", false);

        texture = game.atlas.findRegion("parts/" + id.toLowerCase());
        collider = new Collider(new JSONArray(Gdx.files.internal("colliders/parts/" + id.toLowerCase() + ".json").readString()));
        
        for(int i = 0; i < data.getJSONArray("attachmentPoints").length(); i++){
            JSONObject vec = data.getJSONArray("attachmentPoints").getJSONObject(i);
            attachments.add(new Node(this, vec.getFloat("x"), vec.getFloat("y")));
        }
    }

    // Functions
    public Part get(int index){
        Node thisNode = attachments.get(index);
        
        if(thisNode.next != null){
            return thisNode.next.part;
        } else if(thisNode.previous != null){
            return thisNode.previous.part;
        }

        return null;
    }

    public Part attach(int from, int to, Part part){
        Node fromNode = attachments.get(from);
        Node toNode = part.attachments.get(to);

        if(fromNode.next != null) return null;
        if(toNode.previous != null) return null;

        fromNode.next = toNode;
        toNode.previous = fromNode;

        return part;
    }

    public boolean deattach(int index){
        Node thisNode = attachments.get(index);

        if(thisNode.next != null){
            // This node is the parent, orphan the child
            Node toNode = thisNode.next;
            toNode.previous = null;
            thisNode.next = null;
            return true;
        } else if(thisNode.previous != null){
            // This node is the child, run away from home
            Node fromNode = thisNode.previous;
            fromNode.next = null;
            thisNode.previous = null;
            return true;
        }

        return false;
    }
    
    public void setParent(Ship ship, float x, float y){
        BodyComponent bc = ship.getBody();
        parent = ship;
        collider.setScale((flipX ? -1 : 1) / bc.world.getPhysScale(), (flipY ? -1 : 1) / bc.world.getPhysScale());
        collider.setPosition(new Vector2(x, y).scl(1 / bc.world.getPhysScale()));
        collider.setRotation(rotation);
        collider.attachCollider(bc.body);

        pos.set(x, y);

        for(Node node : attachments){
            if(node.next != null){
                Vector2 position = node.point.cpy().sub(node.next.point.x, node.next.point.y).add(x, y);
                node.next.part.setParent(ship, position.x, position.y);
            }
        }
    }

    public void tick(float delta){
        for(Node node : attachments){
            if(node.next != null){
                node.next.part.tick(delta);
            }
        }
    }
    
    @Override
    public void dispose(){}

    // Rendering functions
    protected void drawEffectsAbove(Batch batch){}
    protected void drawEffectsBelow(Batch batch){}

    public void draw(Batch batch, Matrix4 trans){
        batch.setTransformMatrix(trans);
        drawEffectsBelow(batch);
        batch.draw(
            texture,
            texture.getRegionWidth() / -2, texture.getRegionHeight() / -2,
            texture.getRegionWidth() / 2, texture.getRegionHeight() / 2,
            texture.getRegionWidth(), texture.getRegionHeight(),
            flipX ? -1 : 1, flipY ? -1 : 1,
            rotation
        );
        drawEffectsAbove(batch);

        for(Node node : attachments){
            if(node.next != null){
                Vector2 position = node.point.cpy().sub(node.next.point.x, node.next.point.y);
                trans.translate(position.x, position.y, 0);
                node.next.part.draw(batch, trans);
                trans.translate(-position.x, -position.y, 0);
            }
        }
    }

    public void drawAttachmentPoints(ShapeRenderer renderer, Matrix4 trans){
        for(Node node : attachments){
            if(node.previous != null || node.next != null){
                renderer.setColor(1, 0, 0, 1);
            } else {
                renderer.setColor(0, 1, 0, 1);
            }

            renderer.setTransformMatrix(trans);
            renderer.circle(node.point.x, node.point.y, 1);

            if(node.next != null){
                Vector2 position = node.point.cpy().sub(node.next.point.x, node.next.point.y);

                trans.translate(position.x, position.y, 0);
                node.next.part.drawAttachmentPoints(renderer, trans);
                trans.translate(-position.x, -position.y, 0);
            }
        }
    }

    public void drawDebug(ShapeRenderer renderer){
        renderer.setColor(0.2f, 0.2f, 1, 1);
        renderer.circle(pos.x, pos.y, 1);

        for(Node node : attachments){
            if(node.next != null){
                node.next.part.drawDebug(renderer);
            }
        }
    }

    // Getters & setters
    public boolean getFreeform(){ return freeform; }
    public int getInteriorSize(){ return interiorSize; }
    public float getPartScale(){ return partScale; }
    public float getWidth(){ return texture.getRegionWidth() * partScale; }
    public float getHeight(){ return texture.getRegionHeight() * partScale; }
    public void setFlipX(){ flipX = !flipX; }
    public void setFlipY(){ flipY = !flipY; }
    public boolean getFlipX(){ return flipX; }
    public boolean getFlipY(){ return flipY; }
    public void setRotation(float rot){ rotation = rot; }
    public Vector2 getPosition(){ return pos; }
    public float getRotation(){ return rotation; }
    public TextureRegion getTexture(){ return texture; }
    public String getType(){ return type; }
    public String getID(){ return id; }
    public String getName(){ return name; }
    public String getDescription(){ return description; }
    public Array<Node> getAttachments(){ return attachments; }

    // Serialization functions
    public JSONObject serialize(){
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("id", id);
        obj.put("rotation", rotation);
        obj.put("flipX", flipX);
        obj.put("flipY", flipY);

        JSONArray children = new JSONArray();
        for(Node node : attachments){
            if(node.next != null){
                int connectingIndex = 0;
                for(Node nextNodes : node.next.part.attachments){
                    if(nextNodes.previous != node){
                        connectingIndex++;
                    } else {
                        break;
                    }
                }

                JSONObject attachment = new JSONObject();
                attachment.put("nodeData", node.next.part.serialize());
                attachment.put("connectingIndex", connectingIndex);
                children.put(attachment);
            } else {
                children.put(JSONObject.NULL);
            }
        }
        obj.put("attachments", children);

        return obj;
    }

    public static Part spawn(final App game, final Ship ship, String type, String id){
        // Load part information from the json object
        switch(type){
            case "AERO":
                return new Aero(game, ship, game.partManager.get(type, id));
                
            case "STRUCTURAL":
                return new Structural(game, ship, game.partManager.get(type, id));
                
            case "THRUSTER":
                return new Thruster(game, ship, game.partManager.get(type, id));
                
            // case "RCSPORT":
            //     return new RCSPort(game, ship, game.partManager.get(type, id));

            default:
                return null;
        }
    }

    public static Part unserialize(final App game, final Ship ship, JSONObject obj){
        String type = obj.getString("type");
        String id = obj.getString("id");
        float rotation = obj.getFloat("rotation");
        boolean flipX = obj.getBoolean("flipX");
        boolean flipY = obj.getBoolean("flipY");

        Part newPart = Part.spawn(game, ship, type, id);
        newPart.rotation = rotation;
        newPart.flipX = flipX;
        newPart.flipY = flipY;

        // Spawn every child
        JSONArray arr = obj.getJSONArray("attachments");
        for(int i = 0; i < arr.length(); i++){
            Object rawData = arr.get(i);

            if(!rawData.equals(null)){
                // Create part for the children
                JSONObject data = (JSONObject)rawData;
                JSONObject nodeData = data.getJSONObject("nodeData");
                int connectingIndex = data.getInt("connectingIndex");
                newPart.attach(i, connectingIndex, unserialize(game, ship, nodeData));
            }
        }

        return newPart;
    }
}
