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

### PreAction

FragmentBuilder has two Actions can be used. It is same to FragmentTransaction add() and replace().

BUT there are two PreActions back() and reset() can use.

#### PreAction back()

back() PreAction is used to transfer TargetObject and ContainerViewId even Action according back **FragmentPath**

back() is useful to implement wizard steps!

##### Normal Case:

A -> B -> C

then onBackPressed C is popped out from FragmentManager's back stack. B will receive C by onPopFragment().

A -> B <-(C)

then onBackPressed B is popped out from FragmentManager's back stack. A will receive B by onPopFragment().

A <-(B)

##### When C use back() case:

A <-(B) <-(C)

C refer B's setting and pass to A, so A will receive C and B.


#### PreAction reset()

reset() PreAction is used to replace Fragment and follow old FragmentBuilder setting. Like below:

A -> B -> C  
( When C reset to D )
A -> B -> D

You did not to setting everything you do to C.



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


## FragmentActivity Lifecycle
![](/images/FragmentActivityLifecycle.png)


### FragmentActivity View Lifecycle

#### System destory Activity
 
onAttachedToWindow -> onRestoreInstanceState -> onActivityResult

onAttachedToWindow -> onRestoreInstanceState -> FragmentManager.OnBackStackChangedListener

#### Rotation screen

onRestoreInstanceState -> onAttachedToWindow

#### System didn't destory Activity

onActivityResult

FragmentManager.OnBackStackChangedListener

#### Take image intent

onRestoreInstanceState -> onActivityResult -> onAttachedToWindow

為了處理不同的資料來源，所以使用 View.post 的方式來延遲，得到所有資料後再進行處理。 

在所有可能觸發的地方使用延遲處理!

    private boolean isPostRun = false;

    private void delayRun() {
        if (!isPostRun) {
            isPostRun = true;
            post(this);
        }
    }

    @Override
    public void run() {
 		//after handle data
        isPostRun = false;
    }

    @Override
    protected void onAttachedToWindow() {
        delayRun();
        super.onAttachedToWindow();
		// if data is null set default data
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        delayRun();
        super.onRestoreInstanceState(state);
		// set data
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        delayRun();
		// set data
    }

    public void onPopFragment(F11Fragment fragment) {
        delayRun();
		// set data
    }


##
    
    --------------- enter
    performCreate before
    	State:0
    	isAdded:true
    	isResumed:false
    	isInLayout:false
    	isInBackStack:false
    	isDetached:false
    	isRemoving:false
    	isMenuVisible:true
    	isVisible:false
    	isHidden:false
    performCreate after
    	State:1
    
    performCreateView before
    performCreateView after
    
    onCreateAnimation
    
    performActivityCreated before
    	isVisible:true
    performActivityCreated after
    	State:2
    
    onViewStateRestored
    
    performStart before
    onStart
    	State:4
    performStart after
    
    performResume before
    onResume
    	State:5
    	isResumed:true
    performResume after
    
    --------------- exit
    
    performPause before
    	State:5
    	isAdded:false
    	isResumed:true
    	isInBackStack:false
    	isDetached:false
    	isRemoving:true
    	isVisible:false
    onPause
    	State:4
    	isResumed:false
    performPause after
    
    直接移除不用保存狀態
    
    performStop before
    onStop
    	State:3
    performStop after
    
    performReallyStop before
    performReallyStop after
    	State:2
    
    performDestroyView before
    onDestroyView
    	State:1
    performDestroyView after
    
    onCreateAnimation
    
    performDestroy before
    onDestroy
    	State:0
    performDestroy after
    	getFragmentManagerActivity(fragment.getChildFragmentManager())==null:true
    
    ---------------rotation exit then enter
    performPause before
    	State:5
    	isAdded:true
    	isResumed:true
    	isInBackStack:false
    	isDetached:false
    	isRemoving:false
    	isVisible:true
    onPause
    	State:4
    	isResumed:false
    performPause after
    
    performSaveInstanceState before
    onSaveInstanceState
    performSaveInstanceState after
    
    performStop before
    onStop
    	State:3
    performStop after
    
    performReallyStop before
    performReallyStop after
    	State:2
    
    performDestroyView before
    onDestroyView
    	State:1
    performDestroyView after
    
    onCreateAnimation
    
    performDestroy before
    onDestroy
    	State:0
    performDestroy after
    	getFragmentManagerActivity(fragment.getChildFragmentManager())==null:true
    
    performCreate before
    	State:0
    	isAdded:true
    	isResumed:false
    	isVisible:false * 
    performCreate after
    	State:1
    
    performCreateView before
    performCreateView after
    	State:2
    
    onCreateAnimation
    
    performActivityCreated before
    	isVisible:false *
    performActivityCreated after
    
    onViewStateRestored
    
    performStart before
    onStart
    	State:4
    performStart after
    
    performResume before
    onResume
    	State:5
    	isResumed:true
    
    
    ---------------addBackStack push enter
    performCreate before
    	State:0
    	isAdded:true
    	isResumed:false
    	isInBackStack:true
    	isDetached:false
    	isRemoving:false
    	isVisible:false
    performCreate after
    	State:1
    
    performCreateView before
    performCreateView after
    
    onCreateAnimation
    
    performActivityCreated before
    	isVisible:true
    performActivityCreated after
    	State:2
    
    onViewStateRestored
    
    performStart before
    onStart
    	State:4
    performStart after
    
    performResume before
    onResume
    	State:5
    	isResumed:true
    performResume after
    
    ---------------addBackStack push exit
    
    performPause before
    	State:5
    	isAdded:false
    	isResumed:true
    	isInBackStack:true
    	isDetached or isRemoving:true
    	isVisible:false
    onPause
    	State:4
    	isResumed:false
    performPause after
    
    performStop before
    onStop
    	State:3
    performStop after
    
    performReallyStop before
    performReallyStop after
    	State:2
    
    performDestroyView before
    onDestroyView
    	State:1
    performDestroyView after
    
    onCreateAnimation
    
    如果在接 Rotation 會進行 performSaveInstanceState, performDestroy, performCreate
    因此保持 pop enter時步驟不變
    
    ---------------addBackStack pop enter
    
    performCreateView before
    	State:1
    	isAdded:true
    	isResumed:false
    	isInLayout:false
    	isInBackStack:true
    	isDetached:false
    	isRemoving:false
    	isMenuVisible:true
    	isVisible:false
    	isHidden:false
    performCreateView after
    
    onCreateAnimation
    
    performActivityCreated before
    	isVisible:true
    performActivityCreated after
    	State:2
    
    onViewStateRestored
    
    performStart before
    onStart
    	State:4
    performStart after
    
    performResume before
    onResume
    	State:5
    	isResumed:true
    performResume after
    
    ---------------addBackStack pop exit
    
    performPause before
    	State:5
    	isAdded:false
    	isResumed:true
    	isInBackStack:false
    	isDetached or isRemoving:true
    	isVisible:false
    
    onPause
    	State:4
    	isResumed:false
    performPause after
    
    performStop before
    onStop
    	State:3
    performStop after
    
    performReallyStop before
    performReallyStop after
    	State:2
    
    performDestroyView before
    onDestroyView
    	State:1
    performDestroyView after
    
    onCreateAnimation
    
    performDestroy before
    onDestroy
    	State:0
    performDestroy after
    	getFragmentManagerActivity(fragment.getChildFragmentManager())==null:true
    
    

## Fragment detach/attach

Detach cause fragment call onPause(),but attach cause fragment call onCreateView() expect onResume()