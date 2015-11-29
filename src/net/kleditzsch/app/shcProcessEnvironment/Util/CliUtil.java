package net.kleditzsch.app.shcProcessEnvironment.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.InputMismatchException;

/**
 * Hilfsfunktionen für die Kommandozeilenverarbeitung
 */
public class CliUtil {

    private static PrintStream out = System.out;
    private static PrintStream err = System.err;
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static String input(String message) throws IOException {

        out.print(message);
        return in.readLine();
    }

    public static Boolean inputOnOffOption(String message, String onText, String offText, boolean value, int maxDuartions) throws IOException {

        int i = 0;
        while(i < maxDuartions) {

            String input = CliUtil.input(message + " (" + (value == true ? onText : offText) + ") : ");

            input.trim();
            if(input.length() > 0) {

                if(input.equals(onText) || input.equals(onText.substring(0, 1))) {

                    //an
                    return true;
                } else if(input.equals(offText) || input.equals(offText.substring(0, 1))) {

                    //aus
                    return false;
                }
            } else {

                return null;
            }

            i++;
            err.println("Fehlerhafte Eingabe");
        }
        throw new InputMismatchException();
    }

    public static String inputStringOption(String message, String value) throws IOException {

        String input = CliUtil.input(message + " (" + value + ") : ");

        input.trim();
        if(input.length() > 0) {

            return input;
        } else {

            return null;
        }
    }

    public static Integer inputIntegerOption(String message, int value, int min, int max, int maxDuartions) throws IOException {

        int i = 0;
        while(i < maxDuartions) {

            String input = CliUtil.input(message + " (" + value + ") : ");

            input.trim();
            if(input.length() > 0) {

                try {

                    int newValue = Integer.parseInt(input);
                    if(newValue >= min && newValue <= max) {

                        return newValue;
                    }
                } catch (NumberFormatException ex) {}
            } else {

                return null;
            }

            i++;
            err.println("Fehlerhafte Eingabe");
        }
        throw new InputMismatchException();
    }
}
