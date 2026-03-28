package com.gdx.game;

// TODO Add projectiles for towers
// make the tower an Entity and make it's projectiles DynamicEntities that harm enemies

//import java.util.ArrayList;

// import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
// import com.badlogic.gdx.graphics.GL20;
// import com.badlogic.gdx.Gdx;
// import com.badlogic.gdx.Input;
// import com.badlogic.gdx.Input.Keys;
//import com.badlogic.gdx.graphics.Texture;
// import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.math.MathUtils;
//import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

//import com.badlogic.gdx.math.Vector3;
// import com.badlogic.gdx.graphics.OrthographicCamera;
// import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
// import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
// import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)

//Tiled Map Loaders
// import com.badlogic.gdx.maps.tiled.TiledMap;
// import com.badlogic.gdx.maps.tiled.TmxMapLoader;
// import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

abstract class Entity extends Sprite {
    protected float maxHealth;
    protected float currentHealth;
    protected float damageStrength;
    protected float attackRange;
    protected int frameCount;

    protected float timeBetweenAttacks;     //Calculated over here


    protected Vector2 currentXY;     //Starting points (game World Coords not screen coords)  //IN CHILD CLASS NOW

    protected float stateTime = 0; // Time since last attack

    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> attackAnimation;
    Animation<TextureRegion> deadAnimation;
    

    Entity(Animation<TextureRegion> idleAnimation, Animation<TextureRegion> attackAnimation, Animation<TextureRegion> deadAnimation, 
        float stateTime, float maxHealth, float damageStrength, float attackRange){

        super(idleAnimation.getKeyFrame(stateTime));
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.damageStrength = damageStrength;
        this.attackRange = attackRange;

        this.attackAnimation = attackAnimation;
        this.deadAnimation = deadAnimation;
        this.idleAnimation = idleAnimation;

        this.timeBetweenAttacks = attackAnimation.getFrameDuration() * attackAnimation.getKeyFrames().length;       //Calculates itself
    }

    void takeDamage(float damage) {
        currentHealth -= damage;

        if (currentHealth <= 0f) {
            // Set state to dead
        }
    }

        void updateAttack(Entity target, float delta) {                 //Override this for tower entity (If you dont want a rotating tower entity)
        // Check if we have passed the interval of attack and reset the timer
        if (stateTime >= timeBetweenAttacks) {
            target.takeDamage(damageStrength);
            System.out.printf("Damage dealt... %f\n", currentHealth);
            stateTime = 0;
        }

        // Calculate the angle and flip accordingly

        Vector2 displacement = new Vector2();
        target.getBoundingRectangle().getCenter(displacement);
        displacement.sub(currentXY);

        float angle = MathUtils.atan2Deg360(displacement.y, displacement.x);

        if (angle > 90f && angle < 270f) {
            setFlip(true, false);
        } else {
            setFlip(false, false);
        }

        stateTime += delta;
    }

    abstract public void Update(float stateTime, float delta);
}
