package projects.tryhard.androidflexiblefilter;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import projects.tryhard.androidflexiblefilter.FlexibleFilter.OptionClickCallback;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Use to hold things we need for a filter.
 */
public class FilterHolder<T> {
    private int mFilterNum;
    private List<Option<T>> mOptions;
    private FlexboxLayout mContainer;
    private int mHeight = 0;
    private T mCurrentSelected = null;
    private View mEmptyView;

    private OptionClickCallback<T> mOptionClickCallback;
    private FlexibleFilter.SettingCallback mSettingCallback;

    private boolean mIsRemoved = false;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future mGetHeightRunnableFuture = null;

    FilterHolder(int filterNum, List<Option<T>> mOptions, FlexboxLayout mContainer, View emptyView, FlexibleFilter.SettingCallback settingCallback) {
        this.mFilterNum = filterNum;
        this.mOptions = mOptions;
        this.mContainer = mContainer;
        this.mEmptyView = emptyView;
        this.mSettingCallback = settingCallback;

        mContainer.addView(mEmptyView);
        mEmptyView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        mEmptyView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        mEmptyView.setVisibility(GONE);

        setContainerVisible(false);
    }

    Option getOption(T optionId) {
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).getOptionId().equals(optionId)) {
                return mOptions.get(i);
            }
        }
        return mOptions.get(0);
    }

    void updateAll() {
        if (mOptions.size() == 0) return;

        boolean isEveryOptionCountZero = true;
        int sum = 0;
        for (int i = 0; i < mOptions.size(); i++) {
            if (i != 0) {
                // Don't need to add default all.
                sum += mOptions.get(i).getResultCount();
            }

            if (sum > 0) {
                isEveryOptionCountZero = false;
            }

            if (mCurrentSelected != null && mCurrentSelected.equals(mOptions.get(i).getOptionId())) {
                mOptions.get(i).invalidate(true);
            } else {
                mOptions.get(i).invalidate(false);
            }
        }
        mOptions.get(0).setResultCount(sum);

        if (isEveryOptionCountZero && mSettingCallback.getFlexibleFilter().isShouldHideAll()) {
            mEmptyView.setVisibility(VISIBLE);
        } else {
            mEmptyView.setVisibility(GONE);
        }

        mContainer.invalidate();
    }

    void setOptionVisible(int pos, boolean visible) {
        if (pos < mOptions.size()) {
            if (visible) {
                mOptions.get(pos).getAutofitTextView().setVisibility(VISIBLE);
            } else {
                mOptions.get(pos).getAutofitTextView().setVisibility(GONE);
            }
        }
    }

    public void setFilterClickCallback(OptionClickCallback<T> mOptionClickCallback) {
        this.mOptionClickCallback = mOptionClickCallback;
    }

    void hideZeroOptions() {
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).getResultCount() == 0) {
                mOptions.get(i).getAutofitTextView().setVisibility(GONE);
            }
        }
    }

    void showNonZeroOptions() {
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).getResultCount() != 0) {
                mOptions.get(i).getAutofitTextView().setVisibility(VISIBLE);
            }
        }
    }

    void showAllOptions() {
        for (int i = 0; i < mOptions.size(); i++) {
            mOptions.get(i).getAutofitTextView().setVisibility(VISIBLE);
        }
    }

    void removeFilter() {
        mContainer.setVisibility(GONE);
        mIsRemoved = true;
    }

    public List<T> getAllFilterIds() {
        List<T> allFilterIds = new ArrayList<>();
        for (int i = 0; i < getOptions().size(); i++) {
            allFilterIds.add(getOptions().get(i).getOptionId());
        }
        return allFilterIds;
    }

    public List<Option<T>> getOptions() {
        return mOptions;
    }

    void setOptionsDeco() {
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).getOptionId().equals(mCurrentSelected)) {
                mOptions.get(i).setSelected();
            } else {
                mOptions.get(i).setUnSelected();
            }
        }
    }

    void addNewFilterButton(final Option<T> option) {
        mContainer.addView(option.getAutofitTextView());
        mOptions.add(option);

        Log.d("Filter Log", "addNewFilterButton " + isContainerVisible());
        if (isContainerVisible()) {
            readyToTakeHeight(false, false);
        } else {
            readyToTakeHeight(true, false);
        }

        option.getAutofitTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOptionClickCallback != null) {
                    optionClicked(option.getOptionId());
                }
            }
        });
    }

    void removeOption(T optionId){
        Option<T> option = getOption(optionId);
        mContainer.removeView(option.getAutofitTextView());
        mOptions.remove(option);

        if (isContainerVisible()) {
            readyToTakeHeight(false, false);
        } else {
            readyToTakeHeight(true, false);
        }
    }

    void setContainerSize(int width) {
        mContainer.getLayoutParams().width = width;
        readyToTakeHeight(isContainerVisible(), true);
    }

    boolean isContainerVisible() {
        return mContainer.getVisibility() == VISIBLE;
    }

    void setContainerVisible(boolean visible) {
        if (mIsRemoved) return;
        Log.d("Filter Log", "setContainerVisible: " + visible);
        if (visible) {
            mContainer.setVisibility(VISIBLE);
        } else {
            mContainer.setVisibility(GONE);
        }
    }

    public int getHeight() {
        return mHeight;
    }


    public int getFilterNum() {
        return mFilterNum;
    }

    private boolean shouldSetToGoneWhenDoneLast;

    void readyToTakeHeight(final boolean shouldSetToGoneWhenDone, final boolean force) {
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.width = mContainer.getLayoutParams().width;
        mContainer.setLayoutParams(lp);

        setContainerVisible(true);


        mContainer.post(new Runnable() {
            @Override
            public void run() {
                final boolean finalShouldSetToGoneWhenDone;

                if (mGetHeightRunnableFuture != null) {
                    mGetHeightRunnableFuture.cancel(true);
                    if (!force) {
                        finalShouldSetToGoneWhenDone = shouldSetToGoneWhenDoneLast;
                    } else {
                        finalShouldSetToGoneWhenDone = shouldSetToGoneWhenDone;
                    }
                    shouldSetToGoneWhenDoneLast = finalShouldSetToGoneWhenDone;

                    Log.d("Filter Log", "canceled runnable, force = " + force + " shouldSetToGoneWhenDoneLast = " + shouldSetToGoneWhenDoneLast);
                } else {
                    shouldSetToGoneWhenDoneLast = shouldSetToGoneWhenDone;
                    finalShouldSetToGoneWhenDone = shouldSetToGoneWhenDoneLast;
                }

                mGetHeightRunnableFuture = executorService.submit(new GetHeightRunnable(finalShouldSetToGoneWhenDone));
            }
        });
    }

    public void setCurrentSelected(T currentSelected) {
        this.mCurrentSelected = currentSelected;
    }

    public void unSelectedAll() {
        if (mOptionClickCallback != null) {
            mOptionClickCallback.filterUnSelectedAll(mFilterNum);
        }
        setCurrentSelected(null);
    }

    public void optionClicked(T optionId) {
        if (optionId == null) {
            unSelectedAll();
            setOptionsDeco();
        } else {
            setCurrentSelected(optionId);

            if (mSettingCallback.getFlexibleFilter().isChangeColorWhenSelect()) {
                setOptionsDeco();
            }

            if (mOptionClickCallback != null) {
                mOptionClickCallback.filterOptionClicked(mFilterNum, optionId);
            }

        }
    }


    private class GetHeightRunnable implements Runnable {
        boolean shouldSetToGoneWhenDone;
        String from;

        public GetHeightRunnable(boolean shouldSetToGoneWhenDone) {
            this.shouldSetToGoneWhenDone = shouldSetToGoneWhenDone;
            this.from = from;
        }

        @Override
        public void run() {
            mHeight = mContainer.getMeasuredHeight();
            Log.d("Filter Log", "GetHeightRunnable: " + shouldSetToGoneWhenDone + ", h = " + mHeight);
            if (shouldSetToGoneWhenDone) {
                setContainerVisible(false);
            } else {
                setContainerVisible(true);
            }
            mGetHeightRunnableFuture = null;
        }
    }


    public FlexboxLayout getContainer() {
        return mContainer;
    }
}