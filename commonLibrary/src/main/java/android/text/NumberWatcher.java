package android.text;

import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/9/14.
 */

public class NumberWatcher implements TextWatcher, InputFilter, Runnable {
    private final static Pattern removeZerosPattern = Pattern.compile("(-?)(?:0+?)([123456789]+(?:\\.)?(?:\\d+)?)$");
    private final static String MINUS = "-";

    private int decimalPlaces = 0;
    private Pattern decimalPlacesPattern;
    private double min, max, defaultValue;
    private String newText;
    private EditText mEditText;

    public NumberWatcher(EditText editText) {
        mEditText = editText;
        mEditText.addTextChangedListener(this);
        mEditText.setFilters(new InputFilter[]{this});
    }

    public void set(double min, double max, double defaultValue, int decimalPlaces) {
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.decimalPlaces = decimalPlaces;
        decimalPlacesPattern = Pattern.compile("(-?\\d+\\.\\d{" + String.valueOf(Math.max(0, decimalPlaces)) + "})(?:\\d+)");
    }

    @Override
    public void beforeTextChanged(CharSequence s,
                                  int start,
                                  int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s,
                              int start,
                              int before,
                              int count) {

    }

    public boolean isInRange() {
        boolean IsInAGivenRange = false;
        if (decimalPlacesPattern != null) {
            try {
                double value = Double.parseDouble(mEditText.getText().toString());
                IsInAGivenRange = true;
            } catch (Throwable throwable) {
            }
        }
        return IsInAGivenRange;
    }

    public String getTextToString() {
        return mEditText.getText().toString();
    }

    public void setNumber(double number) {
        mEditText.setText(shortNumberString(number));
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (decimalPlacesPattern == null) {
            return;
        }

        SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) editable;
        String text = spannableStringBuilder.toString();
        String tempText = text;

        // 有設定newText的話，必須等到設定完成後才能往下繼續執行
        if (newText != null) {
            if (newText.equals(text)) {
                newText = null;
                return;
            } else {
                return;
            }
        }

        if (text != null && text.trim().equals("-")) {
            return;
        }

        double value = defaultValue;
        try {
            value = Double.parseDouble(text);
        } catch (Throwable throwable) {
            tempText = shortNumberString(value);
        }

        if (min <= value && value <= max) {
            // OK
            // 拿掉數字前多餘的0
            tempText = removeZeros(tempText);
            // decimalPlacesPattern  取小數點第幾位
            tempText = toDecimalPlaces(tempText);
            // Final
            if (!tempText.equals(text)) {
                newText = tempText;
            }
        } else if (min > value) {
            newText = shortNumberString(min);
        } else if (max < value) {
            newText = shortNumberString(max);
        }
        if (newText != null) {
            int originSelectionStart = newText.length();
            mEditText.setText(newText);
            originSelectionStart = Math.min(originSelectionStart, mEditText.getText().length());
            mEditText.setSelection(originSelectionStart);
            // spannableStringBuilder.replace(0, spannableStringBuilder.length(), newText, 0, newText.length());
        }
    }

    private String shortNumberString(double value) {
        long valueLong = Double.valueOf(value).longValue();
        String shortNumberString;
        if (Math.ceil(valueLong) == value) {
            shortNumberString = String.valueOf(valueLong);
        } else {
            shortNumberString = String.valueOf(value);
        }
        shortNumberString = toDecimalPlaces(shortNumberString);
        return shortNumberString;
    }

    private String removeZeros(String text) {
        Matcher matcher;
        // 拿掉數字前多餘的0
        matcher = removeZerosPattern.matcher(text);
        if (matcher.matches()) {
            text = matcher.replaceAll("$1$2");
        }
        return text;
    }

    private String toDecimalPlaces(String text) {
        if (decimalPlacesPattern != null) {
            Matcher matcher;
            // decimalPlacesPattern  取小數點第幾位
            matcher = decimalPlacesPattern.matcher(text);
            if (matcher.matches()) {
                text = matcher.replaceAll("$1");
            }
        }
        return text;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (newText != null || decimalPlacesPattern == null) {
            return null;
        }
        if (min < 0d) {
            if (MINUS.equals(source.toString())) {
                mEditText.post(this);
            }
        }
        return null;
    }

    @Override
    public void run() {
        // 處理正負數，當按下減字鍵會自動改變數字符號
        String signedText = mEditText.getText().toString();
        int originSelectionStart = mEditText.getSelectionStart();
        if (signedText.startsWith(MINUS)) {
            signedText = signedText.substring(1);
            originSelectionStart -= 1;
        } else {
            signedText = MINUS + signedText;
            originSelectionStart += 1;
        }
        mEditText.setText(signedText);
        originSelectionStart = Math.min(originSelectionStart, mEditText.getText().length());
        mEditText.setSelection(originSelectionStart);
    }
}
