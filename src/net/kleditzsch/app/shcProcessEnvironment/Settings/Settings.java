package net.kleditzsch.app.shcProcessEnvironment.Settings;

import net.kleditzsch.app.shcProcessEnvironment.Util.CliUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Verwaltet die Einstellungen
 */
public class Settings {

    private static Settings settings = new Settings();

    private Properties properties = new Properties();

    //Allgemein
    private static final String SETTINGS_FILE = "settings.xml";
    Path settingsFile = Paths.get(SETTINGS_FILE);

    //Kommandozeilen Befehle
    public static final String COMMAND_RCSWITCH = "switchServer.commandRcswitch";
    public static final String COMMAND_PILIGHT = "switchServer.commandPilight";
    public static final String COMMAND_GPIO = "switchServer.commandGpio";

    //Sheduler
    public static final String SHEDULER_ACTIVE = "sheduler.active";
    public static final String SHEDULER_REDIS_CONFIG_PATH = "sheduler.redisConfigPath";
    public static final String SHEDULER_STATE_LED_PIN = "sheduler.stateLedPin";
    public static final String SHEDULER_PERFORMANCE_PROFILE = "sheduler.performanceProfile";

    //Schaltserver
    public static final String SWITCH_SERVER_ACTIVE = "switchServer.active";
    public static final String SWITCH_SERVER_IP = "switchServer.ip";
    public static final String SWITCH_SERVER_PORT = "switchServer.port";
    public static final String SWITCH_SERVER_TRANSMITTER_ACTIVE = "switchServer.transmitterActive";
    public static final String SWITCH_SERVER_READ_GPIO_ACTIVE = "switchServer.readGpio";
    public static final String SWITCH_SERVER_WRITE_GPIO_ACTIVE = "switchServer.writeGpio";
    public static final String SWITCH_SERVER_SEND_LED_PIN = "switchServer.sendLedPin";

    //Sensortransmitter
    public static final String SENSORTRANSMITTER_ACTIVE = "sensortransmitter.active";
    public static final String SENSORTRANSMITTER_RECEIVER_IP = "sensortransmitter.receiverIp";
    public static final String SENSORTRANSMITTER_RECEIVER_PORT = "sensortransmitter.receiverPort";
    public static final String SENSORTRANSMITTER_SENSORPOINT_ID = "sensortransmitter.sensorPointId";
    public static final String SENSORTRANSMITTER_STATE_LED_PIN = "sensortransmitter.stateLedPin";

    private Settings() {

        //prüfen ob die Datei existiert, wenn nicht erstellen und initalisieren

        if(Files.exists(settingsFile)) {

            //laden
            try(InputStream in = Files.newInputStream(settingsFile)) {

                properties.loadFromXML(in);
            } catch(IOException ex) {

                //Fehler
                System.err.println("Die Konfigurationsdatei konnte nicht gelesen werden!");
                System.exit(1);
            }
        } else {

            //erstellen und Initalisieren
            properties.setProperty(COMMAND_RCSWITCH, "/opt/rcswitch-pi/send");
            properties.setProperty(COMMAND_PILIGHT, "/usr/local/bin/pilight-send");
            properties.setProperty(COMMAND_GPIO, "/usr/local/bin/gpio");

            properties.setProperty(SHEDULER_ACTIVE, "0");
            properties.setProperty(SHEDULER_REDIS_CONFIG_PATH, "/var/www/shc/rwf/db.config.json");
            properties.setProperty(SHEDULER_STATE_LED_PIN, "-1");
            properties.setProperty(SHEDULER_PERFORMANCE_PROFILE, "2");

            properties.setProperty(SWITCH_SERVER_ACTIVE, "0");
            properties.setProperty(SWITCH_SERVER_IP, "127.0.0.1");
            properties.setProperty(SWITCH_SERVER_PORT, "9274");
            properties.setProperty(SWITCH_SERVER_TRANSMITTER_ACTIVE, "0");
            properties.setProperty(SWITCH_SERVER_READ_GPIO_ACTIVE, "0");
            properties.setProperty(SWITCH_SERVER_WRITE_GPIO_ACTIVE, "0");
            properties.setProperty(SWITCH_SERVER_SEND_LED_PIN, "-1");

            properties.setProperty(SENSORTRANSMITTER_ACTIVE, "0");
            properties.setProperty(SENSORTRANSMITTER_RECEIVER_IP, "127.0.0.1");
            properties.setProperty(SENSORTRANSMITTER_RECEIVER_PORT, "80");
            properties.setProperty(SENSORTRANSMITTER_SENSORPOINT_ID, "1");
            properties.setProperty(SENSORTRANSMITTER_STATE_LED_PIN, "-1");

            storePropertys();
        }
    }

