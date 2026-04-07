package com.gdx.game;

import java.util.ArrayList;
import com.badlogic.gdx.math.Vector2;

class Goblin extends DynamicEntity{
    public static ArrayList<Goblin> goblinList = new ArrayList<>(); 

    Goblin (GoblinPreset preset, int startX, int startY){
        super(
            Loader.attack(preset),
            Loader.run(preset),
            Loader.dead(preset),
            Loader.idle(preset),
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

        goblinList.add(this);
    }

    @Override
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
    public void Update(float delta){
        setAttackTarget(getAttackTarget());
        super.Update(delta);
    }

    
}

