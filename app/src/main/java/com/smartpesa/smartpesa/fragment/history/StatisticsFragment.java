package com.smartpesa.smartpesa.fragment.history;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.adapters.StatisticsAdapter;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.helpers.CustomMaterialSpinner;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.DateUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;
import smartpesa.sdk.models.statistics.GetStatisticsCallback;
import smartpesa.sdk.models.statistics.Statistics;

public class StatisticsFragment extends BaseFragment {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    @Bind(R.id.statistics_grid) GridView gridView;
    @Bind(R.id.filter_spinner) CustomMaterialSpinner filterSpin;
    @Bind(R.id.loading_progress) ProgressBar mProgressBar;
    @Bind(R.id.no_transaction_container) LinearLayout noTransactionLL;
    @Bind(R.id.statistics_prompt) TextView mSelectFilterHintTv;
    @Bind(R.id.date_range_tv) TextView dateRangeTv;

    Context mContext;
    ServiceManager mServiceManager;
    String mCurrencySymbol;
    private StatisticsFilterAdapter mAdapter;
    public boolean mViewAllHistory;
    Date startDate, endDate;
    VerifiedMerchantInfo mVerifyMerchantInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mServiceManager = SmartPesaApplication.component(getActivity()).serviceManager().get();

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        mVerifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();
        mCurrencySymbol = mVerifyMerchantInfo.getCurrency().getCurrencySymbol();
        mContext = getActivity();

        mViewAllHistory = mVerifyMerchantInfo.getProfilePermissions().canViewAllHistory();
        if (mViewAllHistory == true) {
            mViewAllHistory = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponents(view);
    }

