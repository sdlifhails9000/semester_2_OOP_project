package com.gdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

interface State<T extends Entity> {
    void enter(T e);    // Called when entering this state
    void update(T e, float delta);  // Called every frame while in this stat
    void exit(T e); // Called when exiting the state
}

//---------REAL HERO PLAYER STATES-----------
class HeroIdleState implements State<HeroPlayer> {
    @Override
    public void enter(HeroPlayer e) {
        e.currentAnimation = e.idleAnimation;
    }

    @Override
    public void update(HeroPlayer e, float delta) {
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            return;
        }

        // if the target position and current position don't match, start moving to that target position
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroMoveState);
            return;
        }

        if(e.getAttackTarget() != null){
            e.setState(e.heroAttackState);
        }

        e.healTimer += delta;
        if (e.healTimer >= 5) {
            e.healingRateRestrictTimer += delta;
            if (e.healingRateRestrictTimer > 1f) {
                e.currentHealth = MathUtils.clamp(e.currentHealth + 20f, 0, e.maxHealth);
                e.healingRateRestrictTimer = 0f;
            }
        }
    }

    @Override
    public void exit (HeroPlayer e){
        e.animationTimer = 0;
    }
}

class HeroMoveState implements State<HeroPlayer> {
    @Override
    public void enter(HeroPlayer e) {
        e.currentAnimation = e.runAnimation;
    }

    @Override
    public void update(HeroPlayer e, float delta) {
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            return;
        }

        //In case of rightClickEvent an attackTarget is generated
        if(e.getAttackTarget() != null){
            e.setState(e.heroAttackState);
            return;
        }

        // If the current position and target position are equal, to a certain degree, stop walking and go into the
        if (e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroIdleState);
            return;
        }

        // calculate speed and turn towards the target
        // Takes leftClick as input
        e.moveTowards(e.getTargetPosition(), delta);

        // Change position based on velocity

        // Change x
        e.currentXY.x += e.velocity.x * delta;
        e.updateBoxes();

        // Handle collision;
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.x -= e.velocity.x * delta;
            e.velocity.x = 0;
            e.updateBoxes();
        }

        // Change y
        e.currentXY.y += e.velocity.y * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.y -= e.velocity.y * delta;
            e.velocity.y = 0;
            e.updateBoxes();
        }

        if (e.velocity.isZero()) {
            e.setState(e.heroIdleState);
        }

        // Update collision and hitboxes and update the sprite position
        e.setCenter(e.currentXY.x, e.currentXY.y);

        e.healTimer += delta;
        if (e.healTimer >= 5) {
            e.healingRateRestrictTimer += delta;
            if (e.healingRateRestrictTimer > 1f) {
                e.currentHealth = MathUtils.clamp(e.currentHealth + 20f, 0, e.maxHealth);
                e.healingRateRestrictTimer = 0f;
            }
        }
    }

    @Override
    public void exit(HeroPlayer e) {
        e.velocity.setZero();
        e.targetPosition.set(e.currentXY);
        e.animationTimer = 0;
    }
}

class HeroAttackState implements State<HeroPlayer> {
    public void enter(HeroPlayer e){
        e.currentAnimation = e.attackAnimation;
        e.healTimer = 0f;
        e.healingRateRestrictTimer = 0f;
    }

    @Override
    public void update(HeroPlayer e, float delta){
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            e.attackTarget = null;
            return;
        }

        //In case hero leftClicks a targetPosition is set and currentXY will obv not match it
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroMoveState);
            e.attackTarget = null;
            return;
        }

        //If the target is dead
        if (e.getAttackTarget().isDead) {
            e.setState(e.heroIdleState);
            e.attackTarget = null;
            return;
        }

        //If our hero is far from the enemy
        if (!e.isCloseToEnemy()){
            e.setState(e.heroChaseState);
            return;
        }

        // Check if we have passed the interval of attack and reset the timer
        if (e.attackTimer >= e.attackSpeed) {
            e.attackTarget.takeDamage(e.attackStrength);
            e.attackTimer = 0;
        }

        // Calculate the angle and flip accordingly

        Vector2 displacement =  e.attackTarget.getCurrentPosition().cpy();
        displacement.sub(e.getCurrentPosition());

        float angle = MathUtils.atan2Deg360(displacement.y, displacement.x);

        if (angle > 90f && angle < 270f) {
            //this.setRotation(angle + 180);
            e.setFlip(true, false);
        } else {
            //this.setRotation(angle);
            e.setFlip(false, false);
        }

        e.attackTimer += delta;
    }

    @Override
    public void exit(HeroPlayer e) {
        e.animationTimer = 0;
    }
}

