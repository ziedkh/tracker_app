package com.ziedkh.tracker;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class MyService extends IntentService {

    public static  boolean IsRunning = false;
    DatabaseReference  dbr;
    public MyService(){
        super("MyService");
        IsRunning = true;
        dbr = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

       dbr.child("Users").child(GlobaLInfo.PhoneNumber).child("Updates").addValueEventListener(new ValueEventListener() {

           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               Location location= TrackLocation.location;
               dbr.child("Users").child(GlobaLInfo.PhoneNumber).
                       child("Location").child("Lat").setValue(location.getLatitude());

               dbr.child("Users").child(GlobaLInfo.PhoneNumber).
                       child("Location").child("Lag").setValue(location.getLongitude());

               DateFormat df = new SimpleDateFormat("YYYY/MM/DD HH:MM:SS");

               dbr.child("Users").child(GlobaLInfo.PhoneNumber).
                       child("Location").child("LastOnLineDate").setValue(df.format(new Date()).toString());



           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }
}
