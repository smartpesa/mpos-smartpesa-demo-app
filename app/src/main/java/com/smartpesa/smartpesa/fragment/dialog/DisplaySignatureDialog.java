package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.UUID;

import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.transaction.GetSignatureCallback;

public class DisplaySignatureDialog extends BaseDialogFragment {

    ImageView signatureIV;
    public ServiceManager serviceManager;
    public UUID transactionId;
    ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_display_signature, container);

        getDialog().setTitle(getActivity().getResources().getString(R.string.title_signature));
        initializeComponents(view);
        showSignature();

        return view;
    }

    //show the signature
    private void showSignature(){

        mProgressBar.setVisibility(View.VISIBLE);
        
        serviceManager.getSignature(transactionId, new GetSignatureCallback() {

            @Override
            public void onSuccess(Bitmap bitmap) {
                if(null == getActivity()) return;

                mProgressBar.setVisibility(View.GONE);

                if (bitmap != null) {

                    signatureIV.setImageBitmap(bitmap);
                } else {
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.bitmap_null));
                }
            }

            @Override
            public void onError(SpException exception) {
                if(null == getActivity()) return;
                if (exception instanceof SpSessionException) {
                    mProgressBar.setVisibility(View.GONE);
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    UIHelper.showErrorDialog(getActivity(), getResources().getString(R.string.app_name), exception.getMessage());

                }
            }
        });
    }

    private void initializeComponents(View view){

        signatureIV = (ImageView) view.findViewById(R.id.signatureIV);
        mProgressBar = (ProgressBar) view.findViewById(R.id.signaturePB);

    }

}