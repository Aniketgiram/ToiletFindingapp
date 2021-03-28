package com.example.thetoiletfindingapp.ui.PublicToilet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thetoiletfindingapp.DirectionsParser;
import com.example.thetoiletfindingapp.R;
import com.example.thetoiletfindingapp.adapter;
import com.example.thetoiletfindingapp.directionhelpers.FetchURL;
import com.example.thetoiletfindingapp.directionhelpers.TaskLoadedCallback;
import com.example.thetoiletfindingapp.recycleModel;
import com.example.thetoiletfindingapp.viewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PublicToiletFragment extends Fragment implements OnMapReadyCallback, TaskLoadedCallback {

    private PublicToiletViewModel publicToiletViewModel;
    private GoogleMap  mMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("toilets").child("public");
    private Double lat, lng;
    private String key;
    private LatLng location;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    BottomSheetDialog bottomSheetDialog;
    FirebaseRecyclerOptions<recycleModel> options;
    FirebaseRecyclerAdapter<recycleModel, viewHolder> adapter1;
    MarkerOptions source, destination;
    String DestLAT, DestLNG;
    Polyline currentPolyline;
    int count = 0;

    LocationRequest locationRequest;

    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if(locationResult == null){
                return;
            }else {
                for(Location location:locationResult.getLocations()){
                    System.out.println(location);
                    currentLocation = location;
                }
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.publicMap);
                mapFragment.getMapAsync(PublicToiletFragment.this);
            }
        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        publicToiletViewModel = ViewModelProviders.of(this).get(PublicToiletViewModel.class);
        View view = inflater.inflate(R.layout.fragment_publictoilet, container, false);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        locationRequest = LocationRequest.create();
