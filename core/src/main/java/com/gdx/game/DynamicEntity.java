//TODO:
//Add Attack range when constructing DynamicSprite and rewrite updateMovement to be resued in the updateAttack accordingly 

package com.gdx.game;

//import java.util.ArrayList;

// import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
// import com.badlogic.gdx.graphics.GL20;
// import com.badlogic.gdx.Gdx;
// import com.badlogic.gdx.Input;
// import com.badlogic.gdx.Input.Keys;
//import com.badlogic.gdx.graphics.Texture;
// import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

//import com.badlogic.gdx.math.Vector3;
// import com.badlogic.gdx.graphics.OrthographicCamera;
// import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
// import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
//import com.badlogic.gdx.graphics.g2d.Sprite;
// import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)


//Tiled Map Loaders
// import com.badlogic.gdx.maps.tiled.TiledMap;
// import com.badlogic.gdx.maps.tiled.TmxMapLoader;
// import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

// DynamicSprite is a subclass of Sprite with movement functonality
abstract class DynamicEntity extends Entity {
    // This will be the state that everything DynamicSprite is in
    enum State {
        IDLE,
        MOVING,
        ATTACK,
        DEAD,
    }

    // Data members
    protected float speed = 20f;  // -> Player Move Speed (based off above world size)

    //Declare Animation Variables
    protected Animation<TextureRegion> runAnimation;

    // Currently used animation
    protected Animation<TextureRegion> currentAnimation;

    // Move this to Player Subclass, this is respawn position HARDCODED
    //Use them to define starting position for camera and sprite
    // Make getters?
    // public int startX;      // COMMENTED OUT BECAUSE MADE A CHILD CLASS
    // public int startY;

    protected Vector2 destVector;
    protected Vector2 moveTargetVector;     //Every subclass will define its own target in Update() method


    State state;

    DynamicEntity attackTarget;     //Stores entity to attack

    float health;

    // Creates the Sprite(Parent Class)
    // TODO Pass the animations as a list and the other stuff as a list
    DynamicEntity(Animation<TextureRegion> runAnimation, Animation<TextureRegion> idleAnimation , Animation<TextureRegion> attackAnimation, Animation<TextureRegion> deadAnimation,
                  float stateTime, int startX, int startY, float speed, float maxHealth, float damageStrength, float attackRange) {
        super(idleAnimation, attackAnimation, deadAnimation, stateTime, maxHealth, damageStrength, attackRange);

        this.runAnimation = runAnimation; 
        this.speed = speed;
        this.currentXY = new Vector2(startX, startY);

        state = State.IDLE;
        currentAnimation = idleAnimation;
    }

    //Concrete methods (Getter and setter)

    // Only used when you click left click
    public void setMove(Vector2 clickCoords){
        this.moveTargetVector = new Vector2(clickCoords);
    }

    public Vector2 getMove(){
        return moveTargetVector;
    }

    // Only set when you click right click. Only set it if the position is near an enemy
    public void setAttackInfo(DynamicEntity entity){
        attackTarget = entity;
    }

    public DynamicEntity getAttackInfo() {
        return attackTarget;
    }

    public boolean isCloseToEnemy() {
        Rectangle enemyBounds = attackTarget.getBoundingRectangle();
        Vector2 center = new Vector2();
        enemyBounds.getCenter(center);

        boolean isClose = center.dst(currentXY) <= attackRange;

        return isClose;
    }

    // Get Postion
    abstract public Vector2 getPosition();


    // Movement Method
    public void updateMovement(Vector2 targetVector, float stateTime , float delta){
        //For angle calculation to rotate sprite
        float angle;
    
        destVector = new Vector2();
        destVector.set(targetVector).sub(currentXY);        //Calculate the destination vector

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destVector.y, destVector.x);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        
        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        if (angle > 90 && angle < 270){ 
            //player.setRotation(angle + 180);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant 
            this.setFlip(true, false);       //Flip the x axis
        }

        else{
            //player.setRotation(angle);         //Same reason as above
            this.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }

        //-----ROTATION CALCULATION END-----

        if (destVector.len() > 0.5) {         //ALERT: Add a check for bounding box aswell so if we click in open area it goes there using 0.5 otherwise by bounding box calculations
            destVector.nor().scl(delta*speed);    // Normalize then multiply(scale) by speed and delta time

            // Change current coordiantes
            currentXY.add(destVector);      //Updates the current vector co-ordiantes
            // Add clamping later
            this.setCenter(currentXY.x, currentXY.y);     //Update player position
        } 
        else if (isCloseToEnemy() || destVector.len() <= 0.5) {
            moveTargetVector = null;
        }
        //No need for updating camera over here as its handled in cameraRoam otherwise it causes conflict between two

    }


}
//Child class number 1

class HeroPlayer extends DynamicEntity{
    // Later
    // enum Class {
    //     HEAVY,
    //     LIGHT,
    // }
    // final float HEAVY_SPEED, LIGHT_SPEED

    HeroPlayer(Animation<TextureRegion> runAnimation, Animation<TextureRegion> idleAnimation, Animation<TextureRegion> attackAnimation, Animation<TextureRegion> deadAnimation,
               float stateTime, int startX, int startY, float speed,
               float maxHealth, float damageStrength, float attackRange) {
        super(runAnimation, idleAnimation, attackAnimation, deadAnimation, stateTime, startX, startY, speed,
              maxHealth, damageStrength, attackRange);
        attackRange = 50f;
    }

    @Override
    public Vector2 getPosition(){
        return new Vector2(currentXY);
    }


    @Override
    public void Update(float stateTime, float delta) {
        this.setRegion(currentAnimation.getKeyFrame(stateTime));    //Updates current Animation or you get slender man running

        if (moveTargetVector != null)  {
            state = State.MOVING;
            currentAnimation = runAnimation;
        }
        else if (isCloseToEnemy()) {
            state = State.ATTACK;
            currentAnimation = attackAnimation;
        }
        else if (moveTargetVector == null){
            state = State.IDLE;    
            currentAnimation = idleAnimation;
        }
        //Modify the else if above to check for range and reuse updateMovement

        switch(state){
        case MOVING:
            updateMovement(getMove(), stateTime, delta);
            break;

        case IDLE:
            // Do absolutely nothing...
            break;

        case ATTACK:
            updateAttack(attackTarget, delta);
            break;

        default:
            System.out.println("NOT HANDLED YET");
            break;
        }   
    }

    // @Override
    // public void Update(Vector3 clickCoords, float stateTime, float delta){

    //     if (clickCoords != null){
    //         targetVector = new Vector2(clickCoords.x, clickCoords.y);
    //     }
    //     this.updateMovement(targetVector, stateTime, delta);

    // }
}