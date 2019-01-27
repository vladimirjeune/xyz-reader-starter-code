package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A Fragment representing a single Article Detail Screen. Either contained in an
 * {@link ArticleListActivity} in 2-pane mode (on tablets) or a
 * {@link ArticleDetailActivity} on handsets
 */
public class ArticleDetailCoordinatorFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleDetailCoordinatorFragment.class.getName();

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333; // TODO: Use brighter color
    private ObservableScrollView mScrollView; // TODO: May nee to use another one
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout; // TODO: May not be necessary
    private ColorDrawable mStatusBarColorDrawable; // TODO: May not be needed, we'll see

//    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
//    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    private TextView mBookTitleView;
    private TextView mBylineView;
    private LinearLayout mLinearLayoutTitlesView;
    private Toolbar mToolbar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailCoordinatorFragment() {
    }


    public static ArticleDetailCoordinatorFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailCoordinatorFragment fragment = new ArticleDetailCoordinatorFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);

    }


    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }


    @Override
    public void onActivityCreated(Bundle onSavedInstanceState) {
        super.onActivityCreated(onSavedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail_coordinator_layout, container, false);

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mRootView.findViewById(R.id.share_fab_coord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();

        ViewCompat.requestApplyInsets(container);  // Trying to call onApply, may not need.


        return mRootView;

    }


    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {

        if (null == mRootView) {
            return;
        }

        // Title should be set on CollapsingToolbarLayout
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                mRootView.findViewById(R.id.collapsing_toolbar);
        final Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                int toolbarHeight = toolbar.getHeight();
                Log.d(TAG, "run() called. Toolbar height is: " + toolbarHeight);
            }
        });  // TODO: Does nothing can remove

//         TODo: Put this in to see if can get anything dispatched
        final CoordinatorLayout coordinatorLayout = mRootView.findViewById(R.id.coordinator_layout_detail);
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                return windowInsetsCompat;
            }
        });




        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        mLinearLayoutTitlesView = (LinearLayout) mRootView.findViewById(R.id.meta_bar);


        // Guaranteed to run after the view has been laid out, at least once. :
        mLinearLayoutTitlesView.post(new Runnable() {
            @Override
            public void run() {
                int metabarMinHeight = mLinearLayoutTitlesView.getMinimumHeight();
                int metabarHeight = mLinearLayoutTitlesView.getHeight();
//                Log.d(TAG, "run() called. Metabar [Min|Normal] height: [" + metabarMinHeight + "|"
//                + metabarHeight +"]");

                if (metabarHeight != 0) {
//                    Log.i(TAG, "run: curr Height before: " + toolbar.getHeight());

                    toolbar.getLayoutParams().height = metabarHeight + 25 + 16;

                    toolbar.invalidate();
                    toolbar.requestLayout();
//                    Log.i(TAG, "run: new Height after: " + toolbar.getHeight());
                }
            }
        });

        // Set the margin for the top the way Chris Banes wants you to.
        // https://www.youtube.com/watch?v=_mGDMVRO3iE&t=
        // TODO:

        ViewCompat.setOnApplyWindowInsetsListener(mLinearLayoutTitlesView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                Log.d(TAG, "onApplyWindowInsets() called with: view = [" + view + "], windowInsetsCompat = [" + windowInsetsCompat + "]");

                // TODO: Remove when you can get this thing to be called.
                //                int statusBarDisplacement = windowInsetsCompat.getSystemWindowInsetTop();
//                int metabarHeight = mLinearLayoutTitlesView.getHeight();
//
//                if (metabarHeight != 0) {
//                    toolbar.getLayoutParams().height = metabarHeight;
//                    toolbar.invalidate();
//                    toolbar.requestLayout();
//                }
//
//
//                ViewGroup.MarginLayoutParams marginLayoutParams
//                        = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//                marginLayoutParams.topMargin = statusBarDisplacement;
//
//                mLinearLayoutTitlesView.invalidate();
//                mLinearLayoutTitlesView.requestLayout();


                return windowInsetsCompat.consumeSystemWindowInsets();

            }
        });


        mBookTitleView = (TextView) mRootView.findViewById(R.id.article_title);
        mBylineView = (TextView) mRootView.findViewById(R.id.article_byline);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            collapsingToolbarLayout.setTitleEnabled(false); // TODO: RM
