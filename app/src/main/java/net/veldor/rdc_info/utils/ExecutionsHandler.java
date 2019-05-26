package net.veldor.rdc_info.utils;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import net.veldor.rdc_info.subclasses.Execution;

import java.util.HashMap;

public class ExecutionsHandler {
    HashMap<String, Execution> allExecutionsList;
    public MutableLiveData<HashMap<String, Execution>> executionsList = new MutableLiveData<>();

    public void addExecutionByName(String executionName) {
        if (executionName != null) {
            HashMap<String, Execution> list = executionsList.getValue();
            if (list == null) {
                list = new HashMap<>();
            }
            Execution elem = allExecutionsList.get(executionName);
            if (elem != null) {
                list.put(executionName, elem);
            }
            executionsList.postValue(list);
            Log.d("surprise", "addExecutionByName: " + list.size());
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
}
