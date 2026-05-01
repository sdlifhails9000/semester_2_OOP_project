package com.gdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class HeroPlayer extends DynamicEntity {
    public static ArrayList<HeroPlayer> heroList = new ArrayList<>();

    //Entities Declaration
    Entity attackTarget;

    //Healthbar Sprite declaration
    Sprite HealthBarSprite;

    //State declaration (for setState)
    State currentState;
    State heroIdleState;
    State heroMoveState;
    State heroChaseState;
    State heroAttackState;
    State heroDeadState;

    //Animation Declaration (Idle and dead is handled in Entity.java)   (Current animation is in entity.java because idle and dead is handled there)
    protected Animation<TextureRegion> runAnimation;
    protected Animation<TextureRegion> attackAnimation;
    protected Animation<TextureRegion> deadAnimation;

    //Stats declaration
    protected float attackRange;
    protected float attackSpeed;
    protected float attackStrength;
    protected float attackTimer;

    private TextureRegion fullHealthRegion;
    
    HeroPlayer(HeroPreset preset, int startX, int startY) {
        super(Loader.idle(preset),
              startX, startY,
              preset.maxHealth,
              preset.speed,
              preset.spriteWidth,
              preset.spriteHeight,
              preset.isAlly
        );

        heroList.add(this);

        // Selecting required states from the factory State.java
        heroIdleState = new HeroIdleState();
        heroMoveState = new HeroMoveState();
        heroChaseState = new HeroChaseState();
        heroAttackState = new HeroAttackState();
        heroDeadState = new HeroDeadState();

        //Set the currentState (its idle initially)
        currentState = heroIdleState;

        //Loads Animations
        this.attackAnimation = Loader.attack(preset);
        this.runAnimation = Loader.run(preset);
        this.deadAnimation = Loader.dead(preset);

        //Loads Stats
        this.attackRange = preset.attackRange;
        this.attackSpeed = preset.attackSpeed;
        this.attackStrength = preset.attackStrength;

        //Loads healthBarSprite and sets it above the hero with offset
        this.fullHealthRegion = Loader.healthBar(preset);
        this.HealthBarSprite = new Sprite(fullHealthRegion);
        //HealthBarSprite.setScale(0.15f);
        

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

    public void updateHealthBar() {
    float healthPercent = currentHealth / maxHealth;

    int fullWidth = fullHealthRegion.getRegionWidth();
    int height = fullHealthRegion.getRegionHeight();

    int visibleWidth = (int)(fullWidth * healthPercent);

    // Clamp so it doesn’t go negative and cry
    visibleWidth = Math.max(0, visibleWidth);

    HealthBarSprite.setRegion(
        fullHealthRegion.getRegionX(),
        fullHealthRegion.getRegionY(),
        visibleWidth,
        height
    );

    HealthBarSprite.setSize(visibleWidth * 0.15f, height * 0.15f);

    HealthBarSprite.setCenter(
        getCurrentPosition().x,
        getCurrentPosition().y + spriteHeight / 2f + 1f
    );
}

    @Override
    public void Update(float delta){
        super.Update(delta);
        currentState.update(this, delta);

        updateHealthBar();

    }

    @Override
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }
}