//            collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            mBookTitleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            // TODO: Get size for Sub/Title to find min value of Toolbar when collapsed.

            Date publishedDate = parsePublishedDate();
            Spanned byline ;
            if (! publishedDate.before(START_OF_EPOCH.getTime())) {
                byline = Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(),
                                DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"
                );

            } else {
                // If date is after 1902, just show the string. Might be before, check.
                byline = Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>");
            }
//            toolbar.setSubtitle(byline);
            mBylineView.setText(byline);

            // TODO: Find height of metabar
            // TODO: Make the min height of the Toolbar
            // TODO: Cannot look for height in onCreate. So will be done in Runnable:
            // https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0
            // This part:: "2. Add a runnable to the layout queue: View.post()"
//            int endToolbarSizeInPixels = mLinearLayoutTitlesView.getHeight();
//            Log.i(TAG, "bindViews: endToolbarSizeInPixels:" + endToolbarSizeInPixels);
//            mToolbar.setMinimumHeight(endToolbarSizeInPixels);





            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)
                    .replaceAll("(\r\n|\n)", "<br />")));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();

                            if (bitmap != null) {

//                                Palette.Builder paletteBuilder = getBuilderWithWhiteTextBGFilter(bitmap);
//                                setCollapsibleScrimColor(paletteBuilder);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());

                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }

                        /**
                         * SETCOLLAPSIBLESCRIMCOLOR - Use PaletteBuilder to create an appropriate background color
                         * for the scrim of the CollapsibleLayout.
                         */
                        private void setCollapsibleScrimColor(Palette.Builder paletteBuilder) {

                            paletteBuilder.generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@Nullable Palette palette) {

                                    final int blackColor = 0;  // Actual color may have been set to grey
                                    final int vibrantColor = palette.getVibrantColor(blackColor);
                                    final int darkVibrantColor = palette.getDarkVibrantColor(blackColor);
                                    final int darkMutedColor = palette.getDarkMutedColor(blackColor);
                                    final int mutedColor = palette.getMutedColor(blackColor);
                                    final int lightMutedColor = palette.getLightMutedColor(blackColor);
                                    final int lightVibrantColor = palette.getLightVibrantColor(blackColor);

                                    if (vibrantColor != blackColor) {
                                        collapsingToolbarLayout.setContentScrimColor(vibrantColor);
                                    } else if (darkVibrantColor != blackColor) {
                                        collapsingToolbarLayout.setContentScrimColor(darkVibrantColor);
                                    } else if (darkMutedColor != blackColor) {
                                        collapsingToolbarLayout.setContentScrimColor(darkMutedColor);
                                    } else if (mutedColor != blackColor) {
                                        collapsingToolbarLayout.setContentScrimColor(mutedColor);
                                    } else if (lightMutedColor != blackColor) {
                                        collapsingToolbarLayout.setContentScrimColor(lightMutedColor);
                                    } else if (lightVibrantColor != blackColor) {
                                        collapsingToolbarLayout.setContentScrimColor(lightVibrantColor);
                                    }
                                }
                            });
                        }


                        /**
                         * GETBUILDERWITHWHITETEXTBGFILTER - Returns a Builder that has filtered out the Palettes
                         * that would make white text placed on top of it look bad.
                         * @param bitmap - The Bitmap we are looking at to get the appropriate background color
                         * @return - Palette.Builder - Palettes that would make good white text backgrounds
                         * based on Bitmap.
                         */
                        @NonNull
                        private Palette.Builder getBuilderWithWhiteTextBGFilter(Bitmap bitmap) {
                            return new Palette.Builder(bitmap)
                                    .addFilter(new Palette.Filter() {
                                        @Override
                                        public boolean isAllowed(int rgb, float[] hsl) {
                                            // From: https://stackoverflow.com/questions/3942878/
                                            // how-to-decide-font-color-in-white-or-black-depending-on-background-color
                                            float contrastFormulaBlackWhiteText = 0.179f;
                                            int luminanceIndex = 2;
                                            float luminance = hsl[luminanceIndex];

                                            return luminance <= contrastFormulaBlackWhiteText;  // Good BG for White Text
                                        }
                                    });
                        }

                    });
        } else {
            mRootView.setVisibility(View.GONE);
            collapsingToolbarLayout.setTitle("N/A");
//            toolbar.setSubtitle("No/A");
            bodyView.setText("N/A");
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (! isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if ((mCursor != null) && (! mCursor.moveToFirst())){
            Log.e(TAG, "onLoadFinished: Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursor = null;
        bindViews();
    }
}
