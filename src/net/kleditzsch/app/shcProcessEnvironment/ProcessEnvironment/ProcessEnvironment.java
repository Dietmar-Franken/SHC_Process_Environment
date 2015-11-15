package net.kleditzsch.app.shcProcessEnvironment.ProcessEnvironment;

import net.kleditzsch.app.shcProcessEnvironment.Settings.Settings;
import net.kleditzsch.app.shcProcessEnvironment.Tasks.BlinkTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Zentraler Einstigspunkt in die Anwendung
 */
public class ProcessEnvironment {

    private static boolean debugEnabled = false;

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

            System.out.println(arguments.contains("-c") );
            System.out.println(arguments.contains("--config") );
            System.out.println(!Files.exists(settingsFile) );

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

        //Shutdown Methode

        //TODO Schalstserver starten

        //TODO Sensortransmitter starten

        //TODO Sheduler starten
        if(Settings.getInstance().getProperty(Settings.SHEDULER_ACTIVE).equals("1")) {

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

            //Blink LED
            int schedulerStateLed = Integer.parseInt(Settings.getInstance().getProperty(Settings.SHEDULER_STATE_LED_PIN));
            if(schedulerStateLed >= 0) {

                scheduler.scheduleAtFixedRate(new BlinkTask(schedulerStateLed), 0, 1, TimeUnit.SECONDS);
            }
        }

    }

    public static boolean isDebugEnabled() {

        return debugEnabled;
    }
}
