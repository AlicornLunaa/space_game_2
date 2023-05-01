package com.alicornlunaa.spacegame.util.state_management;

import java.util.HashMap;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Handles creation of a new save, loading of a save, etc
 */
public class SaveManager {

    // Static classes
    public static abstract class SaveSerializer<T> implements ISerializable<T> {

        public String write(T object){
            JSONObject o = new JSONObject();
            this.serialize(o, object);
            return o.toString();
        }

        public T read(JSONObject o){
            return unserialize(o);
        }

    }

    // Variables
    private static HashMap<Class<?>, SaveSerializer<?>> serializers = new HashMap<>();
    
    // Functions
    public static void init(final App game){
        // Create registers
        /* register(BaseEntity.class, new SaveSerializer<BaseEntity>() {
            @Override
            public void serialize(JSONObject data, BaseEntity object) {
                data.put("entity_type", object.getClass().getName());
                data.put("x", object.getX());
                data.put("y", object.getY());
                data.put("vx", object.getVelocity().x);
                data.put("vy", object.getVelocity().y);
                data.put("physworld_id", game.simulation.getWorldID(object.getWorld()));
                data.put("celestial_id", (game.universe.getParentCelestial(object) == null) ? -1 : game.universe.getParentCelestial(object).getCelestialID());
            }

            @Override
            public BaseEntity unserialize(JSONObject data) {
                BaseEntity e = new BaseEntity();
                game.universe.addEntity(e);

                Celestial parent = game.universe.getCelestial(data.getInt("celestial_id"));
                if(parent != null){
                    if(game.simulation.getWorldID(parent.getInfluenceWorld()) != data.getInt("physworld_id") && parent instanceof Planet){
                        game.simulation.addEntity(((Planet)parent).getInternalPhysWorld(), e);
                    } else {
                        game.simulation.addEntity(parent.getInfluenceWorld(), e);
                    }
                }

                e.setPosition(data.getFloat("x"), data.getFloat("y"));
                e.setVelocity(data.getFloat("vx"), data.getFloat("vy"));
                
                return p;
            }
        }); */

        register(Player.class, new SaveSerializer<Player>() {
            @Override
            public void serialize(JSONObject data, Player object) {
                data.put("x", object.getX());
                data.put("y", object.getY());
                data.put("vx", object.getVelocity().x);
                data.put("vy", object.getVelocity().y);
                data.put("physworld_id", game.simulation.getWorldID(object.getWorld()));
                data.put("celestial_id", (game.universe.getParentCelestial(object) == null) ? -1 : game.universe.getParentCelestial(object).getCelestialID());
            }

            @Override
            public Player unserialize(JSONObject data) {
                Player p = new Player(game, 0, 0);
                game.universe.addEntity(p);

                Celestial parent = game.universe.getCelestial(data.getInt("celestial_id"));
                if(parent != null){
                    if(game.simulation.getWorldID(parent.getInfluenceWorld()) != data.getInt("physworld_id") && parent instanceof Planet){
                        game.simulation.addEntity(((Planet)parent).getInternalPhysWorld(), p);
                    } else {
                        game.simulation.addEntity(parent.getInfluenceWorld(), p);
                    }
                }

                p.setPosition(data.getFloat("x"), data.getFloat("y"));
                p.setVelocity(data.getFloat("vx"), data.getFloat("vy"));
                
                return p;
            }
        });
        
        register(Celestial.class, new SaveSerializer<Celestial>() {
            @Override
            public void serialize(JSONObject data, Celestial object) {
                data.put("x", object.getX());
                data.put("y", object.getY());
                data.put("vx", object.getVelocity().x);
                data.put("vy", object.getVelocity().y);
                data.put("physworld_id", game.simulation.getWorldID(object.getWorld()));
                data.put("celestial_id", (game.universe.getParentCelestial(object) == null) ? -1 : game.universe.getParentCelestial(object).getCelestialID());
                data.put("parent_celestial_id", (object.getCelestialParent() == null) ? -1 : object.getCelestialParent().getCelestialID());
                data.put("celestial_radius", object.getRadius());
            }

            @Override
            public Celestial unserialize(JSONObject data) {
                Celestial c = new Celestial(game, data.getFloat("celestial_radius"));
                c.setPosition(data.getFloat("x"), data.getFloat("y"));
                c.setVelocity(data.getFloat("vx"), data.getFloat("vy"));
                game.universe.addCelestial(c);
                return c;
            }
        });
    }

    public static <T> void register(Class<T> c, SaveSerializer<T> serializer){
        serializers.put(c, serializer);
    }

    @SuppressWarnings("unchecked")
    public static <T> String write(Class<T> c, T obj){
        // I really shouldnt do this but im tired of trying to find the right way
        return ((SaveSerializer<T>)serializers.get(c)).write(obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> T read(Class<T> c, String data){
        // I really shouldnt do this but im tired of trying to find the right way
        return ((SaveSerializer<T>)serializers.get(c)).read(new JSONObject(data));
    }

    public static void save(final App game, String saveName){
        // Save player data
        FileHandle f = Gdx.files.local("./saves/universes/" + saveName + "/player.dat");
        f.writeString(write(Player.class, game.player), false);

        // Save entity data

        // Save celestial data
        for(Celestial c : game.universe.getCelestials()){
            f = Gdx.files.local("./saves/universes/" + saveName + "/planets/celestial_" + c.getCelestialID() + "/level.dat");
            f.writeString(write(Celestial.class, c), false);
        }
    }

    public static void load(final App game, String saveName){
        // Save celestial data
        // FileHandle f = Gdx.files.local("./saves/universes/" + saveName + "/planets/");
        // FileHandle planetData[] = f.list();
        // for(FileHandle d : planetData){
        //     FileHandle h = Gdx.files.local("./saves/universes/" + saveName + "/planets/" + d.name() + "/level.dat");
        //     read(Celestial.class, h.readString());
        // }

        // Load player data
        FileHandle f = Gdx.files.local("./saves/universes/" + saveName + "/player.dat");
        game.player = read(Player.class, f.readString());
    }

}
