package com.alicornlunaa.selene_engine.phys;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;

/** Collider JSON data format
 * {
 *   vertices: [ float list containing vectors ],
 *   indices: [ integer list connecting the vertices ],
 *   convex: true,
 *   friction: 0.2,
 *   restitution: 0,
 *   density: 0,
 *   sensor: false
 * }
 */
public class Collider {
    // Static classes
    public static class Shape {
        // Variables
        private Array<Vector2> vertices = new Array<>();
        private Array<Integer> indices = new Array<>();
        private Polygon polygon = new Polygon();
        private boolean convex = true;
        private boolean sensor = false;
        private float friction = 0.2f;
        private float restitution = 0.0f;
        private float density = 0.0f;

        // Private functions
        private void updatePolygon(){
            float rawVertices[] = new float[vertices.size * 2];

            for(int i = 0; i < vertices.size; i++){
                Vector2 v = vertices.get(i);
                rawVertices[(i * 2)] = v.x;
                rawVertices[(i * 2) + 1] = v.y;
            }

            if(rawVertices.length >= 6)
                polygon.setVertices(rawVertices);
        }

        // Constructor
        private Shape(){}

        private Shape(Shape s){
            // Copy constructor
            for(Vector2 v : s.vertices){
                vertices.add(v.cpy());
            }

            for(int i : s.indices){
                indices.add(i);
            }

            polygon = new Polygon(s.polygon.getVertices());
            convex = s.convex;
            sensor = s.sensor;
            friction = s.friction;
            restitution = s.restitution;
            density = s.density;
        }

        // Getters & Setters
        public Polygon getPolygon(){ return polygon; }
        public boolean getConvex(){ return convex; }
        public boolean getSensor(){ return sensor; }
        public float getFriction(){ return friction; }
        public float getRestitution(){ return restitution; }
        public float getDensity(){ return density; }

        public void setConvex(boolean c){ convex = c; }
        public void setSensor(boolean s){ sensor = s; }
        public void setFriction(float f){ friction = f; }
        public void setRestitution(float r){ restitution = r; }
        public void setDensity(float d){ density = d; }

        // Functions
        public int addVertex(Vector2 vertex){
            vertices.add(vertex.cpy());
            updatePolygon();
            return vertices.size - 1;
        }
    
        public void removeVertex(Vector2 vertex){
            vertices.removeValue(vertex, false);
            updatePolygon();
        }
    
        public Vector2 getVertex(int vertex){ return vertices.get(vertex); }
    
        public int getVertexCount(){ return vertices.size; }
        
        public int getIndex(int i){ return indices.get(i); }

        public int getIndexCount(){ return indices.size; }

        public void simplify(){
            if(!convex){
                triangulate();
            } else {
                giftwrap();
            }
            updatePolygon();
        }

        private int orientation(Vector2 p, Vector2 q, Vector2 r){
            float val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
            if (val == 0) return 0;
            return (val > 0) ? 1 : 2;
        }

        private void giftwrap(){
            // Error checking
            if(vertices.size < 3) return;

            // Start with left-most point
            int min = 0;
            float minX = vertices.get(min).x;
            for(int i = 1; i < vertices.size; i++){
                if(vertices.get(i).x < minX){
                    min = i;
                    minX = vertices.get(i).x;
                }
            }

            indices.clear();
            convex = true;

            // Giftwrapping algorithm to find convex hull
            int p = min;
            int q;

            do {
                indices.add(p);
                q = (p + 1) % vertices.size;
                
                for(int i = 0; i < vertices.size; i++){
                    if(orientation(vertices.get(p), vertices.get(i), vertices.get(q)) == 2){
                        q = i;
                    }
                }

                p = q;
            } while(p != min);
        }

        private void triangulate(){
            // Convert to float-data for the delaunay triangulator
            float[] rawVertices = new float[vertices.size * 2];
            for(int i = 0; i < vertices.size; i++){
                rawVertices[i * 2] = vertices.get(i).x;
                rawVertices[i * 2 + 1] = vertices.get(i).y;
            }
    
            // Run the triangulation
            DelaunayTriangulator triangulator = new DelaunayTriangulator();
            ShortArray res = triangulator.computeTriangles(rawVertices, false);
    
            // Save the returned indices
            indices.clear();
            for(int i = 0; i < res.size; i++){
                indices.add((int)res.get(i));
            }
        }

        private JSONObject serialize(){
            /** Returns JSON collider defined above */
            JSONObject obj = new JSONObject();
            obj.put("convex", convex);
            obj.put("sensor", sensor);
            obj.put("friction", friction);
            obj.put("restitution", restitution);
            obj.put("density", density);

            simplify();

            JSONArray vertexData = new JSONArray();
            for(Vector2 v : vertices){
                vertexData.put(v.x);
                vertexData.put(v.y);
            }
            obj.put("vertices", vertexData);

            JSONArray indexData = new JSONArray();
            for(int i : indices){
                indexData.put(i);
            }
            obj.put("indices", indexData);

            return obj;
        }

