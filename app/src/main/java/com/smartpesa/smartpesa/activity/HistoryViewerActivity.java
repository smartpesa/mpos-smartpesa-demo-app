package com.smartpesa.smartpesa.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.adapters.HistoryFragmentPagerAdapter;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryViewerActivity extends BaseActivity {

    public static final String INDEX = HistoryViewerActivity.class.getName() + ".index";
    public static final String DATA = HistoryViewerActivity.class.getName() + ".data";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.toolbar_title) TextView toolbarTitle;
    private HistoryFragmentPagerAdapter mCustomPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_viewer);
        ButterKnife.bind(this);

        //setup toolbar
        setSupportActionBar(mToolbar);
        setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final ArrayList<ParcelableTransactionResponse> data = getIntent().getParcelableArrayListExtra(DATA);
        int index = getIntent().getIntExtra(INDEX, 0);

        //declare the custom adapter
        mCustomPagerAdapter = new HistoryFragmentPagerAdapter(getSupportFragmentManager(), data);

        //assign the CustomAdapter to the ViewPager
        mViewPager.setAdapter(mCustomPagerAdapter);

        //set the current item of the ViewPager to the row selected from the ListView
        mViewPager.setCurrentItem(index);

        //set title for the first element
        setToolBarTitle(data.get(mViewPager.getCurrentItem()).getTransactionDescription());

        //set title when the viewpager is changed
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setToolBarTitle(data.get(position).getTransactionDescription());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setToolBarTitle(String title) {
        toolbarTitle.setText(title);
    }

}
