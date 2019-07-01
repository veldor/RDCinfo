package net.veldor.rdc_info.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.BR;
import net.veldor.rdc_info.MainActivity;
import net.veldor.rdc_info.R;
import net.veldor.rdc_info.databinding.ComplexLinearItemBinding;
import net.veldor.rdc_info.databinding.ExecutionLinearItemBinding;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.utils.CashHandler;

import java.util.ArrayList;
import java.util.Locale;

public class ComplexesAdapter extends RecyclerView.Adapter<ComplexesAdapter.ViewHolder> {

    private final ArrayList<Execution> mExecutions;
    private LayoutInflater mLayoutInflater;

    public ComplexesAdapter(ArrayList<Execution> executions) {
        mExecutions = executions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        ComplexLinearItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.complex_linear_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mExecutions.get(i));
    }

    @Override
    public int getItemCount() {
        return mExecutions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ComplexLinearItemBinding mBinding;

        ViewHolder(@NonNull ComplexLinearItemBinding itemView) {
            super(itemView.getRoot());
            mBinding = itemView;
        }

        void bind(final Execution execution) {
            mBinding.setVariable(BR.execution, execution);
            int fullSumm = 0;
            int priceSumm = Integer.valueOf(execution.summ);
            StringBuilder sb = new StringBuilder();
            for (Execution ex :
                    execution.innerExecutions.values()) {
                if(ex != null && ex.summ != null){
                    fullSumm += Integer.valueOf(ex.summ);
                sb.append(ex.price);
                sb.append(" + ");
                }
            }
            sb.setLength(sb.length() - 2);
            sb.append(" = ");
            sb.append(CashHandler.toRubles(fullSumm));

            String newSumm = String.format(Locale.ENGLISH, " (Скидка: %s%%, %s)", CashHandler.countPercentDifference(fullSumm, priceSumm), CashHandler.toRubles(fullSumm - priceSumm));

            String styledText = String.format(Locale.ENGLISH, "<strike><font color='#000000'>%s</font></strike><h2><font color='#D81B60'>%s</font></h2>", sb.toString(), newSumm);

            mBinding.complexCostDetails.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);

            // выберу комплекс при нажатии на поле
            View container = mBinding.getRoot();
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // оповещу активити о том, что надо закрыть диалог и выберу нужный комплекс
                    App.getInstance().executionsHandler.addExecutionByName(execution.name);
                    if(MainActivity.mFoundedComplexesDialog != null){
                        MainActivity.mFoundedComplexesDialog.dismiss();
                    }
                }
            });
        }
    }
}
