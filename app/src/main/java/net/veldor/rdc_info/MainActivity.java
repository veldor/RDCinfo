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
import net.veldor.rdc_info.utils.CostViewModel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private CostViewModel mMyViewModel;
    private PriceInfo mData;
    private ExecutionsAdapter mAdapter;
    private AlertDialog.Builder mDiscountDialog;
    private int mDiscountSelected = 0;
    private View mRootView;
    private Snackbar mTotalCostSnackbar;

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
        mData = executions.getValue();
        if (mData != null) {
            mAdapter = new ExecutionsAdapter(mData.executions);
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
                calculateTotal(executions);
            }
        });
    }

    private void calculateTotal(HashMap<String, Execution> executions) {
        if (executions != null) {
            String cost = mMyViewModel.calculateExecutions(executions, mDiscountSelected);
            if (cost != null) {
                mTotalCostSnackbar = Snackbar.make(mRootView, cost, Snackbar.LENGTH_INDEFINITE);
                mTotalCostSnackbar.show();
            } else {
                if (mTotalCostSnackbar != null) {
                    mTotalCostSnackbar.dismiss();
                }
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
        }
        return super.onOptionsItemSelected(item);
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