    public static Settings getInstance() {

        return settings;
    }

    public void cliConfig() throws IOException {

        //Standard Einstellungen
        String rcswitchCommand = CliUtil.inputStringOption("Pfad zum RcSwitch send Befehl", properties.getProperty(COMMAND_RCSWITCH));
        if(rcswitchCommand != null) {

            properties.setProperty(COMMAND_RCSWITCH, rcswitchCommand.toString());
        }

        String pilightCommand = CliUtil.inputStringOption("Pfad zum Pilight send Befehl", properties.getProperty(COMMAND_PILIGHT));
        if(pilightCommand != null) {

            properties.setProperty(COMMAND_PILIGHT, pilightCommand.toString());
        }

        String gpioCommand = CliUtil.inputStringOption("Pfad zum wiringpi GPIO Befehl", properties.getProperty(COMMAND_GPIO));
        if(gpioCommand != null) {

            properties.setProperty(COMMAND_GPIO, gpioCommand.toString());
        }

        //Sheduler aktivieren
        Boolean shedulerActive = CliUtil.inputOnOffOption("Sheduler Dienst aktivieren", "ja", "nein", (properties.getProperty(SHEDULER_ACTIVE).equals("1") ? true : false), 5);
        if(shedulerActive != null) {

            properties.setProperty(SHEDULER_ACTIVE, (shedulerActive ? "1" : "0"));
        }

        shedulerActive = (properties.getProperty(SHEDULER_ACTIVE).equals("1") ? true : false);
        if(shedulerActive) {

            //Sheduler Einstellungen

            //Redis Konfiguration
            int i = 0;
            boolean success = false;
            while(i < 5) {

                String redisConfig = CliUtil.inputStringOption("Pfad zur Redis Konfiguration", properties.getProperty(SHEDULER_REDIS_CONFIG_PATH));
                if(redisConfig != null) {

                    Path redisConfigPath = Paths.get(redisConfig);
                    if(!Files.exists(redisConfigPath) || !Files.isRegularFile(redisConfigPath)) {

                        System.err.println("Der Pfad existiert nicht oder ist keine Datei");
                        continue;
                    }
                    properties.setProperty(SHEDULER_REDIS_CONFIG_PATH, redisConfig);
                }
                success = true;
                break;
            }

            if(!success) {

                System.err.println("Du hast zu oft Fehlerhafte Werte eingegeben, versuche es später noch einmal");
                System.exit(1);
            }

            //Performance Profil
            System.out.println("folgende Performance Profile stehen zur verfügung");
            System.out.println("+- ID -+- Profil -+ Beschreibung -+------------------------------------------------------------------------------------------------------------------------------------------+");
            System.out.println("|   1  |    fast  | Reaktionszeiten von 1 - 3 Sekunden (dafür höhere System- und Netzwerkauslasutung)                                                                        |");
            System.out.println("|   2  | default  | Reaktionszeiten von 1 - 10 Sekunden (empfohlen)                                                                                                          |");
            System.out.println("|   3  |    slow  | Reaktionszeiten von 1 - 30 Sekunden                                                                                                                      |");
            System.out.println("+------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------+");
            Integer performanceProfile = CliUtil.inputIntegerOption("Performance Profil", Integer.parseInt(properties.getProperty(SHEDULER_PERFORMANCE_PROFILE)), 1, 3, 5);
            if(performanceProfile != null) {

                properties.setProperty(SHEDULER_PERFORMANCE_PROFILE, performanceProfile.toString());
            }

            //Status LED
            Integer statusLed = CliUtil.inputIntegerOption("Status LED Pin [-1 um die LED zu deaktivieren]", Integer.parseInt(properties.getProperty(SHEDULER_STATE_LED_PIN)), -1, 30, 5);
            if(statusLed != null) {

                properties.setProperty(SHEDULER_STATE_LED_PIN, statusLed.toString());
            }
        }

        //Schaltserver aktivieren
        Boolean switchserverActive = CliUtil.inputOnOffOption("Schaltserver Dienst aktivieren", "ja", "nein", (properties.getProperty(SWITCH_SERVER_ACTIVE).equals("1") ? true : false), 5);
        if(switchserverActive != null) {

            properties.setProperty(SWITCH_SERVER_ACTIVE, (switchserverActive ? "1" : "0"));
        }

        switchserverActive = (properties.getProperty(SWITCH_SERVER_ACTIVE).equals("1") ? true : false);
        if(switchserverActive) {

            //Schaltserver Einstellungen

            //IP
            int i = 0;
            boolean success = false;
            while(i < 5) {

                String switchServerIp = CliUtil.inputStringOption("IP Adresse", properties.getProperty(SWITCH_SERVER_IP));
                if(switchServerIp != null) {

                    try {

                        InetAddress.getByName(switchServerIp);
                        properties.setProperty(SWITCH_SERVER_IP, switchServerIp);
                    } catch (UnknownHostException ex) {

                        System.err.println("Die IP Adresse ist ungültig");
                        continue;
                    }
                }
                success = true;
                break;
            }

            if(!success) {

                System.err.println("Du hast zu oft Fehlerhafte Werte eingegeben, versuche es später noch einmal");
                System.exit(1);
            }

            //Port
            Integer port = CliUtil.inputIntegerOption("Port", Integer.parseInt(properties.getProperty(SWITCH_SERVER_PORT)), 1, 65535, 5);
            if(port != null) {

                properties.setProperty(SWITCH_SERVER_PORT, port.toString());
            }

            //Status LED
            Integer sendLed = CliUtil.inputIntegerOption("Sende LED Pin [-1 um die LED zu deaktivieren]", Integer.parseInt(properties.getProperty(SWITCH_SERVER_SEND_LED_PIN)), -1, 30, 5);
            if(sendLed != null) {

                properties.setProperty(SWITCH_SERVER_SEND_LED_PIN, sendLed.toString());
            }

            //433MHz Transmitter
            Boolean switchserverTransmitterActive = CliUtil.inputOnOffOption("Der Schaltserver kann 433MHz Befehle senden", "ja", "nein", (properties.getProperty(SWITCH_SERVER_TRANSMITTER_ACTIVE).equals("1") ? true : false), 5);
            if(switchserverTransmitterActive != null) {

                properties.setProperty(SWITCH_SERVER_TRANSMITTER_ACTIVE, (switchserverTransmitterActive ? "1" : "0"));
            }

            //GPIO Lesen
            Boolean switchserverReadGpioActive = CliUtil.inputOnOffOption("Der Schaltserver kan GPIO's lesen", "ja", "nein", (properties.getProperty(SWITCH_SERVER_READ_GPIO_ACTIVE).equals("1") ? true : false), 5);
            if(switchserverReadGpioActive != null) {

                properties.setProperty(SWITCH_SERVER_READ_GPIO_ACTIVE, (switchserverReadGpioActive ? "1" : "0"));
            }

            //GPIO schreiben
            Boolean switchserverWriteGpioActive = CliUtil.inputOnOffOption("Der Schaltserver kann GPIO's schreiben", "ja", "nein", (properties.getProperty(SWITCH_SERVER_WRITE_GPIO_ACTIVE).equals("1") ? true : false), 5);
            if(switchserverWriteGpioActive != null) {

                properties.setProperty(SWITCH_SERVER_WRITE_GPIO_ACTIVE, (switchserverWriteGpioActive ? "1" : "0"));
            }
        }

        //Sensortransmitter aktivieren
        Boolean sensortransmitterActive = CliUtil.inputOnOffOption("Sensortransmitter Dienst aktivieren", "ja", "nein", (properties.getProperty(SENSORTRANSMITTER_ACTIVE).equals("1") ? true : false), 5);
        if(sensortransmitterActive != null) {

            properties.setProperty(SENSORTRANSMITTER_ACTIVE, (sensortransmitterActive ? "1" : "0"));
        }

        sensortransmitterActive = (properties.getProperty(SENSORTRANSMITTER_ACTIVE).equals("1") ? true : false);
        if(sensortransmitterActive) {

            //Sensortransmitter Einstellungen

            //IP
            int i = 0;
            boolean success = false;
            while(i < 5) {

                String sensorTransmitterIp = CliUtil.inputStringOption("IP Adresse des Empfängers", properties.getProperty(SENSORTRANSMITTER_RECEIVER_IP));
                if(sensorTransmitterIp != null) {

                    try {

                        InetAddress.getByName(sensorTransmitterIp);
                        properties.setProperty(SENSORTRANSMITTER_RECEIVER_IP, sensorTransmitterIp);
                    } catch (UnknownHostException ex) {

                        System.err.println("Die IP Adresse ist ungültig");
                        continue;
                    }
                }
                success = true;
                break;
            }

            if(!success) {

                System.err.println("Du hast zu oft Fehlerhafte Werte eingegeben, versuche es später noch einmal");
                System.exit(1);
            }

            //Port
            Integer port = CliUtil.inputIntegerOption("Port des Empfängers", Integer.parseInt(properties.getProperty(SENSORTRANSMITTER_RECEIVER_PORT)), 1, 65535, 5);
            if(port != null) {

                properties.setProperty(SENSORTRANSMITTER_RECEIVER_PORT, port.toString());
            }

            //Sensorpunkt ID
            System.out.println("Die Sensor Punkt ID muss im gesamten Netzwerk eindeutig sein, über diese ID können die Sensoren einem Standort besser zugeordnet werden");
            Integer sensorPointId = CliUtil.inputIntegerOption("Sensor Punkt ID", Integer.parseInt(properties.getProperty(SENSORTRANSMITTER_SENSORPOINT_ID)), 1, 998, 5);
            if(sensorPointId != null) {

                properties.setProperty(SENSORTRANSMITTER_SENSORPOINT_ID, sensorPointId.toString());
            }

            //Status LED
            Integer stateLed = CliUtil.inputIntegerOption("Status LED Pin [-1 um die LED zu deaktivieren]", Integer.parseInt(properties.getProperty(SENSORTRANSMITTER_STATE_LED_PIN)), -1, 30, 5);
            if(stateLed != null) {

                properties.setProperty(SENSORTRANSMITTER_STATE_LED_PIN, stateLed.toString());
            }
        }

        //Einstellungen speichern
        storePropertys();
        System.out.println("Die Einstellungen wurden Erfolgreich gespeichert");
    }

    /**
     * gibt die Einstellung zu dem Schlüssel zurück
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {

        return properties.getProperty(key);
    }

    /**
     * speichert die Einstellungen in eine XML Datei
     */
    private void storePropertys() {

        try(OutputStream os = Files.newOutputStream(settingsFile)) {

            properties.storeToXML(os, null);
        } catch (IOException ex) {

            System.err.println("Die Konfigurationsdatei konnte nicht erstellt werden!");
            System.exit(1);
        }
    }
}
