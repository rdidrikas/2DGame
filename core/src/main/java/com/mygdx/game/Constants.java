package com.mygdx.game;

public class Constants {

    public static final float PPM = 32; // 32 pixels = 1 meter
    public static final float GRAVITY = -9.8f;

    public static final float PLAYER_SPEED = 4;
    public static final float SPRITE_YOFFSET = 0.10f;
    public static final float PLAYER_JUMP = 1f;
    public static final float PLAYER_SPEED_MID_AIR = 0.4f;

    public static final short PLAYER_CATEGORY = 0x0001;
    public static final short BULLET_CATEGORY = 0x0002;
    public static final short TILE_CATEGORY = 0x0008;

    public static final float RAMBO_BULLET_SPEED = 5f;
    public static final int RAMBO_BULLET_DAMAGE = 10;
    public static final float RAMBO_SHOT_COOLDOWN = 0.5f; // Seconds
    public static final float RAMBO_BULLET_DISTANCE = 200f;
    public static final float RAMBO_BULLET_XOFFSET = 10f / PPM;
    public static final float RAMBO_BULLET_YOFFSET = 3f / PPM;

}
