package com.smartpesa.smartpesa.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.fragment.dialog.LoyaltyDialogFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.UIRedeemable;

import java.util.ArrayList;

public class LoyaltyAdapter extends RecyclerView.Adapter<LoyaltyAdapter.ViewHolder> {

    private ArrayList<UIRedeemable> parseObjectList;
    Context context;
    boolean isInquiry;
    LoyaltyDialogFragment mLoyaltyDialogFragment;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv, amountTv, typeTv, unitTv, balanceTv;
        CheckBox loyaltyCB;
        LinearLayout loyaltyLL;

        public ViewHolder(View v) {
            super(v);
            nameTv = (TextView) v.findViewById(R.id.loyaltyName);
            amountTv = (TextView) v.findViewById(R.id.loyaltyAmount);
            typeTv = (TextView) v.findViewById(R.id.loyaltyType);
            unitTv = (TextView) v.findViewById(R.id.loyaltyUnit);
            balanceTv = (TextView) v.findViewById(R.id.loyaltyBalance);
            loyaltyCB = (CheckBox) v.findViewById(R.id.loyaltyCB);
            loyaltyLL = (LinearLayout) v.findViewById(R.id.loyaltyLL);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LoyaltyAdapter(ArrayList<UIRedeemable> objects, Context mContext, boolean isInquiry, LoyaltyDialogFragment loyaltyDialogFragment) {
        parseObjectList = objects;
        context = mContext;
        this.isInquiry = isInquiry;
        this.mLoyaltyDialogFragment = loyaltyDialogFragment;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LoyaltyAdapter(ArrayList<UIRedeemable> objects, Context mContext, boolean isInquiry) {
        parseObjectList = objects;
        context = mContext;
        this.isInquiry = isInquiry;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public LoyaltyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loyalty, parent, false);

        LoyaltyAdapter.ViewHolder viewHolder = new LoyaltyAdapter.ViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final UIRedeemable object = parseObjectList.get(position);

        holder.nameTv.setText(object.getName());
        holder.amountTv.setText(UIHelper.formatBigdecimal(object.getAmount()));
        holder.typeTv.setText(UIHelper.getLoyaltyTypeFromId(object.getType()));
        holder.unitTv.setText(object.getUnit());
        holder.balanceTv.setText(UIHelper.formatBalance(object.getBalance()));

        if (isInquiry) {
            holder.amountTv.setVisibility(View.GONE);
            holder.loyaltyCB.setVisibility(View.GONE);
        } else {
            holder.amountTv.setVisibility(View.VISIBLE);
            holder.loyaltyCB.setVisibility(View.VISIBLE);

            holder.loyaltyLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.loyaltyCB.isChecked()) {
                        holder.loyaltyCB.setChecked(false);
                    } else {
                        holder.loyaltyCB.setChecked(true);
                    }
                }
            });

            holder.loyaltyCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isChecked()) {
                        object.setSelected(true);
                        mLoyaltyDialogFragment.updateAmount(object.getAmount(), true);
                    } else {
                        object.setSelected(false);
                        mLoyaltyDialogFragment.updateAmount(object.getAmount(), false);
                    }
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return parseObjectList.size();
    }
}

