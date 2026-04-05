//NOTES
    //Make arraylists of all types so for playertypes whose movement attack, etc has same logic make an array list for that
    //For goblins (Whose movement logic is obv different from player (they wont use LeftClick()) make a seperate array list and then update where necessary
    //REASON why i removed updateAllMovement() was that each subclass will have its unique way of feeding value to whatever updateMovement it has defined
    //So i thought to make that updateAllMovement an abstract method THOUGH if its uesless later on when we actually make other child classes.
    //Then just make Update() in DynamicSprite a concrete method in whatever way you see fit.

package com.gdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)

//Tiled Map Loaders
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;


// This contain default values for specific hero types

public class MainGame extends ApplicationAdapter {
    public static AssetManager manager = new AssetManager();

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
    float worldWidth = 200f;                // -> Playable Region (Scaled to 1 tile = 1 world units)
    float worldHeight = 200f;               //Equivalent to a pixel in tiled map

    float mapWidth = 50f;
    float mapHeight = 50f;       //Map number of tiles i.e 50x50 tiles
    float tileSize = 16f;        //Each tile size in pixels (16x16)

    float scale = (worldWidth / mapWidth) / tileSize;       //WORKS FOR SQUARE DIMENSION ONLY       Current scale is 1 tile = 4 gameUnits

    float aspectRatio = 0.5625f;        //Use to fix scaling (comment out viewport portion and see orthocamera libgdx way)

    private float cameraWidth = 50f;                 // -> Visible Region
    private float cameraHeight = 50f;

    //Player move speed and camera move speed
    float cameraSpeed = 40f;             // -> Camera Move Speed (Based off above world size)

    Vector3 clickCoords;

    // Declare Spritbatch and textureAtlas for Images
    SpriteBatch batch;
    Sprite background, healthBarSprite;

    //DynamicSprites (self defined)
    HeroPlayer player;
    HeroBot testEnemy;

    //A variable to track elapsed time during animation
    // float stateTime; //Handled in Entity.java

    //Initialize 3D vector (since we have to unproject camera and it takes vector3 not vector 2)

    //Declaring a viewport (to handle resizing) (You can use anykind fit extend etc just change name)
    Viewport viewport;      //Viewport is an abstract class so cannot be instantiated and should be downcasted to child (extend, fit, stretchViewport, etc)

    // Declare camera
    OrthographicCamera camera;

    // TODO: REMOVE
    // DEBUGGING HITBOXES
    ShapeRenderer shapeRenderer;  // DEBUG tool

