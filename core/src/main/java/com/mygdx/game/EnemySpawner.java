package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class EnemySpawner {
    private Array<Enemy> enemies = new Array<>();
    private Array<Vector2> spawnPoints = new Array<>();
    private World world;
    private Player player;
    private Array<Vector2> enemiesLevel1 = new Array<>();

    public EnemySpawner(World world, Player player) {
        this.world = world;
        this.player = player;

        enemiesLevel1.add(new Vector2(9, 6.85f));
        enemiesLevel1.add(new Vector2(10, 6.85f));
        enemiesLevel1.add(new Vector2(15.7f, 6.4f));
        enemiesLevel1.add(new Vector2(21.8f, 5.85f));
        enemiesLevel1.add(new Vector2(33.5f, 8.85f));
        enemiesLevel1.add(new Vector2(37.25f, 9.85f));

        spawnPoints.addAll(enemiesLevel1);
        spawnEnemy();
    }

    public void spawnEnemy() {
        for (Vector2 pos : spawnPoints) {
            enemies.add(new Enemy(world, pos.x, pos.y, player));
        }
    }

    public void update(float delta, Vector2 playerPosition) {
        for (Enemy enemy : enemies) {
            enemy.update(delta, playerPosition);
        }
        Iterator<Enemy> iterator = enemies.iterator();
        while(iterator.hasNext()){
            Enemy enemy = iterator.next();
            enemy.update(delta, playerPosition);
            // Check if the enemy is dead and its death animation is finished (or if you no longer need to render it)
            if(enemy.isShot && enemy.alreadyRendered) {
                iterator.remove();
            }
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
