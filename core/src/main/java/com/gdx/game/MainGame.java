package com.gdx.game;

//import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
//import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)

//Tiled Map Loaders
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

// ALERT: Main character is lizard temporarily
// ALERT: Change Coordinate system to pure vectors??

public class MainGame extends ApplicationAdapter {
    //-----MAP WORK DECLARATION START----
    //ALERT: google why we use tmxmaploader and tiledmap together

    //Declare Maploader using TmxMapLoader
    TmxMapLoader mapLoader;

    //Declare Map var using TiledMpa
    TiledMap map;

    //Declare mapRendered by OrthogonalTiledMapRenderer
    OrthogonalTiledMapRenderer mapRenderer;

    //-----MAP WORK DECLARATION END----

    //Initializing gameWorld sizing and camera sizing
    float worldWidth = 200;                // -> Playable Region (Scaled to 1 tile = 1 world units)
    float worldHeight = 200;               //Equivalent to a pixel in tiled map

    float mapWidth = 50;
    float mapHeight = 50;       //Map number of tiles i.e 50x50 tiles
    float tileSize = 16;        //Each tile size in pixels (16x16)

    float scale = (worldWidth / mapWidth) / tileSize;       //WORKS FOR SQUARE DIMENSION ONLY       Current scale is 1 tile = 4 gameUnits

    float aspectRatio = 0.5625f;        //Use to fix scaling (comment out viewport portion and see orthocamera libgdx way)

    private float cameraWidth = 50;                 // -> Visible Region
    private float cameraHeight = 50;

    //Player move speed and camera move speed
    float cameraSpeed = 40f;             // -> Camera Move Speed (Based off above world size)

    //Health bar percent (testing to show dmg taken and if health bar works as intended)
    float healthPercent = 0.2f;   //(Keep value between 0 and 1)

    //Boolean setup to check if moving or not
    boolean playerMovement = false;
    boolean movingRight;

    // Declare Spritbatch and textureAtlas for Images
    SpriteBatch batch;
    Sprite background, healthBarSprite;

    TextureAtlas atlas;
    TextureAtlas heroAtlas;

    //DynamicSprites (self defined)
    DynamicSprite player;
    DynamicSprite testPlayer;

    //Declaring animations (of type TextureRegions)
    Animation<TextureRegion> heroRunAnimation;
    Animation<TextureRegion> heroIdleAnimation;

    //A variable to track elapsed time during animation
    float stateTime;

    //Initialize vectors 2d
    Vector2 currentXY;

    //Initialize 3D vector (since we have to unproject camera and it takes vector3 not vector 2)
    Vector3 clickCoords;
    Vector3 heroPos;

    //Declaring a viewport (to handle resizing) (You can use anykind fit extend etc just change name)
    Viewport viewport;      //Viewport is an abstract class so cannot be instantiated and should be downcasted to child (extend, fit, stretchViewport, etc)

    // Declare camera
    OrthographicCamera camera;

