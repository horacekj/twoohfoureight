package com.example.tassadar.twoohfoureight;

import android.os.Bundle;

import java.util.HashSet;
import java.util.Random;

public class GameController {
    public static final int GRID = 4;
    public static final int MAX_TILES = GRID*GRID;

    public static final int RIGHT = 1;
    public static final int LEFT = -1;
    public static final int UP = -GRID;
    public static final int DOWN = GRID;

    private static final int START_TILES = 2;

    private static class Tile {
        int id;
        int value;
    }

    private Tile[] m_tiles;
    private int m_tileIdCounter;
    private int m_usedTiles;
    private Random m_random;

    private static final int STATE_UNINITIALIZED = 0;
    private static final int STATE_IN_GAME = 1;
    private int m_state;

    GameController() {
        m_tiles = new Tile[MAX_TILES];
        for(int i = 0; i < m_tiles.length; ++i) {
            m_tiles[i] = new Tile();
        }

        m_random = new Random();
    }

    private void initialize(Renderer renderer) {
        m_usedTiles = 0;

        renderer.removeAllTiles();
        for(int i = 0; i < START_TILES; ++i) {
            addRandomTile(renderer);
        }

        m_state = STATE_IN_GAME;

        renderer.invalidate();
    }

    public void restoreState(Renderer renderer) {
        switch(m_state) {
            case STATE_UNINITIALIZED:
                initialize(renderer);
                break;
            case STATE_IN_GAME:
                renderer.removeAllTiles();
                for(int i = 0; i < MAX_TILES; ++i) {
                    if(m_tiles[i].value != 0) {
                        renderer.addTile(m_tiles[i].id, m_tiles[i].value, i);
                    }
                }
                renderer.invalidate();
                break;
        }
    }

    public void saveInstanceState(Bundle state) {
        state.putInt("game_state", m_state);

        int[] tileValues = new int[MAX_TILES];
        for(int i = 0; i < MAX_TILES; ++i) {
            tileValues[i] = m_tiles[i].value;
        }
        state.putIntArray("game_tiles", tileValues);
    }

    public void restoreInstanceState(Bundle state) {
        m_state = state.getInt("game_state");
        int[] tileValues = state.getIntArray("game_tiles");
        if(m_state >= STATE_IN_GAME && tileValues != null && tileValues.length == MAX_TILES) {
            for(int i = 0; i < MAX_TILES; ++i) {
                if(tileValues[i] != 0) {
                    m_tiles[i].id = ++m_tileIdCounter;
                    m_tiles[i].value = tileValues[i];
                }
            }
        }
    }

    public void onSwipe(int direction, Renderer renderer) {
        if(m_state != STATE_IN_GAME) {
            return;
        }

        boolean moved = false;
        int start, delta;
        if(direction > 0) {
            start = MAX_TILES - 1;
            delta = -1;
        } else {
            start = 0;
            delta = 1;
        }

        renderer.finishAllAnimations();

        HashSet<Integer> merged = new HashSet<>();

        for(int i = start; i >= 0 && i < MAX_TILES; i += delta) {
            Tile t = m_tiles[i];
            if(t.value == 0) {
                continue;
            }

            if(!isEdgePosition(direction, i)) {
                int next = findNextPosition(direction, i, t.value, merged);

                if(next != i && t.value == m_tiles[next].value) { // Merge
                    renderer.mergeTiles(m_tiles[next].id, t.id, next);

                    m_tiles[next].value *= 2;
                    t.id = 0;
                    t.value = 0;
                    --m_usedTiles;

                    merged.add(next);
                    moved = true;
                } else if(m_tiles[next].value == 0) { // just move
                    renderer.setTilePosition(t.id, next, true);
                    m_tiles[i] = m_tiles[next];
                    m_tiles[next] = t;
                    moved = true;
                }
            }
        }

        if(moved) {
            addRandomTile(renderer);
        } else {
            renderer.shake();
        }
        renderer.invalidate();
    }

    private int findNextPosition(int direction, int pos, int value, HashSet<Integer> merged) {
        while(true) {
            pos += direction;
            if(m_tiles[pos].value != 0 && (m_tiles[pos].value != value || merged.contains(pos))) {
                return pos - direction;
            } else if(isEdgePosition(direction, pos) || m_tiles[pos].value == value) {
                return pos;
            }
        }
    }

    private boolean isEdgePosition(int direction, int pos) {
        switch(direction) {
            case LEFT: return pos%GRID == 0;
            case RIGHT: return pos%GRID == GRID-1;
            case UP: return (pos + direction) < 0;
            case DOWN: return (pos + direction) >= MAX_TILES;
        }
        return false;
    }

    private boolean addRandomTile(Renderer renderer) {
        if(m_usedTiles >= MAX_TILES) {
            return false;
        }

        int pos = getRandomFreePosition();

        ++m_usedTiles;
        m_tiles[pos].value = m_random.nextFloat() < 0.9 ? 2 : 4;
        m_tiles[pos].id = ++m_tileIdCounter;

        renderer.addTile(m_tiles[pos].id, m_tiles[pos].value, pos);
        return true;
    }

    private int getRandomFreePosition() {
        while(m_usedTiles < MAX_TILES) {
            int idx = m_random.nextInt(MAX_TILES);
            if(m_tiles[idx].value == 0) {
                return idx;
            }
        }
        return 0;
    }
}