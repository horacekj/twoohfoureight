package com.example.tassadar.foobar;

import android.util.Log;

import java.util.Random;

/**
 * Created by apophis on 24/01/17.
 */

public class GameController {
    private static final int TILES_CNT = RenderImpl.GRID*RenderImpl.GRID;
    private static final int START_TILES = 4;

    Renderer r;

    private class Tile {
        int id;
        int value; // value = 0 <-> empty tile
    }

    public static final int D_LEFT = -1;
    public static final int D_RIGHT = +1;
    public static final int D_UP = -RenderImpl.GRID;
    public static final int D_DOWN = +RenderImpl.GRID;

    Tile[] tiles;

    GameController(Renderer r) {
        this.r = r;

        tiles = new Tile[TILES_CNT];
        for(int i = 0; i < TILES_CNT; i++)
            tiles[i] = new Tile();
    }

    void restart() {
        // remove all tails
        for (int i = 0; i < TILES_CNT; i++) {
            tiles[i].value = 0;
            tiles[i].id = 0;
        }
        r.removeAllTails();

        // create new tails
        Random rnd = new Random();
        int pos;

        for(int i = 0; i < START_TILES; i++) {
            // generate unique pos
            do {
                pos = rnd.nextInt(TILES_CNT);
            } while (tiles[pos].value != 0);

            tiles[pos].id = i;
            if (rnd.nextBoolean())
                tiles[pos].value = 2;
            else
                tiles[pos].value = 4;

            r.addTile(tiles[pos].id, tiles[pos].value, pos);
        }
    }

    public void swipe(int direction) {
        for(int i = ((TILES_CNT-1) * (int)Math.signum(Math.signum(direction)+1)); (i >= 0) && (i < TILES_CNT); i += -Math.signum(direction)) {
            if (tiles[i].value == 0) continue;

            int new_pos = i;
            while ((new_pos+direction >= 0) && (new_pos+direction < TILES_CNT) && ((tiles[new_pos+direction].value == 0) || ((tiles[new_pos+direction].value == 0)))) {
                if ((direction == D_LEFT) && ((new_pos+direction)%4 == 3)) break;
                if ((direction == D_RIGHT) && ((new_pos+direction)%4 == 0)) break;
                // TODO
                new_pos += direction;
            }

            if (new_pos != i) {
                Tile tmp = tiles[new_pos];
                tiles[new_pos] = tiles[i];
                tiles[i] = tmp;
                r.setTilePosition(tiles[new_pos].id, new_pos, true);
            }
        }
    }
}
