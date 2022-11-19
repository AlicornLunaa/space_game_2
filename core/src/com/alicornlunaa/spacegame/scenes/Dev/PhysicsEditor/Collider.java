package com.alicornlunaa.spacegame.scenes.Dev.PhysicsEditor;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Vector2;
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
    private static class Shape {
        /** Class holds definition for each fixture */

        // Variables
        private Array<Vector2> vertices = new Array<>();
        private Array<Integer> indices = new Array<>();
        private boolean convex = true;
        private boolean sensor = false;
        private float friction = 0.2f;
        private float restitution = 0.0f;
        private float density = 0.0f;

        // Constructor
        private Shape(){}

        // Functions
        public void simplify(){
            convex = (vertices.size <= 8);

            if(!convex){
                triangulate();
            } else {
                giftwrap();
            }
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

            JSONArray indexData = obj.getJSONArray("vertices");
            for(int i = 0; i < indexData.length(); i++){
                s.indices.add(indexData.getInt(i));
            }

            return s;
        }

    };

    // Variables
    private Array<Shape> shapes = new Array<>();

    // Constructors
    public Collider(){}

    // Functions
    public int addShape(){
        shapes.add(new Shape());
        return shapes.size - 1;
    }

    public void removeShape(int i){ shapes.removeIndex(i); }

    public Shape getShape(int i){ return shapes.get(i); }

    public int getShapeCount(){ return shapes.size; }


    public int addVertex(int shape, Vector2 vertex){
        Shape s = shapes.get(shape);
        s.vertices.add(vertex.cpy());
        return s.vertices.size - 1;
    }

    public void removeVertex(int shape, Vector2 vertex){ shapes.get(shape).vertices.removeValue(vertex, false); }

    public Vector2 getVertex(int shape, int vertex){ return shapes.get(shape).vertices.get(vertex); }

    public int getVertexCount(int shape){ return shapes.get(shape).vertices.size; }


    public boolean getConvex(int shape){ return shapes.get(shape).convex; }
    public boolean getSensor(int shape){ return shapes.get(shape).sensor; }
    public float getFriction(int shape){ return shapes.get(shape).friction; }
    public float getRestitution(int shape){ return shapes.get(shape).restitution; }
    public float getDensity(int shape){ return shapes.get(shape).density; }

    public void setSensor(int shape, boolean s){ shapes.get(shape).sensor = s; }
    public void setFriction(int shape, float f){ shapes.get(shape).friction = f; }
    public void setRestitution(int shape, float r){ shapes.get(shape).restitution = r; }
    public void setDensity(int shape, float d){ shapes.get(shape).density = d; }

    
    public int getIndex(int shape, int i){ return shapes.get(shape).indices.get(i); }

    public int getIndexCount(int shape){ return shapes.get(shape).indices.size; }

    public void calculateShapes(){
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

    public static Collider unserialize(JSONArray arr){
        Collider c = new Collider();

        for(int i = 0; i < arr.length(); i += 2){
            c.shapes.add(Shape.unserialize(arr.getJSONObject(i)));
        }

        return c;
    }
    
}
