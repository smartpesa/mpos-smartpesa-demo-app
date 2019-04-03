
package com.smartpesa.smartpesa.fragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.currency.Currency;
import smartpesa.sdk.models.currency.GetAvailableCurrenciesCallback;

public class CurrencyDialogFragment extends BaseDialogFragment {

    @BindView(R.id.cancel_btn)
    Button cancelBtn;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    Lazy<ServiceManager> serviceManager;
    protected CurrencyAdapter adapter;
    protected CurrencySelectedListener<Currency> mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_currency, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setCancelable(false);
        return d;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

        getCurrencyList();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
    }

    private void getCurrencyList() {
        progressBar.setVisibility(View.VISIBLE);
        serviceManager.get().getAvailableCurrencies(new GetAvailableCurrenciesCallback() {
            @Override
            public void onSuccess(final List<Currency> currencies) {
                progressBar.setVisibility(View.GONE);

                final List<Currency> fiatCurrencies = new ArrayList<>();

                if (currencies != null && currencies.size() > 0) {
                    for (int i=0; i < currencies.size(); i++) {
                        Currency currency = currencies.get(i);

                        if (currency.getCurrencyType().equals(Currency.Type.FIAT)) {
                            fiatCurrencies.add(currency);
                        }
                    }
                }

                ObjectComparator comparator = new ObjectComparator();
                Collections.sort(fiatCurrencies, comparator);

                adapter = new CurrencyAdapter(getActivity(), fiatCurrencies);
                list.setAdapter(adapter);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mListener.onSelected(fiatCurrencies.get(position));
                        dismiss();
                    }
                });

            }

            @Override
            public void onError(SpException e) {
                if (null == getActivity()) return;

                if (e instanceof SpSessionException) {
                    progressBar.setVisibility(View.GONE);
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    progressBar.setVisibility(View.GONE);
                    UIHelper.showErrorDialog(getActivity(), getResources().getString(R.string.app_name), e.getMessage());
                }
            }
        });
    }

    public void setSelectedListener(CurrencySelectedListener<Currency> listener) {
        mListener = listener;
    }

    public interface CurrencySelectedListener<Currency> {
        void onSelected(Currency currency);
    }

    protected static class CurrencyAdapter extends ArrayAdapter {

        public CurrencyAdapter(Context context, List<Currency> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Currency currency = (Currency) getItem(position);
            String sourceString = "<b>" + currency.getCurrencySymbol() + "</b> - " + currency.getCurrencyName();
            ((TextView) view).setText(Html.fromHtml(sourceString));
            return view;
        }
    }

    public class ObjectComparator implements Comparator<Currency> {
        public int compare(Currency obj1, Currency obj2) {
            return obj1.getCurrencyName().compareTo(obj2.getCurrencyName());
        }
    }
}