class HeroChaseState implements State<HeroPlayer> {
    public void enter(HeroPlayer e){
        e.currentAnimation = e.runAnimation;
    }

    public void update(HeroPlayer e, float delta){
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            e.attackTarget = null;
            return;
        }

        //In case hero leftClicks a targetPosition is set and currentXY will obv not match it
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroMoveState);
            e.attackTarget = null;
            return;
        }

        //If the target is dead
        if (e.getAttackTarget().isDead) {
            e.setState(e.heroIdleState);
            e.attackTarget = null;
            return;
        }

        if (e.isCloseToEnemy()){
            e.setState(e.heroAttackState);
            return;
        }

        // calculate speed and turn towards the target
        e.moveTowards(e.attackTarget.getCurrentPosition(), delta);

        // Change position based on velocity

        // Change x
        e.currentXY.x += e.velocity.x * delta;
        e.targetPosition.x += e.velocity.x * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.x -= e.velocity.x * delta;
            e.targetPosition.x -= e.velocity.x * delta;
            e.velocity.x = 0;
            e.updateBoxes();
        }

        // Change y
        e.currentXY.y += e.velocity.y * delta;
        e.targetPosition.y += e.velocity.y * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.y -= e.velocity.y * delta;
            e.targetPosition.y -= e.velocity.y * delta;
            e.velocity.y = 0;
            e.updateBoxes();
        }

        if (e.velocity.isZero()) {
            e.setState(e.heroIdleState);
        }

        // Update collision and hitboxes and update the sprite position
        e.setCenter(e.currentXY.x, e.currentXY.y);

        e.healTimer += delta;
        if (e.healTimer >= 5) {
            e.currentHealth = MathUtils.clamp(e.currentHealth + 20f, 0, e.maxHealth);
        }
    }

    public void exit(HeroPlayer e){
        e.velocity.setZero();
        e.animationTimer = 0;
    }
}

class HeroDeadState implements State<HeroPlayer> {
    private float timer = 0f;
    private static final float MAX_RESPAWN_TIME = 5f; // In seconds btw
    private boolean isDying;
    private boolean isRespawing;
    private boolean blocked;

    public void enter (HeroPlayer e){
        e.currentAnimation = e.deadAnimation;
        e.currentAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        isDying = true;
        isRespawing = false;
        blocked = false;        //When in deadState your entity is not blocked
    }

    public void update (HeroPlayer e, float delta) {
        if (isDying) {
            if (e.currentAnimation.isAnimationFinished(e.animationTimer)) {
                isDying = false;
            }
            return;
        }
        else if (isRespawing) {
            if (e.currentAnimation.isAnimationFinished(e.animationTimer)) {
                e.setState(e.heroIdleState);
            }
            return;
        }
        else if (timer < MAX_RESPAWN_TIME) {
            timer += delta;
            return;
        }

        blocked = false;
        //Every entity has their own specific spawn points
        float respawnPositionX = e.startX;
        float respawnPositionY = e.startY;

        e.setCurrentPosition(respawnPositionX, respawnPositionY);
        e.updateBoxes();

        //In case someone is standing on their spawn point
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            blocked = true;     //In case someone is standing on his respawn point
            // Revert immediately
            e.setCurrentPosition(-9999, -9999); // Some random position while we wait
            e.updateBoxes();
        }


        if (!blocked){
            e.setTargetPosition(respawnPositionX, respawnPositionY);
            e.setCenter(respawnPositionX, respawnPositionY);
            e.currentHealth = e.maxHealth;
            e.isDead = false;
            timer = 0f;
            isRespawing = true;     //This flag is what respawns the entity
            e.currentAnimation.setPlayMode(Animation.PlayMode.REVERSED);
            e.animationTimer = 0f;
        }
    }

    @Override
    public void exit (HeroPlayer e) {
        e.animationTimer = 0;
    }
}
