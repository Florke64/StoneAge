/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import win.flrque.g2p.stoneage.StoneAge;

public class DatabaseController {

    private final StoneAge plugin;

    private final HikariConfig hikariConfig;
    private final HikariDataSource hikari;

    public DatabaseController(HikariConfig config) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        hikariConfig = config;
        hikari = new HikariDataSource(hikariConfig);
    }

    public void runQuery() {
        //TODO: Do Query
    }

    public HikariDataSource getHikari() {
        return hikari;
    }
}
