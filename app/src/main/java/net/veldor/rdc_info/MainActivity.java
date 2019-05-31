package net.veldor.rdc_info;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.veldor.rdc_info.adapters.ExecutionsAdapter;
import net.veldor.rdc_info.subclasses.Anesthesia;
import net.veldor.rdc_info.subclasses.Contrast;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.subclasses.PriceInfo;
import net.veldor.rdc_info.utils.CashHandler;
import net.veldor.rdc_info.utils.CostViewModel;
import net.veldor.rdc_info.utils.DiscountHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private CostViewModel mMyViewModel;
    private ExecutionsAdapter mAdapter;
    private AlertDialog.Builder mDiscountDialog;
    private int mDiscountSelected = 0;
    private int mContrastSelected = 0;
    private int mAnesthesiaSelected = 0;
    private ConstraintLayout mRootView;
    private Snackbar mTotalCostSnackbar;
    private AlertDialog.Builder mContrastDialog;
    private int mContrastCost = 0;
    private int mTotalSumm;
    private AlertDialog mDetailsDialog;
    private boolean mResetDetailsDialog;
    private HashMap<String, Execution> mExecutions;

    private RecyclerView mRecycler;
    private boolean mIsGrid = false;
    private boolean mPrint = false;
    private Integer mPrintPrice = 0;
    private AlertDialog.Builder mAnesthesiaDialog;
    private int mAnesthesiaCost = 0;

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
        mRecycler = findViewById(R.id.executions_list);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Добавлю отслеживание изменения списка обследований
        MutableLiveData<HashMap<String, Execution>> executionsList = App.getInstance().executionsHandler.executionsList;
        executionsList.observe(this, new Observer<HashMap<String, Execution>>() {
            @Override
            public void onChanged(@Nullable HashMap<String, Execution> executions) {
                mExecutions = executions;
                // рассчитаю стоимость услуг и выведу её в снекбаре
                PriceInfo executionsData = App.getInstance().executionsData.getValue();
                if (executionsData != null) {
                    mAdapter.setExecutionsList(executionsData.executions);
                    mAdapter.notifyDataSetChanged();
                }
                calculateTotal(mExecutions);
            }
        });
    }

    private void calculateTotal(HashMap<String, Execution> executions) {
        int summ = 0;
        // посчитаю сумму за печать плёнки
        summ += mPrintPrice;
        // посчитаю сумму за контраст
        summ += mContrastCost;
        // посчитаю сумму за контраст
        summ += mAnesthesiaCost;
        if (executions != null) {
            summ = mMyViewModel.calculateExecutions(executions, mDiscountSelected, summ);
        }
        if (summ > 0) {
            mTotalSumm = summ;
            mTotalCostSnackbar = Snackbar.make(mRootView, CashHandler.toRubles(summ), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.cost_details_message), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectedDetails();
                }
            });
            mTotalCostSnackbar.show();
        } else {
            mTotalSumm = 0;
            if (mTotalCostSnackbar != null) {
                mTotalCostSnackbar.dismiss();
            }
        }
        if (mResetDetailsDialog) {
            mResetDetailsDialog = false;
            showSelectedDetails();
        }
    }

    private void showSelectedDetails() {
        if (mTotalSumm > 0) {
            LayoutInflater inflater = getLayoutInflater();
            ScrollView view = (ScrollView) inflater.inflate(R.layout.details_dialog, mRootView, false);
            TextView targetView = view.findViewById(R.id.total_summ);
            targetView.setText(String.format(Locale.ENGLISH, "Итого: %s", CashHandler.toRubles(mTotalSumm)));
            // если назначена скидка- выведу её
            targetView = view.findViewById(R.id.discount_text);
            if (mDiscountSelected > 0) {
                targetView.setText(String.format(Locale.ENGLISH, getApplicationContext().getString(R.string.discount_details_message), mDiscountSelected * 5));
            } else {
                targetView.setVisibility(View.GONE);
            }
            targetView = view.findViewById(R.id.print_text);
            if(mPrintPrice > 0){
                targetView.setText(String.format(Locale.ENGLISH, getApplicationContext().getString(R.string.print_list_message), CashHandler.toRubles(mPrintPrice)));
            } else {
                targetView.setVisibility(View.GONE);
            }

            targetView = view.findViewById(R.id.contrast_text);
            if (mContrastSelected > 0) {
                PriceInfo executionsInfo = App.getInstance().executionsData.getValue();
                if (executionsInfo != null) {
                    Contrast contrastsInfo = executionsInfo.contrasts.get(mContrastSelected - 1);
                    targetView.setText(String.format(Locale.ENGLISH, getApplicationContext().getString(R.string.contrast_details_message), contrastsInfo.name, CashHandler.toRubles(contrastsInfo.summ)));
                }
            } else {
                targetView.setVisibility(View.GONE);
            }
            targetView = view.findViewById(R.id.anesthesia_text);
            if (mAnesthesiaSelected > 0) {
                PriceInfo executionsInfo = App.getInstance().executionsData.getValue();
                if (executionsInfo != null) {
                    Anesthesia anesthesia = executionsInfo.anesthesia.get(mAnesthesiaSelected - 1);
                    targetView.setText(String.format(Locale.ENGLISH, getApplicationContext().getString(R.string.anesthesia_details_message), anesthesia.name, CashHandler.toRubles(anesthesia.summ)));
                }
            } else {
                targetView.setVisibility(View.GONE);
            }
            // теперь добавлю значение для каждого выбранного обследования
            HashMap<String, Execution> list = App.getInstance().executionsHandler.executionsList.getValue();
            if (list != null) {
                LinearLayout root = view.findViewById(R.id.executions_block);
                Collection<Execution> iterable = list.values();
                for (final Execution item :
                        iterable) {
                    CardView ex = (CardView) inflater.inflate(R.layout.execution_item, root, false);
                    TextView executionNameView = ex.findViewById(R.id.execution_name);
                    executionNameView.setText(String.format(Locale.ENGLISH, "%s: %s", item.name, CashHandler.toRubles(DiscountHandler.countDiscount(item.summ, mDiscountSelected * 5))));

                    Button removeButton = ex.findViewById(R.id.remove_from_list_button);
                    removeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            App.getInstance().executionsHandler.removeExecutionByName(item.name);
                            // пока просто перерисую диалог
                            mDetailsDialog.dismiss();
                            mResetDetailsDialog = true;
                        }
                    });
                    root.addView(ex);
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Детализация выбранных обследований")
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(R.string.reset_message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dropSelected();
                        }
                    })
                    .setView(view);
            mDetailsDialog = builder.create();
            mDetailsDialog.show();
            mDetailsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    calculateTotal(mExecutions);
                }
            });
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi = menu.findItem(R.id.pro);
        mi.setChecked(mIsGrid);
        mi = menu.findItem(R.id.print);
        mi.setChecked(mPrint);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        // выбранный вид
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
            case R.id.action_cancel:
                dropSelected();
                return false;
            case R.id.action_contrast:
                showContrastDialog();
                return false;
            case R.id.action_anesthesia:
                showAnesthesiaDialog();
                return false;
            case R.id.pro:
                switchViewMode();
                return false;
            case R.id.print:
                handlePrint();
        }
        return super.onOptionsItemSelected(item);
    }


    private void handlePrint() {
        mPrint = !mPrint;
        if(mPrint){
            // добавлю к общей стоимости стоимость печати
            mPrintPrice = mMyViewModel.getPrintPrice();
        }
        else{
            mPrintPrice = 0;
        }
        // оповещу об изменении цены
        App.getInstance().executionsHandler.executionsList.postValue(App.getInstance().executionsHandler.executionsList.getValue());
    }

    private void switchViewMode() {
        mIsGrid = !mIsGrid;
        mAdapter.isGrid = mIsGrid;
        mRecycler.removeAllViews();
        if(mIsGrid){
            mRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        }
        else{
            mRecycler.setLayoutManager(new LinearLayoutManager(this));
        }
        mAdapter.notifyDataSetChanged();
        Log.d("surprise", "switchViewMode: mode switched");

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


    private void showAnesthesiaDialog() {
        // получу список названий наркозов
        PriceInfo list = App.getInstance().executionsData.getValue();
        ArrayList<String> values = new ArrayList<>();
        values.add(getBaseContext().getString(R.string.no_anesthesia_item));
        if(list != null){
            for (Anesthesia item : list.anesthesia) {
                values.add(item.name);
            }
        }
        String[] valuesArr = values.toArray(new String[0]);
        if (mAnesthesiaDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.anesthesia_size_message))
                    .setSingleChoiceItems(valuesArr, mAnesthesiaSelected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setAnesthesia(which);
                            dialog.dismiss();
                        }
                    });
            mAnesthesiaDialog = builder;
        } else {
            mAnesthesiaDialog.setSingleChoiceItems(valuesArr, mAnesthesiaSelected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setAnesthesia(which);
                    dialog.dismiss();
                }
            });
        }
        mAnesthesiaDialog.create().show();
    }


    private void dropSelected() {
        // сброшу выбранные обследования
        setDiscount(0);
        setContrast(0);
        setAnesthesia(0);
        mPrintPrice = 0;
        mPrint = false;
        App.getInstance().executionsHandler.executionsList.postValue(null);
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
    private void setAnesthesia(int which) {
        mAnesthesiaCost = mMyViewModel.applyAnesthesia(which);
        mAnesthesiaSelected = which;
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
