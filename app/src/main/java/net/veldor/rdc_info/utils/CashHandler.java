package net.veldor.rdc_info.utils;

public class CashHandler {
    public static String toRubles(String inputString){
        // разобью строку на рубли и копейки
        String rubles = inputString.substring(0, inputString.length() - 2);
        return rubles + " \u20BD";
    }
    public static String toRubles(int summ){
        // разобью строку на рубли и копейки
        int rubles = summ / 100;
        return rubles + " \u20BD";
    }
}
