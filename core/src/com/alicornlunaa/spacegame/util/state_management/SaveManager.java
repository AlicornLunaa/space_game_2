package com.alicornlunaa.spacegame.util.state_management;

import java.util.HashMap;

import org.json.JSONObject;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

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

                p.setPosition(data.getFloat("x"), data.getFloat("y"));
                p.setVelocity(data.getFloat("vx"), data.getFloat("vy"));

                Celestial parent = game.universe.getCelestial(data.getInt("celestial_id"));
                if(parent != null && game.simulation.getWorldID(parent.getWorld()) != data.getInt("physworld_id") && parent instanceof Planet){
                    Planet planet = (Planet)parent;
                    planet.addEntityWorld(p);
                    p.setPosition(data.getFloat("x"), data.getFloat("y"));
                    p.setVelocity(new Vector2(data.getFloat("vx"), data.getFloat("vy")));
                }
                
                return p;
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
    }

    public static void load(final App game, String saveName){
        // Load player data
        FileHandle f = Gdx.files.local("./saves/universes/" + saveName + "/player.dat");
        game.player = read(Player.class, f.readString());
    }

}
