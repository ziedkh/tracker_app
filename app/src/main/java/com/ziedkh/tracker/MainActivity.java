package com.ziedkh.tracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.LocationManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<AdapterItems> listnewsData = new ArrayList<AdapterItems>();
    MyCustomAdapter myadapter;
    DatabaseReference dbr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobaLInfo globaLInfo = new GlobaLInfo(this);
        globaLInfo.LoadData();
        dbr = FirebaseDatabase.getInstance().getReference();
        CheckUserPermsions();
       // Refresh();
        myadapter = new MyCustomAdapter(listnewsData);
        ListView lsNews = (ListView) findViewById(R.id.listView);
        lsNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //Get the position of the same user
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AdapterItems adapterItems = listnewsData.get(position);
                GlobaLInfo.UpdatesInfo(adapterItems.PhoneNumber);
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("PhoneNumber", adapterItems.PhoneNumber);
                //take the phone number and start another activity (map)
                startActivity(intent);
            }
        });
        lsNews.setAdapter(myadapter);//intisal with data

    }

    @Override
    public  void onResume(){
        super.onResume();
        Refresh();
    }

    void Refresh() {
        listnewsData.clear();
     /* for (Map.Entry m:GlobaLInfo.MyTrackers.entrySet()){
            listnewsData.add(new AdapterItems(m.getValue().toString(),m.getKey().toString()));
        }*/

        dbr.child("Users").child(GlobaLInfo.PhoneNumber).
                child("Finders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();

                listnewsData.clear();
                if (td == null)  //no one allow you to find him
                {
                    listnewsData.add(new AdapterItems("NoTicket", "no_desc"));
                    myadapter.notifyDataSetChanged();
                    return;
                }
                // List<Object> values = td.values();


                // get all contact to list
                ArrayList<AdapterItems> list_contact = new ArrayList<AdapterItems>();
                Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    list_contact.add(new AdapterItems(name, GlobaLInfo.FormatPhoneNumber(phoneNumber)
                    ));


                }


// if the name is save chane his text
                // case who find me
                String tinfo;
                for (String Numbers : td.keySet()) {
                    for (AdapterItems cs : list_contact) {

                        //IsFound = SettingSaved.WhoIFindIN.get(cs.Detals);  // for case who i could find list
                        if (cs.PhoneNumber.length() > 0)
                            if (Numbers.contains(cs.PhoneNumber)) {
                                listnewsData.add(new AdapterItems(cs.UserName, cs.PhoneNumber));
                                break;
                            }

                    }

                }
                myadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        myadapter.notifyDataSetChanged();
    }

    // another  alternative of google api
    void CheckUserPermsions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },

                          REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        StartService();

    }

    //get acces to location permsion
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    StartService();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "your message", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void StartService() {

        //start location track
        if (!TrackLocation.isRunning) {


            TrackLocation trackLocation = new TrackLocation();
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, trackLocation);
        }

        if (!MyService.IsRunning){
            Intent intent = new Intent(this ,MyService.class );
            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.addTracker :
                Intent intent = new Intent(this , MyTracker.class);
                startActivity(intent);

                return true;
            case R.id.help:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyCustomAdapter extends BaseAdapter {

        public ArrayList<AdapterItems> listnewsDataAdpater ;

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

            final   AdapterItems s = listnewsDataAdpater.get(position);

            if (s.UserName.equals("NoTicket")){

                View myView = mInflater.inflate(R.layout.news_ticket_no_news, null);

                return myView;
            }
            else {
                View myView = mInflater.inflate(R.layout.single_row_conact, null);
                TextView tv_user_name = (TextView) myView.findViewById(R.id.tv_user_name);
                tv_user_name.setText(s.UserName);

                TextView tv_phone = (TextView) myView.findViewById(R.id.tv_phone);
                tv_phone.setText(s.PhoneNumber);


                return myView;
            }
        }

    }



}
