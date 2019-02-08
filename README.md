# AndroidFlexibleFilter
An library to create flexible and dynamic filter, custom title with tags.

# Screen Shots
### Basic
<p float="left">
	<img src="https://j.gifs.com/WL6LJg.gif" alt="alt text" width="270" height="480">
	<img src="https://j.gifs.com/q7x71r.gif" alt="alt text" width="270" height="480">
</p>

### Advance
<p float="left">
	<img src="https://j.gifs.com/nx5x14.gif" alt="alt text" width="270" height="480">
	<img src="https://j.gifs.com/ANDNpB.gif" alt="alt text" width="270" height="480">
</p>

# Import
### Gradle
    allprojects {    
		repositories {    
			...
			maven { url 'https://jitpack.io' }     
		}
	}
---
    dependencies {
	        implementation 'com.github.tryhardprojects:AndroidFlexibleFilter:1.0.3'
	}
# How to use
First add this to the xml layout file.
```
<projects.tryhard.androidflexiblefilter.FlexibleFilter
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
```
And then initialize the filter using: (you will need a [FilterCallback](#FilterCallback))
```
    /**
     * Use to init the whole filter with default.
     *
     * @param context             We use to inflate layouts.
     * @param filterNum           A number for default filter, use it when you want to update, show or hide certain filter.
     * @param titleLayout         The title you want for the filter, -1 means no title, 0 means default title.
     * @param allT                A unique ID for the default all option.
     * @param filterErrorCallback Callbacks when error occurs.
     */
    public void init(Context context, int filterNum, @LayoutRes int titleLayout, final T allT, FilterErrorCallback filterErrorCallback)
```
Or
```
  /**
     * Use to init the whole filter with more detail.
     *
     * @param context             We use to inflate layouts.
     * @param filterNum           A number for default filter, use it when you want to update, show or hide certain filter.
     * @param titleLayout         The title you want for the filter, -1 means no title, 0 means default title.
     * @param allT                A unique ID for the default all option.
     * @param emptyDefaultLayout  A default Empty Layout, -1 means use default.
     * @param hideAll             Should we hide the default all option?
     * @param hideZeroOptions     Should we hide the options that is 0?
     * @param orientation         The mOrientation of the filters' layout.
     * @param colCount            How many filters we show at one row. Default is one.
     * @param filterErrorCallback Callbacks when error occurs.
     */
    public void init(Context context, int filterNum, @LayoutRes int titleLayout, final T allT, @LayoutRes int emptyDefaultLayout,
                     boolean hideAll, boolean hideZeroOptions, Orientation orientation, int colCount, boolean shouldCloseAfterClick,
                     boolean changeColorWhenSelect, FilterErrorCallback filterErrorCallback)
```

### Customizable attributes
You can customizable the attributes by xml, java (with init() or set methods).

| Attribute                | default value | xml                             | java                           |
|--------------------------|---------------|---------------------------------|--------------------------------|
| Orientation              | vertical      | app:orientation                 | setFilterOrientation           |
| Hide Zero Options        | false         | app:shouldHideZeroOptions       | setShouldHideZeroOptions       |
| Change Color When Select | true          | app:shouldChangeColorWhenSelect | setShouldChangeColorWhenSelect |
| Column Count             | 1             | app:colCount                    | setFilterColCount              |
| Close After Click        | true          | app:shouldCloseAfterClick       | setShouldCloseAfterClick       |
| Hide All                 | false         | app:shouldHideAll               | setShouldHideAll               |

You will have one default filter with an default all button. Add another filter with:

    /**
     * @param filterNum       A number for default filter, use it when you want to update, show or hide certain filter.
     * @param defaultT        A unique ID for the default all option.
     * @param emptyViewLayout A view to display when no option are shown. -1 means default emptyView.
     */
    public <S> void addFilter(int filterNum, final S defaultT, @LayoutRes int emptyViewLayout)
Add option to certain filter with: (you will need a [OptionGetStringCallback](#OptionGetStringCallback))

    /**
     * Add a option to filter with left margin 4dp, right 4dp, up and down 8dp.
     *
     * @param filterNum                The filter number where you want to add an option.
     * @param optionId                 The unique ID you give to the option.
     * @param count                    The count of this option from the beginning.
     * @param width                    The width of this option.
     * @param optionGetStringCallback For you to decide the text on the option.
     * @param <S>                      optionId class.
     */
    public <S> void addFilterOption(int filterNum, S optionId, int count, int width, OptionGetStringCallback<S> optionGetStringCallback)
    
Set up [OptionClickCallback](#OptionClickCallback) to option with setOptionOnClickCallback function, do your logic after option being clicked in filterOptionClicked. Like changing views in title by calling getTitle().
    
# Callbacks
### FilterCallback
```
public interface FilterCallback {
    /**
     * Decide the text on default all option.
     * @param filterNum The filterNum for this sub filter.
     * @param count     the sum of all the option counts.
     * @return A string that will be display on the option.
     */
    String defaultAllOptionString(int count);

    /**
     * Get called when is no FilterNum match that.
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
```
### OptionClickCallback
```
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
```

### OptionGetStringCallback
```
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
```
Enjoy!
