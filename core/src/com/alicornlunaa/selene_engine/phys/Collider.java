package com.alicornlunaa.selene_engine.phys;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ShortArray;

// TODO: Add more advanced functionality such as being able to disable the fixtures temporarily

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
    public static abstract class Shape {
        // Enums
        protected enum ShapeType { POLYGON, CIRCLE, EDGE };

        // Variables
        private ShapeType shapeType;
        public boolean convex = true;
        public boolean sensor = false;
        public float friction = 0.2f;
        public float restitution = 0.2f;
        public float density = 1.0f;

        // Constructor
        private Shape(ShapeType type){
            shapeType = type;
        }

        private Shape(Shape shape){
            // Copy constructor
            shapeType = shape.shapeType;
            convex = shape.convex;
            sensor = shape.sensor;
            friction = shape.friction;
            restitution = shape.restitution;
            density = shape.density;
        }

        // Functions
        public abstract Shape2D getShape();

        public ShapeType getType(){
            return shapeType;
        }

        protected JSONObject serialize(){
            // Converts to JSON
            JSONObject obj = new JSONObject();
            obj.put("type", (shapeType == ShapeType.POLYGON) ? "polygon" : "circle");
            obj.put("convex", convex);
            obj.put("sensor", sensor);
            obj.put("friction", friction);
            obj.put("restitution", restitution);
            obj.put("density", density);
            return obj;
        }

        protected static Shape unserialize(JSONObject obj){
            Shape shape;

            switch(obj.optString("type", "polygon")){
                case "polygon":
                    PolygonShape polyShape = new PolygonShape();

                    JSONArray vertexData = obj.getJSONArray("vertices");
                    for(int i = 0; i < vertexData.length(); i += 2){
                        float x = vertexData.getFloat(i);
                        float y = vertexData.getFloat(i + 1);
                        polyShape.vertices.add(new Vector2(x, y));
                    }
        
                    JSONArray indexData = obj.getJSONArray("indices");
                    for(int i = 0; i < indexData.length(); i++){
                        polyShape.indices.add(indexData.getInt(i));
                    }
                    
                    shape = polyShape;
                    break;

                case "circle":
                    CircleShape circShape = new CircleShape();

                    circShape.x = obj.optFloat("x", 0.f);
                    circShape.y = obj.optFloat("y", 0.f);
                    circShape.radius = obj.optFloat("radius", 1.f);

                    shape = circShape;
                    break;

                default:
                    return null;
            }

            shape.convex = obj.optBoolean("convex", true);
            shape.sensor = obj.optBoolean("sensor", false);
            shape.friction = obj.optFloat("friction", 0.2f);
            shape.restitution = obj.optFloat("restitution", 0.0f);
            shape.density = obj.optFloat("density", 0.0f);

            return shape;
        }
    }

    public static class PolygonShape extends Shape {
        // Variables
        private Array<Vector2> vertices = new Array<>();
        private Array<Integer> indices = new Array<>();

        // Constructor
        public PolygonShape(){
            // Default constructor
            super(ShapeType.POLYGON);
        }

        public PolygonShape(PolygonShape shape){
            // Copy constructor
            super(shape);

            for(Vector2 v : shape.vertices){
                vertices.add(v.cpy());
            }

            for(int i : shape.indices){
                indices.add(i);
            }
        }

        // Functions
        @Override
        public Polygon getShape(){
            float rawVertices[] = new float[vertices.size * 2];
            Polygon polygon = new Polygon();

            for(int i = 0; i < vertices.size; i++){
                Vector2 v = vertices.get(i);
                rawVertices[(i * 2)] = v.x;
                rawVertices[(i * 2) + 1] = v.y;
            }

            if(rawVertices.length >= 6)
                polygon.setVertices(rawVertices);

            return polygon;
        }

        @Override
        protected JSONObject serialize(){
            JSONArray vertexData = new JSONArray();
            for(Vector2 v : vertices){
                vertexData.put(v.x);
                vertexData.put(v.y);
            }

            JSONArray indexData = new JSONArray();
            for(int i : indices){
                indexData.put(i);
            }

            JSONObject obj = super.serialize();
            obj.put("vertices", vertexData);
            obj.put("indices", indexData);
            return obj;
        }


        public int addVertex(Vector2 vertex){
            vertices.add(vertex.cpy());
            simplify();
            return vertices.size - 1;
        }
    
        public void removeVertex(Vector2 vertex){
            vertices.removeValue(vertex, false);
            simplify();
        }
    
        public Vector2 getVertex(int vertexID){
            return vertices.get(vertexID);
        }
        
        public int getVertexCount(){
            return vertices.size;
        }
        
        public int getIndex(int i){
            return indices.get(i);
        }

        public int getIndexCount(){
            return indices.size;
        }
    
        public void simplify(){
            if(convex){
                giftwrap();
            } else {
                triangulate();
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
    }

    public static class CircleShape extends Shape {
        // Variables
        private float x = 0.f;
        private float y = 0.f;
        private float radius = 1.f;

        // Constructors
        public CircleShape(){
            // Default constructor
            super(ShapeType.CIRCLE);
        }

        public CircleShape(CircleShape shape){
            // Copy constructor
            super(shape);
            radius = shape.radius;
        }

        // Functions
        @Override
        public Circle getShape(){
            Circle circle = new Circle();
            circle.set(x, y, radius);
            return circle;
        }

        @Override
        protected JSONObject serialize(){
            JSONObject obj = super.serialize();
            obj.put("x", x);
            obj.put("y", y);
            obj.put("radius", radius);
            return obj;
        }


        public void setPosition(float x, float y){
            this.x = x;
            this.y = y;
        }

        public Vector2 getPosition(){
            return new Vector2(x, y);
        }

        public void setRadius(float rad){
            radius = rad;
        }

        public float getRadius(){
            return radius;
        }
    }

    public static class EdgeShape extends Shape {
        // Variables
        private Vector2 start = new Vector2();
        private Vector2 end = new Vector2();

        // Constructor
        public EdgeShape(){
            // Default constructor
            super(ShapeType.EDGE);
        }

        public EdgeShape(EdgeShape shape){
            // Copy constructor
            super(shape);
            start.set(shape.start);
            end.set(shape.end);
        }

        // Functions
        @Override
        public Polygon getShape(){
            float rawVertices[] = new float[4];
            Polygon polygon = new Polygon();

            rawVertices[0] = start.x;
            rawVertices[1] = start.y;
            rawVertices[2] = end.x;
            rawVertices[3] = end.y;

            polygon.setVertices(rawVertices);
            return polygon;
        }

        @Override
        protected JSONObject serialize(){
            JSONArray vertexData = new JSONArray();
            vertexData.put(start.x);
            vertexData.put(start.y);
            vertexData.put(end.x);
            vertexData.put(end.y);

            JSONObject obj = super.serialize();
            obj.put("vertices", vertexData);
            return obj;
        }


        public void setStartVertex(Vector2 vertex){
            start.set(vertex);
        }

        public void setEndVertex(Vector2 vertex){
            end.set(vertex);
        }
    
        public Vector2 getStartVertex(){
            return start;
        }
    
        public Vector2 getEndVertex(){
            return end;
        }
    }

    // Variables
    private Array<Shape> shapes = new Array<>();
    private Array<Fixture> fixtures = new Array<>();
    private @Null Body bodyRef = null;
    
    private Vector2 position = new Vector2(0, 0);
    private Vector2 origin = new Vector2(0, 0);
    private Vector2 scale = new Vector2(1, 1);
    private float rotation = 0.0f;

    // Constructors
    public Collider(){} // Default constructor

    public Collider(JSONArray arr){
        // JSON constructor
        for(int i = 0; i < arr.length(); i++)
            shapes.add(Shape.unserialize(arr.getJSONObject(i)));
    }

    public Collider(Collider collider){
        // Copy constructor
        for(Shape shape : collider.shapes){
            if(shape instanceof PolygonShape){
                shapes.add(new PolygonShape((PolygonShape)shape));
            } else if(shape instanceof CircleShape){
                shapes.add(new CircleShape((CircleShape)shape));
            } else if(shape instanceof EdgeShape){
                shapes.add(new EdgeShape((EdgeShape)shape));
            }
        }

        position = collider.position.cpy();
        origin = collider.origin.cpy();
        scale = collider.scale.cpy();
        rotation = collider.rotation;
    }

    // Getters & setters
    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setOrigin(Vector2 origin) {
        this.origin.set(origin);
    }

    public Vector2 getOrigin() {
        return origin;
    }

    public void setScale(float x, float y) {
        this.scale.set(x, y);
    }

    public void setScale(float scale) {
        this.scale.set(scale, scale);
    }

    public Vector2 getScale() {
        return scale;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public Collider setFixture(float friction, float restitution, float density, boolean sensor){
        for(Shape shape : shapes){
            shape.density = density;
            shape.friction = friction;
            shape.restitution = restitution;
            shape.sensor = sensor;
        }

        return this;
    }

    // Physics functions
    private Matrix3 getMatrix(){
        return new Matrix3().translate(position).rotate(rotation).translate(origin).scale(scale);
    }

    public void detach(){
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

    public Body attach(Body b){
        if(bodyRef != null)
            // Remove existing body
            detach();
            
        // Attach to new body
        Matrix3 trans = getMatrix();
        bodyRef = b;

        for(int i = 0; i < getShapeCount(); i++){
            // Each shape must be attached
            Shape shape = getShape(i);

            if(shape instanceof PolygonShape){
                PolygonShape polygonShape = (PolygonShape)shape;
                Vector2[] vertexData = new Vector2[polygonShape.getIndexCount()];

                for(int j = 0; j < polygonShape.getIndexCount(); j++){
                    int index = polygonShape.getIndex(j);
                    Vector2 v = polygonShape.getVertex(index);
                    vertexData[j] = v.cpy().mul(trans);
                }

                com.badlogic.gdx.physics.box2d.PolygonShape physShape = new com.badlogic.gdx.physics.box2d.PolygonShape();
                physShape.set(vertexData);
                
                FixtureDef def = new FixtureDef();
                def.shape = physShape;
                def.friction = shape.friction;
                def.restitution = shape.restitution;
                def.density = shape.density;
                def.isSensor = shape.sensor;
                fixtures.add(b.createFixture(def));

                physShape.dispose();
            } else if(shape instanceof CircleShape){
                CircleShape circleShape = (CircleShape)shape;
                com.badlogic.gdx.physics.box2d.CircleShape physShape = new com.badlogic.gdx.physics.box2d.CircleShape();
                physShape.setPosition(new Vector2(circleShape.x, circleShape.y));
                physShape.setRadius(circleShape.radius);
                
                FixtureDef def = new FixtureDef();
                def.shape = physShape;
                def.friction = shape.friction;
                def.restitution = shape.restitution;
                def.density = shape.density;
                def.isSensor = shape.sensor;
                fixtures.add(b.createFixture(def));

                physShape.dispose();
            } else if(shape instanceof EdgeShape){
                EdgeShape edgeShape = (EdgeShape)shape;
                com.badlogic.gdx.physics.box2d.EdgeShape physShape = new com.badlogic.gdx.physics.box2d.EdgeShape();
                physShape.set(edgeShape.start, edgeShape.end);
                
                FixtureDef def = new FixtureDef();
                def.shape = physShape;
                def.friction = shape.friction;
                def.restitution = shape.restitution;
                def.density = shape.density;
                def.isSensor = shape.sensor;
                fixtures.add(b.createFixture(def));

                physShape.dispose();
            }
        }

        return b;
    }

    public void reattach(){ attach(bodyRef); }

    // Shape functions
    public boolean contains(Vector2 p){
        // Checks if this collider contains the point
        Matrix3 trans = new Matrix3().translate(position).rotate(rotation);
        Vector2 point = p.cpy().mul(trans.inv());

        for(Shape s : shapes){
            if(s.getShape().contains(point)){
                return true;
            }
        }

        return false;
    }

    public PolygonShape addPolygon(){
        PolygonShape polygonShape = new PolygonShape();
        shapes.add(polygonShape);
        return polygonShape;
    }
    
    public CircleShape addCircle(){
        CircleShape circleShape = new CircleShape();
        shapes.add(circleShape);
        return circleShape;
    }
    
    public EdgeShape addEdgeShape(){
        EdgeShape edgeShape = new EdgeShape();
        shapes.add(edgeShape);
        return edgeShape;
    }

    public void removeShape(int i){
        shapes.removeIndex(i);
    }

    public Shape getShape(int i){
        return shapes.get(i);
    }

    public int getShapeCount(){
        return shapes.size;
    }

    public void clear(){
        shapes.clear();
    }

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

        PolygonShape s = c.addPolygon();
        s.addVertex(new Vector2(w * -1.f, h * -1.f));
        s.addVertex(new Vector2(w * 1.f, h * -1.f));
        s.addVertex(new Vector2(w * 1.f, h * 1.f));
        s.addVertex(new Vector2(w * -1.f, h * 1.f));
        s.density = 1.f;
        s.friction = 1.f;
        s.restitution = 0.5f;
        s.sensor = false;
        s.convex = true;

        return c;
    }
    
    public static Collider circle(float x, float y, float radius){
        Collider c = new Collider();
        c.position.set(x, y);

        CircleShape s = c.addCircle();
        s.x = x;
        s.y = y;
        s.radius = radius;
        s.density = 1.f;
        s.friction = 1.f;
        s.restitution = 0.5f;
        s.sensor = false;
        s.convex = true;

        return c;
    }
    
    public static Collider triangle(float x, float y, float w, float h, float rotation){
        Collider c = new Collider();
        c.position.set(x, y);
        c.rotation = rotation;

        PolygonShape s = c.addPolygon();
        s.addVertex(new Vector2(w * -1.f, h * -1.f));
        s.addVertex(new Vector2(w * 1.f, h * -1.f));
        s.addVertex(new Vector2(w * -1.f, h * 1.f));
        s.density = 1.f;
        s.friction = 1.f;
        s.restitution = 0.5f;
        s.sensor = false;
        s.convex = true;

        return c;
    }
}
