package com.gdx.game;

//import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.HashMap;
import java.util.Map;


enum HeroPreset {
    // Last is height ,second last is width
    HERO_HEAVY("HeroAtlas/heavyHero.atlas", 15f, 30f, 10f, 150f, 1f, 14, 12, true),

    HERO_LIGHT("HeroAtlas/lightHero.atlas", 20f, 20f, 10f, 100f, 0.5f, 10, 10, true),

    ENEMY_HERO_LIGHT("HeroAtlas/lightEnemyHero.atlas", 20f, 20f, 10f, 125f, 0.5f, 10, 10, false),

    ENEMY_HERO_HEAVY("HeroAtlas/heavyEnemyHero.atlas", 15f, 30f, 10f, 150f, 1f, 14, 12, false);

    final String assetPath;

    final float speed;
    final float attackStrength;
    final float attackRange;
    final float maxHealth;

    // This will effect the attack animation
    float attackSpeed;

    final float spriteWidth;        //Store width and height and set at Entity.java
    final float spriteHeight;       //This gets rid of manually settings each entity size

    boolean isAlly;

    HeroPreset(String path, float speed, float attackStrength,
               float attackRange, float maxHealth, float attackSpeed,
                float spriteWidth, float spriteHeight, boolean isAlly) {

        this.assetPath = path;
        this.speed = speed;
        this.attackStrength = attackStrength;
        this.attackRange = attackRange;
        this.maxHealth = maxHealth;
        this.attackSpeed = attackSpeed;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.isAlly = isAlly;
    }
}

interface BotPreset{
    // getter methods
    float getMaxHealth();
    float getSpeed();
    float getSpriteWidth();
    float getSpriteHeight();
    boolean getIsAlly();
    float getAttackStrength();
    float getAttackSpeed();
    float getAttackRange();

}

enum GoblinPreset implements BotPreset {
    // Hardcoding gridspan at the end, width ,height
    GOBLIN("GoblinAtlas/Goblin.atlas", 10, 20, 10, 75, 2, 8, 8, true,1,2),
    ENEMY_GOBLIN("GoblinAtlas/EnemyGoblin.atlas", 10, 20, 10, 75, 2, 8, 8, false,1,2);

    final String assetPath;

    final float speed;
    final float attackStrength;
    final float attackRange;
    final float maxHealth;

    // This will effect the attack animation
    float attackSpeed;

    final float spriteWidth;        //Store width and height and set at Entity.java
    final float spriteHeight;       //This gets rid of manually settings each entity size

    int gridSpanHeight;
    int gridSpanWidth;

    boolean isAlly;

    GoblinPreset(String path, float speed, float attackStrength,
               float attackRange, float maxHealth, float attackSpeed,
                float spriteWidth, float spriteHeight, boolean isAlly, int gridSpanWidth,int gridSpanHeight) {

        this.assetPath = path;
        this.speed = speed;
        this.attackStrength = attackStrength;
        this.attackRange = attackRange;
        this.maxHealth = maxHealth;
        this.attackSpeed = attackSpeed;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.isAlly = isAlly;
        this.gridSpanWidth = gridSpanWidth;
        this.gridSpanHeight = gridSpanWidth;
    }

    @Override
    public float getMaxHealth() {
        return maxHealth;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getSpriteWidth() {
        return spriteWidth;
    }

    @Override
    public float getSpriteHeight() {
        return spriteHeight;
    }

    @Override
    public boolean getIsAlly() {
        return isAlly;
    }

    @Override
    public float getAttackStrength() {
        return attackStrength;
    }

    @Override
    public float getAttackSpeed() {
        return attackSpeed;
    }

    @Override
    public float getAttackRange() {
        return attackRange;
    }
}

enum TowerPreset {
    MAIN("TowerAtlas/MainTower.atlas",
        200, 20, 20, true),

    MINI("TowerAtlas/MiniTower.atlas",
         200, 20, 20, true),

