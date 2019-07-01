package net.veldor.rdc_info.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.android.databinding.library.baseAdapters.BR;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.R;
import net.veldor.rdc_info.databinding.ExecutionGridItemBinding;
import net.veldor.rdc_info.databinding.ExecutionLinearItemBinding;
import net.veldor.rdc_info.subclasses.Execution;

import java.util.ArrayList;
import java.util.HashMap;

public class ExecutionsAdapter extends RecyclerView.Adapter<ExecutionsAdapter.ViewHolder> implements Filterable {

    public boolean isGrid;
    private ArrayList<Execution> mExecutions;
    private ArrayList<Execution> mFilteredExecutions;
    private LayoutInflater mLayoutInflater;

    public ExecutionsAdapter(ArrayList<Execution> executions) {
        mExecutions = executions;
    }

    @Override
    public long getItemId(int position) {
        return mFilteredExecutions.get(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        if(isGrid){
            ExecutionGridItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.execution_grid_item, viewGroup, false);
            return new ViewHolder(binding);
        }
        else{
            ExecutionLinearItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.execution_linear_item, viewGroup, false);
            return new ViewHolder(binding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mFilteredExecutions.get(i));
    }

    @Override
    public int getItemCount() {
        return mFilteredExecutions != null ? mFilteredExecutions.size() : 0;
    }

    @Override
    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    mFilteredExecutions = mExecutions;
                } else {
                    ArrayList<Execution> filtredList = new ArrayList<>();
                    for (Execution c : mExecutions) {
                        if(c.name.contains(constraint)){
                            filtredList.add(c);
                        }
                    }
                    mFilteredExecutions = filtredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredExecutions;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredExecutions = (ArrayList<Execution>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void setExecutionsList(final ArrayList<Execution> executions) {
        if (mExecutions == null) {
            mExecutions = executions;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mExecutions.size();
                }

                @Override
                public int getNewListSize() {
                    return executions.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mExecutions.get(oldItemPosition).name.equals(executions.get(newItemPosition).name);
                }

                @Override
                public boolean areContentsTheSame(int i, int i1) {
                    Execution oldExecution = mExecutions.get(i);
                    Execution newExecution = executions.get(i1);
                    return oldExecution.name.equals(newExecution.name) && oldExecution.price.equals(newExecution.price) && oldExecution.summWithDiscount.equals(newExecution.summWithDiscount);
                }
            });
            mExecutions = executions;
            result.dispatchUpdatesTo(this);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private final ViewDataBinding mBinding;

        ViewHolder(@NonNull ExecutionLinearItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
        ViewHolder(@NonNull ExecutionGridItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(final Execution execution) {
            HashMap<String, Execution> selected = App.getInstance().executionsHandler.executionsList.getValue();
            mBinding.setVariable(BR.execution, execution);
            mBinding.executePendingBindings();
            View container = mBinding.getRoot();
            final CheckBox[] checkbox = {container.findViewById(R.id.selectExecutionCheckbox)};
            checkbox[0].setOnClickListener(null);
            // проверю, нет ли обследования в списке
            final String name = execution.name;
            if(selected != null && name != null && selected.containsKey(execution.name)){
                checkbox[0].setChecked(true);
            }
            else{
                checkbox[0].setChecked(false);
            }
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isGrid){
                        Toast.makeText(App.getInstance(), execution.name, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        checkbox[0].performClick();
                    }
                }
            });
            checkbox[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkbox = (CheckBox) v;
                    if(checkbox.isChecked()){
                        App.getInstance().executionsHandler.addExecutionByName(name);
                    }
                    else{
                        App.getInstance().executionsHandler.removeExecutionByName(name);
                    }
                }
            });
        }

    }
}