        private static Shape unserialize(JSONObject obj){
            Shape s = new Shape();
            s.convex = obj.optBoolean("convex", true);
            s.sensor = obj.optBoolean("sensor", false);
            s.friction = obj.optFloat("friction", 0.2f);
            s.restitution = obj.optFloat("restitution", 0.0f);
            s.density = obj.optFloat("density", 0.0f);

            JSONArray vertexData = obj.getJSONArray("vertices");
            for(int i = 0; i < vertexData.length(); i += 2){
                float x = vertexData.getFloat(i);
                float y = vertexData.getFloat(i + 1);
                s.vertices.add(new Vector2(x, y));
            }
            s.updatePolygon();

            JSONArray indexData = obj.getJSONArray("indices");
            for(int i = 0; i < indexData.length(); i++){
                s.indices.add(indexData.getInt(i));
            }

            return s;
        }
    }

    // Variables
    private Array<Shape> shapes = new Array<>();
    private Array<Fixture> fixtures = new Array<>();
    private Vector2 position = new Vector2();
    private Vector2 scale = new Vector2(1, 1);
    private float rotation = 0.0f;
    private Body bodyRef;

    // Constructors
    public Collider(){}

    public Collider(JSONArray arr){
        for(int i = 0; i < arr.length(); i++){
            Shape s = Shape.unserialize(arr.getJSONObject(i));
            shapes.add(s);
        }
    }

    public Collider(Collider c){
        // Copy constructor
        for(Shape s : c.shapes){
            shapes.add(new Shape(s));
        }

        position = c.position.cpy();
        scale = c.scale.cpy();
        rotation = c.rotation;
    }

    // Getters & setters
    public Vector2 getPosition() { return position; }
    public void setPosition(Vector2 position) { this.position = position; }
    public Vector2 getScale() { return scale; }
    public void setScale(float x, float y) { this.scale.set(x, y); }
    public void setScale(float scale) { this.scale.set(scale, scale); }
    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }

    // Physics functions
    public void detachCollider(){
        // Remove existing body
        if(bodyRef != null){
            for(Fixture f : fixtures){
                if(f == null || bodyRef.getFixtureList().size <= 0) continue;
                bodyRef.destroyFixture(f);
            }

            fixtures.clear();
        }

        bodyRef = null;
    }

    public Body attachCollider(Body b){
        // Remove existing body
        detachCollider();

        // Attach to new body
        Matrix3 trans = new Matrix3().translate(position).rotate(rotation).scale(scale);
        bodyRef = b;

        for(int i = 0; i < this.getShapeCount(); i++){
            // Each shape must be attached
            Shape shape = this.getShape(i);
            Vector2[] vertexData = new Vector2[shape.getIndexCount()];

            for(int j = 0; j < shape.getIndexCount(); j++){
                Vector2 v = shape.getVertex(shape.getIndex(j));
                vertexData[j] = v.cpy().mul(trans);
            }

            PolygonShape physShape = new PolygonShape();
            physShape.set(vertexData);
            
            FixtureDef def = new FixtureDef();
            def.shape = physShape;
            def.friction = shape.getFriction();
            def.restitution = shape.getDensity();
            def.density = shape.getDensity();
            def.isSensor = shape.getSensor();
            fixtures.add(b.createFixture(def));

            physShape.dispose();
        }

        return b;
    }

    public void reattach(){ attachCollider(bodyRef); }

    // Shape functions
    public boolean contains(Vector2 p){
        // Checks if this collider contains the point
        Matrix3 trans = new Matrix3().translate(position).rotate(rotation);
        Vector2 point = p.cpy().mul(trans.inv());

        for(Shape s : shapes){
            if(s.getPolygon().contains(point)){
                return true;
            }
        }

        return false;
    }

    public Shape addShape(){
        shapes.add(new Shape());
        return shapes.peek();
    }

    public void removeShape(int i){ shapes.removeIndex(i); }

    public Shape getShape(int i){ return shapes.get(i); }

    public int getShapeCount(){ return shapes.size; }

    public void simplifyShapes(){
        for(Shape s : shapes){
            s.simplify();
        }
    }

    public void clear(){ shapes.clear(); }

    public JSONArray serialize(){
        /** Returns an array of JSON colliders */
        JSONArray arr = new JSONArray();

        for(Shape s : shapes){
            arr.put(s.serialize());
        }

        return arr;
    }

    // Static functions
    public static Collider box(float x, float y, float w, float h, float rotation){
        Collider c = new Collider();
        c.position.set(x, y);
        c.rotation = rotation;

        Shape s = c.addShape();
        s.addVertex(new Vector2(w * -1.f, h * -1.f));
        s.addVertex(new Vector2(w * 1.f, h * -1.f));
        s.addVertex(new Vector2(w * 1.f, h * 1.f));
        s.addVertex(new Vector2(w * -1.f, h * 1.f));
        s.density = 1.f;
        s.friction = 1.f;
        s.restitution = 0.5f;
        s.sensor = false;

        c.simplifyShapes();
        return c;
    }
    
    public static Collider circle(float x, float y, float radius, float rotation){
        Collider c = new Collider();
        c.position.set(x, y);
        c.rotation = rotation;

        Shape s = c.addShape();
        s.addVertex(new Vector2(w * -1.f, h * -1.f));
        s.addVertex(new Vector2(w * 1.f, h * -1.f));
        s.addVertex(new Vector2(w * 1.f, h * 1.f));
        s.addVertex(new Vector2(w * -1.f, h * 1.f));
        s.density = 1.f;
        s.friction = 1.f;
        s.restitution = 0.5f;
        s.sensor = false;

        c.simplifyShapes();
        return c;
    }
}