    ENEMY_MAIN("TowerAtlas/EnemyMainTower.atlas",
         200, 20, 20, false),

    ENEMY_MINI("TowerAtlas/EnemyMiniTower.atlas",
         200, 20, 20, false);

    final String assetPath;

    final float maxHealth;

    final float spriteWidth;        //Store width and height and set at Entity.java
    final float spriteHeight;       //This gets rid of manually settings each entity size

    final boolean isAlly;

    TowerPreset(String towerAssetPath,
                       float maxHealth,
                       float spriteWidth,
                       float spriteHeight,
                       boolean isAlly) {

        this.assetPath = towerAssetPath;
        this.spriteHeight = spriteHeight;
        this.spriteWidth = spriteWidth;
        this.isAlly = isAlly;
        this.maxHealth = maxHealth;
    }
}

enum WeaponPreset {
    MAIN_TOWER("TowerAtlas/MainTowerWeapon.atlas", true, 2, 40,  10, 10),
    MINI_TOWER("TowerAtlas/MiniTowerWeapon.atlas",true, 2, 40, 6, 6),
    ENEMY_MAIN_TOWER("TowerAtlas/EnemyMainTowerWeapon.atlas", false, 2, 40, 10, 10),
    ENEMY_MINI_TOWER("TowerAtlas/EnemyMiniTowerWeapon.atlas", false, 2, 40, 6, 6);

    final String assetPath;

    final boolean isAlly;       //To decide who to play attackAnimation on

    final float maxHealth;

    final float attackInterval;
    final float attackRange;

    final float spriteWidth;        //Store width and height and set at Entity.java
    final float spriteHeight;       //This gets rid of manually settings each entity size

    WeaponPreset(String assetPath,
                        boolean isAlly,
                        float attackInterval,
                        float attackRange,
                        float spriteWidth,
                        float spriteHeight){

        this.assetPath = assetPath;
        this.isAlly = isAlly;
        this.attackInterval = attackInterval;
        this.attackRange = attackRange;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;

        this.maxHealth = 1000000f; // Later, we could use ranged attacks to take down just the bow
    }
}

enum ProjectilePreset {
    MAIN_TOWER("TowerAtlas/MainProjectile.atlas", true, 20, 30,1, 5),
    MINI_TOWER("TowerAtlas/MiniProjectile.atlas",true, 20, 30, 1, 5),
    ENEMY_MAIN_TOWER("TowerAtlas/EnemyMainProjectile.atlas", false, 20, 30, 1, 5),
    ENEMY_MINI_TOWER("TowerAtlas/EnemyMiniProjectile.atlas", false, 20, 30, 1, 5);

    final String assetPath;

    final boolean isAlly;       //To decide who to play attackAnimation on

    final float projectileSpeed;
    final float attackStrength;

    final float spriteWidth;        //Store width and height and set at Entity.java
    final float spriteHeight;       //This gets rid of manually settings each entity size

    ProjectilePreset(String assetPath,
                            boolean isAlly,
                            float projectileSpeed,
                            float attackStrength,
                            float spriteWidth,
                            float spriteHeight){

            this.assetPath = assetPath;
            this.isAlly = isAlly;
            this.projectileSpeed = projectileSpeed;
            this.attackStrength = attackStrength;
            this.spriteWidth = spriteWidth;
            this.spriteHeight = spriteHeight;
    }
}

//General loader which will NOT be instantiated just USED
final class Loader {
    // A hash map is like a python dictionary, but instead, we can any type as the key
    private static Map<HeroPreset, TextureAtlas> heroAtlass;

    private static Map<HeroPreset, Animation<TextureRegion>> heroRunAnimation;
    private static Map<HeroPreset, Animation<TextureRegion>> heroIdleAnimation;
    private static Map<HeroPreset, Animation<TextureRegion>> heroAttackAnimation;
    private static Map<HeroPreset, Animation<TextureRegion>> heroDeadAnimation;
    private static Map<HeroPreset, TextureRegion> heroHealthBar;


