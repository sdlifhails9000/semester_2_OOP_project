//TODO:
//Add Attack range when constructing DynamicSprite and rewrite updateMovement to be resued in the updateAttack accordingly 

package com.gdx.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two

// DynamicSprite is a subclass of Sprite with movement functonality
abstract class DynamicEntity extends Entity {
    // This will be the state that everything DynamicSprite is in

    //Declare Animation Variables
    protected Animation<TextureRegion> runAnimation;

    protected Vector2 moveTargetVector;     //Every subclass will define its own target in Update() method
    protected float speed;

    // Creates the Sprite(Parent Class)
    // TODO Pass the animations as a list and the other stuff as a list
    DynamicEntity(Animation<TextureRegion> attack,
                  Animation<TextureRegion> run,
                  Animation<TextureRegion> dead,
                  Animation<TextureRegion> idle,
                  float startX, float startY,
                  float maxHealth,
                  float attackRange,
                  float attackSpeed,
                  float attackStrength,
                  float speed,
                  float spriteWidth,
                  float spriteHeight,
                  boolean isAlly) {

        super(
            attack, dead, idle,
            startX, startY,
            maxHealth, attackRange, attackSpeed, attackStrength,
            spriteWidth, spriteHeight, isAlly
        );
        
        this.runAnimation = run;
        this.speed = speed;
    }

    // GETTER SETTERS

    public void setMove(Vector2 clickCoords){
        this.moveTargetVector = clickCoords;
    }

    public Vector2 getMove(){
        return moveTargetVector;
    }

    // Movement Method
    public void updateMovement(float delta){
        //For angle calculation to rotate sprite
        float angle;
    
        Vector2 destVector = moveTargetVector.cpy().sub(currentXY);

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destVector.y, destVector.x);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        
        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        if (angle > 90 && angle < 270) { 
            //this.setRotation(angle + 180);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant 
            this.setFlip(true, false);       //Flip the x axis
        }
        else {
            //this.setRotation(angle);         //Same reason as above
            this.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }

        //-----ROTATION CALCULATION END-----

        
        if (destVector.len() > 0.5) {         //ALERT: Add a check for bounding box aswell so if we click in open area it goes there using 0.5 otherwise by bounding box calculations
            Vector2 oldPosition = new Vector2(currentXY);   // Store old position before updating and checking collision
            destVector.nor().scl(delta * speed);    // Normalize then multiply(scale) by speed and delta time

            currentXY.add(destVector);      //Updates the current vector co-ordiantes
            this.setCenter(currentXY.x, currentXY.y);     //Update player position
            updateBoxes();

            if (checkEntityCollision()) {
                currentXY.set(oldPosition);
                this.setCenter(oldPosition.x, oldPosition.y);   // Move back to old position
                updateBoxes(); // Remake hitbox
                moveTargetVector = null;
            }
        } else {
            moveTargetVector = null;
        }
        //No need for updating camera over here as its handled in cameraRoam otherwise it causes conflict between two

        // destVector.nor().scl(delta * speed);    // Normalize then multiply(scale) by speed and delta time

        // currentXY.add(destVector);      //Updates the current vector co-ordiantes
        // this.setCenter(currentXY.x, currentXY.y);     //Update player position
        // updateBoxes();

        // if (checkEntityCollision()) {
        //     currentXY.sub(destVector);
        //     this.setCenter(currentXY.x, currentXY.y);   // Move back to old position
        //     updateBoxes();
        //     moveTargetVector = null;
        // }
        // else if (currentXY.epsilonEquals(moveTargetVector, 0.5f)) {         //ALERT: Add a check for bounding box aswell so if we click in open area it goes there using 0.5 otherwise by bounding box calculations
        //     moveTargetVector = null;
        // }
    }



    @Override
    public void Update(float delta) {
        if (state != prevState){
            animationTimer = 0;      //Reset stateTime if we have a state CHANGE
            prevState = state;  //Update the state
        }

        this.setRegion(currentAnimation.getKeyFrame(animationTimer));        //Use the locally made stateTime which resets if a state is switched

        if (isDead) {
            state = State.DEAD;
            currentAnimation = deadAnimation;
        }
        else if (moveTargetVector != null)  {
            state = State.MOVING;
            currentAnimation = runAnimation;
        }
        else if (getAttackTarget() != null) {
            state = State.ATTACK;
        }
        else {
            state = State.IDLE;    
            currentAnimation = idleAnimation;
        }
        //Modify the else if above to check for range and reuse updateMovement
        //updateBoxes();        //UpdateBoxes in places where boxes needs to be updated like when moving

        switch(state){
        case MOVING:
            updateMovement(delta);
            break;

        case ATTACK:
            if (!isCloseToEnemy()) {
                setMove(getAttackTarget().getPosition());
            } else {
                updateAttack(delta);
                currentAnimation = attackAnimation;
            }
            break;

        default:
            break;
        }

        animationTimer += delta;
    }
}
