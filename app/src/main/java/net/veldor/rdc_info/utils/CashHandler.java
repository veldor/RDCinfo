package net.veldor.rdc_info.utils;

import android.util.Log;

public class CashHandler {
    public static String toRubles(String inputString){
        // разобью строку на рубли и копейки
        int rubles = Integer.valueOf(inputString.substring(0, inputString.length() - 2));
        if(Integer.valueOf(inputString.substring(inputString.length() - 2)) > 0){
            ++rubles;
        }
        return rubles + "\u00A0\u20BD";
    }
    public static String toRubles(int summ){
        // разобью строку на рубли и копейки
        int rubles = summ / 100;
        if(summ % 100 > 0){
            ++rubles;
        }
        return rubles + "\u00A0\u20BD";
    }

    public static int countPercentDifference(int fullSumm, int priceSumm) {
        // найду разницу в %
        return (int)(((fullSumm - priceSumm) / (double)fullSumm) * 100);
    }
}
