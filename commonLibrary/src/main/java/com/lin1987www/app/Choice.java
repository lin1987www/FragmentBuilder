package com.lin1987www.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class Choice<T> implements Parcelable {
    public final static String TAG = Choice.class.getName();
    public final static String SEPARATOR = ",";
    private String separator = SEPARATOR;
    // #region Choice Mode
    // 跟AbsListView得 參數值一致
    // http://developer.android.com/reference/android/widget/AbsListView.html#setChoiceMode%28int%29
    public final static int CHOICE_MODE_NONE = 0;
    public final static int CHOICE_MODE_SINGLE = 1;
    public final static int CHOICE_MODE_MULTIPLE = 2;
    private int choiceMode = CHOICE_MODE_MULTIPLE;
    private T[] itemArray;
    private boolean[] checkedItemArray;
    private String[] itemNameArray;
    private DialogInterface.OnClickListener delegateAlertDialogClickListener;
    private boolean disableEventIfSame = true;

    private static boolean[] booleanArraysCopyOf(boolean[] original) {
        boolean[] copy = new boolean[original.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        // API 9
        // copy = Arrays.copyOf(original, original.length);
        return copy;
    }

    public void disableEventIfSame(boolean disableEventIfSame) {
        this.disableEventIfSame = disableEventIfSame;
    }

    private T[] getItemArray() {
        if (itemArray == null) {
            setItemArray(createItemArray());
        }
        return itemArray;
    }

    private void setItemArray(T[] itemArray) {
        this.itemArray = itemArray;
    }

    public boolean[] getCheckedItemArray() {
        if (checkedItemArray == null) {
            setCheckedItemArray(createCheckedItemArray());
        }
        return checkedItemArray;
    }

    private void setCheckedItemArray(boolean[] checkedItemArray) {
        this.checkedItemArray = checkedItemArray;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public boolean[] copyCheckedItemArray() {
        return booleanArraysCopyOf(getCheckedItemArray());
    }

    private boolean[] createCheckedItemArray() {
        boolean[] array = new boolean[getItemArray().length];
        for (int i = 0; i < array.length; i++) {
            array[i] = false;
        }
        return array;
    }

    public void clear() {
        setItemArray(null);
        setCheckedItemArray(null);
    }

    // #region Base Override
    // Java不能直接使用 Enum<?>.values()，所以必須透過繼承去產生
    public abstract T[] createItemArray();

    // 提供Enum Item的唯一識別編號
    public abstract String getItemId(T item);

    public abstract int getDialogTitleResId();

    // #endregion

    public Choice<T> setChoiceMode(int choiceMode) {
        this.choiceMode = choiceMode;
        return this;
    }

    public int getCheckedItemIndex() {
        if (choiceMode != CHOICE_MODE_SINGLE) {
            throw new RuntimeException(String.format("It's not signle mode %s",
                    this.toString()));
        }

        for (int i = 0; i < getCheckedItemArray().length; i++) {
            if (getCheckedItemArray()[i]) {
                return i;
            }
        }
        return -1;
    }

    public T getCheckedItem() {
        int index = getCheckedItemIndex();
        if (index >= 0) {
            return getItemArray()[index];
        } else {
            return null;
        }
    }

    // #region 基本操作
    public void check(T item) {
        check(getItemId(item));
    }

    public void check(String itemId) {
        modify(itemId, true);
    }

    public void check(int itemIndex) {
        if (itemIndex == -1) {
            uncheckAll();
        } else {
            modify(itemIndex, true);
        }
    }

    public void uncheck(T item) {
        uncheck(getItemId(item));
    }

    public void uncheck(String itemId) {
        modify(itemId, false);
    }

    public void uncheck(int itemIndex) {
        modify(itemIndex, false);
    }

    public void uncheckAll() {
        for (int i = 0; i < getItemArray().length; i++) {
            uncheck(i);
        }
    }

    public void modify(String itemId, Boolean value) {
        for (int i = 0; i < getItemArray().length; i++) {
            if (itemId.equals(getItemId(getItemArray()[i]))) {
                modify(i, value);
                return;
            }
        }
        throw new RuntimeException(String.format("沒找到 %s not found! in %s",
                itemId, this.toString()));
    }

    public void modify(int itemIndex, Boolean value) {
        modify(getCheckedItemArray(), itemIndex, value);
    }

    public void modify(boolean[] array, int itemIndex, Boolean value) {
        if (choiceMode == CHOICE_MODE_SINGLE) {
            // 當false -> true，清除其他為 true的成false
            if (!array[itemIndex] && value) {
                for (int i = 0; i < array.length; i++) {
                    if (array[i]) {
                        array[i] = false;
                        break;
                    }
                }
            }
            array[itemIndex] = value;
        } else {
            array[itemIndex] = value;
        }
    }

    public String getCheckedItemIdCsv() {
        List<String> itemIdList =new ArrayList<String>();
        for (int i = 0; i < getItemArray().length; i++) {
            if (getCheckedItemArray()[i]) {
                itemIdList.add(getItemId(getItemArray()[i]));
            }
        }
        String itemIdCsv = TextUtils.join(separator,itemIdList);
        return itemIdCsv;
    }

    // #endregion

    public int getCheckedItemCount() {
        int count = 0;
        for (int i = 0; i < getItemArray().length; i++) {
            if (getCheckedItemArray()[i]) {
                count = count + 1;
            }
        }
        return count;
    }

    public List<T> getCheckedItemList() {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < getCheckedItemArray().length; i++) {
            if (getCheckedItemArray()[i]) {
                list.add(getItemArray()[i]);
            }
        }
        return list;
    }

    public void parse(String itemIdCsv) {
        uncheckAll();
        if (TextUtils.isEmpty(itemIdCsv)) {
            return;
        }
        // 由於之前使用 | 做為區別，所以導致解析錯誤，因此為了相容，則自動取代成 ,
        if (itemIdCsv.contains("|")) {
            itemIdCsv = itemIdCsv.replace("|", separator);
        }
        String[] itemIdArray = itemIdCsv.split(java.util.regex.Pattern
                .quote(separator));
        for (String itemId : itemIdArray) {
            check(itemId);
        }
    }

    // #region AlertDialog.Builder.setMultiChoiceItems
    // 由於android的字串可能要透過 Context才能取得，所以這裡無法提供
    public abstract int getItemNameResId(T item);


    // TODO 只能給Single Choice 使用，不好用...未來有時間請用 ListView重做
    // 參考 http://www.wingnity.com/blog/use-checkedtextview-android/
    // private ArrayList<HashMap<String, Object>> itemMapList;
    //
    // private ArrayList<HashMap<String, Object>> getItemMapList() {
    // if (itemMapList == null) {
    // synchronized (TAG) {
    // if (itemMapList == null) {
    // itemMapList = new ArrayList<HashMap<String, Object>>();
    // for (int i = 0; i < itemArray.length; i++) {
    // T item = itemArray[i];
    // HashMap<String, Object> itemMap = new HashMap<String, Object>();
    // itemMap.put("icon", getItemIconResId(item));
    // itemMap.put("text", getItemNameResId(item));
    // itemMapList.add(itemMap);
    // }
    // }
    // }
    // }
    // return itemMapList;
    // }
    // protected ListAdapter createListAdapter(Context context) {
    // SimpleAdapterFix adapter = new SimpleAdapterFix(context,
    // getItemMapList(), R.layout.multiple_choice_item,
    // //
    // new String[] { "icon", "text" },
    // //
    // new int[] { R.id.imageView, R.id.textView });
    // return adapter;
    // }

    public abstract String getItemName(T item);

    public int getItemIconResId(T item) {
        return android.R.color.transparent;
    }

    public String[] getItemNameArray(Context context) {
        if (itemNameArray == null) {
            synchronized (TAG) {
                if (itemNameArray == null) {
                    itemNameArray = new String[getItemArray().length];
                    for (int i = 0; i < getItemArray().length; i++) {
                        int nameResId = getItemNameResId(getItemArray()[i]);
                        String name = null;
                        if (nameResId == 0) {
                            if (!TextUtils.isEmpty(getItemName(getItemArray()[i]))) {
                                name = getItemName(getItemArray()[i]);
                            } else {
                                throw new RuntimeException(String.format("沒有Name %s", getItemName(getItemArray()[i]).toString()));
                            }
                        } else {
                            name = context.getResources().getString(
                                    nameResId);
                        }
                        itemNameArray[i] = name;
                    }
                }
            }
        }
        return itemNameArray;
    }

    public void setAlertDialogOnClickListener(
            DialogInterface.OnClickListener listener) {
        delegateAlertDialogClickListener = listener;
    }

    public void performDialogOkClick() {
        performDialogClick(null, DialogInterface.BUTTON_POSITIVE);
    }

    public void performDialogClick(DialogInterface dialog, int which) {
        delegateAlertDialogClickListener.onClick(dialog, which);
    }


    public void setChoiceMode(Context context, AlertDialog.Builder builder,
                              AlertDialogListener listener) {
        if (choiceMode == CHOICE_MODE_SINGLE) {
            builder.setSingleChoiceItems(getItemNameArray(context),
                    getCheckedItemIndex(), listener);
        } else {
            builder.setMultiChoiceItems(getItemNameArray(context),
                    listener.getCheckItemArray(), listener);
        }
    }

    public String getDialogTitle() {
        return null;
    }

    public AlertDialog createAlertDialog(Context context) {
        AlertDialog alertDialog;
        AlertDialogListener listener = new AlertDialogListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (getDialogTitleResId() != 0) {
            builder.setTitle(getDialogTitleResId());
        } else {
            builder.setTitle(getDialogTitle());
        }

        setChoiceMode(context, builder, listener);
        builder.setPositiveButton(android.R.string.ok, listener);
        builder.setNegativeButton(android.R.string.cancel, listener);
        alertDialog = builder.create();

        return alertDialog;
    }

    public AlertDialog showAlertDialog(Context context) {
        // Show alertDialog after building
        AlertDialog alertDialog = createAlertDialog(context);
        alertDialog.show();
        // and find positiveButton and negativeButton
        Button positiveButton = (Button) alertDialog.findViewById(android.R.id.button1);
        Button negativeButton = (Button) alertDialog.findViewById(android.R.id.button2);
        // then get their parent ViewGroup
        ViewGroup buttonPanelContainer = (ViewGroup) positiveButton.getParent();
        int positiveButtonIndex = buttonPanelContainer.indexOfChild(positiveButton);
        int negativeButtonIndex = buttonPanelContainer.indexOfChild(negativeButton);
        if (positiveButtonIndex < negativeButtonIndex) {
            // prepare exchange their index in ViewGroup
            buttonPanelContainer.removeView(positiveButton);
            buttonPanelContainer.removeView(negativeButton);
            buttonPanelContainer.addView(negativeButton, positiveButtonIndex);
            buttonPanelContainer.addView(positiveButton, negativeButtonIndex);
        }
        return alertDialog;
    }

    // #endregion

    // #region SQL
    public String getLikeWherePattern(String columnName) {
        List<String> patterns = new ArrayList<String>();

        for (T item : getCheckedItemList()) {
            // 兩個 %% 會變成 % 屬於 String.format 的 escape 文字
            // 兩個 || 是 SQLite 的文字相加
            patterns.add(String.format(
                    "( '%2$s' || %3$s || '%2$s' LIKE '%%%2$s%1$s%2$s%%' )",
                    getItemId(item), separator, columnName));
        }
        String returnedValue = TextUtils.join(" OR ",patterns);
        if (patterns.size() > 1) {
            returnedValue = " ( " + returnedValue + " ) ";
        }
        return returnedValue;
    }

    public String getRegularWherePattern(String columnName) {
        List<String> patterns = new ArrayList<String>();
        for (T item : getCheckedItemList()) {
            patterns.add(String
                    .format("( %3$s REGEXP \"^(%1$s)%2$s|%2$s(%1$s)%2$s|%2$s(%1$s)$\" )",
                            getItemId(item), separator, columnName));
        }
        String returnedValue =  TextUtils.join(" OR ",patterns);
        if (patterns.size() > 1) {
            returnedValue = "( " + returnedValue + " )";
        }
        return returnedValue;
    }
    // #endregion

    protected class AlertDialogListener implements
            DialogInterface.OnClickListener,
            DialogInterface.OnMultiChoiceClickListener {
        private boolean[] cloneCheckedItemArray;

        public AlertDialogListener() {
            cloneCheckedItemArray = booleanArraysCopyOf(getCheckedItemArray());
        }

        public boolean[] getCheckItemArray() {
            return cloneCheckedItemArray;
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // android.R.string.ok
                    if (Arrays.equals(getCheckedItemArray(), cloneCheckedItemArray)) {
                        if (disableEventIfSame) {
                            // 如果沒有改變的話，就不繼續執行
                            return;
                        }
                    } else {
                        setCheckedItemArray(null);
                        setCheckedItemArray(cloneCheckedItemArray);
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // android.R.string.cancel;
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    break;
            }
            // 支援SingleChoice會用到
            if (which >= 0) {
                modify(cloneCheckedItemArray, which, true);
            } else {
                // 一致化SingleChoice的行為，將會跟MultiChoice一樣，只有
                // DialogInterface.BUTTON_POSITIVE:
                // DialogInterface.BUTTON_NEGATIVE:
                // DialogInterface.BUTTON_NEUTRAL:
                // 才會觸發onClick事件
                if (delegateAlertDialogClickListener != null) {
                    delegateAlertDialogClickListener.onClick(dialog, which);
                }
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            modify(cloneCheckedItemArray, which, isChecked);
        }
    }
}
