package net.veldor.rdc_info.adapters;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.R;
import net.veldor.rdc_info.databinding.ExecutionLinearItemBinding;
import net.veldor.rdc_info.subclasses.Execution;

import java.util.ArrayList;
import java.util.HashMap;

public class ExecutionsAdapter extends RecyclerView.Adapter<ExecutionsAdapter.ViewHolder> implements Filterable {

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
        ExecutionLinearItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.execution_linear_item, viewGroup, false);
        return new ViewHolder(binding);
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
        private final ExecutionLinearItemBinding mBinding;
        private final HashMap<String, Execution> mSelected;

        ViewHolder(@NonNull ExecutionLinearItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;

            // получу список выбранных обследований
            mSelected = App.getInstance().executionsHandler.executionsList.getValue();
        }

        void bind(Execution execution) {
            mBinding.setExecution(execution);
            mBinding.executePendingBindings();
            View container = mBinding.getRoot();
            CheckBox checkbox = container.findViewById(R.id.selectExecutionCheckbox);
            checkbox.setOnCheckedChangeListener(null);
            // проверю, нет ли обследования в списке
            final String name = execution.name;
            if(mSelected != null && name != null && mSelected.containsKey(execution.name)){
                checkbox.setChecked(true);
            }
            else{
                checkbox.setChecked(false);
            }
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d("surprise", "onCheckedChanged: checkbox clicked " + name + " is checked " + isChecked);
                    if(isChecked){
                        // добавлю обследование в список
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
