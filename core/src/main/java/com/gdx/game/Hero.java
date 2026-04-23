package com.gdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

abstract class Hero extends DynamicEntity {
    public static ArrayList<Hero> heroList = new ArrayList<>();

    //Entities Declaration
    Entity attackTarget;

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

    Hero(HeroPreset preset, int startX, int startY) {
        super(Loader.idle(preset),
              Loader.dead(preset),
              startX, startY,

              preset.maxHealth,
              preset.speed,
              preset.spriteWidth,
              preset.spriteHeight,
              preset.isAlly
        );

        heroList.add(this);

        this.attackAnimation = Loader.attack(preset);
        this.runAnimation = Loader.run(preset);

        this.attackRange = preset.attackRange;
        this.attackSpeed = preset.attackSpeed;
        this.attackStrength = preset.attackStrength;
    }

    //Setters and Getters
    public void setAttackTarget(Entity e){
        this.attackTarget = e;
    }

    public Entity getAttackTarget(){
        return attackTarget;
    }

    // Is used to determine the state, don't touchy wouchy this or else things will break. TF2 coconut.jpeg moment
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

    @Override
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

}

//Keeping it a seperate class if we want some seperate capabilities other than HeroBot
class HeroPlayer extends Hero{
    State heroIdleState;
    State heroMoveState;
    State heroChaseState;
    State heroAttackState;
    State heroDeadState;

    HeroPlayer(HeroPreset preset, int startX, int startY) {
        super(preset, startX, startY);

        // Selecting required states from the factory State.java
        heroIdleState = new HeroIdleState();
        heroMoveState = new HeroMoveState();
        heroChaseState = new HeroChaseState();
        heroAttackState = new HeroAttackState();
        heroDeadState = new HeroDeadState();

        //Set the currentState (its idle initially)
        currentState = heroIdleState;
    }

    @Override
    public void Update(float delta){
        super.Update(delta);
        currentState.update(this, delta);
    }
}

class HeroBot extends Hero {
    private static final float HERO_ALERT_RADIUS = 10f;

    HeroBot(HeroPreset preset, int startX, int startY) {
        super(preset, startX, startY);
    }

    @Override
    public Entity getAttackTarget() {
        for (Hero hero : Hero.heroList) {
            if (isAlly == hero.isAlly || hero.isDead || this == hero) {
                continue;
            }

            Vector2 heroPos = hero.getCurrentPosition();
            float enemyHeroDistance = heroPos.dst(getCurrentPosition());

            if (enemyHeroDistance < HERO_ALERT_RADIUS) {
                return hero;
            }
        }

        Entity nearestEntity = null;
        float nearestEnemyDistance = Float.MAX_VALUE;

        // This finds the nearest enemy to this bot
        for (Entity entity : Entity.entityList) {
            // This skips allies. They aren't enemies
            if (isAlly == entity.isAlly || entity.isDead || this == entity) {
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
            System.out.println("None found");
            return null;
        }

        return nearestEntity;
    }

    @Override
    public void Update(float delta) {
        setAttackTarget(getAttackTarget());
        super.Update(delta);
    }
}
