package com.ziedkh.tracker;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import  android.widget.BaseAdapter;
import  java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import  android.view.*;
import  android.widget.*;
//import android.widget.ListView;
import android.content.Intent;
import android.provider.ContactsContract;
import android.app.*;
import android.net.Uri;
import  android.database.Cursor;
import  android.os.Build;
import  android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyTracker extends AppCompatActivity {

    ArrayList<AdapterItems>    listnewsData = new ArrayList<AdapterItems>();
    MyCustomAdapter myadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tracker);
       // listnewsData.add(new AdapterItems("zied","21514692"));
        myadapter=new MyCustomAdapter(listnewsData);
        ListView  lsNews=(ListView)findViewById(R.id.listView);
        lsNews.setAdapter(myadapter);//data

        Refresh();
        lsNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position , long id) {

                GlobaLInfo.MyTrackers.remove(listnewsData.get(position).PhoneNumber);
                DatabaseReference nDatabase = FirebaseDatabase.getInstance().getReference();
                nDatabase.child("Users").child(listnewsData.get(position).PhoneNumber).child("Finders").
                        child(GlobaLInfo.PhoneNumber).removeValue();
                GlobaLInfo globaLInfo= new GlobaLInfo(getApplicationContext());
                globaLInfo.SaveData();
                Refresh();
            }
        });


    }

    void Refresh(){
        listnewsData.clear();
        for (Map.Entry m:GlobaLInfo.MyTrackers.entrySet()){
            listnewsData.add(new AdapterItems(m.getValue().toString(),m.getKey().toString()));
        }
        myadapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contact_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.goback:
                GlobaLInfo globaLInfo= new GlobaLInfo(this);
                globaLInfo.SaveData();
                finish();
                return true;
            case R.id.add :
                CheckUserPermsions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void CheckUserPermsions(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }

        PickContact();

    }
    //get acces to location permsion
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PickContact();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    void PickContact(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    // Declare
    static final int PICK_CONTACT=1;
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {


                        String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String cNumber="No number";
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                    null, null);
                            phones.moveToFirst();
                            cNumber =GlobaLInfo.FormatPhoneNumber (phones.getString(phones.getColumnIndex("data1")));
                            System.out.println("number is:"+cNumber);
                        }
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        GlobaLInfo.MyTrackers.put(cNumber,name);
                        DatabaseReference nDatabase = FirebaseDatabase.getInstance().getReference();
                        nDatabase.child("Users").child(cNumber).child("Finders").
                                child(GlobaLInfo.PhoneNumber).setValue(true);

                        GlobaLInfo globaLInfo= new GlobaLInfo(this);
                        globaLInfo.SaveData();
                        Refresh();


                        //update firebase and
                        //update list
                        //update database
                    }
                }
                break;
        }
    }

    private class MyCustomAdapter extends BaseAdapter{

        public  ArrayList<AdapterItems>  listnewsDataAdpater ;

        public MyCustomAdapter(ArrayList<AdapterItems>  listnewsDataAdpater) {
            this.listnewsDataAdpater=listnewsDataAdpater;
        }


        @Override
        public int getCount() {
            return listnewsDataAdpater.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(R.layout.single_row_conact, null);

            final   AdapterItems s = listnewsDataAdpater.get(position);

            TextView tv_user_name=( TextView)myView.findViewById(R.id.tv_user_name);
            tv_user_name.setText(s.UserName);

            TextView tv_phone=( TextView)myView.findViewById(R.id.tv_phone);
            tv_phone.setText(s.PhoneNumber);




            return myView;
        }

    }


}
