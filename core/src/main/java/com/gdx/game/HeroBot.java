package com.gdx.game;
import com.badlogic.gdx.math.Vector2;
class HeroBot extends Bot{
    HeroBot(BotPreset preset, int startX, int startY){
    super(preset, startX,startY);
    }

    @Override
    public Entity getAttackTarget() {
        Entity nearestEntity = null;
        float nearestEnemyDistance = Float.MAX_VALUE;
        Entity player = GameScreen.getPlayer();
        if(player.currentXY.dst(this.currentXY) <= attackRange+12 && !player.isDead){
            return player;
        }

        // This finds the nearest enemy to this bot
        for (Entity entity : Entity.entityList) {
            // This skips allies. They aren't enemies, entities which are dead and itself
            if (this.isAlly == entity.isAlly || entity.isDead || this == entity) {
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

        return nearestEntity;
    }
}