//        locationRequest.setInterval(10000);
//        locationRequest.setFastestInterval(5000);
        locationRequest.setInterval(100000000);
        locationRequest.setFastestInterval(5000000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("On Start Called");
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            System.out.println("permission granted getting location");
//            getLastloc();
            checkSettingsAndStartLocationupdate();
        }else {
            askLocPermission();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void checkSettingsAndStartLocationupdate(){
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(getContext());

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdate();
            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Faild to Get Location setting not satisfied "+e);
                ResolvableApiException apiException = (ResolvableApiException) e;
                try{
                    apiException.startResolutionForResult(getActivity(),1001);
                    System.out.println("asking re baba");
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void startLocationUpdate(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void askLocPermission(){
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }else {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
            return;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("sdfsdf");
        switch (requestCode) {
            case REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
//                        getLastloc();
                        checkSettingsAndStartLocationupdate();
                }else{
//                    permission not granted
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        final LatLng currentlocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        source = new MarkerOptions().position(currentlocation);
        MarkerOptions markerOptions = new MarkerOptions().position(currentlocation).title("This is your Current Location");
//        map.addMarker(new MarkerOptions().position(Maharashtra).title("Maharashtra"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,16));
        mMap.setMinZoomPreference(8);
        mMap.addMarker(markerOptions);
//        mMap.addCircle(new CircleOptions()
//                .center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
//                .radius(150)
//                .strokeColor(R.color.grey)
//                .fillColor(R.color.grey));
        subscribeToUpdates();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                String title = marker.getTitle();
            if(title.equals("This is your Current Location")){
                Toast.makeText(getContext(),"This is your current Location",Toast.LENGTH_SHORT).show();
            }else{
                View v = LayoutInflater.from(getContext()).inflate(R.layout.bottomsheet,null);
                Button navigate = v.findViewById(R.id.navigate);
                bottomSheetDialog = new BottomSheetDialog(getContext());
                final RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
                final RecyclerView recyclerView1 = v.findViewById(R.id.recyclerView2);
                Button closeBtmst = v.findViewById(R.id.closeDialouge);
                recyclerView1.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView1.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,true));
                myRef.child(marker.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
                        String services = (String) value.get("services").toString();
                        String available = (String) value.get("Available_for").toString();
                        String landmark = (String) value.get("Landmark").toString();
                        String loc = (String) value.get("Location").toString();
                        String type = (String) value.get("Type").toString();
                        String tow = (String) value.get("Type_of_washroom").toString();
                        String dim = (String) value.get("doors_in_mens").toString();
                        String es = (String) value.get("extra_services").toString();
                        String paid = (String) value.get("paid").toString();
                        String sm = value.get("sink_men").toString();
                        String sim = (String) value.get("stalls_in_mens").toString();
                        String siw = (String) value.get("stalls_in_women").toString();
                        String urinals = (String) value.get("urinals").toString();
                        String id = (String) value.get("id").toString();
                        DestLAT = value.get("latitude").toString();
                        DestLNG = value.get("longitude").toString();

                        List<String> myList = new ArrayList<String>();
                        myList.add("Services");
                        if (!services.isEmpty())
                        {
                            myList.add("Services : "+services);
                        }if(!es.isEmpty()){
                            myList.add("Extra Services : "+es);
                        }if (!available.isEmpty())
                        {
                            myList.add("Available For : "+available);
                        }if (!landmark.isEmpty()) {
                            myList.add("Landmark : "+landmark);
                        }if (!loc.isEmpty()){
                            myList.add("Location : "+loc);
                        }if(!type.isEmpty()) {
                            myList.add("Type : "+type);
                        }if(!tow.isEmpty()){
                            myList.add("Type of Washroom : "+tow);
                        }if (!paid.isEmpty()){
                            myList.add("Paid : "+paid);
                        }if(!sm.isEmpty()){
                            myList.add("Sink in Men : "+sm);
                        }if(!sim.isEmpty()){
                            myList.add("Stalls in Men : "+sim);
                        }if(!siw.isEmpty()){
                            myList.add("Stalls in Women : "+siw);
                        }if(!urinals.isEmpty()){
                            myList.add("Urinals : "+urinals);
                        }

                        DatabaseReference mref = database.getReference().child("toilets").child("public").child(marker.getTitle()).child("links");


                        options = new FirebaseRecyclerOptions.Builder<recycleModel>().setQuery(mref,recycleModel.class).build();

                        adapter1 = new FirebaseRecyclerAdapter<recycleModel, viewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(viewHolder holder, int position,recycleModel model) {
//
                                Picasso.get().load(model.getImage()).into(holder.mimageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                            @Override
                            public viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, viewGroup, false);
                                return new viewHolder(v);
                            }
                        };

                        adapter1.startListening();
                        recyclerView1.setAdapter(adapter1);

//                        Image slider

                        recyclerView.setAdapter(new adapter(myList));
//                        Log.d("TAG", "Value is: " + services);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                bottomSheetDialog.setContentView(v);
                bottomSheetDialog.show();

                closeBtmst.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                navigate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        double lat = Double.parseDouble(DestLAT);
                        double lng = Double.parseDouble(DestLNG);
                        destination = new MarkerOptions().position(new LatLng(lat,lng));

//                        MarkerOptions src = new MarkerOptions().position(new LatLng(17.82456602,73.96323));
//                        MarkerOptions dest = new MarkerOptions().position(new LatLng(16.84022833,74.29106851));

//                        String url = getUrl(source.getPosition(),destination.getPosition(),"driving");
                        String url = getUrl(source.getPosition(),destination.getPosition(),"driving");

                        new FetchURL(PublicToiletFragment.this).execute(url,"driving");

                        bottomSheetDialog.dismiss();
//                        System.out.println(url);

//                        System.out.println("Source => "+ source.getPosition() + " Destination => "+ destination.getPosition());

//                        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
//                        taskRequestDirections.execute(url);
                    }
                });

                return true;
            }
            return false;
            }
        });
    }



    private String getUrl(LatLng position, LatLng position1, String driving) {
        String str_origin = "origin="+position.latitude+","+position.longitude;
        String str_dest = "destination="+position1.latitude+","+position1.longitude;
        String mode = "mode="+driving;
        String parameter = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameter + "&key=" + getString(R.string.google_maps_key);
        return  url;
    }




    private void subscribeToUpdates() {

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }


        });
    }
    private void setMarker(DataSnapshot dataSnapshot) {

        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        key = dataSnapshot.getKey();
        final HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        lat = Double.parseDouble(value.get("latitude").toString());
        lng = Double.parseDouble(value.get("longitude").toString());
//        double dist = distance(18.2079899,73.9369005,lat,lng);
//        double dist = distance(18.655557,73.77107,lat,lng);//Akurdi
        double dist = distance(currentLocation.getLatitude(),currentLocation.getLongitude(),lat,lng);

        if (Math.round(dist * 100.0) / 100.0 <= 10000.00){
            if(lat!=null && lng!=null){
            location = new LatLng(lat, lng);
            if (!mMarkers.containsKey(key)) {
                mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location)));
            } else {
                mMarkers.get(key).setPosition(location);
                System.out.println("set position");
            }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : mMarkers.values()) {
                    builder.include(marker.getPosition());
                }
            }
            else {
                System.out.println("sorry no toilet");
            }
        }
//        else {
//            if(count < 1){
//                Toast.makeText(this.getContext(),"Sorry no toilets near you!",Toast.LENGTH_SHORT).show();
//            }
//            count++;
//        }

    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        // haversine great circle distance approximation, returns meters
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of seperation
        dist = dist * 1852; // 1852 meters per nautical mile
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline!=null)
            currentPolyline.remove();
        currentPolyline= mMap.addPolyline((PolylineOptions)values[0]);

    }
}