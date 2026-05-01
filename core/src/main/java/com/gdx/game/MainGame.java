package com.gdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.audio.Sound;

public class MainGame extends Game {
    AssetManager manager;

    @Override
    public void create() {
        manager = new AssetManager();

        // -------------- Queue all the assets in `manager` --------------

        manager.load("atlas\\practiceAtlas.atlas", TextureAtlas.class);

        //Load sounds
        manager.load("Kwality_Sounds/Game_Click.mp3", Sound.class);

        // Load the main menu music
        manager.load("Kwality_Sounds/Menu_Music.mp3", Music.class);

        // Load the ending music
        manager.load("Kwality_Sounds/Game_Lose.wav", Music.class);
        manager.load("Kwality_Sounds/Game_Win.wav", Music.class);

        //Loads HeroPreset which is made in Loader.java
        for (HeroPreset preset : HeroPreset.values()) {
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        //Loads GoblinPreset which is made in Loader.java
        for (GoblinPreset preset: GoblinPreset.values()){
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        //Loads TowerPreset which is made in Loader.java
        for (TowerPreset preset: TowerPreset.values()){
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        //Loads WeaponPreset which is made in Loader.java
        for (WeaponPreset preset: WeaponPreset.values()){
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        //Loads WeaponPreset which is made in Loader.java
        for (ProjectilePreset preset: ProjectilePreset.values()){
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load("practiceMap/projectmap.tmx", TiledMap.class);

        setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        manager.dispose();
    }
}