    // Declare hash map to store assets for GoblinPreset
    private static Map<GoblinPreset, TextureAtlas> goblinAtlass;

    private static Map<GoblinPreset, Animation<TextureRegion>> goblinRunAnimation;
    private static Map<GoblinPreset, Animation<TextureRegion>> goblinIdleAnimation;
    private static Map<GoblinPreset, Animation<TextureRegion>> goblinAttackAnimation;
    private static Map<GoblinPreset, Animation<TextureRegion>> goblinDeadAnimation;
    private static Map<GoblinPreset, TextureRegion> goblinHealthBar;

    // Declaring hash map to store assets related to Tower, Weapon, Projectile (All three related in Tower.java)
    private static Map<TowerPreset, TextureAtlas> towerAtlass;
    private static Map<WeaponPreset, TextureAtlas> weaponAtlass;
    private static Map<ProjectilePreset, TextureAtlas> projectileAtlass;


    //Tower Animations
    private static Map<TowerPreset, Animation<TextureRegion>> towerIdleAnimation;
    private static Map<TowerPreset, Animation<TextureRegion>> towerDeadAnimation;
    private static Map<TowerPreset, TextureRegion> towerHealthBar;

    //Weapon Animations
    private static Map<WeaponPreset, Animation<TextureRegion>> weaponAttackAnimation;
    private static Map<WeaponPreset, Animation<TextureRegion>> weaponIdleAnimation;
    private static Map<WeaponPreset, Animation<TextureRegion>> weaponDeadAnimation;

    //Projectile Animations
    private static Map<ProjectilePreset, Animation<TextureRegion>> projectileFlyingAnimation;
    private static Map<ProjectilePreset, Animation<TextureRegion>> projectileImpactAnimation;
    private static Map<ProjectilePreset, Animation<TextureRegion>> projectileIdleAnimation;

