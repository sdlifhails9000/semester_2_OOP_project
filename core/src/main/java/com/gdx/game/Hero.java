package com.gdx.game;

import java.util.ArrayList;
import com.badlogic.gdx.math.Vector2;

abstract class Hero extends DynamicEntity {
    public static ArrayList<Hero> heroList = new ArrayList<>();

    Hero(Preset preset, int startX, int startY) {
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

        heroList.add(this);
    }
}

// Child class number 1
class HeroPlayer extends Hero {
    HeroPlayer(Preset preset, int startX, int startY) {
        super(preset, startX, startY);
    }
}

class HeroBot extends Hero {
    private static final float HERO_ALERT_RADIUS = 10f;

    HeroBot(Preset preset, int startX, int startY) {
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
