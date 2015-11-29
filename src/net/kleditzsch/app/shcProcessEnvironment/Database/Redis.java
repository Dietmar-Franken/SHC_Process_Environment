package net.kleditzsch.app.shcProcessEnvironment.Database;

import com.google.gson.*;
import net.kleditzsch.app.shcProcessEnvironment.Settings.Settings;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Verwaltet die Redis Datenbank Anbindung
 */
public class Redis {

    private static Redis redis = new Redis();

    private Jedis jedis;

    public Redis() {

        //Redis Verbindung aufbauen
        String host;
        int port;
        int timeout;
        String pass;
        int db;

        //DB Config Lesen
        Path dbConfigFile = Paths.get(Settings.getInstance().getProperty(Settings.SHEDULER_REDIS_CONFIG_PATH));
        try(BufferedReader in = Files.newBufferedReader(dbConfigFile)) {

            JsonObject jsonObject = new JsonParser().parse(in).getAsJsonObject();
            host = jsonObject.get("host").getAsString();
            port = jsonObject.get("port").getAsInt();
            timeout = jsonObject.get("timeout").getAsInt();
            pass = jsonObject.get("pass").getAsString();
            db = jsonObject.get("db").getAsInt();

            //DB Verbinden
            try {

                jedis = new Jedis(host, port, timeout * 1000);
                if(pass.length() > 0) {

                    jedis.auth(pass);
                }
                jedis.select(db);
            } catch (JedisConnectionException ex) {

                ex.printStackTrace();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public Jedis getConnection() {

        return jedis;
    }

    public static Redis getInstance() {

        return redis;
    }
}
