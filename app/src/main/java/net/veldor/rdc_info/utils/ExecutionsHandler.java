package net.veldor.rdc_info.utils;

import android.arch.lifecycle.MutableLiveData;

import net.veldor.rdc_info.subclasses.Execution;

import java.util.ArrayList;
import java.util.HashMap;

public class ExecutionsHandler {
    HashMap<String, Execution> allExecutionsList;
    MutableLiveData<ArrayList<Execution>> foundedComplexes = new MutableLiveData<>();
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

                // если обследование комплексное- отмечу выбранными все обычные обследования, учавствующие в нём
                if(elem.type == Execution.TYPE_COMPLEX){
                    for (Execution ex :
                            elem.innerExecutions.values()) {
                        if(ex != null){
                            if(!list.containsKey(ex.name)){
                                list.put(ex.name, ex);
                            }
                        }
                    }
                }
            }
            executionsList.postValue(list);
        }

    }


    public void removeExecutionByName(String executionName) {

        if (executionName != null) {
            HashMap<String, Execution> list = executionsList.getValue();
            if (list != null) {
                // если обследование комплексное- удалю все обычные обследования, учавствующие в нём
                Execution elem = allExecutionsList.get(executionName);
                if(elem != null){
                    if(elem.type == Execution.TYPE_COMPLEX){
                        for (Execution ex :
                                elem.innerExecutions.values()) {
                            if(ex != null){
                                list.remove(ex.name);
                            }
                        }
                    }
                    else{
                        // удаляю все комплексы, в которых использовалось данное обследование
                        ArrayList<String> forDelete = new ArrayList<>();
                        for (Execution ex :
                                list.values()){
                            if(ex != null && ex.type == Execution.TYPE_COMPLEX && ex.innerExecutions.containsKey(executionName)){
                                if(ex.name != null){
                                    forDelete.add(ex.name);
                                }
                            }
                        }
                        if(forDelete.size() > 0){
                            for (String s :
                                    forDelete){
                                list.remove(s);
                            }
                        }
                    }
                }
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
