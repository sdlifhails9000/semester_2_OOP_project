package com.gdx.game;

// TODO Add projectiles for towers
// make the tower an Entity and make it's projectiles DynamicEntities that harm enemies

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Sprite;

abstract class Entity extends Sprite {
    enum State {
        IDLE,
        MOVING,
        ATTACK,
        DEAD,
    }

    protected float maxHealth;
    protected float currentHealth;
    protected float attackStrength;
    protected float attackRange;
    protected float attackSpeed;
    protected float spriteWidth;
    protected float spriteHeight;

    protected Vector2 currentXY;     //Starting points (game World Coords not screen coords)  //IN CHILD CLASS NOW

    protected float stateTime = 0; // Time since last attack

    DynamicEntity attackTarget;     //Stores entity to attack

    State state;

    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> attackAnimation;
    Animation<TextureRegion> deadAnimation;

    Animation<TextureRegion> currentAnimation;
    
    Entity(Animation<TextureRegion> attack,
           Animation<TextureRegion> dead,
           Animation<TextureRegion> idle,
           float stateTime, float startX, float startY,
           float maxHealth,
           float attackRange,
           float attackSpeed,
           float attackStrength,
           float spriteWidth,
           float spriteHeight) {

        super(idle.getKeyFrame(stateTime));

        this.attackAnimation = attack;
        this.idleAnimation = idle;
        this.deadAnimation = dead;

        this.currentXY = new Vector2(startX, startY);

        this.stateTime = stateTime;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;     //Forgot to initialize this
        this.attackRange = attackRange;
        this.attackSpeed = attackSpeed;
        this.attackStrength = attackStrength;
        
        this.setSize(spriteWidth, spriteHeight);        //Set size here

        state = State.IDLE;
        currentAnimation = idleAnimation;
    }
    
    // Only set when you click right click. Only set it if the position is near an enemy
    protected void setAttackInfo(DynamicEntity entity){
        attackTarget = entity;
    }

    public DynamicEntity getAttackInfo() {
        return attackTarget;
    }

    protected boolean isCloseToEnemy() {
        Rectangle enemyBounds = attackTarget.getBoundingRectangle();
        Vector2 center = new Vector2();
        enemyBounds.getCenter(center);

        boolean isClose = center.dst(currentXY) <= attackRange;

        return isClose;
    }

    protected void takeDamage(float damage) {
        currentHealth -= damage;

        if (currentHealth <= 0f) {
            // Set state to dead
        }
    }

    protected void updateAttack(float delta) {                 //Override this for tower entity (If you dont want a rotating tower entity)
        // Check if we have passed the interval of attack and reset the timer
        if (stateTime >= attackSpeed) {
            attackTarget.takeDamage(attackStrength);
            System.out.printf("Attacker's Current Health... %f\n", currentHealth);
            stateTime = 0;
        }

        // Calculate the angle and flip accordingly

        Vector2 displacement = new Vector2();
        attackTarget.getBoundingRectangle().getCenter(displacement);
        displacement.sub(currentXY);

        float angle = MathUtils.atan2Deg360(displacement.y, displacement.x);

        if (angle > 90f && angle < 270f) {
            setFlip(true, false);
        } else {
            setFlip(false, false);
        }

        stateTime += delta;
    }

    abstract public void Update(float stateTime, float delta);
}
