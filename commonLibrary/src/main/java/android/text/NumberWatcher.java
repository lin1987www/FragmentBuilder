package android.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/9/14.
 */

public class NumberWatcher implements TextWatcher {
    private final static Pattern removeZerosPattern = Pattern.compile("(?:0+?)([123456789]+(?:\\.)?(?:\\d+)?)$");

    private int decimalPlaces = 0;
    private Pattern decimalPlacesPattern;
    private double min, max, defaultValue;
    private String newText;


    public void set(double min, double max, double defaultValue, int decimalPlaces) {
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.decimalPlaces = decimalPlaces;
        decimalPlacesPattern = Pattern.compile("(\\d+\\.\\d{" + String.valueOf(Math.max(0, decimalPlaces)) + "})(?:\\d+)");
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

    @Override
    public void afterTextChanged(Editable editable) {
        SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) editable;
        String text = spannableStringBuilder.toString();
        String tempText = text;

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
            spannableStringBuilder.replace(0, spannableStringBuilder.length(), newText);
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
            text = matcher.replaceAll("$1");
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
}
