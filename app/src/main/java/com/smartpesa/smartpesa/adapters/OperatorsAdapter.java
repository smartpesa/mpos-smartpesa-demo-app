package com.smartpesa.smartpesa.adapters;

import com.smartpesa.smartpesa.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import smartpesa.sdk.models.operator.Operator;

public class OperatorsAdapter extends ArrayAdapter<Operator> implements Filterable {

    private final Filter mFilter;
    private List<Operator> allOperators;
    private List<Operator> mOperatorInfoList;
    private Context context;
    boolean isEnabled;

    public OperatorsAdapter(Context context, final int resource, List<Operator> items, boolean enableEdit) {
        super(context, resource, items);
        this.mOperatorInfoList = items;
        this.allOperators = Collections.unmodifiableList(mOperatorInfoList);
        this.context = context;
        this.mFilter = new OperatorInfoActiveFilter();
        this.isEnabled = enableEdit;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        OperatorViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_operator, parent, false);
            viewHolder = new OperatorViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (OperatorViewHolder) convertView.getTag();
        }
        Operator operatorInfo = this.mOperatorInfoList.get(position);

        viewHolder.nameTv.setText(operatorInfo.getOperatorName());
        viewHolder.codeTv.setText(context.getResources().getString(R.string.code, operatorInfo.getOperatorCode()));

        String name = operatorInfo.getOperatorName();
        viewHolder.initialTv.setText(name.substring(0, 1).toUpperCase());

        if (operatorInfo.isActive()) {
            viewHolder.initialTv.setBackground(context.getResources().getDrawable(R.drawable.green_circle));
        } else {
            viewHolder.initialTv.setBackground(context.getResources().getDrawable(R.drawable.ic_blue_circle));
        }

        if (!isEnabled) {
            viewHolder.editIv.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mOperatorInfoList.size();
    }

    @Override
    public Operator getItem(int position) {
        return mOperatorInfoList.get(position);
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public class OperatorInfoActiveFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Operator> filteredOperators;
            if (constraint == null) {
                filteredOperators = allOperators;
            } else {
                filteredOperators = new ArrayList<>();
                for (Operator operatorInfo : OperatorsAdapter.this.allOperators) {
                    if (operatorInfo.isActive()) {
                        filteredOperators.add(operatorInfo);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.count = filteredOperators.size();
            results.values = filteredOperators;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            synchronized (this) {
                mOperatorInfoList = (List<Operator>) results.values;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    protected static class OperatorViewHolder {
        @Bind(R.id.row_operator_nameTV) public TextView nameTv;
        @Bind(R.id.row_operatorCode_TV) public TextView codeTv;
        @Bind(R.id.row_operator_initial_TV) public TextView initialTv;
        @Bind(R.id.edit_iv) public ImageView editIv;

        public OperatorViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return isEnabled;
    }
}
