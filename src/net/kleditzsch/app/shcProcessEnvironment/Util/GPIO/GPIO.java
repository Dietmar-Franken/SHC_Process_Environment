package net.kleditzsch.app.shcProcessEnvironment.Util.GPIO;

import java.util.HashMap;
import java.util.Map;

/**
 * Wiring Pi GPIO
 */
public class GPIO {

    public static final int MODE_INPUT = 1;

    public static final int MODE_OUTPUT = 2;

    public static final int STATE_LOW = 0;

    public static final int STATE_HIGH = 1;

    private static GPIO instance = new GPIO();

    private Map<Integer, Pin> pins = new HashMap<>();

    private GPIO() {}

    public Pin getPin(int pinNumber) {

        if(pins.containsKey(pinNumber)) {

            return pins.get(pinNumber);
        }
        Pin gpioPin = new Pin(pinNumber);
        pins.put(pinNumber, gpioPin);
        return gpioPin;
    }

    public boolean isPinUsed(int pinNumber) {

        if(pins.containsKey(pinNumber)) {

            return true;
        }
        return false;
    }

    public static GPIO getInstance() {

        return instance;
    }
}
