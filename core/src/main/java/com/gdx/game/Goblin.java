package com.gdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class Goblin extends DynamicEntity{
    public static ArrayList<Goblin> goblinList = new ArrayList<>();

    //Entities Declaration
    Entity attackTarget;

    // Goblin States
    State goblinIdleState = new GoblinIdleState();
    State goblinMoveState = new GoblinMoveState();
    State goblinChaseState = new GoblinChaseState();
    State goblinAttackState = new GoblinAttackState();
    State goblinDeadState = new GoblinDeadState();

    //State declaration (for setState)
    State currentState;

    
    //Animation Declaration (Idle and dead is handled in Entity.java)   (Current animation is in entity.java because idle and dead is handled there)
    protected Animation<TextureRegion> runAnimation;
    protected Animation<TextureRegion> attackAnimation;

    //Stats declaration
    protected float attackRange;
    protected float attackSpeed;
    protected float attackStrength;
    protected float attackTimer;

    Goblin (GoblinPreset preset, int startX, int startY){
        super(
            Loader.idle(preset),
            Loader.dead(preset),
            startX, startY,
            
            preset.maxHealth,
            preset.speed,
            preset.spriteWidth,
            preset.spriteHeight,
            preset.isAlly
        );

        goblinList.add(this);

        this.attackAnimation = Loader.attack(preset);
        this.runAnimation = Loader.run(preset);

        this.attackRange = preset.attackRange;
        this.attackSpeed = preset.attackSpeed;
        this.attackStrength = preset.attackStrength;
        this.currentState = goblinIdleState;
    }

    //Setters and Getters
    public void setAttackTarget(Entity e){
        this.attackTarget = e;
    }

    // Is used to determine the state, sybau shaheer :wilting_rose:
    protected boolean isCloseToEnemy() {
        if (attackTarget == null) {
            return false;
        }

        Rectangle enemyBounds = attackTarget.getHitBox();
        Rectangle playerBounds = this.getHitBox();

        boolean isClose = playerBounds.overlaps(enemyBounds);

        Vector2 enemyPos = attackTarget.getCurrentPosition();

        // This condition is so that the hits of a light class register on a heavy class while chasing it
        boolean isInRange = enemyPos.dst(this.getCurrentPosition()) <= attackRange;

        return isClose || isInRange;
    }

    public Entity getAttackTarget() {
        Entity nearestEntity = null;
        float nearestEnemyDistance = Float.MAX_VALUE;

        // This finds the nearest enemy to this bot
        for (Entity entity : Entity.entityList) {
            // This skips allies. They aren't enemies, entities which are dead and itself
            if (this.isAlly == entity.isAlly || entity.isDead || this == entity) {
                continue;
            }

            // Calculate distance
            Vector2 entityPos = entity.getCurrentPosition();
            float distance = entityPos.dst(getCurrentPosition());

            // Check if distance is smaller than the current nearestEnemyDistance
            if (nearestEnemyDistance > distance) {
                nearestEnemyDistance = distance;
                nearestEntity = entity;
            }
        }

        // Handle the scenario when no entities were found
        if (nearestEnemyDistance == Float.MAX_VALUE) {
            return null;
        }

        return nearestEntity;
    }
    
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

    @Override
    public void Update(float delta) {
        setAttackTarget(getAttackTarget());
        currentState.update(this, delta);
        super.Update(delta);
    }

}