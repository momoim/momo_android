
package cn.com.nd.momo.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.Country;
import cn.com.nd.momo.api.util.Log;

public class CountryAdapter extends BaseAdapter {
    private String TAG = "ContryAdapter";

    private List<Country> mArrayData = new ArrayList<Country>();

    private Context mContext;

    private ArrayList<Integer> mPositions = new ArrayList<Integer>();

    private final String[] mIndexer = new String[] {
            "#", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q",
            "R", "S", "T",
            "U", "V", "W",
            "X", "Y", "Z"
    };

    public CountryAdapter(Context c) {
        mContext = c;
    }

    public void SetDataArray(List<Country> arrayData) {
        if (arrayData == null) {
            Log.e(TAG, "parameter arrayData is null");
            return;
        }

        mArrayData.clear();
        mArrayData.addAll(arrayData);

        // if is contact list, refresh indexer for new array
        refreshIndexer();

        notifyDataSetChanged();
    }

    public int getPositionByIndexerLetter(String indexer) {
        int nIndex = 0;
        for (nIndex = 0; nIndex < mIndexer.length; nIndex++) {
            if (mIndexer[nIndex].equals(indexer)) {
                break;
            }
        }

        return mPositions.get(nIndex);
    }

    @Override
    public int getCount() {
        return mArrayData.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mArrayData.size()) {
            return mArrayData.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return mArrayData.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView txtName = null;
        TextView txtCode = null;

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.country_list_item, null);
        }

        View v = convertView;

        txtName = (TextView)v.findViewById(R.id.txt_country_name);
        txtCode = (TextView)v.findViewById(R.id.txt_country_code);

        txtName.setText(mArrayData.get(position).getCnName());
        txtCode.setText(mArrayData.get(position).getZoneCode());

        return v;
    }

    private void refreshIndexer() {
        int Pos = 0;

        // check contact list
        if (mArrayData == null) {
            Log.e(TAG, "refreshIndexer: find mGroup not initialize");
            return;
        }

        // init position indexer
        mPositions.clear();
        for (int i = 0; i < 27; i++) {
            mPositions.add(new Integer(-1));
        }

        if (mArrayData.size() == 0) {
            return;
        }

        // find first position of letters
        String prevHeaderText = null;
        String[][] tempArray;
        String tempString = "";
        for (; Pos < mArrayData.size(); Pos++) {
            // get first letter
            Country c = mArrayData.get(Pos);
            String header = "";
            tempArray = c.getCnNamePinyin();
            if (tempArray.length > 0) {
                tempString = c.getCnNamePinyin()[0][0];
            } else {
                tempString = "";
            }

            if (tempString.length() >= 1) {
                header = tempString.substring(0, 1).toLowerCase();
            }

            if (header.length() == 0) {
                header = "#";
            }

            // compare with last one
            if (header.equals(prevHeaderText)) {
                continue;

            } else {
                prevHeaderText = header;

            }

            // set letter indexer position
            int nCharPos = header.charAt(0) - 'a';
            if (nCharPos >= 0 && nCharPos < 26) {
                if (mPositions.get(nCharPos + 1) == -1) {
                    mPositions.set(nCharPos + 1, new Integer(Pos));
                }

            } else {
                if (mPositions.get(0) == -1) {
                    mPositions.set(0, new Integer(Pos));
                }
            }
        }

        // set other position which is null (make same as previous one)
        for (int i = 1; i < mPositions.size(); i++) {
            if (mPositions.get(i) == -1) {
                mPositions.set(i, mPositions.get(i - 1));
            }
        }
    }

}
