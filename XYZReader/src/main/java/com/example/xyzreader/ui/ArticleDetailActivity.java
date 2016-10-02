package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.util.ViewUtils;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener, ArticleDetailFragment.Callbacks {

    private static final String TAG = ArticleDetailActivity.class.getSimpleName();

    public static final String EXCLUDE_TARGET_NAME_EXTRA = TAG + ".EXCLUDE_TARGET_NAME_EXTRA";

    private Cursor mCursor;
    private long mStartId;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private FloatingActionButton mFab;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (isLollipopWithAnimTarget()) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            final Transition transition = new Slide();
            transition.excludeTarget(getExcludeTargetNameExtra(), true);
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
        }

        getWindow().getDecorView().setSystemUiVisibility(
                ViewUtils.getSystemUiVisibilityFlags(getWindow())
        );

        ActivityCompat.postponeEnterTransition(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);

        getSupportLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }

    }

    private String getExcludeTargetNameExtra() {
        return getIntent().getStringExtra(EXCLUDE_TARGET_NAME_EXTRA);
    }

    private boolean isLollipopWithAnimTarget() {
        final boolean isLollipop = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        final boolean hasAnimTarget = (getExcludeTargetNameExtra() != null);
        return (isLollipop && hasAnimTarget);
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

    @Override
    public void onClick(View view) {
        if (mCursor.moveToPosition(mPager.getCurrentItem())) {
            Intent shareTextIntent = new Intent(Intent.ACTION_SEND);
            final String text =
                    mCursor.getString(mCursor.getColumnIndex(ItemsContract.ItemsColumns.TITLE));
            shareTextIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareTextIntent.setType("text/plain");
            startActivity(shareTextIntent);
        }
    }

    @Override
    public void onScrollChange(int y, int oldY) {
        if (oldY > y && !mFab.isShown()) {
            mFab.show();
        } else if (y > oldY && mFab.isShown()) {
            mFab.hide();
        }
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), position);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

    }
}