    public static void load(AssetManager manager) {
        // I'm keeping the ANGLED BRACKETS blank because the compiler figures out what should go there for you
        // Hash maps for hero animations to be recieved from HeroPreset
        heroAtlass = new HashMap<>();
        heroRunAnimation = new HashMap<>();
        heroIdleAnimation = new HashMap<>();
        heroAttackAnimation = new HashMap<>();
        heroDeadAnimation = new HashMap<>();
        heroHealthBar = new HashMap<>();

        //Hash maps for goblin animations to be recieved from GoblinPreset
        goblinAtlass = new HashMap<>();
        goblinRunAnimation = new HashMap<>();
        goblinIdleAnimation = new HashMap<>();
        goblinAttackAnimation = new HashMap<>();
        goblinDeadAnimation = new HashMap<>();
        goblinHealthBar = new HashMap<>();

        // Hash maps for tower and projectile presets
        towerAtlass = new HashMap<>();
        weaponAtlass = new HashMap<>();
        projectileAtlass = new HashMap<>();

        towerIdleAnimation = new HashMap<>();
        towerDeadAnimation = new HashMap<>();
        towerHealthBar = new HashMap<>();

        weaponAttackAnimation = new HashMap<>();
        weaponIdleAnimation = new HashMap<>();
        weaponDeadAnimation = new HashMap<>();

        projectileFlyingAnimation = new HashMap<>();
        projectileImpactAnimation = new HashMap<>();
        projectileIdleAnimation = new HashMap<>();

        for (HeroPreset preset : HeroPreset.values()) {
            TextureAtlas atlas = manager.get(preset.assetPath, TextureAtlas.class);
            heroAtlass.put(preset, atlas);

            heroRunAnimation.put(preset, new Animation<>(0.075f, atlas.findRegions("Run"), PlayMode.LOOP));

            heroIdleAnimation.put(preset, new Animation<>(0.5f, atlas.findRegions("Idle"), PlayMode.LOOP));

            heroDeadAnimation.put(preset, new Animation<>(0.25f, atlas.findRegions("Dead"), PlayMode.NORMAL));

            Animation<TextureRegion> attack = new Animation<>(
            0.5f, // this is just a temporary value
            atlas.findRegions("Attack"), PlayMode.LOOP);

            // Calculate the correct frame duration for the attack speed, OK?
            float attackFrameDuration = preset.attackSpeed / attack.getKeyFrames().length;

            // this reset the frame duration to the correct amount
            attack.setFrameDuration(attackFrameDuration);

            // Load all the animations into the hash map
            heroAttackAnimation.put(preset, attack);

            // Create the healthbar SPRITE
            heroHealthBar.put(preset, atlas.findRegion("heartStrip"));
        }

        //Store animation of GoblinPreset in its destined hashmap
        for (GoblinPreset preset : GoblinPreset.values()) {
            TextureAtlas gobAtlas = manager.get(preset.assetPath, TextureAtlas.class);
            goblinAtlass.put(preset, gobAtlas);

            goblinRunAnimation.put(preset, new Animation<>(0.075f, gobAtlas.findRegions("Run"), PlayMode.LOOP));

            goblinIdleAnimation.put(preset, new Animation<>(0.5f, gobAtlas.findRegions("Idle"), PlayMode.LOOP));

            goblinDeadAnimation.put(preset, new Animation<>(0.25f, gobAtlas.findRegions("Dead"), PlayMode.NORMAL));

            Animation<TextureRegion> attack = new Animation<>(
            0.5f,     // this is just a temporary value
            gobAtlas.findRegions("Attack"), PlayMode.LOOP);

            // Calculate the correct frame duration for the attack speed, OK?
            float attackFrameDuration = preset.attackSpeed / attack.getKeyFrames().length;

            // this reset the frame duration to the correct amount
            attack.setFrameDuration(attackFrameDuration);

            // Load all the animations into the hash map
            goblinAttackAnimation.put(preset, attack);

            //Create goblin health bar sprite
            goblinHealthBar.put(preset, gobAtlas.findRegion("heartStrip"));
        }

        //Store animation of TowerPreset in its destined hashmap

        // Tower loop
        for (TowerPreset preset : TowerPreset.values()) {
            TextureAtlas towerAtlas = manager.get(preset.assetPath, TextureAtlas.class);
            towerAtlass.put(preset, towerAtlas);

            // ---- Loading tower animations ----
            towerIdleAnimation.put(preset, new Animation<>(0.075f, towerAtlas.findRegions("Idle"), PlayMode.LOOP));


            towerDeadAnimation.put(preset, new Animation<>(0.25f, towerAtlas.findRegions("Dead"), PlayMode.NORMAL));

            //Create the tower healthbar sprite
            towerHealthBar.put(preset, towerAtlas.findRegion("heartStrip"));
        }

        for (WeaponPreset preset: WeaponPreset.values()){
            TextureAtlas weaponAtlas = manager.get(preset.assetPath, TextureAtlas.class);
            weaponAtlass.put(preset, weaponAtlas);

            // ---- Loading Weapon animations ----
            weaponIdleAnimation.put(preset, new Animation<>(0.075f, weaponAtlas.findRegions("weaponIdle"), PlayMode.NORMAL));

            weaponDeadAnimation.put(preset, new Animation<>(0.075f, weaponAtlas.findRegions("Dead"), PlayMode.NORMAL));

            Animation<TextureRegion> attack = new Animation<>(
                0.5f,     // this is just a temporary value
                weaponAtlas.findRegions("Attack"), PlayMode.LOOP);

            // Calculate the correct frame duration for the attack speed, OK?
            float attackFrameDuration = preset.attackInterval / attack.getKeyFrames().length;

            // this reset the frame duration to the correct amount
            attack.setFrameDuration(attackFrameDuration);

            weaponAttackAnimation.put(preset, attack);
        }

        for (ProjectilePreset preset: ProjectilePreset.values()){
            TextureAtlas projectileAtlas = manager.get(preset.assetPath, TextureAtlas.class);
            projectileAtlass.put(preset, projectileAtlas);

            // ---- Loading projectile animations ----
            projectileFlyingAnimation.put(preset, new Animation<>(0.075f, projectileAtlas.findRegions("Flying"), PlayMode.LOOP));

            projectileImpactAnimation.put(preset, new Animation<>(0.145f, projectileAtlas.findRegions("Impact"), PlayMode.NORMAL));

            projectileIdleAnimation.put(preset, new Animation<>(0.145f, projectileAtlas.findRegions("Idle"), PlayMode.NORMAL));
        }



    }

