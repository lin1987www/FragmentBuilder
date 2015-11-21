package android.support.v4.app;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FragmentStatePagerAdapterFix extends PagerAdapter {
    private static final String TAG = FragmentStatePagerAdapterFix.class.getSimpleName();
    private static final boolean DEBUG = false;

    private WeakReference<FragmentActivity> wrFragmentActivity;
    private WeakReference<Fragment> wrParentFragment;
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    protected ArrayList<Fragment> mFragments = new ArrayList<>();
    protected ArrayList<FragmentState> mFragmentStates = new ArrayList<>();
    protected ArrayList<String> mFragmentTags = new ArrayList<>();
    protected ArrayList<String> mFragmentClassNames = new ArrayList<>();
    protected ArrayList<Bundle> mFragmentArgs = new ArrayList<>();

    private Fragment mCurrentPrimaryItem = null;
    private boolean[] mTempPositionChange;

    @Override
    public int getCount() {
        return mFragmentClassNames.size();
    }

    public FragmentActivity getFragmentActivity() {
        return wrFragmentActivity.get();
    }

    public Fragment getParentFragment() {
        return wrParentFragment.get();
    }

    public FragmentStatePagerAdapterFix(FragmentActivity activity) {
        mFragmentManager = activity.getSupportFragmentManager();
        wrFragmentActivity = new WeakReference<>(activity);
        wrParentFragment = new WeakReference<>(null);
    }

    public FragmentStatePagerAdapterFix(Fragment fragment) {
        mFragmentManager = fragment.getChildFragmentManager();
        wrFragmentActivity = new WeakReference<>(fragment.getActivity());
        wrParentFragment = new WeakReference<>(fragment);
    }

    public void add(Class<? extends android.support.v4.app.Fragment> fragClass) {
        add(fragClass, null, null);
    }

    public void add(Class<? extends android.support.v4.app.Fragment> fragClass, Bundle args) {
        add(fragClass, args, null);
    }

    public void add(Class<? extends android.support.v4.app.Fragment> fragClass, String tag) {
        add(fragClass, null, tag);
    }

    public void add(Class<? extends android.support.v4.app.Fragment> fragClass, Bundle args, String tag) {
        add(fragClass, args, tag, getCount());
    }

    public void add(Class<? extends android.support.v4.app.Fragment> fragClass, Bundle args, String tag, int position) {
        mFragments.add(position, null);
        mFragmentStates.add(position, null);
        mFragmentTags.add(position, tag);
        mFragmentClassNames.add(position, fragClass.getName());
        mFragmentArgs.add(position, args);
        mTempPositionChange = new boolean[getCount()];
    }

    public void remove(int position) {
        if (position < getCount()) {
            mTempPositionChange = new boolean[getCount()];
            for (int i = position; i < mTempPositionChange.length; i++) {
                mTempPositionChange[i] = true;
            }
            mFragments.remove(position);
            mFragmentStates.remove(position);
            mFragmentTags.remove(position);
            mFragmentClassNames.remove(position);
            mFragmentArgs.remove(position);
        }
    }

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        Fragment fragment = null;
        if (mFragmentStates.size() > position) {
            FragmentState fs = mFragmentStates.get(position);
            if (fs != null) {
                fragment = fs.instantiate(getFragmentActivity(), getParentFragment());
                // Fix bug
                // http://stackoverflow.com/questions/11381470/classnotfoundexception-when-unmarshalling-android-support-v4-view-viewpagersav
                if (fragment.mSavedFragmentState != null) {
                    fragment.mSavedFragmentState.setClassLoader(fragment.getClass().getClassLoader());
                }
            }
        }
        if (fragment == null) {
            fragment = Fragment.instantiate(getFragmentActivity(), mFragmentClassNames.get(position), mFragmentArgs.get(position));
        }
        if (DEBUG) {
            Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        mFragments.set(position, fragment);
        mFragmentStates.set(position, null);
        mCurTransaction.add(container.getId(), fragment, mFragmentTags.get(position));

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) {
            Log.v(TAG, "Removing item #" + position + ": f=" + object
                    + " v=" + ((Fragment) object).getView());
        }
        if (position < getCount()) {
            FragmentState fragmentState = new FragmentState(fragment);
            Fragment.SavedState savedState = mFragmentManager.saveFragmentInstanceState(fragment);
            if (savedState != null) {
                fragmentState.mSavedFragmentState = savedState.mState;
            }
            mFragmentStates.set(position, fragmentState);
            mFragments.set(position, null);
        }
        mCurTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
            // Fix: Fragment is added by transaction. BUT didn't add to FragmentManager's mActive.
            for (Fragment fragment : mFragments) {
                if (fragment != null) {
                    fixActiveFragment(mFragmentManager, fragment);
                }
            }
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        // 目前顯示的 Fragments
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null && f.isAdded()) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        if (mFragmentStates.size() > 0) {
            if (state == null) {
                state = new Bundle();
            }
            FragmentState[] fs = new FragmentState[mFragmentStates.size()];
            mFragmentStates.toArray(fs);
            state.putParcelableArray("states_fragment", fs);
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fs = bundle.getParcelableArray("states_fragment");
            mFragments.clear();
            mFragmentStates.clear();
            mFragmentTags.clear();
            mFragmentClassNames.clear();
            mFragmentArgs.clear();
            if (fs != null) {
                for (int i = 0; i < fs.length; i++) {
                    FragmentState fragmentState = (FragmentState) fs[i];
                    mFragmentStates.add(fragmentState);
                    if (fragmentState != null) {
                        mFragmentArgs.add(fragmentState.mArguments);
                        mFragmentTags.add(fragmentState.mTag);
                        mFragmentClassNames.add(fragmentState.mClassName);
                    } else {
                        mFragmentArgs.add(null);
                        mFragmentTags.add(null);
                        mFragmentClassNames.add(null);
                    }
                    mFragments.add(null);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                        mFragmentArgs.set(index, f.mArguments);
                        mFragmentTags.set(index, f.mTag);
                        mFragmentClassNames.set(index, f.getClass().getName());
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
            // If restore will change
            notifyDataSetChanged();
        }
    }

    public static void fixActiveFragment(FragmentManager fragmentManager, Fragment fragment) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        if (fm.mActive != null) {
            int index = fragment.mIndex;
            Fragment origin = fm.mActive.get(index);
            if (origin != null) {
                if ((origin.mIndex != fragment.mIndex) || !(origin.equals(fragment))) {
                    Log.e(TAG,
                            String.format("fixActiveFragment: Not Equal! Origin: %s %s, Fragment: %s $s",
                                    origin.getClass().getName(), origin.mIndex,
                                    fragment.getClass().getName(), fragment.mIndex
                            ));
                }
            }
            fm.mActive.set(index, fragment);
        }
    }

    // Fix
    // http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android
    @Override
    public int getItemPosition(Object object) {
        int index = mFragments.indexOf(object);
        if (index < 0) {
            return PagerAdapter.POSITION_NONE;
        }
        boolean isPositionChange = mTempPositionChange[index];
        int result = PagerAdapter.POSITION_UNCHANGED;
        if (isPositionChange) {
            result = PagerAdapter.POSITION_NONE;
        }
        return result;
    }
}
