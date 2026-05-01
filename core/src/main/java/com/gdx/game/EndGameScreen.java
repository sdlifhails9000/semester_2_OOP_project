package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class EndGameScreen implements Screen {
    private final MainGame game;
    private final boolean playerWon;

    private Stage stage;
    private Skin skin;

    private Sound clickSound;
    private Music loseMusic;
    private Music winMusic;
    private Music currentMusic;

    private static final float BG_R = 0.07f, BG_G = 0.07f, BG_B = 0.10f;

    // -----------------------------------------------------------------------
    // Constructor — pass in outcome data from GameScreen
    // -----------------------------------------------------------------------
    public EndGameScreen(MainGame game, boolean playerWon) {
        this.game = game;
        this.playerWon = playerWon;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createSkin();

        Table root = buildLayout();
        stage.addActor(root);

        // ── Audio ──────────────────────────────────────────────────────────
        clickSound = game.manager.get("Kwality_Sounds/Game_Click.mp3", Sound.class);
        // Swap "End_Music.mp3" for whatever track you have
        loseMusic = game.manager.get("Kwality_Sounds/Game_Lose.wav", Music.class);
        winMusic = game.manager.get("Kwality_Sounds/Game_Win.wav", Music.class);

        if (playerWon) {
            currentMusic = winMusic;
        } else {
            currentMusic = loseMusic;
        }

        currentMusic.setLooping(false);
        currentMusic.setVolume(0.4f);
        currentMusic.play();
    }

    // -----------------------------------------------------------------------
    // Skin — mirrors MainScreen.createSkin() exactly.
    // To swap fonts/styles later, only touch this method.
    // -----------------------------------------------------------------------
    private void createSkin() {
        skin = new Skin();

        // 1. Fonts  (drop-in replacements: load a FreeTypeFontGenerator
        //    or TTF via game.manager and just swap the BitmapFont here)
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(4.0f);
        skin.add("title", titleFont);

        BitmapFont btnFont = new BitmapFont();
        btnFont.getData().setScale(2.2f);
        skin.add("default", btnFont);

        BitmapFont hintFont = new BitmapFont();
        hintFont.getData().setScale(1.3f);
        skin.add("hint", hintFont);

        // 2. Button textures
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        pixmap.setColor(0.15f, 0.15f, 0.22f, 1f);   // idle
        pixmap.fill();
        skin.add("btn-bg", new Texture(pixmap));

        pixmap.setColor(0.25f, 0.20f, 0.45f, 1f);   // hover
        pixmap.fill();
        skin.add("btn-hov", new Texture(pixmap));

        pixmap.dispose();

        // 3. Button style  ← change tbs.font / tbs.up / tbs.over freely
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up   = skin.newDrawable("btn-bg");
        tbs.over = skin.newDrawable("btn-hov");
        tbs.font = skin.getFont("default");
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        // 4. Label styles  ← swap colors / fonts per style independently
        skin.add("title", new Label.LabelStyle(
            skin.getFont("title"), new Color(0.90f, 0.75f, 1.00f, 1f)));

        skin.add("sub", new Label.LabelStyle(
            skin.getFont("default"), new Color(0.65f, 0.60f, 0.80f, 1f)));

        skin.add("hint", new Label.LabelStyle(
            skin.getFont("hint"), new Color(0.65f, 0.60f, 0.80f, 0.75f)));

        // 5. Outcome-specific label styles
        //    WIN  → bright gold
        skin.add("outcome-win", new Label.LabelStyle(
            skin.getFont("title"), new Color(1.00f, 0.85f, 0.20f, 1f)));

        //    LOSE → muted red
        skin.add("outcome-lose", new Label.LabelStyle(
            skin.getFont("title"), new Color(0.90f, 0.25f, 0.25f, 1f)));
    }

    // -----------------------------------------------------------------------
    // Layout
    // -----------------------------------------------------------------------
    private Table buildLayout() {
        Table t = new Table();
        t.setFillParent(true);

        // ── Outcome headline ───────────────────────────────────────────────
        String outcomeText  = playerWon ? "VICTORY!"   : "DEFEATED";
        String outcomeStyle = playerWon ? "outcome-win" : "outcome-lose";
        Label outcomeLabel  = new Label(outcomeText, skin, outcomeStyle);


        // ── Buttons ────────────────────────────────────────────────────────
        TextButton retryBtn = new TextButton("PLAY AGAIN", skin);
        TextButton menuBtn  = new TextButton("MAIN MENU",  skin);

        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                // Re-open hero picker instead of hard-coding a preset
                game.setScreen(new MainScreen(game));
            }
        });

        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new MainScreen(game));
            }
        });

        // ── Assembly ───────────────────────────────────────────────────────
        t.add(outcomeLabel).padBottom(20f).row();
        t.add(retryBtn).size(280f, 54f).padBottom(20f).row();
        t.add(menuBtn).size(280f, 54f);

        return t;
    }

    // -----------------------------------------------------------------------
    // Screen lifecycle
    // -----------------------------------------------------------------------
    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_R, BG_G, BG_B, 1.0f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { dispose(); }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        currentMusic.stop();
    }
}
