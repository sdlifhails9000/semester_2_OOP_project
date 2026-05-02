package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainScreen implements Screen {

    private final MainGame game;

    private Stage stage;
    private Skin skin;

    private Table mainTable;
    private Table pickTable;

    private Sound clickSound;
    private Music menuMusic;

    private static final float BG_R = 0.07f, BG_G = 0.07f, BG_B = 0.10f;

    public MainScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("UI/uiskin.json"));

        skin.getFont("default-font").getRegion().getTexture().setFilter(
        Texture.TextureFilter.Linear,
        Texture.TextureFilter.Linear
    );

        // --- Custom label styles built on top of uiskin's default font ---
        // Title — large purple-white
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = new Color(0.90f, 0.75f, 1.00f, 1f);
        skin.add("title", titleStyle);

        // Sub — muted purple
        Label.LabelStyle subStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        subStyle.fontColor = new Color(0.65f, 0.60f, 0.80f, 1f);
        skin.add("sub", subStyle);

        // Hint — same as sub but slightly transparent
        Label.LabelStyle hintStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        hintStyle.fontColor = new Color(0.65f, 0.60f, 0.80f, 0.75f);
        skin.add("hint", hintStyle);

        // --- Hover button style ---
        // Clones uiskin default overrides hover state only
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle(
            skin.get(TextButton.TextButtonStyle.class)
        );
        btnStyle.over          = skin.newDrawable("default-round", new Color(0.6f, 0.4f, 1f, 1f));
        btnStyle.fontColor     = Color.WHITE;
        btnStyle.overFontColor = new Color(1.00f, 0.85f, 0.20f, 1f);
        skin.add("hover-btn", btnStyle);

        // --- Build layout ---
        mainTable = createMainTable();
        pickTable = createPickTable();

        stage.addActor(mainTable);
        stage.addActor(pickTable);

        showMainState();

        // --- Audio ---
        clickSound = game.manager.get("Kwality_Sounds/Game_Click.mp3", Sound.class);
        menuMusic  = game.manager.get("Kwality_Sounds/Menu_Music.mp3", Music.class);
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();
    }

    
    // Main menu table — title + start button
    private Table createMainTable() {
        Table t = new Table();
        t.setFillParent(true);

        Label title = new Label("DITTO 2", skin, "title");
        title.setFontScale(4f);

        TextButton startBtn = new TextButton("START GAME", skin, "hover-btn");

        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                showPickState();
            }
        });

        t.add(title).padBottom(80f).row();
        t.add(startBtn).size(280f, 54f);
        return t;
    }

    // -----------------------------------------------------------------------
    // Hero pick table — two hero columns
    // -----------------------------------------------------------------------
    private Table createPickTable() {
        Table t = new Table();
        t.setFillParent(true);

        Label title = new Label("CHOOSE YOUR HERO", skin, "title");
        title.setFontScale(2.5f);

        Label sub = new Label("pick a class to begin", skin, "sub");

        // --- Light hero ---
        VerticalGroup lightCol = new VerticalGroup();
        lightCol.space(6f);

        TextButton lightBtn  = new TextButton("LIGHT HERO", skin, "hover-btn");
        lightBtn.getLabelCell().pad(5f);
        Label lightHint = new Label("Fast  |  Low HP  |  High DPS", skin, "hint");

        lightBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new GameScreen(game, HeroPreset.HERO_LIGHT));
            }
        });

        lightCol.addActor(lightBtn);
        lightCol.addActor(lightHint);

        // --- Heavy hero ---
        VerticalGroup heavyCol = new VerticalGroup();
        heavyCol.space(6f);

        TextButton heavyBtn  = new TextButton("HEAVY HERO", skin, "hover-btn");
        heavyBtn.getLabelCell().pad(5f);
        Label heavyHint = new Label("Slow  |  High HP  |  Tank", skin, "hint");

        heavyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new GameScreen(game, HeroPreset.HERO_HEAVY));
            }
        });

        heavyCol.addActor(heavyBtn);
        heavyCol.addActor(heavyHint);

        // --- Assemble ---
        Table btnRow = new Table();
        btnRow.add(lightCol).width(280f).height(100f).padRight(30f);
        btnRow.add(heavyCol).width(280f).height(100f);

        t.add(title).padBottom(10f).row();
        t.add(sub).padBottom(60f).row();
        t.add(btnRow);
        return t;
    }

    // -----------------------------------------------------------------------
    // State switching
    // -----------------------------------------------------------------------
    private void showMainState() {
        mainTable.setVisible(true);
        pickTable.setVisible(false);
    }

    private void showPickState() {
        mainTable.setVisible(false);
        pickTable.setVisible(true);
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
        menuMusic.stop();
    }
}