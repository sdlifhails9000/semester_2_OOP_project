package com.gdx.game;

//import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import java.util.HashMap;
import java.util.Map;


enum HeroPreset {
    HERO_HEAVY("HeroAtlas/heavyHero.atlas", 15f, 30f, 10f, 150f, 1f, 14, 12, true),

    HERO_LIGHT("HeroAtlas/lightHero.atlas", 20f, 20f, 10f, 125f, 0.5f, 10, 10, true),

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

enum GoblinPreset {
    GOBLIN("GoblinAtlas/Goblin.atlas", 10, 10, 10, 75, 2, 8, 8, true),
    ENEMY_GOBLIN("GoblinAtlas/EnemyGoblin.atlas", 10, 10, 10, 75, 2, 8, 8, false);

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

    GoblinPreset(String path, float speed, float attackStrength,
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

enum TowerPreset {
    MAIN("TowerAtlas/MainTower.atlas", "TowerAtlas/MainProjectile.Atlas",
        40, 30, 200, 20, 20, true),

    MINI("TowerAtlas/MiniTower.atlas", "TowerAtlas/MiniProjectile.Atlas",
        40, 30, 200, 20, 20, true),

    ENEMY_MAIN("TowerAtlas/EnemyMainTower.atlas", "TowerAtlas/EnemyMainProjectile.Atlas",
        40, 30, 200, 20, 20, true),

    ENEMY_MINI("TowerAtlas/EnemyMiniTower.atlas", "TowerAtlas/EnemyMiniProjectile.Atlas",
        40, 30, 200, 20, 20, true);

    final String towerAssetPath;
    final String projectileAssetPath;

    final float projectileAttackStrength;
    final float attackRange;
    final float maxHealth;

    // This will effect the attack animation
    float attackSpeed;

    final float spriteWidth;        //Store width and height and set at Entity.java
    final float spriteHeight;       //This gets rid of manually settings each entity size

    boolean isAlly;

    TowerPreset(String towerAssetPath,
                       String projectileAssetPath,
                       float strength,
                       float attackRange,
                       float maxHealth,
                       float spriteWidth,
                       float spriteHeight,
                       boolean isAlly) {

        this.towerAssetPath = towerAssetPath;
        this.projectileAssetPath = projectileAssetPath;
        this.projectileAttackStrength = strength;
        this.attackRange = attackRange;
        this.spriteHeight = spriteHeight;
        this.spriteWidth = spriteWidth;
        this.isAlly = isAlly;
        this.maxHealth = maxHealth;
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

    // Declare hash map to store assets for GoblinPreset
    private static Map<GoblinPreset, TextureAtlas> goblinAtlass;

    private static Map<GoblinPreset, Animation<TextureRegion>> goblinRunAnimation;
    private static Map<GoblinPreset, Animation<TextureRegion>> goblinIdleAnimation;
    private static Map<GoblinPreset, Animation<TextureRegion>> goblinAttackAnimation;
    private static Map<GoblinPreset, Animation<TextureRegion>> goblinDeadAnimation;

    // Declaring hash map to store assets related to TowerPreset
    private static Map<TowerPreset, TextureAtlas> towerAtlass;
    private static Map<TowerPreset, TextureAtlas> projectileAtlass;

    private static Map<TowerPreset, Animation<TextureRegion>> towerIdleAnimation;
    private static Map<TowerPreset, Animation<TextureRegion>> weaponAttackAnimation;
    private static Map<TowerPreset, Animation<TextureRegion>> weaponIdleAnimation;
    private static Map<TowerPreset, Animation<TextureRegion>> towerDeadAnimation;

    private static Map<TowerPreset, Animation<TextureRegion>> projectileFlyingAnimation;
    private static Map<TowerPreset, Animation<TextureRegion>> projectileImpactAnimation;

    public static void load(AssetManager manager) {
        // I'm keeping the ANGLED BRACKETS blank because the compiler figures out what should go there for you
        // Hash maps for hero animations to be recieved from HeroPreset
        heroAtlass = new HashMap<>();
        heroRunAnimation = new HashMap<>();
        heroIdleAnimation = new HashMap<>();
        heroAttackAnimation = new HashMap<>();
        heroDeadAnimation = new HashMap<>();

        //Hash maps for goblin animations to be recieved from GoblinPreset
        goblinAtlass = new HashMap<>();
        goblinRunAnimation = new HashMap<>();
        goblinIdleAnimation = new HashMap<>();
        goblinAttackAnimation = new HashMap<>();
        goblinDeadAnimation = new HashMap<>();

        // Hash maps for tower and projectile presets
        towerAtlass = new HashMap<>();
        projectileAtlass = new HashMap<>();

        towerIdleAnimation = new HashMap<>();
        weaponAttackAnimation = new HashMap<>();
        weaponIdleAnimation = new HashMap<>();
        towerDeadAnimation = new HashMap<>();

        projectileFlyingAnimation = new HashMap<>();
        projectileImpactAnimation = new HashMap<>();

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
        }

        //Store animation of TowerPreset in its destined hashmap

        // Tower loop
        for (TowerPreset preset : TowerPreset.values()) {
            TextureAtlas towerAtlas = manager.get(preset.towerAssetPath, TextureAtlas.class);
            TextureAtlas projectileAtlas = manager.get(preset.projectileAssetPath, TextureAtlas.class);

            // ---- Loading tower animations ----
            towerIdleAnimation.put(preset, new Animation<>(0.075f, towerAtlas.findRegions("Idle"), PlayMode.LOOP));


            towerDeadAnimation.put(preset, new Animation<>(0.25f, towerAtlas.findRegions("Dead"), PlayMode.NORMAL));

            Animation<TextureRegion> attack = new Animation<>(
                0.5f,     // this is just a temporary value
                towerAtlas.findRegions("Attack"), PlayMode.LOOP);

            // Calculate the correct frame duration for the attack speed, OK?
            float attackFrameDuration = preset.attackSpeed / attack.getKeyFrames().length;

            // this reset the frame duration to the correct amount
            attack.setFrameDuration(attackFrameDuration);

            weaponAttackAnimation.put(preset, attack);

            weaponIdleAnimation.put(preset, new Animation<>(0.075f, towerAtlas.findRegions("Idle"), PlayMode.LOOP));

            // ---- Loading projectile animations ----
            projectileFlyingAnimation.put(preset, new Animation<>(0.075f, towerAtlas.findRegions("Flying"), PlayMode.LOOP));

            projectileImpactAnimation.put(preset, new Animation<>(0.25f, towerAtlas.findRegions("Impact"), PlayMode.NORMAL));
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

    //GoblinPreset getters
    public static Animation<TextureRegion> run(GoblinPreset preset) {
        return goblinRunAnimation.get(preset);
    }

    public static Animation<TextureRegion> attack(GoblinPreset preset) {
        return goblinAttackAnimation.get(preset);
    }

    public static Animation<TextureRegion> idle(GoblinPreset preset) {
        return goblinIdleAnimation.get(preset);
    }

    public static Animation<TextureRegion> dead(GoblinPreset preset) {
        return goblinDeadAnimation.get(preset);
    }

    // TowerPreset getters
    public static Animation<TextureRegion> idle(TowerPreset preset) {
        return towerIdleAnimation.get(preset);
    }


    public static Animation<TextureRegion> weaponIdle(TowerPreset preset) {
        return weaponIdleAnimation.get(preset);
    }

    public static Animation<TextureRegion> weaponAttack(TowerPreset preset) {
        return weaponAttackAnimation.get(preset);
    }

    public static Animation<TextureRegion> dead(TowerPreset preset) {
        return towerDeadAnimation.get(preset);
    }

    public static Animation<TextureRegion> flying(TowerPreset preset) {
        return projectileFlyingAnimation.get(preset);
    }

    public static Animation<TextureRegion> impact(TowerPreset preset) {
        return projectileImpactAnimation.get(preset);
    }
}