    @Override
    public void create() {
        // Initialize SpriteBatch
        batch = new SpriteBatch();

        //Initialize TextureAtlas
        atlas = new TextureAtlas(Gdx.files.internal("atlas\\practiceAtlas.atlas"));
        heroAtlas = new TextureAtlas(Gdx.files.internal("HeroAtlas\\Hero.atlas"));

        //Initialize bg and sprite and health bar from atlas
        
        //background = atlas.createSprite("Background");        //No need since we made our tiled map
        healthBarSprite = atlas.createSprite("healthBar");

        //Initialize the animation
        heroRunAnimation = new Animation<TextureRegion>(0.033f, heroAtlas.findRegions("Run"), PlayMode.LOOP);   //heroAtlas.findRegion gives a textureRegion from a big Texture (the big Hero.png)
        heroIdleAnimation = new Animation<TextureRegion>(0.1f, heroAtlas.findRegions("Idle"), PlayMode.LOOP); 

        //Initialize stateTime
        stateTime = 0f;

        //Initialize the DYNAMIC SPRITES
        player = new HeroPlayer(heroRunAnimation,heroIdleAnimation,stateTime,10,10,20);
        testPlayer = new HeroPlayer(heroRunAnimation, heroIdleAnimation, stateTime, 15, 15, 30);

        
        //Set background and lizard size+position
        //background.setSize(worldWidth, worldHeight);      NO NEED ANYMORE
        //background.setPosition(0,0);

        player.setSize(6,8);        //Game world units
        player.setOriginCenter();
        player.setCenter(player.getPosition().x, player.getPosition().y);            //Centre of world     (setPosition draws from bottom left setCentre draws from centre)

        testPlayer.setSize(6,8);
        testPlayer.setOriginCenter();
        testPlayer.setCenter(testPlayer.getPosition().x, testPlayer.getPosition().y);
        //Initialize starting vector coords (sprite coords here its lizard)

        // Initialize Camera
        float height = Gdx.graphics.getHeight();    //For aspect ration calculation
        float width = Gdx.graphics.getWidth();

        camera = new OrthographicCamera(cameraWidth, cameraHeight * (height / width));        //Visible region (multiplied height by aspect ratio)
        camera.position.set(player.getPosition());         //Takes vector3 so pass in the whole method made in DynamicSprite class
        camera.update();

        //Initialize Viewport (for scaling and resizing)
        viewport = new ExtendViewport(cameraWidth, cameraHeight, camera);  //Same sizing as camera as we want to scale camera coords if resized (for extend no need for aspect ratio google reason)
        
        //----MAPWORK INITIALIZATION----
        mapLoader  = new TmxMapLoader();

        //Initialize the tiledMap
        map = mapLoader.load("practiceMap\\MainMap.tmx");

        

        //Initialize the mapRenderer (this is the main working unit here which scales the tiledMap with our current game units)
        mapRenderer = new OrthogonalTiledMapRenderer(map, scale);       //We can pass our own spritebatch for optimization but will have to change some internal methods so jst let it use its own spritebatch for map
    }

