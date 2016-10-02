package com.example.xyzreader.ui;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.TextUtils;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, NestedScrollView.OnScrollChangeListener {
    private static final String TAG = "ArticleDetailFragment";

    public static final String POSITION_KEY = "pos_key";

    public static final String ARG_ITEM_ID = "item_id";

    private Callbacks mCallbacks;

    private ImageView mPhoto;
    private View mMetaBar;

    private Cursor mCursor;
    private int mDarkVibrantColor = 0xFF333333;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int position) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(POSITION_KEY, position);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement "
                    + Callbacks.class.getSimpleName());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mPhoto = (ImageView) rootView.findViewById(R.id.photo);
        ViewCompat.setTransitionName(mPhoto, getString(R.string.transition_template_image, getPosition()));

        mMetaBar = rootView.findViewById(R.id.meta_bar);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.fad_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        NestedScrollView body =
                (NestedScrollView) rootView.findViewById(R.id.fad_nested_scrollview);
        body.setOnScrollChangeListener(this);

        AppBarLayout appBar = (AppBarLayout) rootView.findViewById(R.id.fad_appbar_layout);
        ViewCompat.setTransitionName(appBar, getString(R.string.transition_template_text, getPosition()));

        bindViews(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        mCallbacks = null;
        super.onDetach();
    }

    private void bindViews(final View rootView) {
        if (rootView == null) {
            return;
        }

        TextView titleView = (TextView) rootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) rootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) rootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            rootView.setAlpha(0);
            rootView.setVisibility(View.VISIBLE);
            rootView.animate().alpha(1);

            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(TextUtils.getByLineText(getResources(), mCursor, true));
            bodyView.setText(TextUtils.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(final ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                new Palette.Builder(bitmap)
                                        .maximumColorCount(12)
                                        .generate(new PaletteListener(bitmap));
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            if (getActivity() != null) {
                                ActivityCompat.startPostponedEnterTransition(getActivity());
                            }
                        }
                    });
        } else {
            rootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), getItemId());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews(getView());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews(getView());
    }

    private long getItemId() {
        return getArguments().getLong(ARG_ITEM_ID);
    }

    private int getPosition() {
        return getArguments().getInt(POSITION_KEY);
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        mCallbacks.onScrollChange(scrollY, oldScrollY);
    }

    interface Callbacks {
        void onScrollChange(int y, int oldY);
    }

    private class PaletteListener implements Palette.PaletteAsyncListener {

        private final Bitmap bitmap;

        private PaletteListener(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void onGenerated(Palette palette) {
            mDarkVibrantColor = palette.getDarkVibrantColor(mDarkVibrantColor);
            mPhoto.setImageBitmap(bitmap);
            mMetaBar.setBackgroundColor(mDarkVibrantColor);
            if (getActivity() != null) {
                ActivityCompat.startPostponedEnterTransition(getActivity());
            }
        }
    }

}
