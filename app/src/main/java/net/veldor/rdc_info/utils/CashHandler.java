package net.veldor.rdc_info.utils;

public class CashHandler {
    static String toRubles(String inputString){
        // разобью строку на рубли и копейки
        String rubles = inputString.substring(0, inputString.length() - 2);
        return rubles + " \u20BD";
    }
}
