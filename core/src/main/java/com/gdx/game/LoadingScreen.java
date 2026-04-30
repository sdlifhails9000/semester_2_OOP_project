package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LoadingScreen implements Screen {

    private final MainGame game;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;

    // Bar visual settings
    private static final float BAR_WIDTH  = 400f;
    private static final float BAR_HEIGHT = 28f;
    private static final float BAR_BORDER = 2f;

    public LoadingScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch         = new SpriteBatch();
        font          = new BitmapFont(); // default libGDX font, swap for your own
        font.setColor(Color.WHITE);

        camera   = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {

        // manager.update() loads a chunk of assets per frame.
        // Returns true once ALL assets are fully loaded.
        if (game.manager.update()) {
            // Assets are ready — run Loader.load() then switch to menu
            Loader.load(game.manager);
            game.setScreen(new GameScreen(game));
            return; // stop rendering this screen
        }

        // 0.0 → 1.0 progress value from the AssetManager
        float progress = game.manager.getProgress();

        // Clear screen to a dark background
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();


        float screenW = viewport.getWorldWidth();
        float screenH = viewport.getWorldHeight();
        float barX    = (screenW - BAR_WIDTH)  / 2f;
        float barY    = (screenH - BAR_HEIGHT) / 2f;

        shapeRenderer.setProjectionMatrix(camera.combined);

        // --- Draw background (dark grey track) ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);
        shapeRenderer.rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);

        // --- Draw filled progress portion ---
        shapeRenderer.setColor(0.2f, 0.75f, 0.35f, 1f); // green fill
        shapeRenderer.rect(barX, barY, BAR_WIDTH * progress, BAR_HEIGHT);
        shapeRenderer.end();

        // --- Draw white border around track ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
            barX    - BAR_BORDER,
            barY    - BAR_BORDER,
            BAR_WIDTH  + BAR_BORDER * 2,
            BAR_HEIGHT + BAR_BORDER * 2
        );
        shapeRenderer.end();

        // --- Draw "Loading... XX%" text above the bar ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        String text = "Loading... " + (int)(progress * 100) + "%";
        font.draw(batch, text, barX, barY + BAR_HEIGHT + 20f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { dispose(); }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        // do NOT dispose MainGame.manager here — it lives in MainGame
    }
}
