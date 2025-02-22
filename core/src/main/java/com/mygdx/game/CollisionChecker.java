package com.mygdx.game;

public interface CollisionChecker {
    boolean isCollidable(float x, float y);
    boolean isCollidingBelow(float x, float y, float width, float height);
    boolean isCollidingAbove(float x, float y, float width, float height);
}
