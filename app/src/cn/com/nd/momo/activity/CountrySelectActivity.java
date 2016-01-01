
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.adapters.CountryAdapter;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.types.Country;
import cn.com.nd.momo.api.util.ContactFilter;
import cn.com.nd.momo.view.AlphabeticBar;

public class CountrySelectActivity extends ListActivity {

    // controllers
    private EditText mTxtSearch = null;

    // list view
    private ListView mList = null;

    private CountryAdapter mAdapter = null;

    // data
    private HashMap<Integer, Country> mArrayData = new HashMap<Integer, Country>();

    private ArrayList<Contact> mArrayTempContact = new ArrayList<Contact>();

    private TextSearchWatcher mTxtWatcher = new TextSearchWatcher();

    private TextView mTxtIndexerView;

    private AlphabeticBar mAlphabeticBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.country_select);

        // set text change event
        mTxtSearch = (EditText)findViewById(R.id.txt_country_search);
        mTxtSearch.addTextChangedListener(mTxtWatcher);
        mTxtSearch.requestFocus();

        // change country to contact temporary
        List<Country> marr = Country.getCountryList(this);
        for (Country c : marr) {
            mArrayData.put(c.getId(), c);
            Contact contact = new Contact();
            contact.setContactId(c.getId());
            contact.setFormatName(c.getCnName());
            contact.setNamePinyin(c.getCnNamePinyin());
            mArrayTempContact.add(contact);
        }

        mList = getListView();
        mAdapter = new CountryAdapter(this);
        mList.setOnItemClickListener(new OnListItemClick());
        mList.setAdapter(mAdapter);

        mTxtIndexerView = (TextView)findViewById(R.id.txt_letter_index);
        mTxtIndexerView.setVisibility(View.GONE);

        // initialize indexer list
        // -----------------------------------------------------------
        mAlphabeticBar = (AlphabeticBar)findViewById(R.id.alphabetic_bar);
        mAlphabeticBar.setOnTouchingLetterChangedListener(new LetterListViewListener());

        // sort array list
        GlobalContactList.getInstance().sortContactByPinyin(mArrayTempContact);

        marr.clear();
        for (Contact c : mArrayTempContact) {
            marr.add(mArrayData.get((int)c.getContactId()));
        }
        mAdapter.SetDataArray(marr);

        // initialize contact filter
        ContactFilter.setToBeFilteredContactsList(mArrayTempContact);
        ContactFilter.setQuanpinSupported(true);
        ContactFilter.setHandler(null);

    }

    private class LetterListViewListener implements
            cn.com.nd.momo.view.AlphabeticBar.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            mTxtIndexerView.setText(s);
            mTxtIndexerView.setVisibility(View.VISIBLE);

            // get position by indexer letter
            int nPos = mAdapter.getPositionByIndexerLetter(s);

            // if list contact contain this indexer, scroll to the right
            // position
            if (nPos != -1) {
                mList.setSelection(nPos + mList.getHeaderViewsCount());
            }
        }

        @Override
        public void onChangeIsTouch(boolean isTouch) {
            mTxtIndexerView.setVisibility(View.GONE);
        }

    }

    private class TextSearchWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            List<Contact> filteredData = ContactFilter.doFilter(s.toString());

            ArrayList<Country> fData = new ArrayList<Country>();

            // get filtered data
            for (Contact data : filteredData) {
                fData.add(mArrayData.get((int)data.getContactId()));
            }

            mAdapter.SetDataArray(fData);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    private class OnListItemClick implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Country c = mArrayData.get((int)arg3);
            if (c != null) {
                Intent i = new Intent();
                i.putExtra("country", c.getCnName());
                i.putExtra("code", c.getZoneCode());

                setResult(RESULT_OK, i);
                finish();
            }
        }
    }

}
