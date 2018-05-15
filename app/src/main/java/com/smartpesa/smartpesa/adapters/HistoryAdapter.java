package com.smartpesa.smartpesa.adapters;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.helpers.VerticalTextView;
import com.smartpesa.smartpesa.models.ParcelableAliPayPayment;
import com.smartpesa.smartpesa.models.ParcelableCardPayment;
import com.smartpesa.smartpesa.models.ParcelableGoCoinPayment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.util.DateUtils;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import smartpesa.sdk.models.transaction.Payment;

public class HistoryAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    List<ParcelableTransactionResponse> result;
    Context mContext;
    private Calendar cal;

    @Nullable public MoneyUtils mMoneyUtils;

    public HistoryAdapter(Context context, List<ParcelableTransactionResponse> getHistoryResponse) {
        result = getHistoryResponse;
        mContext = context;
        cal = Calendar.getInstance();
        mMoneyUtils = SmartPesaApplication.merchantComponent(context).provideMoneyUtils();
    }

    public void clearAll() {
        result.clear();
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return result.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        ParcelableTransactionResponse historyInfo = result.get(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_history_item, parent, false);
            holder.amountTV = (TextView) convertView.findViewById(R.id.row_amount_TV);
            holder.referenceIdTV = (TextView) convertView.findViewById(R.id.row_reference_TV);
            holder.transactionTypeTV = (VerticalTextView) convertView.findViewById(R.id.row_transactionType);
            holder.cardHolderNameTV = (TextView) convertView.findViewById(R.id.row_cardHolderName_TV);
            holder.cardNumberTV = (TextView) convertView.findViewById(R.id.row_cardNumber_TV);
            holder.detailRL = (RelativeLayout) convertView.findViewById(R.id.detailRL);
            holder.successIV = (ImageView) convertView.findViewById(R.id.row_success_IV);
            holder.timeTV = (TextView) convertView.findViewById(R.id.row_timeTV);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //set the values here
        if (historyInfo != null) {

            String currency = historyInfo.getCurrencySymbol();
            BigDecimal amount = historyInfo.getAmount();

            if (amount != null && mMoneyUtils != null && currency != null) {
                holder.amountTV.setText(currency + " " + mMoneyUtils.format(amount));
            } else {
                holder.amountTV.setText("");
            }

            String transactionReference = historyInfo.getTransactionReference();

            if (transactionReference != null) {
                holder.referenceIdTV.setText(transactionReference);
            } else {
                holder.referenceIdTV.setText("");
            }

            String type = historyInfo.getTransactionDescription();
            if (type != null) {
                if (type.length() > 10) {
                    String trim = type.substring(0, 8);
                    holder.transactionTypeTV.setText(trim);
                } else {
                    holder.transactionTypeTV.setText(type);
                }
            } else {
                holder.transactionTypeTV.setText("");
            }

            Date dateTime = historyInfo.getTransactionDatetime();

            if (dateTime != null) {
                //covert date to Time
                holder.timeTV.setText(DateUtils.format(historyInfo.getTransactionDatetime(), DateUtils.TIME_FORMAT_HH_MM_AA));
            } else {
                holder.timeTV.setText("");
            }

            Payment.Type paymentType = historyInfo.getPaymentType();

            if (paymentType != null) {

                if (paymentType.equals(Payment.Type.CARD)) {

                    ParcelableCardPayment cardPayment = historyInfo.getCardPayment();

                    if (cardPayment != null) {
                        String cardHolderName = cardPayment.getCardHolderName();

                        if (cardHolderName != null) {
                            holder.cardHolderNameTV.setText(cardHolderName);
                        } else {
                            holder.cardHolderNameTV.setText("");
                        }

                        String cardNumber = cardPayment.getCardNumber();

                        if (cardNumber != null) {
                            holder.cardNumberTV.setText(cardNumber);
                        } else {
                            holder.cardNumberTV.setText("");
                        }

                    } else {
                        holder.cardNumberTV.setText("");
                        holder.cardHolderNameTV.setText("");
                    }

                    holder.successIV.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_card_icon));

                } else if (paymentType.equals(Payment.Type.ALIPAY)) {

                    ParcelableAliPayPayment aliPayPayment = historyInfo.getAlipayPayment();

                    if (aliPayPayment != null) {
                        String buyerId = aliPayPayment.getBuyerId();

                        if (buyerId != null) {
                            holder.cardHolderNameTV.setText("Buyer ID: " + buyerId);
                        } else {
                            holder.cardHolderNameTV.setText("");
                        }

                        String trade = aliPayPayment.getTradeNo();

                        if (trade != null) {
                            holder.cardNumberTV.setText("Trade Out: " + trade);
                        } else {
                            holder.cardNumberTV.setText("");
                        }
                    } else {
                        holder.cardNumberTV.setText("");
                        holder.cardHolderNameTV.setText("");
                    }

                    holder.successIV.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_alipay_history));

                } else if (paymentType.equals(Payment.Type.CRYPTO)) {

                    ParcelableGoCoinPayment goCoinPayment = historyInfo.getGoCoinPayment();

                    if (goCoinPayment != null) {

                        String currencyName = goCoinPayment.getCryptoCurrencyName();

                        if (currencyName != null) {
                            holder.cardHolderNameTV.setText(currencyName);
                        } else {
                            holder.cardHolderNameTV.setText("");
                        }

                        BigDecimal cryptoAmount = goCoinPayment.getCryptoPrice();

                        if (cryptoAmount != null) {
                            holder.cardNumberTV.setText(cryptoAmount.toString());
                        } else {
                            holder.cardNumberTV.setText("");
                        }

                    } else {
                        holder.cardNumberTV.setText("");
                        holder.cardHolderNameTV.setText("");
                    }

                    holder.successIV.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_cryto));
                }

            } else {
                holder.cardNumberTV.setText("");
                holder.cardHolderNameTV.setText("");
            }
        }
        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_history_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.rowHeaderTV);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        ParcelableTransactionResponse historyInfo = result.get(position);

        //convert date to date and time
        String date = DateUtils.format(historyInfo.getTransactionDatetime(), DateUtils.DATE_FORMAT_DD_MM_YYYY);

        holder.text.setText(date);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {

        cal.setTime(result.get(position).getTransactionDatetime());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        public TextView amountTV, referenceIdTV, cardHolderNameTV, cardNumberTV, timeTV;
        public TextView transactionTypeTV;
        public RelativeLayout detailRL;
        public ImageView successIV;
    }
}