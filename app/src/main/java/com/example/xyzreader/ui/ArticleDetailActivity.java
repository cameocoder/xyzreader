package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleDetailActivity.class.getSimpleName();

    public static String START_ARTICLE_POSITION = "start_article_position";
    public static String SELECTED_ARTICLE_POSITION = "selected_article_position";

    private Cursor mCursor;
    private long mStartId;

    private long selectedItemId;
    private int startingPosition;
    private int currentPosition;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private ArticleDetailFragment fragment;

    private final SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        @TargetApi(21)
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (fragment != null) {
                ImageView sharedElement = fragment.getPhotoView();
                if (sharedElement == null) {
                    names.clear();
                    sharedElements.clear();
                } else if (mStartId != currentPosition) {
                    names.clear();
                    names.add(sharedElement.getTransitionName());
                    sharedElements.clear();
                    sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.postponeEnterTransition(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setCurrentItem(currentPosition);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    selectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                    currentPosition = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                selectedItemId = mStartId;
                startingPosition = getIntent().getIntExtra(SELECTED_ARTICLE_POSITION, 0);
                currentPosition = getIntent().getIntExtra(SELECTED_ARTICLE_POSITION, 0);
            }
        } else {
            startingPosition = savedInstanceState.getInt(START_ARTICLE_POSITION);
            currentPosition = savedInstanceState.getInt(SELECTED_ARTICLE_POSITION);
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            setEnterSharedElementCallback(sharedElementCallback);
//        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(START_ARTICLE_POSITION, startingPosition);
        outState.putInt(SELECTED_ARTICLE_POSITION, currentPosition);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
            }
                mCursor.moveToNext();
    }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            fragment = (ArticleDetailFragment) object;
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            final long id = mCursor.getLong(ArticleLoader.Query._ID);
            return ArticleDetailFragment.newInstance(id);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
