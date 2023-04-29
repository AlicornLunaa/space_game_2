package com.alicornlunaa.spacegame.util.state_management;

import org.json.JSONObject;

public interface ISerializable<Type> {
    
    void serialize(JSONObject data, Type object);
    Type unserialize(JSONObject data);

}
