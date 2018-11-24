package au.id.micolous.andprox.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CommandCompletionAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = CommandCompletionAdapter.class.getSimpleName();
    private CommandFilter mFilter;
    private final Context mContext;
    private final LayoutInflater mInflater;

    private static final String[] COMMANDS = {
            "help",
            "hell",
            "hello"
    };

    private ArrayList<String> mSuggestions = new ArrayList<>();

    public CommandCompletionAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getsize()");
        return mSuggestions.size();
    }

    @Override
    public String getItem(int position) {
        Log.d(TAG, "getitem " + Integer.toString(position));
        return mSuggestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(mInflater, position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public Filter getFilter() {
        Log.d(TAG, "getfilter()");
        if (mFilter != null) {
            mFilter = new CommandFilter();
        }

        return mFilter;
    }

    private @NonNull View createViewFromResource(@NonNull LayoutInflater inflater, int position,
                                                 @Nullable View convertView, @NonNull ViewGroup parent, int resource) {
        final View view;
        final TextView text;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }


        try {
            //  If no custom field is assigned, assume the whole resource is a TextView
            text = (TextView) view;

            /*
            } else {
                //  Otherwise, find the TextView field within the layout
                text = view.findViewById(mFieldId);

                if (text == null) {
                    throw new RuntimeException("Failed to find view with ID "
                            + mContext.getResources().getResourceName(mFieldId)
                            + " in item layout");
                }
            }
            */
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        final String item = getItem(position);
        text.setText(item);


        return view;
    }



    private class CommandFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            Log.d(TAG, "filtering on: " + (prefix == null ? "null" : prefix));
            final FilterResults results = new FilterResults();

            if (prefix == null) {
                prefix = "";
            }

            final ArrayList<String> resultArray = new ArrayList<>();
            resultArray.add(prefix + "-abc");
            resultArray.add(prefix + "-xyz");

            results.values = resultArray;
            results.count = resultArray.size();

            /*
            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                final ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                final ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }
            */

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mSuggestions = (ArrayList<String>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
