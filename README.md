#FragmentBuilder

FragmentBuilder is designed for solving back stack behaviour with nested fragments.

[http://stackoverflow.com/a/24176614/1584100](http://stackoverflow.com/a/24176614/1584100)


## How To Use

### FragmentActivity Setting

    @Override
    public void onBackPressed() {
        if (FragmentBuilder.hasPopBackStack(this)) {
            return;
        }
        super.onBackPressed();
    }

### FragmentManager covert to FragmentBuilder

	// Use FragmentManager
	getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container,fragment)
                .commit();

	// Use FragmentBuilder
	FragmentBuilder
	        .create(this)
	        .add(R.id.container,fragment)
	        .build();

### Support listener for the Fragment is pop from BackStack
You can implement the PopFragmentListener on **FragmentActicity**, **Fragment**, even **View**.

	@Override
    public void onPopFragment(Fragment fragment) {
        // Access any you want from Fragment
    }
	
	// Or exactly CLASS
	public void onPopFragment(SelectDateFragment fragment) {
        // Access any you want from Fragment
    }

### Proguard Setting If you use PopFragmentListener

	-keepclassmembers class ** {
	    public void onPopFragment(**);
	}

## Sample

### Wizard Steps

// TODO



## Architecture

![](/images/FragmentViewArchitecture.png)

### FragmentPath is used to find the fragment that use FragmentBuilder.

When backward path, always use FragmentPath to find the source.

When foreward path, didn't use FragmentPath to find the source, just use FragmentManager to get the source Fragment.


## Wizard Steps Test

![](/images/WizardStepsTest.gif)
