#FragmentBuilder

使用方式與**FragmentTransaction**很相似，當 onBackPress 被觸發，Fragment被Remove的時候，則會將此Fragment送回到原本FragmentBuilder使用的地方。

#### How to use

	FragmentBuilder
	        .create(MainFragment.this)
	        .setContainerViewId(R.id.container_main_4)
	        .setFragment(F4Fragment.class, F4Fragment.class.getSimpleName())
	        .add()
	        .traceable()
	        .build();

#### Listener

    public void onPopFragment(F4Fragment fragment) {
        mTextView.setText(String.format("->%s %s", fragment.getClass().getSimpleName(), fragment.result));
    }


#### proguard

	-keepclassmembers class ** {
	    public void onPopFragment(**);
	}

## Architecture

![](/images/FragmentViewArchitecture.png)

## Wizard Steps Test

![](/images/WizardStepsTest.gif)
