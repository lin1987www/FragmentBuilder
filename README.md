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
	
	// Or exactly class
	public void onPopFragment(SelectDateFragment fragment) {
        // Access any you want from Fragment
    }

### Proguard Setting If you use PopFragmentListener

	-keepclassmembers class ** {
	    public void onPopFragment(**);
	}

## Simple

### Wizard Steps - back()
// TODO
### Switch Fragment - reset()
// TODO

## FragmentPath Architecture Simple

FragmentPath is this whole library core. We use it to find all Fragment from FragmentActivity.

![](/images/FragmentViewArchitecture.png)

We put all information to FragmentTransaction's addStackName method, and parse information by RegEx.

Before popBackState(), We parse BackStackEntry's name(naming by addStackName method), then create PopFragmentSender to trigger onPopFragment method.  

Fragment.getId() equals containerViewId.

### FragmentPath is used to find the fragment.

Wherever you call FragmentBuilder, it will find FragmentPath. ( FragmentActivity, Fragment, View )


## Wizard Steps Test
![](/images/WizardStepsTest.gif)
