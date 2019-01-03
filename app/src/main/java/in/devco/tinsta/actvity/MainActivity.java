package in.devco.tinsta.actvity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.devco.tinsta.R;
import in.devco.tinsta.fragment.FragmentTabsGallery;
import in.devco.tinsta.fragment.FragmentTimeline;
import in.devco.tinsta.lib.Tools;
import in.devco.tinsta.lib.User;

//TODO: Add navigation menu
//TODO: Add search button to navigation bar

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.grey_60), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Tinsta");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
    }

    private void initComponent() {
        ViewPager view_pager = findViewById(R.id.view_pager);
        TabLayout tab_layout = findViewById(R.id.tab_layout);
        setupViewPager(view_pager);

        tab_layout.setupWithViewPager(view_pager);

        Objects.requireNonNull(tab_layout.getTabAt(0)).setIcon(R.drawable.ic_home);
        Objects.requireNonNull(tab_layout.getTabAt(1)).setIcon(R.drawable.ic_explore);
        Objects.requireNonNull(tab_layout.getTabAt(2)).setIcon(R.drawable.ic_chat);
        Objects.requireNonNull(tab_layout.getTabAt(3)).setIcon(R.drawable.ic_notifications);
        Objects.requireNonNull(tab_layout.getTabAt(4)).setIcon(R.drawable.ic_person);

        // set icon color pre-selected
        Objects.requireNonNull(Objects.requireNonNull(tab_layout.getTabAt(0)).getIcon()).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.deep_orange_500), PorterDuff.Mode.SRC_IN);
        for (int i = 0; i < 5; i++) {
            Objects.requireNonNull(Objects.requireNonNull(tab_layout.getTabAt(i)).getIcon()).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.grey_60), PorterDuff.Mode.SRC_IN);
        }

        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Objects.requireNonNull(getSupportActionBar()).setTitle(viewPagerAdapter.getTitle(tab.getPosition()));
                Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.deep_orange_500), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.grey_60), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        Bundle user = new Bundle();
        Object[] tmp = User.sessionDetails(MainActivity.this);
        user.putInt("userId", (Integer) tmp[0]);

        viewPagerAdapter.addFragment(FragmentTimeline.newInstance(user), "Tinsta");    // index 0
        viewPagerAdapter.addFragment(FragmentTabsGallery.newInstance(), "Explorer");   // index 1
        viewPagerAdapter.addFragment(FragmentTabsGallery.newInstance(), "Messages");    // index 2
        viewPagerAdapter.addFragment(FragmentTabsGallery.newInstance(), "Notifications");    // index 3
        viewPagerAdapter.addFragment(FragmentTabsGallery.newInstance(), "Profile");    // index 3
        viewPager.setAdapter(viewPagerAdapter);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        String getTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}