    @Override
    public void create() {
        // Initialize SpriteBatch
        batch = new SpriteBatch();

        // -------------- Queue all the assets in `manager` --------------

        manager.load("atlas\\practiceAtlas.atlas", TextureAtlas.class);

        for (Preset preset : Preset.values()) {
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load("practiceMap/MainMap.tmx", TiledMap.class);
        
        manager.finishLoading(); // We could add a loading screen here if there are enough assets that it becomes slow

        // -------------- Make all the necessary thingies --------------

        Loader.load(manager);
        TextureAtlas atlas = manager.get("atlas\\practiceAtlas.atlas");
        
        // Initialize bg and sprite and health bar from atlas
        
        //background = atlas.createSprite("Background");        //No need since we made our tiled map
        healthBarSprite = atlas.createSprite("healthBar");

        clickCoords = new Vector3();

        //Initialize the DYNAMIC SPRITES
        player = new HeroPlayer(Preset.HERO_LIGHT, 50, 50);
        testEnemy = new HeroBot(Preset.ENEMY_HERO_HEAVY, 25, 25);


        // Initialize Camera
        float height = Gdx.graphics.getHeight();    //For aspect ration calculation
        float width = Gdx.graphics.getWidth();

        camera = new OrthographicCamera(cameraWidth, cameraHeight * (height / width));        //Visible region (multiplied height by aspect ratio)
        camera.position.set(player.getPosition(), 0);         //Takes vector3 so pass in the whole method made in DynamicSprite class
        camera.update();

        //Initialize Viewport (for scaling and resizing)
        viewport = new ExtendViewport(cameraWidth, cameraHeight, camera);  //Same sizing as camera as we want to scale camera coords if resized (for extend no need for aspect ratio google reason)
        
        //----MAPWORK INITIALIZATION----

        //Initialize the tiledMap
        map = manager.get("practiceMap/MainMap.tmx");

        //Initialize the mapRenderer (this is the main working unit here which scales the tiledMap with our current game units)
        mapRenderer = new OrthogonalTiledMapRenderer(map, scale);       //We can pass our own spritebatch for optimization but will have to change some internal methods so jst let it use its own spritebatch for map

        // TODO: REMOVE
        shapeRenderer = new ShapeRenderer();  // DEBUG tool
    }

    @Override
    public void render() {
        //delta exists in render because its the amount of time between first and second frame
        float delta = Gdx.graphics.getDeltaTime();  //StateTime is being handled in Update of entity
        
        clickEvent(delta);    // Check left click movement

        for (Entity e : Entity.entityList) {
            e.Update(delta);
        }

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
        batch.dispose();
        manager.dispose();
        shapeRenderer.dispose();   // TODO: Remove DEBUG tool
        mapRenderer.dispose();
    }


    //METHODS USED IN CREATE, RENDER, ETC OVER HERE 

    // Main draw function, draws everything that has breath, praise the Lord
    private void draw(){
        ScreenUtils.clear(Color.BLACK);

        //Draw map and set camera view (OUTSIDE of batch.begin because it has its own spriteBatch initialized (google for more info))
        mapRenderer.setView(camera);    //This makes our tiled map use our camera we made
        mapRenderer.render();           //Render the map obv

        // Draw the batch
        batch.begin();

        //Draw the sprites
        healthBarSprite.draw(batch);    //Draw HealthBar

        //Draw all the entities by for loop
        for (Entity e : Entity.entityList){
            e.draw(batch);
        }

        batch.end();
                    
        // TODO: REMOVE START {

        //DEBUG FOR COLLISION
        shapeRenderer.setProjectionMatrix(camera.combined);
        ArrayList<Rectangle> listOfHitbox = new ArrayList<Rectangle>();

        for (Entity e : Entity.entityList) {
            listOfHitbox.add(e.getCollisionBox());
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(Rectangle rect : listOfHitbox) {
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }

        shapeRenderer.end();

        // } REMOVE END
    }

    
    // Implement later for handling keyboard events
    // private void keyEvent(float delta) {

    // }
    
    // Mouse Click Event
    private void clickEvent(float delta){
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            // Vector is basically a class with 3 data members, includes methods for magnitude,normalization(unit vector)
            clickCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0 );
            camera.unproject(clickCoords);  // Converts screen coords to World coords
            
            //NOTE: Handling an edge case, because camera is only place which needs Vector3
            Vector2 clickCoords2D = new Vector2(clickCoords.x, clickCoords.y);     //We do this because clickCoords is only place where we need a 3d Vector. Everywhere else a 2d

            //We use for loop for playerEntities because bots i.e goblins etc will not get leftClick. Their movement call is seperate
            clickCoords2D.x = MathUtils.clamp(clickCoords2D.x, player.getWidth() / 2, worldWidth - player.getWidth() / 2);        //Doing correction because target is centered
            clickCoords2D.y = MathUtils.clamp(clickCoords2D.y, player.getHeight() / 2,worldHeight - player.getHeight() / 2);      //Binding it to world width and height dimensions
            player.setMove(clickCoords2D);     //Using the small fix for edge case above
            player.setAttackTarget(null);
        }
        else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            clickCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0 );
            camera.unproject(clickCoords);  // Converts screen coords to World coords

            Vector2 clickCoords2D = new Vector2(clickCoords.x, clickCoords.y);
            clickCoords2D.x = MathUtils.clamp(clickCoords2D.x, player.getWidth() / 2, worldWidth - player.getWidth() / 2);
            clickCoords2D.y = MathUtils.clamp(clickCoords2D.y, player.getHeight() / 2,worldHeight - player.getHeight() / 2);

            for (Entity e : Entity.entityList) {
                if (e == player) {
                    continue;
                }

                if (e.getCollisionBox().contains(clickCoords2D)) {
                    player.setMove(clickCoords2D);
                    player.setAttackTarget(e);
                    break;
                }
            }
        }
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
            Vector3 playerPosition3D = new Vector3(player.getPosition(), 0);

            camera.position.lerp(playerPosition3D, 0.1f);            //THIS IS WHERE heroPos IS BEING USED (this brings smoothness for camera following)

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
        float healthBarWidth = 5 * player.currentHealth / player.maxHealth;
        float healthBarHeight = 5;

        healthBarSprite.setSize(healthBarWidth, healthBarHeight);

    }

}