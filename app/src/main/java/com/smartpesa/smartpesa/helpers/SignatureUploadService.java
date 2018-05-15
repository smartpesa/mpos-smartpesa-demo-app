package com.smartpesa.smartpesa.helpers;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;

import java.util.UUID;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.transaction.UploadSignatureCallback;

public class SignatureUploadService extends Service {

    Lazy<ServiceManager> serviceManager;
    Bitmap sign;
    String transactionID;
    Context mContext;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        mContext = SignatureUploadService.this;
        serviceManager = SmartPesaApplication.component(mContext).serviceManager();

        sign = (Bitmap) intent.getParcelableExtra("Signature");
        transactionID = intent.getStringExtra("transactionID");

        sendSignature();

        return START_STICKY;
    }

    private void sendSignature() {
        serviceManager.get().uploadSignature(UUID.fromString(transactionID), sign, new UploadSignatureCallback() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (null == mContext) return;
                UIHelper.showToast(mContext, mContext.getResources().getString(R.string.signature_success));
                stopSelf();
            }

            @Override
            public void onError(SpException exception) {
                if (null == mContext) return;
                if (exception instanceof SpSessionException) {
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.signature_failed));
                } else {
                    UIHelper.showToast(mContext, exception.getMessage());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
