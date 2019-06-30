package net.veldor.rdc_info.utils;

import android.arch.lifecycle.MutableLiveData;

import net.veldor.rdc_info.subclasses.Execution;

import java.util.ArrayList;
import java.util.HashMap;

public class ExecutionsHandler {
    HashMap<String, Execution> allExecutionsList;
    public MutableLiveData<ArrayList<Execution>> foundedComplexes = new MutableLiveData<>();
    public MutableLiveData<HashMap<String, Execution>> executionsList = new MutableLiveData<>();

    public void addExecutionByName(String executionName) {
        if (executionName != null) {
            HashMap<String, Execution> list = executionsList.getValue();
            if (list == null) {
                list = new HashMap<>();
            }
            Execution elem = allExecutionsList.get(executionName);
            if (elem != null) {
                // проверю список на наличие комплексов
                searchComplexesIn(executionName);
                list.put(executionName, elem);
            }
            executionsList.postValue(list);
        }

    }


    public void removeExecutionByName(String executionName) {

        if (executionName != null) {
            HashMap<String, Execution> list = executionsList.getValue();
            if (list != null) {
                list.remove(executionName);
                executionsList.postValue(list);
            }
        }
    }

    private void searchComplexesIn(String executionName) {
        ArrayList<Execution> foundedComplexes = new ArrayList<>();
        // найду комплексы, в которых участвует данное обследование
        for (Execution ex :
                allExecutionsList.values()) {
            // если это комплекс
            if(ex.type == Execution.TYPE_COMPLEX && ex.innerExecutions.containsKey(executionName)){
                // проверю, не входит ли обследование в комплекс
                foundedComplexes.add(ex);
            }
        }
        if(foundedComplexes.size() > 0){
            this.foundedComplexes.postValue(foundedComplexes);
        }
    }
}
