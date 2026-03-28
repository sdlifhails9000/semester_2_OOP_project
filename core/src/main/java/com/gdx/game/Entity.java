package com.gdx.game;

// TODO Add projectiles for towers
// make the tower an Entity and make it's projectiles DynamicEntities that harm enemies

//import java.util.ArrayList;

// import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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


    protected Vector2 currentXY;     //Starting points (game World Coords not screen coords)  //IN CHILD CLASS NOW

    protected float stateTime = 0; // Time since last attack

    Animation<TextureRegion> idleAnimation;

    Entity(Animation<TextureRegion> idleAnimation, float stateTime, float maxHealth, float damageStrength, float attackRange) {
        super(idleAnimation.getKeyFrame(stateTime));
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.damageStrength = damageStrength;
        this.attackRange = attackRange;
    }

    void takeDamage(float damage) {
        currentHealth -= damage;

        if (currentHealth <= 0f) {
            // Set state to dead
        }
    }

    void updateAttack(Entity target, float delta) {
        // How many seconds after can you attack the other guy
        float intervalOfAttack = 0.4f;

        // Check if we have passed the interval of attack and reset the timer
        if (this.stateTime > intervalOfAttack) {
            target.takeDamage(damageStrength);
            System.out.printf("Damage dealt... %f\n", currentHealth);
            this.stateTime = 0;
        }

        this.stateTime += delta;
    }

    abstract public void Update(float stateTime, float delta);
}
