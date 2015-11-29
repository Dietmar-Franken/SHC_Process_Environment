package net.kleditzsch.app.shcProcessEnvironment.Tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.kleditzsch.app.shcProcessEnvironment.Data.UserAtHome;
import net.kleditzsch.app.shcProcessEnvironment.Database.Redis;
import net.kleditzsch.app.shcProcessEnvironment.ProcessEnvironment.ProcessEnvironment;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * erkennt welche Benutzer zu Hause sind
 */
public class UserAtHomeUpdateTask implements Runnable {

    private final String REDIS_KEY = "rwf:shc:usersrathome";

    @Override
    public void run() {

        Gson gson = new Gson();
        final Object o = new Object();

        //Redis Verbindung holen und Daten abrufen
        Jedis redis = Redis.getInstance().getConnection();
        Map<String, String> usersAtHome = redis.hgetAll(REDIS_KEY);
        for(String key : usersAtHome.keySet()) {

            String json = usersAtHome.get(key);

            //JSON Daten laden
            UserAtHome userAtHome = gson.fromJson(json, UserAtHome.class);

            //Erreichbarkeit pruefen
            if(userAtHome.isEnabled()) {

                ProcessEnvironment.getThreadPool().submit(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            //Erreichbarkeit prüfen
                            InetAddress ip = InetAddress.getByName(userAtHome.getIpAddress());
                            if(ip.isReachable(1000)) {

                                userAtHome.setState(1);
                            } else {

                                userAtHome.setState(0);
                            }

                            //Daten in die Datenbank schreiben
                            synchronized (o) {

                                Jedis redis = Redis.getInstance().getConnection();
                                if(redis.hexists(REDIS_KEY, Integer.toString(userAtHome.getId()))) {

                                    String json = gson.toJson(userAtHome);
                                    redis.hset(REDIS_KEY, Integer.toString(userAtHome.getId()), json);
                                }
                            }

                            if(ProcessEnvironment.isDebugEnabled()) {

                                System.out.println("UserAtHomeUpdateTask-" + Thread.currentThread().getName() + ": der Benutzer " + userAtHome.getName() + " ist " + (userAtHome.getState() == 1 ? "zu Hause" : "nicht zu Hause"));
                            }

                        } catch (UnknownHostException e) {

                            if(ProcessEnvironment.isDebugEnabled()) {

                                System.out.println("UserAtHomeUpdateTask-" + Thread.currentThread().getName() + ": IP " + userAtHome.getIpAddress() + " ungültig");
                            }
                        } catch (IOException e) {

                            if(ProcessEnvironment.isDebugEnabled()) {

                                System.out.println("UserAtHomeUpdateTask-" + Thread.currentThread().getName() + ": " + e.getLocalizedMessage());
                            }
                        }
                    }
                });
            }
        }
    }
}
