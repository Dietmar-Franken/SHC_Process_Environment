package net.kleditzsch.app.shcProcessEnvironment.ProcessEnvironment;

import net.kleditzsch.app.shcProcessEnvironment.Database.Redis;
import net.kleditzsch.app.shcProcessEnvironment.Settings.Settings;
import net.kleditzsch.app.shcProcessEnvironment.SwitchServer.SwitchServerTask;
import net.kleditzsch.app.shcProcessEnvironment.Tasks.BlinkTask;
import net.kleditzsch.app.shcProcessEnvironment.Tasks.UserAtHomeUpdateTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Zentraler Einstigspunkt in die Anwendung
 */
public class ProcessEnvironment {

    private static boolean debugEnabled = false;

    private static ExecutorService threadPool;

    public static void main(String[] args) {

        //Kommandozeilen Parameter in ein SET übernehmen
        Set<String> arguments = new HashSet<>();
        Collections.addAll(arguments, args);

        //prüfen ob der Debug Modus aktiviert wurde
        if(arguments.contains("-d") || arguments.contains("--debug")) {

            debugEnabled = true;
        }

        //Einstellungen
        Path settingsFile = Paths.get("settings.xml");
        if(arguments.contains("-c") || arguments.contains("--config") || !Files.exists(settingsFile)) {

            //Konfiguration beim ersten Start durchführen
            try {

                Settings.getInstance().cliConfig();
            } catch(IOException ex) {

                System.err.println("Fehler bei der Konfiguration");
            } catch(InputMismatchException ex) {

                System.err.println("Du hast zu oft Fehlerhafte Werte eingegeben, versuche es später noch einmal");
                System.exit(1);
            }
        }

        //TODO Schalstserver starten
        if(Settings.getInstance().getProperty(Settings.SWITCH_SERVER_ACTIVE).equals("1")) {

            SwitchServerTask switchServer = new SwitchServerTask();
            Thread switchServerThread = new Thread(switchServer);
            switchServerThread.start();
            while(true);
        }

        //TODO Sensortransmitter starten

        //TODO Sheduler starten
        if(Settings.getInstance().getProperty(Settings.SHEDULER_ACTIVE).equals("1")) {

            //Datanbank initalisieren
            Redis.getInstance();

            //Executor Initalisieren
            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
            threadPool = Executors.newCachedThreadPool();

            /*
            //Blink LED
            int schedulerStateLed = Integer.parseInt(Settings.getInstance().getProperty(Settings.SHEDULER_STATE_LED_PIN));
            if(schedulerStateLed >= 0) {

                scheduler.scheduleAtFixedRate(new BlinkTask(schedulerStateLed), 0, 1, TimeUnit.SECONDS);
            }*/

            //Benutzer zu Hause
            scheduler.scheduleAtFixedRate(new UserAtHomeUpdateTask(), 5, 5, TimeUnit.SECONDS);
        }

        //Shutdown Methode
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                //wird vor dem Shutdown ausgeführt

                //Scheduler stoppen

                //Redis Verbindung beenden
                Redis.getInstance().getConnection().close();
            }
        });

    }

    public static boolean isDebugEnabled() {

        return debugEnabled;
    }

    public static ExecutorService getThreadPool() {

        return threadPool;
    }
}
