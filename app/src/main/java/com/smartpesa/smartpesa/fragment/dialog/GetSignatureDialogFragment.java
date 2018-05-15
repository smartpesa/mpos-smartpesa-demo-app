package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.helpers.SignatureUploadService;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.transaction.UploadSignatureCallback;

public class GetSignatureDialogFragment extends BaseDialogFragment implements View.OnClickListener {

    public static final String KEY_CARD_HOLDER_NAME = GetSignatureDialogFragment.class.getName() + ".cardHolderName";
    public static final String KEY_TRANSACTION_ID = GetSignatureDialogFragment.class.getName() + ".transactionID";
    LinearLayout mContent;
    signature mSignature;
    Button mClear, mGetSign;
    private Bitmap mBitmap;
    View mView;
    Context mContext;
    TextView nameTV;
    ProgressBar mProgressBar;

    public ServiceManager serviceManager;
    String mCardHolderName;
    String mTransactionId;

    public static GetSignatureDialogFragment newInstance(UUID transactionId, String cardHolderName) {
        GetSignatureDialogFragment getSignatureDialogFragment = new GetSignatureDialogFragment();
        Bundle b = new Bundle();
        b.putString(KEY_TRANSACTION_ID, transactionId.toString());
        b.putString(KEY_CARD_HOLDER_NAME, cardHolderName);
        getSignatureDialogFragment.setArguments(b);
        return getSignatureDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager().get();
        mTransactionId = getArguments().getString(KEY_TRANSACTION_ID, "");
        mCardHolderName = getArguments().getString(KEY_CARD_HOLDER_NAME, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_get_signature, container);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mContext = getActivity();
        initializeComponents(view);

        if (mCardHolderName != null) {
            nameTV.setText(mCardHolderName);
        }

        return view;
    }

    //listen to on click events
    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.clear:
                mSignature.clear();
                mGetSign.setEnabled(false);
                break;
            case R.id.getsign:
                boolean error = captureSignature();
                if(!error){
                    //save Signature if no error
                    saveSignature();
                }
        }
    }

    //initialize the UI components
    private void initializeComponents(View view){

        mContent = (LinearLayout) view.findViewById(R.id.signView);
        mSignature = new signature(getActivity(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mClear = (Button) view.findViewById(R.id.clear);
        mGetSign = (Button) view.findViewById(R.id.getsign);

        nameTV = (TextView) view.findViewById(R.id.sign_NameTV);

        mProgressBar = (ProgressBar) view.findViewById(R.id.getSignPB);

        mGetSign.setEnabled(false);
        mView = mContent;

        mClear.setOnClickListener(this);

        mGetSign.setOnClickListener(this);

    }

    //saves the signature
    private void saveSignature(){
        mSignature.save(mView);
    }

    //to change the receipt fragment when we are dismissing the fragment
    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    private boolean captureSignature() {
        boolean error = false;
        String errorMessage = "";

        if(error){
            Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 105, 50);
            toast.show();
        }

        return error;
    }

    public class signature extends View
    {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v)
        {
            v.setDrawingCacheEnabled(true);
            v.buildDrawingCache();
            mBitmap =  v.getDrawingCache();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            mBitmap.compress(Bitmap.CompressFormat.JPEG, 0, out);

            Bitmap resized = Bitmap.createScaledBitmap(mBitmap,400,200,true);

            Intent serviceIntent = new Intent(mContext,SignatureUploadService.class);
            serviceIntent.putExtra("Signature",resized);
            serviceIntent.putExtra("transactionID", mTransactionId);
            mContext.startService(serviceIntent);
            getDialog().dismiss();

            serviceManager.uploadSignature(UUID.fromString(mTransactionId), resized, new UploadSignatureCallback() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (null == getActivity()) return;

                    mProgressBar.setVisibility(GONE);
                }

                @Override
                public void onError(SpException exception) {
                    if(null == getActivity()) return;

                    mProgressBar.setVisibility(GONE);
                    UIHelper.showErrorDialog(getActivity(), getString(R.string.app_name), exception.getMessage());
                }
            });
        }


        public void clear()
        {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++)
                    {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:

                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

}