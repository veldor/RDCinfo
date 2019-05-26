package net.veldor.rdc_info.utils;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.subclasses.PriceInfo;

import java.util.Collection;
import java.util.HashMap;

public class CostViewModel extends ViewModel {
    public LiveData<PriceInfo> getExecutions() {
        return App.getInstance().getExecutions();
    }

    public void applyDiscount(int which) {
        DiscountHandler.applyDiscount(which);
    }

    public String calculateExecutions(HashMap<String, Execution> executions, int discountValue) {
        int discount = discountValue * 5;
        Collection<Execution> values = executions.values();
        int totalSumm = 0;
        for (Execution e :
                values) {
            // посчитаю сумму со скидкой
            if(e.type == Execution.TYPE_SIMPLE){
                totalSumm += DiscountHandler.countDiscount(e.summ, discount);
            }
            else{
                totalSumm += Integer.valueOf(e.summ);
            }

        }
        if(totalSumm > 0)
            return CashHandler.toRubles(String.valueOf(totalSumm));
        return null;
    }
}
