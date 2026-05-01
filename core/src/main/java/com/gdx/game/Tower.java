package com.gdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

class Tower extends Entity {
    public static ArrayList<Tower> towerList = new ArrayList<>();

    Animation<TextureRegion> deadAnimation;

    //Healthbar Sprite necessity declaration
    Sprite HealthBarSprite;
    private TextureRegion fullHealthRegion;

    State<Tower> towerIdleState;
    State<Tower> towerDeadState;
    State<Tower> currentState;

    Weapon towerWeapon;

    public Tower(TowerPreset preset, float startX, float startY) {
        super(Loader.idle(preset),
            startX, startY,
            preset.maxHealth,
            preset.spriteWidth,
            preset.spriteHeight,
            preset.isAlly
        );

        deadAnimation = Loader.dead(preset);

        towerIdleState = new TowerIdleState();
        towerDeadState = new TowerDeadState();
        currentState = towerIdleState;

        //Loads healthBarSprite and sets it above the hero with offset
        this.fullHealthRegion = Loader.healthBar(preset);
        this.HealthBarSprite = new Sprite(fullHealthRegion);

        WeaponPreset weaponPreset = mapWeaponPreset(preset);
        float offset = 5;

        towerWeapon = new Weapon(weaponPreset, this, startX, startY + offset);

        towerList.add(this);
    }

    private WeaponPreset mapWeaponPreset(TowerPreset preset) {
        switch (preset) {
        case MAIN:
            return WeaponPreset.MAIN_TOWER;

        case MINI:
            return WeaponPreset.MINI_TOWER;

        case ENEMY_MAIN:
            return WeaponPreset.ENEMY_MAIN_TOWER;

        default:
            break;
        }

        return WeaponPreset.ENEMY_MINI_TOWER;
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
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

    @Override
    public void Update(float delta) {
        super.Update(delta);
        currentState.update(this, delta);

        updateHealthBar();
    }
}

class Weapon extends Entity {
    Tower parent;       //So that weapon can recognize its tower and not kill itself
    Projectile arrow;
    private Entity attackTarget;

    State<Weapon> weaponIdleState;
    State<Weapon> weaponAttackState;
    State<Weapon> weaponDeadState;
    State<Weapon> currentState;

    float attackInterval;
    float attackRange;

    Animation<TextureRegion> attackAnimation;
    Animation<TextureRegion> deadAnimation;

    public Weapon(WeaponPreset preset, Tower parent, float startX, float startY) {
        super(Loader.weaponIdle(preset),
            startX, startY,
            preset.maxHealth,
            preset.spriteWidth,
            preset.spriteHeight,
            preset.isAlly
        );

        this.parent = parent;       //So the weapon can recognize its tower and not kill itself

        weaponIdleState = new WeaponIdleState();
        weaponAttackState = new WeaponAttackState();
        weaponDeadState = new WeaponDeadState();
        currentState = weaponIdleState;

        attackAnimation = Loader.weaponAttack(preset);
        deadAnimation = Loader.weaponDead(preset);
        attackRange = preset.attackRange;
        attackInterval = preset.attackInterval;

        ProjectilePreset projectilePreset = mapProjectilePreset(preset);
        arrow = new Projectile(this, projectilePreset, startX, startY);
    }

    //Setters and Getters
    public void setAttackTarget(Entity e){
        this.attackTarget = e;
    }

    public Entity getAttackTarget(){
        return attackTarget;
    }

    private ProjectilePreset mapProjectilePreset(WeaponPreset preset) {
        switch (preset) {
        case MAIN_TOWER:
            return ProjectilePreset.MAIN_TOWER;

        case MINI_TOWER:
            return ProjectilePreset.MINI_TOWER;

        case ENEMY_MAIN_TOWER:
            return ProjectilePreset.ENEMY_MAIN_TOWER;

        default:
            break;
        }

        return ProjectilePreset.ENEMY_MINI_TOWER;
    }

    @Override
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

    @Override
    public void Update(float delta) {
        super.Update(delta);
        currentState.update(this, delta);
    }
}

class Projectile extends DynamicEntity {
    Weapon parent;

    Animation<TextureRegion> impactAnimation;
    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> flyingAnimation;

    State<Projectile> projectileFlyingState;
    State<Projectile> projectileImpactState;
    State<Projectile> projectileIdleState;
    State<Projectile> currentState;

    float projectileSpeed;
    float attackStrength;
    Entity attackTarget;

    public Projectile(Weapon parent, ProjectilePreset preset, float startX, float startY) {
        super(Loader.flying(preset),    //Passing in flying so that we generate correct sized hitbox (idle animation is not upto scale and is transparent)
            startX, startY,
            1000000, // placeholder
            preset.projectileSpeed,
            preset.spriteWidth,
            preset.spriteHeight,
            parent.isAlly
        );

        projectileIdleState = new ProjectileIdleState();
        projectileFlyingState = new ProjectileFlyingState();
        projectileImpactState = new ProjectileImpactState();

        //Initial state is idleState (DO NOT CHANGE)
        currentState = projectileIdleState;

        projectileSpeed = preset.projectileSpeed;
        attackStrength = preset.attackStrength;

        impactAnimation = Loader.impact(preset);
        idleAnimation = Loader.idle(preset);
        flyingAnimation = Loader.flying(preset);
        currentAnimation = idleAnimation;
        this.parent = parent;
    }

    //Setters and Getters
    public void setAttackTarget(Entity e){
        this.attackTarget = e;
    }

    public Entity getAttackTarget() {
        return attackTarget;
    }

    @Override
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

    @Override
    public void Update(float delta) {
        super.Update(delta);

        currentState.update(this, delta);
    }
}
