package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;
import java.util.Set;

public class EnemySpawner {

    private Array<Enemy> enemies = new Array<>();
    public Array<Vector2> spawnPoints = new Array<>();
    private World world;
    private Player player;
    private Set<Body> bulletsToRemove;


    public EnemySpawner(World world, Player player, Set<Body> bulletsToRemove) {
        this.world = world;
        this.player = player;
        this.bulletsToRemove = bulletsToRemove;
    }

    public void setSpawnPoints(Array<Vector2> points) {
        this.spawnPoints = points;
        spawnEnemy();
    }

    public void spawnEnemy() {
        for (Vector2 pos : spawnPoints) {
            enemies.add(new Enemy(world, pos.x, pos.y, player, bulletsToRemove));
        }
    }

    public void update(float delta, Vector2 playerPosition) {
        Iterator<Enemy> iterator = enemies.iterator();
        while(iterator.hasNext()){
            Enemy enemy = iterator.next();
            enemy.update(delta, playerPosition);
            // Check if the enemy is dead and its death animation is finished (or if you no longer need to render it)
            if(enemy.isShot && enemy.alreadyRendered) {
                iterator.remove();
            }
        }
        for (Enemy enemy : enemies) {
            enemy.update(delta, playerPosition);
        }

    }

    public void render(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
    }

    public void reset() {
        // First clean up existing enemies
        for(Enemy enemy : enemies) {
            enemy.cleanup(); // New method we'll add to Enemy
        }
        enemies.clear();

        // Re-spawn fresh enemies
        spawnEnemy();
    }
    private void cleanupWorldBodies() {
        // Optional: Clean up any remaining bullets
        for(Enemy enemy : enemies) {
            for(EnemyBullet bullet : enemy.bullets) {
                bullet.markForRemoval();
            }
        }
    }
}
