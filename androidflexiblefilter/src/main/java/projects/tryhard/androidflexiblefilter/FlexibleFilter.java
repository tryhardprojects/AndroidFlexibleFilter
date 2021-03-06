package projects.tryhard.androidflexiblefilter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for filter, set up like this. For more detail and samples, go to {@see <a hreh="https://github.com/you55645/Android-Flex-Filter" >FlexibleFilter</a>}.
 * 1. If you use xml to set attributes or you want to use default values, you can use the {@link #init(Context, int, int, Object, FilterCallback)}. If you want to do the init settings through code, use {@link #init(Context, int, int, Object, int, boolean, boolean, Orientation, int, boolean, boolean, FilterCallback)}.
 * --- Basic set up done, you can open up to see how it looks like.
 * 2. After init, you will have at least one filter, you can add filter later also, but if you only planning on using one. You can start to add options. (step 4).
 * 3. If you want to add more filters, use {@link #addFilter(int, Object, int)}.
 * 4. Adding options to filter you want to add by {@link #addFilterOption(int, Object, int, int, OptionGetStringCallback)} or {@link #addFilterOption(int, Object, int, int, int, int, int, OptionGetStringCallback)}.
 * 5. If you want to update certain option from certain filter, use {@link #updateCertainOption(FilterHolder, Object, int)}. If you want to update all, use {@link #updateAllFilters()}.
 * --- Set up done.
 *
 * @param <T> A class for you to decide the unique ID of every option, which will be passed when user clicked an option or call {@link #optionSelect(FilterHolder, Object)} through code.
 */
public class FlexibleFilter<T> extends LinearLayout {
    /**
     * A Enum used to decide the mOrientation of the whole Filter Layout
     */
    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    /**
     * Default color of the text color, can be changed.
     */
    public static @ColorRes
    int mUnSelectedTextColor = R.color.black;
    public static @ColorRes
    int mSelectedTextColor = R.color.white;

    /**
     * Default background of the filter option background, can be changed.
     */
    public static @DrawableRes
    int mSelectedBackground = R.drawable.filter_selector_default_selected;
    public static @DrawableRes
    int mUnSelectedBackground = R.drawable.filter_selector_default_not_selected;

    private Context mContext;

    private List<FilterHolder> mFilters;
    private FilterCallback mFilterCallback;
    private List<Integer> mCurrentOpeningFilters;

    private SettingCallback mSettingCallback = new SettingCallback() {
        @Override
        public FlexibleFilter getFlexibleFilter() {
            return FlexibleFilter.this;
        }
    };

    /**
     * views used.
     */
    private ScrollView mVerticalScrollView;
    private HorizontalScrollView mHorizontalScrollView;
    private LinearLayout mTitleContainer;
    private FlexboxLayout mFilterContainer;
    /**
     * Will return this view in callback so you can do things with it.
     */
    private View mTitleView;


    private int mDefaultEmptyViewLayout;
    /**
     * If this flag set to true, will hide all the options that is 0.
     */
    private boolean mShouldHideZeroFilters = false;
    /**
     * If this flag set to true, will hide the default added all option.
     */
    private boolean mShouldHideAll = false;
    /**
     * A variable decide how many filters we show at one row. Default is one.  -2 means wrap_content. -1 means match_parent, but it's seems only works on vertical orientation.
     */
    private int mFilterColCount = 1;
    /**
     * A variable to check where we are in horizontal or vertical mode.
     */
    private Orientation mOrientation = Orientation.VERTICAL;
    /**
     * A variable to decide whether to close the dropdown or not after click on an option.
     */
    private boolean mShouldCloseAfterClick = true;

    /**
     * A variable to decide when option select, change its' color or not.
     */
    private boolean mShouldChangeColorWhenSelect = true;

    /**
     * Callback for filter. Needed when you add a new filter
     */
    public interface FilterCallback {
        /**
         * Decide the text on default all option.
         *
         * @param filterNum The filterNum for this sub filter.
         * @param count     the sum of all the option counts.
         * @return A string that will be display on the option.
         */
        String defaultAllOptionString(int filterNum, int count);

        /**
         * Get called when is no FilterNum match that.
         *
         * @param notExistFilterNum The number of filter.
         */
        void noSuchFilterError(int notExistFilterNum);

        /**
         * Get called when you select an non-exist option.
         */
        void filterOptionNotExistError();

        /**
         * Get called when you give the wrong optionId type.
         */
        void castFailed();
    }

    /**
     * Callback when option being pressed or called {@link #optionSelect(FilterHolder, Object)}
     *
     * @param <T> T should be unique so you can used it to find out which option user select.
     */
    public interface OptionClickCallback<T> {
        /**
         * Call when option being pressed or called @link #optionSelect(int mFilterNum, T optionId)
         *
         * @param filterNum The filterNum of the clicked filter.
         * @param optionId  Option unique value.
         */
        void filterOptionClicked(int filterNum, T optionId);

        /**
         * Get called when you unSelect all option in a filter by calling {@link #optionSelect(FilterHolder, Object)} with a null parameter on optionId.
         *
         * @param filterNum The filterNum of the filter unselect all..
         */
        void filterUnSelectedAll(int filterNum);

    }

    /**
     * A callback for you to decide the text on option.
     */
    public interface OptionGetStringCallback<S> {
        /**
         * Decide the text on the option.
         *
         * @param optionId The optionId you set for this option.
         * @param count    The count of this very option.
         * @return A string that will be display on the option.
         */
        String getString(S optionId, int count);
    }

    /**
     * For setting purpose.
     */
    public interface SettingCallback {
        FlexibleFilter getFlexibleFilter();
    }

    //region View constructors.
    public FlexibleFilter(Context context) {
        super(context);
    }

    public FlexibleFilter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FlexibleFilter);

        mFilterColCount = attributes.getInteger(R.styleable.FlexibleFilter_colCount, 1);
        mShouldHideAll = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideAll, false);
        mShouldHideZeroFilters = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideZeroOptions, false);
        mShouldCloseAfterClick = attributes.getBoolean(R.styleable.FlexibleFilter_shouldCloseAfterClick, true);
        mShouldChangeColorWhenSelect = attributes.getBoolean(R.styleable.FlexibleFilter_shouldChangeColorWhenSelect, true);

        if (attributes.getInt(R.styleable.FlexibleFilter_orientation, 0) == 0) {
            mOrientation = Orientation.VERTICAL;
        } else {
            mOrientation = Orientation.HORIZONTAL;
        }

        attributes.recycle();
    }

    public FlexibleFilter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.FlexibleFilter, defStyleAttr, 0);

        mFilterColCount = attributes.getInteger(R.styleable.FlexibleFilter_colCount, 1);
        mShouldHideAll = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideAll, false);
        mShouldHideZeroFilters = attributes.getBoolean(R.styleable.FlexibleFilter_shouldHideZeroOptions, false);
        mShouldCloseAfterClick = attributes.getBoolean(R.styleable.FlexibleFilter_shouldCloseAfterClick, true);
        mShouldChangeColorWhenSelect = attributes.getBoolean(R.styleable.FlexibleFilter_shouldChangeColorWhenSelect, true);

        if (attributes.getInt(R.styleable.FlexibleFilter_orientation, 0) == 0) {
            mOrientation = Orientation.VERTICAL;
        } else {
            mOrientation = Orientation.HORIZONTAL;
        }

        attributes.recycle();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlexibleFilter(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //endregion

    //region Init.

    /**
     * Use to init the whole filter with more detail.
     *
     * @param context            We use to inflate layouts.
     * @param filterNum          A number for default filter, use it when you want to update, show or hide certain filter.
     * @param titleLayout        The title you want for the filter, -1 means no title, 0 means default title.
     * @param allT               A unique ID for the default all option.
     * @param emptyDefaultLayout A default Empty Layout, -1 means use default.
     * @param hideAll            Should we hide the default all option?
     * @param hideZeroOptions    Should we hide the options that is 0?
     * @param orientation        The mOrientation of the filters' layout.
     * @param colCount           How many filters we show at one row. Default is one.
     * @param filterCallback     Callbacks when error occurs.
     */
    public void init(Context context, int filterNum, @LayoutRes int titleLayout, final T allT, @LayoutRes int emptyDefaultLayout,
                     boolean hideAll, boolean hideZeroOptions, Orientation orientation, int colCount, boolean shouldCloseAfterClick,
                     boolean changeColorWhenSelect, FilterCallback filterCallback) {
        init(context, filterNum, titleLayout, allT, filterCallback);

        setShouldHideZeroOptions(hideZeroOptions);
        setShouldHideAll(hideAll);
        setFilterOrientation(orientation);
        setFilterColCount(colCount);
        setShouldCloseAfterClick(shouldCloseAfterClick);
        setShouldChangeColorWhenSelect(changeColorWhenSelect);

        if (emptyDefaultLayout != -1) {
            mDefaultEmptyViewLayout = emptyDefaultLayout;
        } else {
            mDefaultEmptyViewLayout = R.layout.filter_default_empty_view;
        }

    }

    /**
     * Use to init the whole filter with default.
     *
     * @param context        We use to inflate layouts.
     * @param filterNum      A number for default filter, use it when you want to update, show or hide certain filter.
     * @param titleLayout    The title you want for the filter, -1 means no title, 0 means default title.
     * @param allT           A unique ID for the default all option.
     * @param filterCallback Callbacks when error occurs.
     */
    public void init(Context context, int filterNum, @LayoutRes int titleLayout, final T allT, FilterCallback filterCallback) {
        inflate(getContext(), R.layout.filter_layout, this);
        mFilterCallback = filterCallback;
        mContext = context;
        mTitleContainer = findViewById(R.id.filter_title_container);
        mFilterContainer = findViewById(R.id.filter_filter_container);
        mHorizontalScrollView = findViewById(R.id.filter_horizontalScrollView);
        mVerticalScrollView = findViewById(R.id.filter_verticalScrollView);

        mDefaultEmptyViewLayout = R.layout.filter_default_empty_view;

        mCurrentOpeningFilters = new ArrayList<>();

        if (titleLayout != -1) {
            if (titleLayout == 0) {
                mTitleView = LayoutInflater.from(context).inflate(R.layout.filter_default_title, null);
                mTitleView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isCurrentOpen()) {
                            open();
                        } else {
                            close();
                        }
                    }
                });
            } else {
                mTitleView = LayoutInflater.from(context).inflate(titleLayout, null);
            }
            mTitleContainer.addView(mTitleView);
        }

        mFilters = new ArrayList<>();

        addFilter(filterNum, allT, -1);

        mCurrentOpeningFilters.add(0);

        setFilterOrientation(mOrientation);

        setOpeningFilters(new ArrayList<>(mCurrentOpeningFilters));
    }
    //endregion

    /**
     * @param filterNum       A number for default filter, use it when you want to update, show or hide certain filter.
     * @param defaultT        A unique ID for the default all option.
     * @param emptyViewLayout A view to display when no option are shown. -1 means default emptyView.
     */
    public <S> void addFilter(final int filterNum, final S defaultT, @LayoutRes int emptyViewLayout) {
        FlexboxLayout flexboxLayout = new FlexboxLayout(mContext);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        flexboxLayout.setLayoutParams(lp);
        flexboxLayout.setFlexDirection(FlexDirection.ROW);
        flexboxLayout.setFlexWrap(FlexWrap.WRAP);
        flexboxLayout.setJustifyContent(JustifyContent.FLEX_START);
        flexboxLayout.setAlignItems(AlignItems.FLEX_START);
        flexboxLayout.setAlignContent(AlignContent.FLEX_START);

        mFilterContainer.addView(flexboxLayout);

        if (emptyViewLayout != -1) {
            View emptyView = LayoutInflater.from(mContext).inflate(emptyViewLayout, null);
            mFilters.add(new FilterHolder<S>(filterNum, new ArrayList<Option<S>>(), flexboxLayout, emptyView, mSettingCallback));
        } else {
            View emptyView = LayoutInflater.from(mContext).inflate(mDefaultEmptyViewLayout, null);
            mFilters.add(new FilterHolder<S>(filterNum, new ArrayList<Option<S>>(), flexboxLayout, emptyView, mSettingCallback));
        }

        addFilterOption(filterNum, defaultT, 0, getScreenWidthPixel(mContext), new OptionGetStringCallback<S>() {
            @Override
            public String getString(S optionId, int count) {
                return mFilterCallback.defaultAllOptionString(filterNum, count);
            }
        });
    }

    /**
     * Update all exist filters, including those are hiding.
     */
    public void updateAllFilters() {
        for (int i = 0; i < mFilters.size(); i++) {
            updateFilter(mFilters.get(i).getFilterNum());
        }
    }

    /**
     * Check if the Dropdown part being opened.
     *
     * @return true if opened.
     */
    public boolean isCurrentOpen() {
        if (getSubFilter(mCurrentOpeningFilters.get(0)) == null) {
            mFilterCallback.noSuchFilterError(0);
            return false;
        }
        return getSubFilter(mCurrentOpeningFilters.get(0)).isContainerVisible();
    }

    /**
     * Just open one filter.
     *
     * @param filterNum The filter number tou want to open.
     */
    public void setOpeningFilterOne(int filterNum) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(filterNum);
        setOpeningFilters(list);
    }

    /**
     * Want to open multiple filters at the same time.
     *
     * @param openFilters The filter numbers tou want to open.
     */
    public void setOpeningFilters(List<Integer> openFilters) {
        if (!isFiltersValid(openFilters)) return;
        if (isCurrentOpen()) {
            hideAllOpeningContainer();
        }

        for (int i = 0; i < openFilters.size(); i++) {
            FilterHolder filterHolder = getSubFilter(openFilters.get(i));
            filterHolder.setContainerVisible(isCurrentOpen());
            if (mFilterColCount > 0) {
                filterHolder.setContainerSize(getScreenWidthPixel(mContext) / mFilterColCount);
            } else if (mFilterColCount == -1) {
                filterHolder.setContainerSize(ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }

        mCurrentOpeningFilters.clear();
        mCurrentOpeningFilters = openFilters;
        updateAllFilters();
    }

    /**
     * Get Opening filter numbers.
     *
     * @return A list contain all the filter numbers opening.
     */
    public List<Integer> getCurrentOpeningFilters() {
        return mCurrentOpeningFilters;
    }

    /**
     * Perform a click on an option by code.
     *
     * @param filterHolder The unique filter number you want to click.
     * @param optionId     The unique filter option ID you want to click. null means unselect all.
     */
    public <S> void optionSelect(FilterHolder<S> filterHolder, @Nullable S optionId) {

        filterHolder.optionClicked(optionId);

        if (mShouldCloseAfterClick) {
            close();
        }

        updateFilter(filterHolder.getFilterNum());
    }

    /**
     * Update a certain filter's certain option count.
     *
     * @param filterHolder The certain filter to update.
     * @param optionId     The certain option ID.
     * @param count        New count.
     * @param <S>          optionId class.
     */
    public <S> void updateCertainOption(FilterHolder<S> filterHolder, S optionId, int count) {
        filterHolder.getOption(optionId).setResultCount(count);
        updateFilter(filterHolder.getFilterNum());
    }

    /**
     * Update a certain filter's certain option count.
     *
     * @param filterNum   filterNum you want to set.
     * @param filterClass optionId class.
     * @param optionId    The certain option ID.
     * @param count       New count.
     * @param <S>         optionId class.
     */
    public <S> void updateCertainOption(int filterNum, Class<S> filterClass, S optionId, int count) {
        getSubFilter(filterNum, filterClass).getOption(optionId).setResultCount(count);
        updateFilter(filterNum);
    }

    /**
     * I don't really remove it, I just hide it up and set a flag to it so it won't be open in any circumstances.
     *
     * @param filterNum The filter number you want to remove.
     */
    public void removeFilter(int filterNum) {
        if (isFiltersValid(filterNum)) {
            getSubFilter(filterNum).removeFilter();
            updateAllFilters();
        }
    }

    /**
     * Add a option to filter with left margin 4dp, right 4dp, up and down 8dp.
     *
     * @param filterNum                The filter number where you want to add an option.
     * @param optionId                 The unique ID you give to the option.
     * @param count                    The count of this option from the beginning.
     * @param width                    The width of this option.
     * @param mOptionGetStringCallback For you to decide the text on the option.
     * @param <S>                      optionId class.
     */
    public <S> void addFilterOption(int filterNum, S optionId, int count, int width, OptionGetStringCallback<S> mOptionGetStringCallback) {
        addFilterOption(filterNum, optionId, count, width, dpToPixels(mContext, 8), dpToPixels(mContext, 4), dpToPixels(mContext, 4), mOptionGetStringCallback);
    }

    /**
     * Add a option to filter.
     *
     * @param filterNum                The filter number where you want to add an option.
     * @param optionId                 The unique ID you give to the option.
     * @param count                    The count of this option from the beginning.
     * @param width                    The width of this option.
     * @param leftMargin               The margin to the left.
     * @param rightMargin              The margin to the right.
     * @param upDownMargin             The margin to the up and down.
     * @param mOptionGetStringCallback For you to decide the text on the option.
     * @param <S>                      optionId class.
     */
    public <S> void addFilterOption(final int filterNum, S optionId, int count, int width, int leftMargin, int rightMargin, int upDownMargin, OptionGetStringCallback<S> mOptionGetStringCallback) {
        if (isFiltersValid(filterNum)) {
            AutofitTextView autofitTextView = getModifiedTextView(width, leftMargin, rightMargin, upDownMargin);
            getSubFilter(filterNum).addNewFilterButton(new Option<>(mContext, optionId, autofitTextView, count, mOptionGetStringCallback, mSettingCallback));

            updateAllFilters();
        }
    }

    /**
     * Open the Dropdown.
     */
    public void open() {
        openAllOpeningFilter();

        mFilterContainer.requestFocus();
    }

    /**
     * Close the dropDown.
     */
    public void close() {
        closeAllOpeningFilter();
    }

    public void setSelectedTextColor(int selectedTextColor) {
        mSelectedTextColor = selectedTextColor;
        updateAllFilters();
    }


    public void setUnSelectedTextColor(int unSelectedTextColor) {
        mUnSelectedTextColor = unSelectedTextColor;
        updateAllFilters();
    }

    public void setSelectedBackground(int selectedBackground) {
        mSelectedBackground = selectedBackground;
        updateAllFilters();
    }

    public void setUnSelectedBackground(int unSelectedBackground) {
        mUnSelectedBackground = unSelectedBackground;
        updateAllFilters();
    }

    public void setFilterColCount(int colCount) {
        mFilterColCount = colCount;
        setOpeningFilters(new ArrayList<>(mCurrentOpeningFilters));
    }

    public void setShouldCloseAfterClick(boolean shouldCloseAfterClick) {
        this.mShouldCloseAfterClick = shouldCloseAfterClick;
    }

    public void setShouldChangeColorWhenSelect(boolean shouldChangeColorWhenSelect) {
        mShouldChangeColorWhenSelect = shouldChangeColorWhenSelect;
    }

    public View getTitleView() {
        return mTitleView;
    }

    private void closeAllOpeningFilter() {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            if (isFiltersValid(mCurrentOpeningFilters.get(i))) {
                if (getSubFilter(mCurrentOpeningFilters.get(i)).getHeight() == 0) {
                    getSubFilter(mCurrentOpeningFilters.get(i)).readyToTakeHeight(true, true);
                } else {
                    closeAnim(getSubFilter(mCurrentOpeningFilters.get(i)).getContainer());
                }
            }
        }
    }

    private FilterHolder getSubFilter(int filterNum) {
        for (int i = 0; i < mFilters.size(); i++) {
            if (mFilters.get(i).getFilterNum() == filterNum) {
                return mFilters.get(i);
            }
        }
        mFilterCallback.noSuchFilterError(filterNum);
        return null;
    }

    public <S> FilterHolder<S> getSubFilter(int filterNum, Class<S> optionIdClass) {
        for (int i = 0; i < mFilters.size(); i++) {
            if (mFilters.get(i).getFilterNum() == filterNum) {
                FilterHolder filterHolder = mFilters.get(i);
                return cast(filterHolder, optionIdClass);
            }
        }
        mFilterCallback.noSuchFilterError(filterNum);
        return null;
    }

    public <S> void setOptionOnClickCallback(int filterNum, Class<S> optionIdClass, OptionClickCallback<S> callback) {
        getSubFilter(filterNum, optionIdClass).setFilterClickCallback(callback);
    }

    /**
     * Clear options from a specific sub filter, and will call unSelectAll callback.
     *
     * @param filterNum     The filterNum to set.
     * @param optionIdClass Your optionId class.
     * @param <S>           optionId class.
     */
    public <S> void clearSubFilter(int filterNum, Class<S> optionIdClass) {
        FilterHolder<S> filterHolder = getSubFilter(filterNum, optionIdClass);
        int optionCount = filterHolder.getOptions().size();
        for (int i = 1; i < optionCount; i++) {
            S s = filterHolder.getOptions().get(1).getOptionId();
            filterHolder.removeOption(s);
        }
        optionSelect(filterHolder, null);
        updateAllFilters();
    }

    public <S> List<S> getSubFilterIds(int filterNum, Class<S> optionIdClass) {
        return getSubFilter(filterNum, optionIdClass).getAllFilterIds();
    }

    public void setFilterOrientation(Orientation orientation) {
        ViewParent viewParent = mFilterContainer.getParent();
        if (viewParent != null) {
            ((ViewGroup) viewParent).removeView(mFilterContainer);
        }
        if (orientation.equals(Orientation.VERTICAL)) {
            mVerticalScrollView.setVisibility(VISIBLE);
            mHorizontalScrollView.setVisibility(GONE);
            mVerticalScrollView.addView(mFilterContainer);
        } else {
            mVerticalScrollView.setVisibility(GONE);
            mHorizontalScrollView.setVisibility(VISIBLE);
            mHorizontalScrollView.addView(mFilterContainer);
        }
        updateAllFilters();
    }

    public void setShouldHideZeroOptions(boolean shouldHideZeroFilters) {
        mShouldHideZeroFilters = shouldHideZeroFilters;
        updateAllFilters();
    }

    public boolean isShouldHideAll() {
        return mShouldHideAll;
    }

    public boolean isShouldHideZeroFilters() {
        return mShouldHideZeroFilters;
    }

    public boolean isChangeColorWhenSelect() {
        return mShouldChangeColorWhenSelect;
    }

    public void setShouldHideAll(boolean shouldHideAll) {
        mShouldHideAll = shouldHideAll;
        updateAllFilters();
    }

    public void setTitleView(View titleView) {
        mTitleView = titleView;
    }

    private boolean isFiltersValid(List<Integer> checkFilters) {
        for (int i = 0; i < checkFilters.size(); i++) {
            if (getSubFilter(checkFilters.get(i)) == null) {
                return false;
            }
        }
        return true;
    }

    private boolean isFiltersValid(int checkFilter) {
        if (getSubFilter(checkFilter) == null) {
            mFilterCallback.noSuchFilterError(checkFilter);
            return false;
        }
        return true;
    }

    private void hideAllOpeningContainer() {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            if (isFiltersValid(mCurrentOpeningFilters.get(i))) {
                getSubFilter(mCurrentOpeningFilters.get(i)).setContainerVisible(false);
            }
        }
    }

    private boolean isFilterOpening(int filterNum) {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            if (mCurrentOpeningFilters.get(i) == filterNum) return true;
        }
        return false;
    }

    private void updateFilter(int filterNum) {
        if (isFiltersValid(filterNum)) {
            FilterHolder filterHolder = getSubFilter(filterNum);
            if (mShouldHideZeroFilters) {
                filterHolder.hideZeroOptions();
                filterHolder.showNonZeroOptions();
            } else {
                filterHolder.showAllOptions();
            }
            if (!mShouldHideAll) {
                filterHolder.setOptionVisible(0, true);
            } else {
                filterHolder.setOptionVisible(0, false);
            }
            filterHolder.updateAll();
        }
    }

    private void openGenreSelectorLayout(int openFilter) {
        if (getSubFilter(openFilter) == null) {
            mFilterCallback.noSuchFilterError(openFilter);
            return;
        }
        FilterHolder filterHolder = getSubFilter(openFilter);
        if (filterHolder.getHeight() == 0) {
            filterHolder.readyToTakeHeight(false, true);
//            filterHolder.mContainer
        } else {
            filterHolder.setContainerVisible(true);
            ValueAnimator va = createDropAnim(filterHolder.getContainer(), 0, filterHolder.getHeight());
            va.start();
        }
    }

    private void closeAnim(final View view) {
        int origHeight = view.getHeight();
        if (origHeight == 0) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            view.setVisibility(View.GONE);
        } else {
            ValueAnimator va = createDropAnim(view, origHeight, 0);
            va.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            va.start();
        }
    }

    private AutofitTextView getModifiedTextView(int width, int marginLeft, int marginRight, int marginUpAndDown) {
        width -= (marginLeft + marginRight);

        AutofitTextView autofitTextView = new AutofitTextView(mContext);
        autofitTextView.setTextColor(ContextCompat.getColor(mContext, mUnSelectedTextColor));
        autofitTextView.setBackground(ContextCompat.getDrawable(mContext, mUnSelectedBackground));
        autofitTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
        lp.setMargins(marginLeft, marginUpAndDown, marginRight, marginUpAndDown);
        autofitTextView.setLayoutParams(lp);
        return autofitTextView;
    }

    private void openAllOpeningFilter() {
        for (int i = 0; i < mCurrentOpeningFilters.size(); i++) {
            openGenreSelectorLayout(mCurrentOpeningFilters.get(i));
        }
    }

    private ValueAnimator createDropAnim(final View view, int start, int end) {
        ValueAnimator va = ValueAnimator.ofInt(start, end);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return va;
    }


    //region Utils.
    private int dpToPixels(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int getScreenWidthPixel(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    @SuppressWarnings("unchecked")
    public <S> FilterHolder<S> cast(FilterHolder filterHolder, Class<S> classOfS) {
        if (classOfS.isAssignableFrom(filterHolder.getAllFilterIds().get(0).getClass())) {
            return (FilterHolder<S>) filterHolder;
        }
        mFilterCallback.castFailed();
        return filterHolder;
    }

//    private void measure(FlexboxLayout flexboxLayout) {
//        if (this.getOrientation() == LinearLayout.VERTICAL) {
//            int h = 0;
//            int w = 0;
//            this.measureChildren(0, 0);
//            for (int i = 0; i < this.getChildCount(); i++) {
//                View v = this.getChildAt(i);
//                h += v.getMeasuredHeight();
//                w = (w < v.getMeasuredWidth()) ? v.getMeasuredWidth() : w;
//            }
//            height = (h < height) ? height : h;
//            width = (w < width) ? width : w;
//        }
//        flexboxLayout.setMeasuredDimension(width, height);
//    }
    //endregion
}