package com.mygdx.game;

public class Constants {

    // World constants
    public static final float PPM = 32; // 32 pixels = 1 meter
    public static final float GRAVITY = -9.8f;

    // Player constants
    public static final float PLAYER_SPEED = 4;
    public static final float SPRITE_YOFFSET = 0.15f;
    public static final float PLAYER_JUMP = 5f;
    public static final float PLAYER_SPEED_MID_AIR = 0.4f;
    public static final float PLAYER_DEATH_DURATION = 3f;

    // Box2D collision categories
    public static final short PLAYER_CATEGORY = 0x0001;
    public static final short BULLET_CATEGORY = 0x0002;
    public static final short TILE_CATEGORY = 0x0004;
    public static final short ENEMY_CATEGORY = 0x0008;
    public static final short ENEMY_BULLET_CATEGORY = 0x0010;


    // Rambo constants
    public static final float RAMBO_BULLET_SPEED = 7f;
    public static final int RAMBO_BULLET_DAMAGE = 10;
    public static final float RAMBO_SHOT_COOLDOWN = 0.3f; // Seconds
    public static final float RAMBO_BULLET_DISTANCE = 8f;
    public static final float RAMBO_BULLET_XOFFSET = 0.3f;
    public static final float RAMBO_BULLET_YOFFSET = 0.1f;

    // Enemy constants
    public static final float ENEMY_SPEED = 2f;
    public static final int ENEMY_DAMAGE = 10;
    public static final float ENEMY_MAX_SHOT_COOLDOWN = 4f;// Seconds
    public static final float ENEMY_MIN_SHOT_COOLDOWN = 0.1f;
    public static final float ENEMY_BULLET_SPEED = 7f;
    public static final float ENEMY_BULLET_ALIVE_TIME = 2f;
    public static final float ENEMY_DEATH_TIMER = 3.5f;
    public static final float ENEMY_DETECTION_REACTION = 1f;


    // Misc

    public static final float SMOKE_DURATION = 0.5f;
    public static final float BUBBLE_DURATION = 1.5f;


}
