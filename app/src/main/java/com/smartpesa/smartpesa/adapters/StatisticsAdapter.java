package com.smartpesa.smartpesa.adapters;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.fragment.history.DisplayStatistics;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsAdapter extends BaseAdapter {
    private Context context;
    private final List<DisplayStatistics> statisticsList;
    private String currency;

    @Nullable public MoneyUtils mMoneyUtils;

    public StatisticsAdapter(Context context, List<DisplayStatistics> statistics, String currency) {
        this.context = context;
        this.statisticsList = statistics;
        this.currency = currency;
        mMoneyUtils = SmartPesaApplication.merchantComponent(context).provideMoneyUtils();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_statistics, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DisplayStatistics statistics = statisticsList.get(position);

        holder.transactionNameTV.setText(statistics.getTransactionDescription());
        holder.countTV.setText(String.valueOf(statistics.getCount()));

        if (statistics.getTransactionDescription().equals("Balance Inquiry")) {
            holder.amountTV.setVisibility(View.INVISIBLE);
        } else {
            holder.amountTV.setText(String.format("%1$s %2$s", currency, mMoneyUtils.format(statistics.getAmount())));
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return statisticsList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public static class ViewHolder {
        @BindView(R.id.transactionNameTV) TextView transactionNameTV;
        @BindView(R.id.countTV) TextView countTV;
        @BindView(R.id.amountStatisticsTV) TextView amountTV;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}