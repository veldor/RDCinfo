package net.veldor.rdc_info.workers;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.R;
import net.veldor.rdc_info.subclasses.PriceInfo;
import net.veldor.rdc_info.utils.XMLHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ExecutionsLoadWorker extends Worker {
    public ExecutionsLoadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // получу данные
            InputStream data = App.getInstance().getResources().openRawResource(R.raw.cost);
            BufferedReader is;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                is = new BufferedReader(new InputStreamReader(data, StandardCharsets.UTF_8));
            } else {
                //noinspection CharsetObjectCanBeUsed
                is = new BufferedReader(new InputStreamReader(data, "UTF8"));
            }
            StringBuilder total = new StringBuilder();
            for (String line; (line = is.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            XMLHandler xmlHandler = new XMLHandler(total.toString());
            // получу список проводимых обследований
            PriceInfo executionsList = xmlHandler.getPriceInfo();
            App.getInstance().executionsData.postValue(executionsList);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}