    //Getter methods in accordance to each preset
    //HeroPreset getters
    public static Animation<TextureRegion> run(HeroPreset preset) {
        return heroRunAnimation.get(preset);
    }

    public static Animation<TextureRegion> attack(HeroPreset preset) {
        return heroAttackAnimation.get(preset);
    }

    public static Animation<TextureRegion> idle(HeroPreset preset) {
        return heroIdleAnimation.get(preset);
    }

    public static Animation<TextureRegion> dead(HeroPreset preset) {
        return heroDeadAnimation.get(preset);
    }

    public static TextureRegion healthBar(HeroPreset preset) {
        return heroHealthBar.get(preset);
    }

//------------------------------------------------------------------------

    //GoblinPreset getters
    public static Animation<TextureRegion> run(BotPreset preset) {
        if(preset instanceof GoblinPreset)
            return goblinRunAnimation.get(preset);
        else
            return goblinRunAnimation.get(preset);
    }

    public static Animation<TextureRegion> attack(BotPreset preset) {
        if(preset instanceof GoblinPreset)
            return goblinAttackAnimation.get(preset);
        else
            return goblinAttackAnimation.get(preset);
    }

    public static Animation<TextureRegion> idle(BotPreset preset) {
        if(preset instanceof GoblinPreset)
            return goblinIdleAnimation.get(preset);
        else
            return goblinIdleAnimation.get(preset);
    }

    public static Animation<TextureRegion> dead(BotPreset preset) {
        if(preset instanceof GoblinPreset)
            return goblinDeadAnimation.get(preset);
        else
            return goblinDeadAnimation.get(preset); // Change to herobot after if
    }

    public static TextureRegion healthBar (BotPreset preset){
        if(preset instanceof GoblinPreset)
            return goblinHealthBar.get(preset);
        else
            return goblinHealthBar.get(preset); // Change to herobot after if
    }

//------------------------------------------------------------------------

    // TowerPreset getters
    public static Animation<TextureRegion> idle(TowerPreset preset) {
        return towerIdleAnimation.get(preset);
    }

    public static Animation<TextureRegion> dead(TowerPreset preset) {
        return towerDeadAnimation.get(preset);
    }

    public static TextureRegion healthBar (TowerPreset preset){
        return towerHealthBar.get(preset);
    }

//------------------------------------------------------------------------

    // WeaponPreset getters
    public static Animation<TextureRegion> weaponIdle(WeaponPreset preset) {
        return weaponIdleAnimation.get(preset);
    }

    public static Animation<TextureRegion> weaponDead(WeaponPreset preset) {
        return weaponDeadAnimation.get(preset);
    }

    public static Animation<TextureRegion> weaponAttack(WeaponPreset preset) {
        return weaponAttackAnimation.get(preset);
    }

//------------------------------------------------------------------------

    // Projectile Getters
    public static Animation<TextureRegion> flying(ProjectilePreset preset) {
        return projectileFlyingAnimation.get(preset);
    }

    public static Animation<TextureRegion> idle(ProjectilePreset preset) {
        return projectileIdleAnimation.get(preset);
    }

    public static Animation<TextureRegion> impact(ProjectilePreset preset) {
        return projectileImpactAnimation.get(preset);
    }
}
