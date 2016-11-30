package com.example.safetysitter.safetysitter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


import static com.example.safetysitter.safetysitter.RegistrationActivity.TAG;
import static com.example.safetysitter.safetysitter.RegistrationActivity.checkValidityNumberPhone;
import static com.example.safetysitter.safetysitter.RegistrationActivity.isHebrew;
import static com.example.safetysitter.safetysitter.RegistrationActivity.safetysitterRegistrationViewPager;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.nameToConnect;
import static com.example.safetysitter.safetysitter.ScreenSlideSafetySitterFragmentThirdPage.phoneToConnect;

/**
 * Created by omeredut on 17/11/2016.
 */

public class ScreenSlideSafetySitterFragmentSecondPage extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {


    public static final String EMPTY_SEARCH_STRING = "";
    private EditText numberPhoneToConnectEditText;
    private String numberPhoneToConnect = null;
    private ImageView btnNumberPhoneToConnectImageView;




    //contacts list
    private ListView contactsListView;
    private SimpleCursorAdapter contactsCursorAdapter;


    //@SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = { Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME/*, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI*/,ContactsContract.Contacts._ID};
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = { R.id.name_contact_text_view};
    // Define global mutable variables
    // Define a ListView object






    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        contactsCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        contactsCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        onClickItemContact();
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        contactsListView = (ListView) getActivity().findViewById(R.id.list_view_contacts_list);
        Cursor mCursor = getContacts(EMPTY_SEARCH_STRING);

        contactsCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_contact_list_view, mCursor, FROM_COLUMNS, TO_IDS, 0);


        contactsListView.setAdapter(contactsCursorAdapter);
        getLoaderManager().initLoader(0, null,  this);
        contactsCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getCursor(constraint.toString());
            }
        });
        contactsListView.setOnItemClickListener(this);

        /* //hidden keyboard
        View viewOnFocus = this.getActivity().getCurrentFocus();
        if (viewOnFocus != null) {
            InputMethodManager imm = (InputMethodManager)this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewOnFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }*/


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);

        final ViewGroup secondRootView;
        if (isHebrew){
            secondRootView = (ViewGroup) inflater.inflate(R.layout.fragment_second_page_screen_slide_safety_sitter_heb, container, false);
        } else {
            secondRootView = (ViewGroup) inflater.inflate(R.layout.fragment_second_page_screen_slide_safety_sitter, container, false);
        }




        numberPhoneToConnectEditText = (EditText) secondRootView.findViewById(R.id.edit_text_number_phone_to_connect);
        numberPhoneToConnectEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                contactsCursorAdapter.getFilterQueryProvider().runQuery(charSequence);
            }
            @Override
            public void afterTextChanged(Editable editable) {
                contactsCursorAdapter.getFilter().filter(editable.toString());
            }
        });



        btnNumberPhoneToConnectImageView = (ImageView) secondRootView.findViewById(R.id.btn_image_view_phone_to_connect);
        btnNumberPhoneToConnectImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNumberPhoneToConnectImageView.setEnabled(false);
                numberPhoneToConnect = String.valueOf(numberPhoneToConnectEditText.getText());
                if (!checkValidityNumberPhone(numberPhoneToConnect)){
                    Toast.makeText(getActivity(), "the number is not validity", Toast.LENGTH_SHORT).show();
                    btnNumberPhoneToConnectImageView.setEnabled(true);
                    return;
                } else {
                    btnNumberPhoneToConnectImageView.setEnabled(true);
                    phoneToConnect = numberPhoneToConnect;
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(secondRootView.getWindowToken(), 0);
                    safetysitterRegistrationViewPager.setCurrentItem(2, true);
                }
            }
        });

        //hidden keyboard
        numberPhoneToConnectEditText.clearFocus();


        return secondRootView;



    }




    private Cursor getContacts(String searchString) {
        // Run query
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts._ID};
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP+" = "+
                1  + " AND "+  ContactsContract.Contacts.HAS_PHONE_NUMBER +" = "+ 1
                + " AND " + ContactsContract.Contacts.DISPLAY_NAME + " LIKE '" + searchString + "%'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        return cursor;
    }

    private Cursor getCursor(String searchString) {
        Cursor mCursor = null;
        if (searchString== null  ||  searchString.length() == 0) {
            mCursor = getContacts(EMPTY_SEARCH_STRING);
        }
        else {
            mCursor = getContacts(searchString);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    private void onClickItemContact(){
        Log.d(TAG, "item clicked");
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor phoneCursor = contactsCursorAdapter.getCursor();
        String id = null;
        if (phoneCursor.getCount() > 0) {
            id = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            nameToConnect = name;

            Cursor getPhoneNumberCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
            String phoneNo = null;
            while (getPhoneNumberCursor.moveToNext()) {
                phoneNo = getPhoneNumberCursor.getString(getPhoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneToConnect = phoneNo;
                safetysitterRegistrationViewPager.setCurrentItem(2, true);
                System.out.println("name: " + name + " phoneNo: " + phoneNo);
            }
            getPhoneNumberCursor.close();
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