    private void getStatsByDate() {
        mSelectFilterHintTv.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        setDateRange(startDate, endDate);

        filterSpin.setEnabled(false);

        mServiceManager.getStatistics(startDate, endDate, mViewAllHistory, new GetStatisticsCallback() {
            @Override
            public void onSuccess(List<Statistics> statisticses) {
                if (null == getActivity()) return;
                mProgressBar.setVisibility(View.GONE);
                filterSpin.setEnabled(true);

                //perform null check
                if (null != statisticses) {
                    //check if the statistics list is empty
                    if (statisticses.isEmpty()) {
                        noTransactionLL.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                    } else {
                        //group the transactions and then send to adapter
                        if (groupTransaction(statisticses) != null) {

                            List<DisplayStatistics> displayStatisticsList = groupTransaction(statisticses);
                            //set the adapter for the GridView
                            gridView.setAdapter(new StatisticsAdapter(mContext, displayStatisticsList, mCurrencySymbol));
                            noTransactionLL.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);

                        }

                    }
                } else {
                    gridView.setVisibility(View.INVISIBLE);
                    noTransactionLL.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;

                if (exception instanceof SpSessionException) {
                    mProgressBar.setVisibility(View.GONE);
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    filterSpin.setEnabled(true);
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }

    //set the date range
    private void setDateRange(Date start, Date end) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        String startDate = df.format(start);
        String endDate = df.format(end);

        //if from date and to date is same just show the date
        if (startDate.equals(endDate)) {
            dateRangeTv.setText(startDate);
        } else {
            dateRangeTv.setText(String.format(getString(R.string.statistics_from_to), startDate, endDate));
        }

        dateRangeTv.setVisibility(View.VISIBLE);
    }

    private List<DisplayStatistics> groupTransaction(List<Statistics> statisticsList) {

        DisplayStatistics sale = new DisplayStatistics();
        DisplayStatistics refund = new DisplayStatistics();
        DisplayStatistics cashBack = new DisplayStatistics();
        DisplayStatistics balanceInquiry = new DisplayStatistics();
        DisplayStatistics cashOut = new DisplayStatistics();
        DisplayStatistics transfer = new DisplayStatistics();
        DisplayStatistics payment = new DisplayStatistics();
        DisplayStatistics loyalty = new DisplayStatistics();
        DisplayStatistics cashAdvance = new DisplayStatistics();

        Boolean hasSale, isRefund, isCashBack, isBalanceInquiry, isCashOut, isTransfer, isPayment, isLoyalty, isCashAdvance;
        hasSale = isRefund = isCashBack = isBalanceInquiry = isCashOut = isTransfer = isPayment = isLoyalty = isCashAdvance = false;

        List<DisplayStatistics> returnStatisticsList = new ArrayList<DisplayStatistics>();

        for (int i = 0; i < statisticsList.size(); i++) {
            Statistics statistics = statisticsList.get(i);
            switch (statistics.getTransactionTypeEnumId()) {
                case 1:
                    setStatisticsValues(sale, statistics);
                    hasSale = true;
                    break;
                case 2:
                    setStatisticsValues(refund, statistics);
                    isRefund = true;
                    break;
                case 3:
                    setStatisticsValues(cashBack, statistics);
                    isCashBack = true;
                    break;
                case 4:
                    setStatisticsValues(balanceInquiry, statistics);
                    isBalanceInquiry = true;
                    break;
                case 5:
                    setStatisticsValues(cashOut, statistics);
                    isCashOut = true;
                    break;
                case 6:
                    setStatisticsValues(transfer, statistics);
                    isTransfer = true;
                    break;
                case 7:
                    setStatisticsValues(payment, statistics);
                    isPayment = true;
                    break;
                case 8:
                    setStatisticsValues(loyalty, statistics);
                    isLoyalty = true;
                    break;
                case 9:
                    setStatisticsValues(cashAdvance, statistics);
                    isCashAdvance = true;
                    break;
                default:
                    break;
            }

        }

        if (hasSale) {
            returnStatisticsList.add(sale);
        }
        if (isRefund) {
            returnStatisticsList.add(refund);
        }
        if (isCashBack) {
            returnStatisticsList.add(cashBack);
        }
        if (isBalanceInquiry) {
            returnStatisticsList.add(balanceInquiry);
        }
        if (isCashOut) {
            returnStatisticsList.add(cashOut);
        }
        if (isTransfer) {
            returnStatisticsList.add(transfer);
        }
        if (isPayment) {
            returnStatisticsList.add(payment);
        }
        if (isLoyalty) {
            returnStatisticsList.add(loyalty);
        }
        if (isCashAdvance) {
            returnStatisticsList.add(cashAdvance);
        }
        return returnStatisticsList;

    }

    private void setStatisticsValues(DisplayStatistics displayStatistics, Statistics statistics) {

        int count = displayStatistics.getCount();
        BigDecimal amount = displayStatistics.getAmount();
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        displayStatistics.setAmount(amount.add(statistics.getAmount()));
        displayStatistics.setCount(count + statistics.getCount());
        displayStatistics.setTransactionDescription(statistics.getTransactionDescription());
    }

    private void initializeComponents(View view) {
        ButterKnife.bind(this, view);

        mAdapter = new StatisticsFilterAdapter(mContext);
        filterSpin.setAdapter(mAdapter);
        noTransactionLL.setVisibility(View.INVISIBLE);

        //set default as this week
        filterSpin.setText(R.string.this_week);
        StatisticsFilter f = mAdapter.getItem(2);
        startDate = f.startDate;
        endDate = f.endDate;
        getStatsByDate();

        filterSpin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StatisticsFilter f = mAdapter.getItem(position);
                startDate = f.startDate;
                endDate = f.endDate;
                getStatsByDate();
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.statisticsCB);
        if (item != null) {
            if (!mVerifyMerchantInfo.getProfilePermissions().canViewAllHistory()) {
                item.setVisible(false);
            }
        }
    }

    //menu button
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_statistics, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.statisticsCB:

                //check if a filter is selected at first to do the change
                if (startDate != null || endDate != null) {
                    if (mViewAllHistory == false) {
                        item.setIcon(R.drawable.ic_checked);
                        mViewAllHistory = true;
                        getStatsByDate();
                    } else {
                        item.setIcon(R.drawable.ic_unchecked);
                        mViewAllHistory = false;
                        getStatsByDate();
                    }
                } else {
                    UIHelper.showToast(mContext, getActivity().getString(R.string.select_filter_first));
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class StatisticsFilterAdapter extends ArrayAdapter<StatisticsFilter> {

        private ArrayFilter mFilter;

        public StatisticsFilterAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, StatisticsFilter.values());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (view instanceof TextView) {
                ((TextView) view).setText(getItem(position).stringResId);
            }
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            if (view instanceof TextView) {
                ((TextView) view).setText(getItem(position).stringResId);
            }
            return view;
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }
            return mFilter;
        }

        private class ArrayFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return resultValue == null? "" : getContext().getString(((StatisticsFilter) resultValue).stringResId);
            }
        }
    }

    private enum StatisticsFilter {
        TODAY(R.string.today, DateUtils.toStartOfDay(new Date()), DateUtils.toEndOfDay(new Date())),
        YESTERDAY(R.string.yesterday, DateUtils.toStartOfDay(DateUtils.yesterday(new Date())), DateUtils.toEndOfDay(DateUtils.yesterday(new Date()))),
        THIS_WEEK(R.string.this_week, DateUtils.getFirstDayOfWeek(new Date()), DateUtils.getLastDayOfWeek(new Date())),
        LAST_WEEK(R.string.last_week, DateUtils.getFirstDayOfWeek(DateUtils.getLastWeek(new Date())), DateUtils.getLastDayOfWeek(DateUtils.getLastWeek(new Date()))),
        THIS_MONTH(R.string.this_month, DateUtils.getFirstDayOfMonth(new Date()), DateUtils.getLastDayOfMonth(new Date())),
        LAST_MONTH(R.string.last_month, DateUtils.getFirstDayOfMonth(DateUtils.getLastMonth(new Date())), DateUtils.getLastDayOfMonth(DateUtils.getLastMonth(new Date()))),;

        @StringRes public final int stringResId;
        public final Date startDate;
        public final Date endDate;

        StatisticsFilter(int stringResId, Date startDate, Date endDate) {
            this.stringResId = stringResId;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}