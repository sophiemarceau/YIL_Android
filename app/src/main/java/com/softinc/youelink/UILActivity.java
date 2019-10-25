package com.softinc.youelink;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.softinc.youelink.R;

/**
 * Created by zhangbing on 15-1-24.
 */
public class UILActivity extends FragmentActivity {
    // ===============================================================================
    // UI
    // ===============================================================================
    private ViewPager pager;
    private Fragment listFragment;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uil);

        initView();
    }

    private void initView() {
        pager = (ViewPager) findViewById(R.id.pager);
//        pager.setAdapter();
    }

    /**
     * 使用Fragment填充Pager
     */
    private class MPagerAdapter extends FragmentPagerAdapter {

        public MPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}
