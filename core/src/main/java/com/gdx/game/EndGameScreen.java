package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class EndGameScreen implements Screen {

    private final MainGame game;
    private final boolean playerWon;
    private final boolean draw;

    private Stage stage;
    private Skin skin;

    private Sound clickSound;
    private Music currentMusic;

    private static final float BG_R = 0.07f, BG_G = 0.07f, BG_B = 0.10f;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public EndGameScreen(MainGame game, boolean playerWon, boolean draw) {
        this.game      = game;
        this.playerWon = playerWon;
        this.draw      = draw;
    }

    // -----------------------------------------------------------------------
    // show()
    // -----------------------------------------------------------------------
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Single skin load — same asset used by GameScreen HUD
        skin = new Skin(Gdx.files.internal("UI/uiskin.json"));

        // Outcome-specific label styles built on top of uiskin's default font
        // Change fontColor values freely — font itself comes from uiskin
        Label.LabelStyle winStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        winStyle.fontColor = new Color(1.00f, 0.85f, 0.20f, 1f);   // gold
        skin.add("outcome-win", winStyle);

        Label.LabelStyle loseStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        loseStyle.fontColor = new Color(0.90f, 0.25f, 0.25f, 1f);  // red
        skin.add("outcome-lose", loseStyle);

        Label.LabelStyle drawStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        drawStyle.fontColor = new Color(0.60f, 0.80f, 1.00f, 1f);  // blue-white
        skin.add("outcome-draw", drawStyle);

        //OverWriting so we can get our own custom hoverbutton effect
        //Clone the default button style, never modify skins original directly
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));

        // Override only the hover state — everything else stays from uiskin
        btnStyle.over = skin.newDrawable("default-round", new Color(0.25f, 0.20f, 0.55f, 1f));
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = new Color(1.00f, 0.85f, 0.20f, 1f);  // gold text on hover

        //Our new hover button mechanic
        skin.add("hover-btn", btnStyle);

        stage.addActor(buildLayout());

        // ── Audio ──────────────────────────────────────────────────────────
        clickSound = game.manager.get("Kwality_Sounds/Game_Click.mp3", Sound.class);

        if (draw) {
            // No strong win or lose — use whichever track fits a neutral end
            // Wire a draw track here when you have one, defaulting to lose for now
            currentMusic = game.manager.get("Kwality_Sounds/Game_Lose.wav", Music.class);
        } else if (playerWon) {
            currentMusic = game.manager.get("Kwality_Sounds/Game_Win.wav", Music.class);
        } else {
            currentMusic = game.manager.get("Kwality_Sounds/Game_Lose.wav", Music.class);
        }

        currentMusic.setLooping(false);
        currentMusic.setVolume(0.4f);
        currentMusic.play();

    }

    // -----------------------------------------------------------------------
    // Layout
    // -----------------------------------------------------------------------
    private Table buildLayout() {
        // Resolve outcome text and style
        String outcomeText;
        String outcomeStyle;

        if (draw) {
            outcomeText  = "DRAW";
            outcomeStyle = "outcome-draw";
        } else if (playerWon) {
            outcomeText  = "VICTORY!";
            outcomeStyle = "outcome-win";
        } else {
            outcomeText  = "DEFEATED";
            outcomeStyle = "outcome-lose";
        }

        Label outcomeLabel = new Label(outcomeText, skin, outcomeStyle);
        outcomeLabel.setFontScale(3f);  // uiskin default font is small — scale it up

        TextButton retryBtn = new TextButton("PLAY AGAIN", skin, "hover-btn");
        TextButton menuBtn  = new TextButton("MAIN MENU",  skin, "hover-btn");

        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
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

        Table t = new Table();
        t.setFillParent(true);
        t.add(outcomeLabel).padBottom(24f).row();
        t.add(retryBtn).size(260f, 50f).padBottom(16f).row();
        t.add(menuBtn).size(260f, 50f);

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