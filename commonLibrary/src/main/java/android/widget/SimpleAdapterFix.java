package android.widget;

import android.content.Context;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class SimpleAdapterFix extends SimpleAdapter {

    public SimpleAdapterFix(Context context,
                            List<? extends Map<String, ?>> data, int resource, String[] from,
                            int[] to) {
        super(context, data, resource, from, to);
        // 如果是 Integer 則使用ResourceId
        setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof TextView) {
                    if (data instanceof Integer) {
                        ((TextView) view).setText((Integer) data);
                        return true;
                    }
                }
                return false;
            }
        });
    }

}
