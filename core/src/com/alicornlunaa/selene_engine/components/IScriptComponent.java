package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;

public interface IScriptComponent extends IComponent {
    /** Once per physics tick */
    void update();

    /** Once per frame */
    void render();
}
