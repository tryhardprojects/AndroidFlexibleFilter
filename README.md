# AndroidFlexibleFilter
An library to create flexible dynamic and filter, custom title with tags.

# Screen Shots
### Basic
<img src="https://j.gifs.com/WL6LJg.gif" alt="alt text" width="270" height="480">
<img src="https://j.gifs.com/q7x71r.gif" alt="alt text" width="270" height="480">

### Advance
<img src="https://j.gifs.com/nx5x14.gif" alt="alt text" width="270" height="480">
<img src="https://j.gifs.com/ANDNpB.gif" alt="alt text" width="270" height="480">

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
    android:id="@+id/quests_filter"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
```
And then initialize the filter using:
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





