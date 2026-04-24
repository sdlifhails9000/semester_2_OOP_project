package com.gdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.sql.Array;
import java.util.ArrayList;

class Tower extends Entity {
    public static ArrayList<Tower> towerList = new ArrayList<>();

    Animation<TextureRegion> deadAnimation;

    State towerIdleState;
    State towerDeadState;
    State currentState;

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

        WeaponPreset weaponPreset = mapWeaponPreset(preset);
        float offset = spriteHeight / 2 + 5;

        towerWeapon = new Weapon(this, weaponPreset, startX, startY + offset);
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

class Weapon extends Entity {
    Projectile arrow;
    private Tower parent;
    private Entity attackTarget;

    State weaponIdleState;
    State weaponAttackState;
    State currentState;

    float attackInterval;
    float attackRange;

    Animation<TextureRegion> attackAnimation;

    public Weapon(Tower parent, WeaponPreset preset, float startX, float startY) {
        super(Loader.weaponIdle(preset),
            startX, startY,
            preset.maxHealth,
            preset.spriteWidth,
            preset.spriteHeight,
            preset.isAlly
        );

        weaponIdleState = new WeaponIdleState();
        weaponAttackState = new WeaponAttackState();
        currentState = weaponIdleState;

        ProjectilePreset projectilePreset = mapProjectilePreset(preset);
        arrow = new Projectile(this, projectilePreset, startX, startY);
        attackAnimation = Loader.weaponAttack(preset);
        attackRange = preset.attackRange;
        attackInterval = preset.attackInterval;
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

    State projectileFlyingState;
    State projectileImpactState;
    State projectileIdleState;
    State currentState;

    float projectileSpeed;
    float attackStrength;
    Entity attackTarget;

    public Projectile(Weapon parent, ProjectilePreset preset, float startX, float startY) {
        super(Loader.flying(preset),
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
        currentState = projectileIdleState;

        projectileSpeed = preset.projectileSpeed;
        impactAnimation = Loader.impact(preset);
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
