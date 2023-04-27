package com.alicornlunaa.spacegame.engine.core;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.Player;
import com.badlogic.gdx.utils.Null;

public abstract class DriveableEntity extends BaseEntity {

    // Variables
    @SuppressWarnings("unused")
    private final App game;
    private @Null Player driver;

    // Constructor
    public DriveableEntity(final App game){
        this.game = game;
    }
    
    // Functions
    public Player getDriver(){ return driver; }

    public void drive(Player driver){
        this.driver = driver;
        driver.setVehicle(this);

        if(driver.getBody() != null){
            driver.getBody().setActive(false);
        }
    }

    public void stopDriving(){
        driver.setPosition(getPosition());
        driver.setVehicle(null);

        if(driver.getBody() != null){
            driver.getBody().setActive(true);
        }
        
        driver = null;
    }

    @Override
    public void afterWorldChange(PhysWorld world){
        // Carry the driver with the vehicle
        if(driver == null) return;
        driver.afterWorldChange(world);
    }
    
}
