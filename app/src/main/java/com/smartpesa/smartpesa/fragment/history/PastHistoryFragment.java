package com.smartpesa.smartpesa.fragment.history;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.CryptoReceiptActivity;
import com.smartpesa.smartpesa.activity.HistoryViewerActivity;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.payment.AliPayReceiptActivity;
import com.smartpesa.smartpesa.adapters.HistoryAdapter;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.fragment.dialog.EmailHistoryDialog;
import com.smartpesa.smartpesa.fragment.dialog.PrintHistoryDialog;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.yalantis.phoenix.PullToRefreshView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import dagger.Lazy;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.history.GetHistoryCallback;
import smartpesa.sdk.models.history.History;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;
import smartpesa.sdk.models.transaction.Payment;
import smartpesa.sdk.models.transaction.TransactionResult;

public class PastHistoryFragment extends BaseFragment implements AbsListView.OnScrollListener {

    public static final String TRANSACTION_RESPONSE = "transactionResponse";
    Lazy<ServiceManager> serviceManager;
    Context mContext;
    StickyListHeadersListView stickyList;
    ProgressBar progressBar;
    PullToRefreshView mPullToRefreshView;
    TextView noHistoryTv;
    int preLast;
    ArrayList<ParcelableTransactionResponse> historyInfos;
    HistoryAdapter adapter;
    int mPage = 1;
    private boolean mViewAllHistory, isFetching;
    LinearLayout noHistoryLL;
    VerifiedMerchantInfo mVerifyMerchantInfo;

    public PastHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        mVerifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_past_history, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stickyList = (StickyListHeadersListView) view.findViewById(R.id.list);
        progressBar = (ProgressBar) view.findViewById(R.id.pastHistory_PB);
        noHistoryLL = (LinearLayout) view.findViewById(R.id.no_transaction_container);
        noHistoryTv = (TextView) view.findViewById(R.id.no_transaction_tv);

        historyInfos = new ArrayList<>();
        adapter = new HistoryAdapter(getActivity(), historyInfos);
        stickyList.setAdapter(adapter);

        mViewAllHistory = mVerifyMerchantInfo.getProfilePermissions().canViewAllHistory();
        if (mViewAllHistory == true) {
            mViewAllHistory = false;
        }
        getHistory();

        stickyList.setOnScrollListener(this);

        stickyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ParcelableTransactionResponse transactionResponse = historyInfos.get(position);

                if (transactionResponse != null && transactionResponse.getPaymentType() != null) {
                    if (transactionResponse.getPaymentType().equals(Payment.Type.ALIPAY)) {
                        Intent intent = new Intent(mContext, AliPayReceiptActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(TRANSACTION_RESPONSE, transactionResponse);
                        intent.putExtras(bundle);
                        getActivity().startActivity(intent);
                    } else if (transactionResponse.getPaymentType().equals(Payment.Type.CRYPTO)) {
                        Intent intent = new Intent(mContext, CryptoReceiptActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(TRANSACTION_RESPONSE, transactionResponse);
                        intent.putExtras(bundle);
                        getActivity().startActivity(intent);
                    } else if (transactionResponse.getPaymentType().equals(Payment.Type.CARD)) {
                        ArrayList<ParcelableTransactionResponse> parcelableTransactionResponses = new ArrayList<ParcelableTransactionResponse>();
                        parcelableTransactionResponses.add(transactionResponse);
                        Intent intent = new Intent(mContext, HistoryViewerActivity.class);
                        intent.putParcelableArrayListExtra(HistoryViewerActivity.DATA, parcelableTransactionResponses);
                        intent.putExtra(HistoryViewerActivity.INDEX, position);
                        getActivity().startActivity(intent);
                    }
                }

            }
        });

        mPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        getHistory();
                    }
                }, 500);
            }
        });
    }


    //to listen the scolling of listview
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView lw, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        final int lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount) {
            if (preLast != lastItem) {

                //in the last item, make call to fetchPage()
                mPage++;
                fetchPage(mPage);
                preLast = lastItem;
            }
        } else {
            preLast = lastItem;
        }
    }

    //add items to the list when reach end of listview
    private void fetchPage(final int page) {
        noHistoryLL.setVisibility(View.INVISIBLE);
        if (!isFetching) {
            isFetching = true;
            progressBar.setVisibility(View.VISIBLE);

            UIHelper.log("History, show all -> " + mViewAllHistory);

            serviceManager.get().getHistory(page, mViewAllHistory, new GetHistoryCallback() {
                @Override
                public void onSuccess(History history) {
                    if (null == getActivity()) return;

                    progressBar.setVisibility(View.GONE);
                    isFetching = false;
                    if (history != null) {
                        if (history.getTotal() == 0 && page == 1) {
                            noHistoryLL.setVisibility(View.VISIBLE);
                            noHistoryTv.setText(getActivity().getString(R.string.no_history));
                        } else {
                            for (TransactionResult transactionResponse : history.getTransactionResults()) {
                                historyInfos.add(new ParcelableTransactionResponse(transactionResponse));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        noHistoryLL.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(SpException exception) {
                    if (null == getActivity()) return;

                    if (exception instanceof SpSessionException) {
                        progressBar.setVisibility(View.GONE);
                        isFetching = false;
                        UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                        //finish the current activity
                        getActivity().finish();

                        //start the splash screen
                        startActivity(new Intent(getActivity(), SplashActivity.class));
                    } else {
                        progressBar.setVisibility(View.GONE);
                        isFetching = false;
                        UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                    }
                }
            });
        }
    }

    //get history from server and show first time
    private void getHistory() {

        progressBar.setVisibility(View.VISIBLE);

        if (adapter != null) {
            adapter.clearAll();
        }
        mPage = 1;
        fetchPage(mPage);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.historyCB);
        if (item != null) {
            if (!mVerifyMerchantInfo.getProfilePermissions().canViewAllHistory()) {
                item.setVisible(false);
            }
        }
    }

    //more fragment dialog box
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.history_email:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                EmailHistoryDialog emailHistoryDialog = new EmailHistoryDialog();
                emailHistoryDialog.setCancelable(false);
                emailHistoryDialog.show(fragmentManager, "dialog_send_history");
                return true;
            case R.id.history_print:
                FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                PrintHistoryDialog printHistoryDialog = new PrintHistoryDialog();
                printHistoryDialog.setCancelable(false);
                printHistoryDialog.show(fragmentManager2, "dialog_print_history");
                return true;
            case R.id.historyCB:
                if (mViewAllHistory == false) {
                    item.setIcon(R.drawable.ic_checked);
                    mViewAllHistory = true;
                    getHistory();
                } else {
                    item.setIcon(R.drawable.ic_unchecked);
                    mViewAllHistory = false;
                    getHistory();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_history));
        }
    }
}