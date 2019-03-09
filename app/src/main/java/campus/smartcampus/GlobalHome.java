package campus.smartcampus;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

public class GlobalHome extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    Fragment currentFragment;
    FragmentLifeCycle fragmentToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.global_activity);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.menuHome);
        viewPager = (ViewPager) findViewById(R.id.pagerHome);

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        setUpViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                fragmentToShow = (FragmentLifeCycle) viewPagerAdapter.getItem(i);
                currentFragment = (Fragment) viewPagerAdapter.getItem(i);
                fragmentToShow.onResumeFragment();

                switch (i) {
                    case 0:

                        bottomNavigationView.findViewById(R.id.home).performClick();
                        break;
                    case 1:

                        bottomNavigationView.findViewById(R.id.feed).performClick();
                        break;
                    case 2:

                        bottomNavigationView.findViewById(R.id.profile).performClick();
                        break;
                    case 3:

                        bottomNavigationView.findViewById(R.id.batch).performClick();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()){

                case R.id.home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.feed:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.profile:
                    viewPager.setCurrentItem(3);
                    return true;
                case R.id.batch:
                    viewPager.setCurrentItem(4);
                    return true;
            }

            return false;
        }
    };

    public interface FragmentLifeCycle {
        public void onResumeFragment();
    }

    private void setUpViewPager(ViewPager viewPager){

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // declare fragments
        Feed feed = new Feed();
        Home home = new Home();
        Profile profile = new Profile();
        Batch batch = new Batch();

        // add fragments
        viewPagerAdapter.addFragment(home, "Home");
        viewPagerAdapter.addFragment(feed, "Feed");
        viewPagerAdapter.addFragment(profile, "Profile");
        viewPagerAdapter.addFragment(batch, "Batch");

        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.notifyDataSetChanged();

        viewPager.setOffscreenPageLimit(2);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public CharSequence getTitle(int position) {

            return mFragmentTitleList.get(position);
        }
    }

}
