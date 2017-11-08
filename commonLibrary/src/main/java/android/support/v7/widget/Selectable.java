package android.support.v7.widget;

import android.os.Parcel;
import android.os.Parcelable;

import com.lin1987www.common.Utility;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/10/5.
 */
public class Selectable implements Parcelable {
    public ArrayList<Parcelable> mChildren;
    public ArrayList<Integer> mSelectedIndexes;

    public Selectable() {
        mChildren = new ArrayList<>();
        mSelectedIndexes = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.mSelectedIndexes);
        //
        dest.writeInt(mSelectedIndexes.size());
        for (int i = 0; i < mChildren.size(); i++) {
            Parcelable data = mChildren.get(i);
            dest.writeParcelable(data, flags);
        }
    }

    protected Selectable(Parcel in) {
        this.mSelectedIndexes = new ArrayList<>();
        in.readList(this.mSelectedIndexes, Integer.class.getClassLoader());
        //
        this.mChildren = new ArrayList<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Parcelable data = in.readParcelable(Utility.getClassLoader());
            mChildren.add(data);
        }
    }

    public static final Creator<Selectable> CREATOR = new Creator<Selectable>() {
        @Override
        public Selectable createFromParcel(Parcel source) {
            return new Selectable(source);
        }

        @Override
        public Selectable[] newArray(int size) {
            return new Selectable[size];
        }
    };
}
