package net.veldor.rdc_info;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.veldor.rdc_info.subclasses.PriceInfo;
import net.veldor.rdc_info.utils.ExecutionsHandler;
import net.veldor.rdc_info.workers.ExecutionsLoadWorker;

public class App extends Application {
    public MutableLiveData<PriceInfo> executionsData = new MutableLiveData<>();

    public ExecutionsHandler executionsHandler = new ExecutionsHandler();
    private static App mAppInstance;

    public static App getInstance() {
        return mAppInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInstance = this;
    }

    public LiveData<PriceInfo> getExecutions() {
        // запущу рабочего, который получит информацию о сменах
        OneTimeWorkRequest loadExecutions = new OneTimeWorkRequest.Builder(ExecutionsLoadWorker.class).build();
        WorkManager.getInstance().enqueue(loadExecutions);
        return executionsData;
    }
}
