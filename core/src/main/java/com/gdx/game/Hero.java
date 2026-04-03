package com.gdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import java.util.HashMap;
import java.util.Map;

enum HeroPreset {
    HEAVY("HeroAtlas/heavyHero.atlas", 15f, 30f, 10f, 150f, 1f, 14, 12, true),
    LIGHT("HeroAtlas/lightHero.atlas", 25f, 20f, 10f, 125f, 0.5f, 10, 10, true),
    ENEMY_LIGHT("HeroAtlas/lightEnemyHero.atlas", 25f, 20f, 10f, 125f, 0.5f, 10, 10, false),
    ENEMY_HEAVY("HeroAtlas/heavyEnemyHero.atlas", 15f, 30f, 10f, 150f, 1f, 14, 12, false);
    
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

    HeroPreset(String path, float speed, float damageStrength,
               float attackRange, float maxHealth, float attackSpeed,
                float spriteWidth, float spriteHeight, boolean isAlly) {
        
        this.assetPath = path;
        this.speed = speed;
        this.attackStrength = damageStrength;
        this.attackRange = attackRange;         //Reserved for ranged units like towers or snipers (to be utilized later)
        this.maxHealth = maxHealth;
        this.attackSpeed = attackSpeed;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.isAlly = isAlly;
    }
}

// This class will never have an instance
class HeroLoader {
    // A hash map is like a python dictionary, but instead, we can any type as the key
    private static Map<HeroPreset, TextureAtlas> heroAtlasses;
    private static Map<HeroPreset, Animation<TextureRegion>> runAnimation;
    private static Map<HeroPreset, Animation<TextureRegion>> idleAnimation;
    private static Map<HeroPreset, Animation<TextureRegion>> attackAnimation;
    private static Map<HeroPreset, Animation<TextureRegion>> deadAnimation;

    public static void load(AssetManager manager) {
        // I'm keeping the ANGLED BRACKETS blank because the compiler figures out what should go there for you
        // In this case we defined above the brackets to contain TextureAtlas
        heroAtlasses = new HashMap<>();

        runAnimation = new HashMap<>();
        idleAnimation = new HashMap<>();
        attackAnimation = new HashMap<>();
        deadAnimation = new HashMap<>();

        for (HeroPreset preset : HeroPreset.values()) {
            TextureAtlas heroAtlas = manager.get(preset.assetPath, TextureAtlas.class);

            heroAtlasses.put(preset, heroAtlas);

            Animation<TextureRegion> attack = new Animation<>(
                0.5f, // this is just a temporary value
                heroAtlas.findRegions("Attack"), PlayMode.LOOP);

            // Calculate the correct frame duration for the attack speed, OK?
            float attackFrameDuration = preset.attackSpeed / attack.getKeyFrames().length;

            // this reset the frame duration to the correct amount
            attack.setFrameDuration(attackFrameDuration);
            
            // Load all the animations into the hash map
            runAnimation.put(preset, new Animation<>(0.075f, heroAtlas.findRegions("Run"), PlayMode.LOOP));
            attackAnimation.put(preset, attack);
            idleAnimation.put(preset, new Animation<>(0.5f, heroAtlas.findRegions("Idle"), PlayMode.LOOP));
            deadAnimation.put(preset, new Animation<>(0.25f, heroAtlas.findRegions("Dead"), PlayMode.NORMAL));
        }
    }

    public static Animation<TextureRegion> run(HeroPreset preset) {
        return runAnimation.get(preset);
    }

    public static Animation<TextureRegion> attack(HeroPreset preset) {
        return attackAnimation.get(preset);
    }

    public static Animation<TextureRegion> idle(HeroPreset preset) {
        return idleAnimation.get(preset);
    }

    public static Animation<TextureRegion> dead(HeroPreset preset) {
        return deadAnimation.get(preset);
    }
}

abstract class Hero extends DynamicEntity {
    public static ArrayList<Hero> heroList = new ArrayList<>();

    Hero(HeroPreset preset, int startX, int startY) {
        super(
            HeroLoader.attack(preset),
            HeroLoader.run(preset),
            HeroLoader.dead(preset),
            HeroLoader.idle(preset),
            startX, startY,
            
            preset.maxHealth,
            preset.attackRange,
            preset.attackSpeed,
            preset.attackStrength,
            preset.speed,
            preset.spriteWidth,
            preset.spriteHeight,
            preset.isAlly
        );

        heroList.add(this);
    }
}

// Child class number 1
class HeroPlayer extends Hero {
    HeroPlayer(HeroPreset preset, int startX, int startY) {
        super(preset, startX, startY);
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

            Vector2 heroPos = hero.getPosition();
            float enemyHeroDistance = heroPos.dst(getPosition());

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
            Vector2 entityPos = entity.getPosition();
            float distance = entityPos.dst(getPosition());

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
