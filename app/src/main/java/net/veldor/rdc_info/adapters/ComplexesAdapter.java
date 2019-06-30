package net.veldor.rdc_info.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.veldor.rdc_info.BR;
import net.veldor.rdc_info.R;
import net.veldor.rdc_info.databinding.ComplexLinearItemBinding;
import net.veldor.rdc_info.databinding.ExecutionLinearItemBinding;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.utils.CashHandler;

import java.util.ArrayList;

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

        void bind(Execution execution) {
            mBinding.setVariable(BR.execution, execution);
            int fullSumm = 0;
            int priceSumm = Integer.parseInt(execution.price);
            StringBuffer sb = new StringBuffer();
            for (Execution ex :
                    execution.innerExecutions.values()) {
                fullSumm += Integer.parseInt(ex.price);
                sb.append(ex.price);
                sb.append(" + ");
            }
            sb.append(" = ");
            sb.append(fullSumm);
            sb.append(" ");
            sb.append(CashHandler.countPercentDifference(fullSumm, priceSumm));
            sb.append("(Скидка:");
            sb.append(CashHandler.toRubles(fullSumm - priceSumm));
            sb.append(")");
            mBinding.complexCostDetails.setText(sb.toString());
        }
    }
}
