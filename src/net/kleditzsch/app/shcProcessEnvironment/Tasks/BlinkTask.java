package net.kleditzsch.app.shcProcessEnvironment.Tasks;

import net.kleditzsch.app.shcProcessEnvironment.ProcessEnvironment.ProcessEnvironment;
import net.kleditzsch.app.shcProcessEnvironment.Util.GPIO.GPIO;
import net.kleditzsch.app.shcProcessEnvironment.Util.GPIO.Pin;

/**
 * Steuert LED zum Blinken an
 */
public class BlinkTask implements Runnable {

    private Pin pin;

    private boolean state = false;

    public BlinkTask(int pinNumber) {

        pin = GPIO.getInstance().getPin(pinNumber);
        pin.mode(GPIO.MODE_OUTPUT);

        //Debug Ausgabe
        if(ProcessEnvironment.isDebugEnabled()) {

            System.out.println("BlinkTask-" + Thread.currentThread().getName() + ": mode " + pin.getPinNumber() + " out");
        }
    }

    @Override
    public void run() {

        if(state == false) {

            //Pin auf High
            pin.write(GPIO.STATE_HIGH);
            state = true;

            //Debug Ausgabe
            if(ProcessEnvironment.isDebugEnabled()) {

                System.out.println("BlinkTask-" + Thread.currentThread().getName() + ": wirte " + pin.getPinNumber() + " 1");
            }
        } else {

            //Pin auf High
            pin.write(GPIO.STATE_LOW);
            state = false;

            //Debug Ausgabe
            if(ProcessEnvironment.isDebugEnabled()) {

                System.out.println("BlinkTask-" + Thread.currentThread().getName() + ": wirte " + pin.getPinNumber() + " 0");
            }
        }
    }
}
