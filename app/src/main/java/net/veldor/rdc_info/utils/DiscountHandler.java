package net.veldor.rdc_info.utils;

import android.util.Log;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.subclasses.PriceInfo;

import java.util.ArrayList;

class DiscountHandler {
    static void applyDiscount(int which) {
        PriceInfo priceInfo = App.getInstance().executionsData.getValue();
        if (priceInfo != null) {
            ArrayList<Execution> executionsList = priceInfo.executions;
            ArrayList<Execution> newExecutionsList = new ArrayList<>();
            if (which > 0) {
                int discountPercent = which * 5;
                for (Execution ex : executionsList) {
                    if(ex.type == Execution.TYPE_SIMPLE){
                        int cost = (Integer.valueOf(ex.summ) / 100) * (100 - discountPercent);
                        String price = CashHandler.toRubles(ex.summ) + " -" + discountPercent + "% = " + CashHandler.toRubles(String.valueOf(cost));
                        Execution newExecution = new Execution();
                        newExecution.id = ex.id;
                        newExecution.name = ex.name;
                        newExecution.summ = ex.summ;
                        newExecution.type = ex.type;
                        newExecution.summWithDiscount = String.valueOf(cost);
                        newExecution.price = price;
                        newExecutionsList.add(newExecution);
                    }
                    else{
                        newExecutionsList.add(ex);
                    }
                }
            } else {
                for (Execution ex : executionsList) {
                    if(ex.type == Execution.TYPE_SIMPLE) {
                        Execution newExecution = new Execution();
                        newExecution.id = ex.id;
                        newExecution.name = ex.name;
                        newExecution.summ = ex.summ;
                        newExecution.type = ex.type;
                        newExecution.summWithDiscount = ex.summ;
                        newExecution.price = CashHandler.toRubles(newExecution.summ);
                        newExecutionsList.add(newExecution);
                    }
                    else{
                        newExecutionsList.add(ex);
                    }
                }
            }
            priceInfo.executions = newExecutionsList;
            App.getInstance().executionsData.postValue(priceInfo);
            App.getInstance().executionsHandler.executionsList.postValue(App.getInstance().executionsHandler.executionsList.getValue());
        }
    }

    static int countDiscount(String cost, int discount){
        return (Integer.valueOf(cost) / 100) * (100 - discount);
    }

}
