package com.alicornlunaa.spacegame.engine.core;

import com.alicornlunaa.spacegame.objects.Player;
import com.badlogic.gdx.utils.Null;

public abstract class DriveableEntity extends BaseEntity {

    // Variables
    private @Null Player driver;
    
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
    
}
