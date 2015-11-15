package net.kleditzsch.app.shcProcessEnvironment.Util.GPIO;

import net.kleditzsch.app.shcProcessEnvironment.Settings.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wiring Pi GPIO Pin
 */
public class Pin {

    private static String gpioCommand = Settings.getInstance().getProperty(Settings.COMMAND_GPIO);

    private int pinNumber;

    private int mode;

    private int state;

    Pin(int pinNumber) {

        this.pinNumber = pinNumber;
    }

    public int getPinNumber() {

        return pinNumber;
    }

    /**
     * setzt den GPIO Pin als Ein- oder Ausgang
     *
     * @param mode
     */
    public void mode(int mode) {

        try {

            this.mode = mode;
            if(mode == GPIO.MODE_INPUT) {

                new ProcessBuilder(gpioCommand + " mode " + pinNumber + " in").start();
            } else if(mode == GPIO.MODE_INPUT) {

                new ProcessBuilder(gpioCommand + " mode " + pinNumber + " out").start();
            }
        } catch (IOException e) {

            System.err.println(e.getLocalizedMessage());
        }
    }

    /**
     * setzt den GPIO Status
     *
     * @param state
     */
    public void write(int state) {

        if(mode == GPIO.MODE_OUTPUT) {

            try {

                if(state == GPIO.STATE_HIGH) {

                    new ProcessBuilder(gpioCommand + " write " + pinNumber + " 1").start();
                    state = GPIO.STATE_HIGH;
                } else {

                    new ProcessBuilder(gpioCommand + " write " + pinNumber + " 0").start();
                    state = GPIO.STATE_LOW;
                }
            } catch (IOException e) {

                System.err.println(e.getLocalizedMessage());
            }
        } else {

            throw new IllegalStateException("Der Pin muss als Ausgang definiert sein");
        }
    }

    public int read() {

        if(mode == GPIO.MODE_OUTPUT || mode == GPIO.MODE_INPUT) {

            try {

                ProcessBuilder gpioRead = new ProcessBuilder(gpioCommand + " read " + pinNumber);
                Process read = gpioRead.start();

                try(BufferedReader input = new BufferedReader(new InputStreamReader(read.getInputStream()))) {

                    String inputState = input.readLine();

                    inputState.trim();
                    if(inputState.equals("1")) {

                        state = GPIO.STATE_HIGH;
                    } else {

                        state = GPIO.STATE_LOW;
                    }
                }
            } catch (IOException e) {

                System.err.println(e.getLocalizedMessage());
            }
        } else {

            throw new IllegalStateException("Der Pin muss als Eingang oder Ausgang definiert sein");
        }

        return state;
    }

    /**
     * gibt den zuletzt gespeicherten GPIO Status zurück
     *
     * @return
     */
    public int getState() {

        return state;
    }
}
