package net.veldor.rdc_info;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.veldor.rdc_info.adapters.ExecutionsAdapter;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.subclasses.PriceInfo;
import net.veldor.rdc_info.utils.CashHandler;
import net.veldor.rdc_info.utils.CostViewModel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private CostViewModel mMyViewModel;
    private ExecutionsAdapter mAdapter;
    private AlertDialog.Builder mDiscountDialog;
    private int mDiscountSelected = 0;
    private int mContrastSelected = 0;
    private View mRootView;
    private Snackbar mTotalCostSnackbar;
    private HashMap<String, Execution> mExecutions;
    private AlertDialog.Builder mContrastDialog;
    private int mContrastCost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = findViewById(R.id.rootView);
        // подключу модель
        mMyViewModel = ViewModelProviders.of(this).get(CostViewModel.class);
        LiveData<PriceInfo> executions = mMyViewModel.getExecutions();
        executions.observe(this, new Observer<PriceInfo>() {
            @Override
            public void onChanged(@Nullable PriceInfo priceInfo) {
                if (priceInfo != null) {
                    mAdapter.setExecutionsList(priceInfo.executions);
                    mAdapter.getFilter().filter("");
                }
            }
        });

        // создам адаптер
        PriceInfo data = executions.getValue();
        if (data != null) {
            mAdapter = new ExecutionsAdapter(data.executions);
        } else {
            mAdapter = new ExecutionsAdapter(null);
        }
        mAdapter.setHasStableIds(true);
        RecyclerView recycler = findViewById(R.id.executions_list);
        recycler.setAdapter(mAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Добавлю отслеживание изменения списка обследований
        MutableLiveData<HashMap<String, Execution>> executionsList = App.getInstance().executionsHandler.executionsList;
        executionsList.observe(this, new Observer<HashMap<String, Execution>>() {
            @Override
            public void onChanged(@Nullable HashMap<String, Execution> executions) {
                // рассчитаю стоимость услуг и выведу её в снекбаре
                mExecutions = executions;
                Log.d("surprise", "onChanged: executions changed " + executions);
                calculateTotal(executions);
            }
        });
    }

    private void calculateTotal(HashMap<String, Execution> executions) {
        // посчитаю сумму за контраст
        int summ = mContrastCost;
        if (executions != null) {
            summ = mMyViewModel.calculateExecutions(executions, mDiscountSelected, summ);
        }
        if(summ > 0){
            mTotalCostSnackbar = Snackbar.make(mRootView, CashHandler.toRubles(summ), Snackbar.LENGTH_INDEFINITE);
            mTotalCostSnackbar.show();
        }
        else{
            if (mTotalCostSnackbar != null) {
                mTotalCostSnackbar.dismiss();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_discount:
                showDiscountDialog();
                return false;
            case R.id.action_show:
                showList();
                return false;
            case R.id.action_cancel:
                dropSelected();
                return false;
            case R.id.action_contrast:
                showContrastDialog();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showContrastDialog() {
        if (mContrastDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.contrast_size_message))
                    .setSingleChoiceItems(PriceInfo.contrastSizes, mContrastSelected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setContrast(which);
                            dialog.dismiss();
                        }
                    });
            mContrastDialog = builder;
        } else {
            mContrastDialog.setSingleChoiceItems(PriceInfo.contrastSizes, mContrastSelected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setContrast(which);
                    dialog.dismiss();
                }
            });
        }
        mContrastDialog.create().show();
    }


    private void dropSelected() {
        // сброшу выбранные обследования
        setDiscount(0);
        App.getInstance().executionsHandler.executionsList.postValue(null);
    }

    private void showList() {
        Log.d("surprise", "showList: " + mExecutions);
    }

    private void showDiscountDialog() {
        if (mDiscountDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.discount_size_message))
                    .setSingleChoiceItems(PriceInfo.discountSizes, mDiscountSelected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setDiscount(which);
                            dialog.dismiss();
                        }
                    });
            mDiscountDialog = builder;
        } else {
            mDiscountDialog.setSingleChoiceItems(PriceInfo.discountSizes, mDiscountSelected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setDiscount(which);
                    dialog.dismiss();
                }
            });
        }
        mDiscountDialog.create().show();
    }

    private void setDiscount(int which) {
        Log.d("surprise", "setDiscount: set discount " + which);
        mMyViewModel.applyDiscount(which);
        mDiscountSelected = which;
        //mAdapter.notifyDataSetChanged();
    }


    private void setContrast(int which) {
        mContrastCost = mMyViewModel.applyContrast(which);
        mContrastSelected = which;
        // оповещу об изменении цены
        App.getInstance().executionsHandler.executionsList.postValue(App.getInstance().executionsHandler.executionsList.getValue());
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        filter(s);
        return true;
    }

    public void filter(String searchString) {
        mAdapter.getFilter().filter(searchString);
    }
}
