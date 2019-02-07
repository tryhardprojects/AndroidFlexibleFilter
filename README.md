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
### Customizable attributes
| Attribute                | default value | xml                             | java                           | description |
|--------------------------|---------------|---------------------------------|--------------------------------|-------------|
| Orientation              | vertical      | app:orientation                 | setFilterOrientation           |             |
| Hide Zero Options        | false         | app:shouldHideZeroOptions       | setShouldHideZeroOptions       |             |
| Change Color When Select | true          | app:shouldChangeColorWhenSelect | setShouldChangeColorWhenSelect |             |
| Column Count             | 1             | app:colCount                    | setFilterColCount              |             |
| Close After Click        | true          | app:shouldCloseAfterClick       | setShouldCloseAfterClick       |             |
| Hide All                 | false         | app:shouldHideAll               | setShouldHideAll               |             |



