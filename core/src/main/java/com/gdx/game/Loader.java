package com.gdx.game;

//import java.util.ArrayList;
import java.util.EnumSet;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import java.util.HashMap;
import java.util.Map;

//To define which animation states the preset contains
enum AnimState {
    IDLE,
    RUN,
    ATTACK,
    DEAD
}

enum Preset {
    HERO_HEAVY("HeroAtlas/heavyHero.atlas", 15f, 30f, 10f, 150f, 1f, 14, 12, true,
                EnumSet.of(AnimState.IDLE, AnimState.RUN, AnimState.ATTACK, AnimState.DEAD)),

    HERO_LIGHT("HeroAtlas/lightHero.atlas", 25f, 20f, 10f, 125f, 0.5f, 10, 10, true,
                EnumSet.of(AnimState.IDLE, AnimState.RUN, AnimState.ATTACK, AnimState.DEAD)),

    ENEMY_HERO_LIGHT("HeroAtlas/lightEnemyHero.atlas", 25f, 20f, 10f, 125f, 0.5f, 10, 10, false,
                EnumSet.of(AnimState.IDLE, AnimState.RUN, AnimState.ATTACK, AnimState.DEAD)),

    ENEMY_HERO_HEAVY("HeroAtlas/heavyEnemyHero.atlas", 15f, 30f, 10f, 150f, 1f, 14, 12, false,
                EnumSet.of(AnimState.IDLE, AnimState.RUN, AnimState.ATTACK, AnimState.DEAD));

    // TODO: Shaheer make its atlas :D
    //GOBLIN()
    //ENEMY_GOBLIN();
    
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
    EnumSet<AnimState> animationStates;     //AnimStates for each preset 

    Preset(String path, float speed, float attackStrength,
               float attackRange, float maxHealth, float attackSpeed,
                float spriteWidth, float spriteHeight, boolean isAlly, EnumSet<AnimState> animationStates) {
        
        this.assetPath = path;
        this.speed = speed;
        this.attackStrength = attackStrength;
        this.attackRange = attackRange;         
        this.maxHealth = maxHealth;
        this.attackSpeed = attackSpeed;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.isAlly = isAlly;
        this.animationStates = animationStates;
    }
}

//General loader which will NOT be instantiated just USED
final class Loader {
    // A hash map is like a python dictionary, but instead, we can any type as the key
    private static Map<Preset, TextureAtlas> atlasses;

    private static Map<Preset, Animation<TextureRegion>> runAnimation;
    private static Map<Preset, Animation<TextureRegion>> idleAnimation;
    private static Map<Preset, Animation<TextureRegion>> attackAnimation;
    private static Map<Preset, Animation<TextureRegion>> deadAnimation;

    public static void load(AssetManager manager) {
        // I'm keeping the ANGLED BRACKETS blank because the compiler figures out what should go there for you
        // In this case we defined above the brackets to contain TextureAtlas
        atlasses = new HashMap<>();

        runAnimation = new HashMap<>();
        idleAnimation = new HashMap<>();
        attackAnimation = new HashMap<>();
        deadAnimation = new HashMap<>();
        
        for (Preset preset : Preset.values()) {
            TextureAtlas atlas = manager.get(preset.assetPath, TextureAtlas.class);
            atlasses.put(preset, atlas);

            if (preset.animationStates.contains(AnimState.RUN)){
                runAnimation.put(preset, new Animation<>(0.075f, atlas.findRegions("Run"), PlayMode.LOOP));
            }

            if (preset.animationStates.contains(AnimState.IDLE)){
                idleAnimation.put(preset, new Animation<>(0.5f, atlas.findRegions("Idle"), PlayMode.LOOP));
            }

            if (preset.animationStates.contains(AnimState.DEAD)){
                deadAnimation.put(preset, new Animation<>(0.25f, atlas.findRegions("Dead"), PlayMode.NORMAL));
            }

            if (preset.animationStates.contains(AnimState.ATTACK)){
                Animation<TextureRegion> attack = new Animation<>(
                0.5f, // this is just a temporary value
                atlas.findRegions("Attack"), PlayMode.LOOP);

                // Calculate the correct frame duration for the attack speed, OK?
                float attackFrameDuration = preset.attackSpeed / attack.getKeyFrames().length;

                // this reset the frame duration to the correct amount
                attack.setFrameDuration(attackFrameDuration);
                
                // Load all the animations into the hash map
                attackAnimation.put(preset, attack);
            }
        }
    }

    public static Animation<TextureRegion> run(Preset preset) {
        return runAnimation.get(preset);
    }

    public static Animation<TextureRegion> attack(Preset preset) {
        return attackAnimation.get(preset);
    }

    public static Animation<TextureRegion> idle(Preset preset) {
        return idleAnimation.get(preset);
    }

    public static Animation<TextureRegion> dead(Preset preset) {
        return deadAnimation.get(preset);
    }
}
