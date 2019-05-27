package net.veldor.rdc_info.utils;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.subclasses.Contrast;
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

    public int calculateExecutions(HashMap<String, Execution> executions, int discountValue, int contrastCost) {
        int discount = discountValue * 5;
        Collection<Execution> values = executions.values();
        int totalSumm = contrastCost;
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
            return totalSumm;
        return 0;
    }

    public int applyContrast(int which) {
        // получу данные о стоимости
        int r = 0;
        if(which > 0){
            // получу сведения о контрасте
            PriceInfo priceInfo = App.getInstance().executionsData.getValue();
            if(priceInfo != null){
                Contrast cost = priceInfo.contrasts.get(which - 1);
                r = Integer.valueOf(cost.summ);
            }
        }
        return r;
    }
}
