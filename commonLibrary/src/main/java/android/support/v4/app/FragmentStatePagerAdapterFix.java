package android.support.v4.app;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.lin1987www.common.Utility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FragmentStatePagerAdapterFix extends PagerAdapter {
    private static final String TAG = FragmentStatePagerAdapterFix.class.getSimpleName();
    private static final boolean DEBUG = Utility.DEBUG;

    private WeakReference<FragmentActivity> wrFragmentActivity;
    private WeakReference<Fragment> wrParentFragment;
    private final FragmentManagerImpl mFragmentManager;
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

    public <F extends Fragment> F getFragment(int index) {
        F fragment = (F) mFragments.get(index);
        return fragment;
    }

    public <B extends Bundle> B getFragmentArgs(int index) {
        B bundle = (B) mFragmentArgs.get(index);
        return bundle;
    }

    public <T extends FragmentActivity> T getFragmentActivity() {
        return (T) wrFragmentActivity.get();
    }

    public <T extends Fragment> T getParentFragment() {
        return (T) wrParentFragment.get();
    }

    public FragmentStatePagerAdapterFix(FragmentActivity activity) {
        mFragmentManager = (FragmentManagerImpl) activity.getSupportFragmentManager();
        wrFragmentActivity = new WeakReference<>(activity);
        wrParentFragment = new WeakReference<>(null);
    }

    public FragmentStatePagerAdapterFix(Fragment fragment) {
        mFragmentManager = (FragmentManagerImpl) fragment.getChildFragmentManager();
        wrFragmentActivity = new WeakReference<>(fragment.getActivity());
        wrParentFragment = new WeakReference<>(fragment);
    }

    public void add(Class<? extends Fragment> fragClass) {
        add(fragClass, null, null);
    }

    public void add(Class<? extends Fragment> fragClass, Bundle args) {
        add(fragClass, args, null);
    }

    public void add(Class<? extends Fragment> fragClass, String tag) {
        add(fragClass, null, tag);
    }

    public void add(Class<? extends Fragment> fragClass, Bundle args, String tag) {
        add(fragClass, args, tag, getCount());
    }

    public void add(Class<? extends Fragment> fragClass, Bundle args, String tag, int position) {
        while (getCount() <= position) {
            mFragments.add(null);
            mFragmentStates.add(null);
            mFragmentTags.add(null);
            mFragmentClassNames.add(null);
            mFragmentArgs.add(null);
        }
        mFragments.set(position, null);
        mFragmentStates.set(position, null);
        mFragmentTags.set(position, tag);
        mFragmentClassNames.set(position, fragClass.getName());
        mFragmentArgs.set(position, args);
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

    public void clear() {
        mFragments.clear();
        mFragmentStates.clear();
        mFragmentTags.clear();
        mFragmentClassNames.clear();
        mFragmentArgs.clear();
    }

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment;
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        fragment = mFragments.get(position);
        if (fragment != null) {
            return fragment;
        }
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        FragmentState fs = mFragmentStates.get(position);
        if (fs != null) {
            FragmentManager fragmentManager = getFragmentActivity().getSupportFragmentManager();
            if (getParentFragment() != null) {
                fragmentManager = getParentFragment().getChildFragmentManager();
            }
            // Fix Can't change tag of fragment Error
            // http://stackoverflow.com/questions/24355838/cant-change-tag-of-fragment-error-trying-to-use-a-pageradapter-for-switching
            if ((fs.mTag != null && fs.mTag.equals(mFragmentTags.get(position))) ||
                    (fs.mTag == null && mFragmentTags.get(position) == null)) {
                fragment = fs.instantiate(FragmentUtils.getFragmentHostCallback(fragmentManager), FragmentUtils.getFragmentContainer(fragmentManager), getParentFragment(), FragmentUtils.getFragmentManagerNonConfig(fragmentManager));
                // Fix bug
                // http://stackoverflow.com/questions/11381470/classnotfoundexception-when-unmarshalling-android-support-v4-view-viewpagersav
                if (fragment.mSavedFragmentState != null) {
                    fragment.mSavedFragmentState.setClassLoader(Utility.getClassLoader());
                }
                // 回復狀態後要清除 取代 .detach(f).attach(f).commit()
                fragment.initState();
            } else {
                Log.e(TAG,
                        String.format("Fragment tag isn't equal! Origin: %s %s",
                                fs.mTag, mFragmentTags.get(position)
                        ));
                mFragmentStates.set(position, null);
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
            Log.v(TAG, "Removing item #" + position + ": f=" + object + " v=" + ((Fragment) object).getView());
        }
        if (position < getCount() && fragment.mIndex >= 0) {
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
        if (FragmentUtils.isStateLoss(mFragmentManager)) {
            return;
        }
        if (mCurTransaction != null) {
            mCurTransaction.commit();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
            if (DEBUG) {
                Log.d(TAG, "finishUpdate");
            }
/*
            for (Fragment fragment : mFragments) {
                if (fragment != null) {
//                    if (fragment.isAdded() && !fragment.isResumed()) {
//                        // Fix sdk 23.0.1 : Fragment isAdded, but didn't resumed.
//                        if (FragmentUtils.isStateLoss(fragment.getFragmentManager())) {
//                            continue;
//                        }
//                        // Test move to fixActiveFragment(mFragmentManager, fragment);
//                        // fragment.getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
//                    }
                    if (FragmentUtils.isStateLoss(fragment.getFragmentManager())) {
                        continue;
                    }
                    // Fix sdk 22.0.1 : Fragment is added by transaction. BUT didn't add to FragmentManager's mActive. If you Rotation.
                    // fixActiveFragment(mFragmentManager, fragment);
                }
            }
*/
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @CallSuper
    @Override
    public Parcelable saveState() {
        if (DEBUG) {
            Log.v(TAG, "saveState");
            log();
        }

        Bundle state = null;
        // 目前顯示的 Fragments
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null && f.isAdded() && f.mIndex > -1) {
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
            // Create new instance for some reason
            ArrayList<FragmentState> fs = new ArrayList<>();
            fs.addAll(mFragmentStates);
            state.putParcelableArrayList("states_fragment", fs);

            ArrayList<Bundle> args = new ArrayList<>();
            args.addAll(mFragmentArgs);
            state.putParcelableArrayList("arg_fragment", args);

            ArrayList<String> tagArray = new ArrayList<>();
            tagArray.addAll(mFragmentTags);
            state.putStringArrayList("tag_fragment", tagArray);

            ArrayList<String> classNameArray = new ArrayList<>();
            classNameArray.addAll(mFragmentClassNames);
            state.putStringArrayList("classname_fragment", classNameArray);
        }
        return state;
    }

    @CallSuper
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            // Don't use default loader
            bundle.setClassLoader(Utility.getClassLoader());
            ArrayList<FragmentState> fs = bundle.getParcelableArrayList("states_fragment");
            ArrayList<Bundle> args = bundle.getParcelableArrayList("arg_fragment");
            ArrayList<String> tags = bundle.getStringArrayList("tag_fragment");
            ArrayList<String> classNames = bundle.getStringArrayList("classname_fragment");

            mFragments.clear();
            mFragmentStates.clear();
            mFragmentTags.clear();
            mFragmentClassNames.clear();
            mFragmentArgs.clear();
            if (fs != null) {
                for (int i = 0; i < fs.size(); i++) {
                    FragmentState fragmentState = fs.get(i);
                    mFragmentStates.add(fragmentState);
                    if (fragmentState != null) {
                        mFragmentArgs.add(fragmentState.mArguments);
                        mFragmentTags.add(fragmentState.mTag);
                        mFragmentClassNames.add(fragmentState.mClassName);
                    } else {
                        mFragmentArgs.add(args.get(i));
                        mFragmentTags.add(tags.get(i));
                        mFragmentClassNames.add(classNames.get(i));
                    }
                    mFragments.add(null);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = null;
                    try {
                        f = mFragmentManager.getFragment(bundle, key);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
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

    private void fixAddedDidNotActive() {
        if (mFragmentManager.mAdded != null) {
            for (int i = 0; i < mFragmentManager.mAdded.size(); i++) {
                Fragment f = mFragmentManager.mAdded.get(i);
                if (f != null) {
                    if (mFragmentManager.mActive.indexOfValue(f) < 0) {
                        if (f.getUserVisibleHint() && !f.isResumed()) {
                            if (!FragmentUtils.isStateLoss(f.getFragmentManager())) {
                                mFragmentManager.beginTransaction().detach(f).attach(f).commit();
                                Log.e(TAG, String.format("fix didn't show up %s, %s, mIndex:%s, contains:%s", f, i, f.mIndex, (mFragmentManager.mActive.indexOfValue(f) > -1)));
                            }
                        }
                    }
                }
            }
        }
    }

    /*
    private void fixActiveSizeDidNotEnough() {
        if (mFragmentManager.mAdded != null) {
            for (int i = 0; i < mFragmentManager.mAdded.size(); i++) {
                Fragment f = mFragmentManager.mAdded.get(i);
                if (f != null) {
                    int fi = f.mIndex;
                    if (fi >= mFragmentManager.mActive.size()) {
                        Log.e(TAG, String.format("fix mActive didn't enough %s %s", f, fi));
                        while (fi >= mFragmentManager.mActive.size()) {
                            mFragmentManager.mActive.add(null);
                        }
                    }
                }
            }
        }
    }
    */

    public static void fixActiveFragment(FragmentManager fragmentManager, Fragment fragment) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        boolean needMatch = false;
        Fragment willRemoveFrag = null;
        if (fm.mActive != null) {
            int index = fragment.mIndex;
            if (index < 0) {
                Log.e(TAG, String.format("Fragment mIndex %s < 0 ! Origin: %s", fragment.mIndex, fragment));
                fragment.getFragmentManager().beginTransaction().remove(fragment).commit();
                return;
            }
            Fragment origin = fm.mActive.get(index);
            if (origin != null) {
                if ((origin.mIndex != fragment.mIndex) || !(origin.equals(fragment))) {
                    Log.e(TAG,
                            String.format("Fragment isn't equal! Origin: %s %s, Fragment: %s %s",
                                    origin.toString(), origin.mIndex,
                                    fragment.toString(), fragment.mIndex
                            ));
                    ArrayList<Integer> indexArray = new ArrayList<>();
                    for (Fragment f : fm.mAdded) {
                        if (indexArray.contains(f.mIndex)) {
                            willRemoveFrag = origin;
                            break;
                        }
                        indexArray.add(f.mIndex);
                    }
                }
            }
            fm.mActive.put(index, fragment);
            if (willRemoveFrag != null) {
                // 2016.01.26 Fix bug mAdded not match
                // 瀏覽所有Fragment後，轉至後瀏覽其他Fragment會產生衝突
                fm.mAdded.remove(willRemoveFrag);
            }
            // 如往回跳的話會發現沒有執行
            // 1~5 is Fragment
            // 1 2 3 4 5 -> 3
            // 1 2 3 -> 1
            // v25.0.0 Fix fragment didn't run, if through step 1, 2, 3 then go back to 1, step 1 fragment didn't run
            if (fragment.getUserVisibleHint() && !fragment.isResumed()) {
                if (!FragmentUtils.isStateLoss(fragment.getFragmentManager())) {
                    fragment.getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
                }
            }
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
        int result = PagerAdapter.POSITION_UNCHANGED;
        if (mTempPositionChange != null) {
            boolean isPositionChange = mTempPositionChange[index];
            if (isPositionChange) {
                result = PagerAdapter.POSITION_NONE;
            }
        }
        return result;
    }

    private void log() {
        Log.d(TAG, "mFragments START");
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null) {
                Log.d(TAG, String.format("%s, %s, mIndex:%s", f, i, f.mIndex));
            }
        }
        Log.d(TAG, "mFragmentManager.mAdded");
        if (mFragmentManager.mAdded != null) {
            for (int i = 0; i < mFragmentManager.mAdded.size(); i++) {
                Fragment f = mFragmentManager.mAdded.get(i);
                if (f != null) {
                    Log.d(TAG, String.format("%s, %s, mIndex:%s, contains:%s", f, i, f.mIndex, mFragmentManager.mAdded.contains(f)));
                }
            }
        }
        Log.d(TAG, "mFragmentManager.mActive");
        if (mFragmentManager.mActive != null) {
            for (int i = 0; i < mFragmentManager.mActive.size(); i++) {
                Fragment f = mFragmentManager.mActive.get(i);
                if (f != null) {
                    Log.d(TAG, String.format("%s, %s, mIndex:%s, contains:%s", f, i, f.mIndex, (mFragmentManager.mActive.indexOfValue(f) > -1)));
                }
            }
        }
        Log.d(TAG, "mFragments END");
    }
}
