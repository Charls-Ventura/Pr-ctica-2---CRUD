package edu.pucmm.icc352.util;

public class Validations {

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isPositiveInt(int n) {
        return n > 0;
    }

    public static boolean isPositiveDouble(double n) {
        return n > 0;
    }
}