    @Override
    public void render() {
        //delta exists in render because its the amount of time between first and second frame
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;     //Update stateTime in render
        
        leftClick();    // Check left click movement
        updateAllMovements(delta);
        cameraRoam(delta);  // Camera Free Roam
        updateHealthBar();  //Updates position and size of health bar

        camera.update();    //Update camera
        
        

        viewport.apply();   //Checks every frame if resized occured
        batch.setProjectionMatrix(camera.combined); 
        draw();

    }
    

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);       //True to centre camera when resized (not resizing camera here as viewport was made to handle it)

        //USE THIS IF YOU WANT FIXED RESIZING SCALE (Abit cursed)

        // float aspect = (float) width / (float) height;
        // camera.viewportWidth = cameraWidth * aspect;
        // camera.viewportHeight = cameraHeight;
    }

     @Override
    public void dispose() {
        // ALERT: Use TexturePacker
        // Dispose all textures in the list
        batch.dispose();
        atlas.dispose();
        heroAtlas.dispose();

        //Clear our map resources
        map.dispose();
        mapRenderer.dispose();      //THIS DISPOSE ISNT NECESSARY BECAUSE OUR MAP IS HANDLED IN batch ABOVE but its a good practice
    }


    //METHODS USED IN CREATE, RENDER, ETC OVER HERE 

    // Main draw function, draws everything that has breath, praise the Lord
    private void draw(){
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Draw map and set camera view (OUTSIDE of batch.begin because it has its own spriteBatch initialized (google for more info))
        mapRenderer.setView(camera);    //This makes our tiled map use our camera we made
        mapRenderer.render();           //Render the map obv

        // Draw the batch
        batch.begin();

        //Draw the sprites
        //background.draw(batch);    //No need for this as now as you can see above we render in our own map
        player.draw(batch);    // Draw player
        testPlayer.draw(batch); //Draw testPlayer
        healthBarSprite.draw(batch);    //Draw HealthBar

        batch.end();
    }
    
    // Mouse Click Event
    private void leftClick(){
        
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            // Vector is basically a class with 3 data members, includes methods for magnitude,normalization(unit vector)
            clickCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0 );
            camera.unproject(clickCoords);  // Converts screen coords to World coords

            playerMovement = true;      //Movement occured

            clickCoords.x = MathUtils.clamp(clickCoords.x, player.getWidth() / 2, worldWidth - player.getWidth() / 2);        //Doing correction because target is centered
            clickCoords.y = MathUtils.clamp(clickCoords.y, player.getHeight() / 2,worldHeight - player.getHeight() / 2);      //Binding it to world width and height dimensions

            player.movement = true;
            testPlayer.movement = true;     //Test case (Try to automate this later on)
        }
    }
    // All movement
    private void updateAllMovements(float delta){
        // Arraylist, use polymorphism call movements using for loop for all entities
        Vector2 targetVector;
        Vector2 testTargetVector;            //Test case (later on use polymorphism to call movement and loop for all entities)

        if (playerMovement){
            targetVector = new Vector2(clickCoords.x,clickCoords.y);
            testTargetVector = new Vector2(clickCoords.x,clickCoords.y);
        }
        else{
            targetVector = new Vector2(player.getPosition().x,player.getPosition().y);
            testTargetVector = new Vector2(testPlayer.getPosition().x, testPlayer.getPosition().y);
        }
           
        player.updateMovement(targetVector, stateTime, delta);
        testPlayer.updateMovement(testTargetVector, stateTime, delta);
    }

    // Camera Roam
    private void cameraRoam(float delta){
        //Camera measurements
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        //Movement
        float dx = 0;
        float dy  = 0;
        // Check inputs
        boolean up = Gdx.input.isKeyPressed(Keys.W);
        boolean down =  Gdx.input.isKeyPressed(Keys.S);
        boolean right =  Gdx.input.isKeyPressed(Keys.D);
        boolean left =  Gdx.input.isKeyPressed(Keys.A);
        
        // Check if any movement
        boolean moving = up || down || right || left ;

        if(!moving){
            // Set camera position to lizard, ALERT change to hero
            //Camera movement

            camera.position.lerp(player.getPosition(), 0.1f);            //THIS IS WHERE heroPos IS BEING USED (this brings smoothness for camera following)

            //No need for the camera clamp commented below because hero is already clamped 
            //and in free roam camera is clamped (it doesnt break you can test)

            //camera.position.x = MathUtils.clamp(heroPos.x, halfWidth - 1, (worldWidth - halfWidth) + 1);      
            //camera.position.y = MathUtils.clamp(heroPos.y, halfHeight - 1, (worldHeight - halfHeight) + 1);  
            }

        // Movement
        if(right){
            dx = cameraSpeed*delta;
        }
        if(left){
            dx = -cameraSpeed*delta;
        }
        if(up){
            dy = cameraSpeed*delta;
        }
        if(down){
            dy = -cameraSpeed*delta;
        }

        //Translating camera
        camera.translate(dx,dy,0);

        //Restricts camera borders to the map
        camera.position.x = MathUtils.clamp(camera.position.x, halfWidth -1, worldWidth - halfWidth +1);
        camera.position.y = MathUtils.clamp(camera.position.y, halfHeight -1, worldHeight - halfHeight +1);
        
    }

    public void updateHealthBar(){
        healthBarSprite.setCenter(player.getPosition().x, player.getPosition().y + 3);        //Setting it just above our hero sprite
        float healthBarWidth = 5 * healthPercent;
        float healthBarHeight = 5;

        healthBarSprite.setSize(healthBarWidth, healthBarHeight);


    }

}