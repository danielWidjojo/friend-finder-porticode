package com.example.daniel.tcpnavigationdrawer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    DatabaseReference locationDatabase;
    String ownId="Daniel";
    String friendId="William";
    FloatingActionButton fab ;
    Double latitudeFriend, longtitudeFriend, latitudeMeetUp,longtitudeMeetUp;
    String friendTitle, meetUpTitle;
    Boolean meetingPointSet=false;
    List<LocationPhone> locationList;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused LocationPhone Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused LocationPhone Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    PlaceAutocompleteFragment autocompleteFragment ;

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent lintent = new Intent(this, LocationService.class);
        this.stopService(lintent);
        Toast.makeText(getApplicationContext(),"Map Activity on Destroy",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        autocompleteFragment= (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                Toast.makeText(getApplicationContext(),"Place: " + place.getName(),Toast.LENGTH_LONG).show();
                //final Place myPlace = place.get(0);
                LatLng queriedLocation = place.getLatLng();

                Log.v("Latitude is", "" + queriedLocation.latitude);
                Log.v("Longitude is", "" + queriedLocation.longitude);

                Toast.makeText(getApplicationContext(),"Place: " + queriedLocation.latitude,Toast.LENGTH_LONG).show();
                mMap.addMarker(new MarkerOptions().position(new LatLng(queriedLocation.latitude, queriedLocation.longitude)).title("Hello world"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(queriedLocation.latitude,
                                queriedLocation.longitude), DEFAULT_ZOOM));
                updateMeetingPoint(ownId,true,String.valueOf(queriedLocation.latitude),String.valueOf(queriedLocation.longitude));


            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        fab= (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
                updateMeetingPoint(ownId,false,String.valueOf(0),String.valueOf(0));

            }
        });

        Intent intent = new Intent(this, LocationService.class);
        startService(intent);

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        else if(item.getItemId() == R.id.action_settings){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //starting the activity with intent
            startActivity(intent);
        }
        return true;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setPadding(40,50,40,40);

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My LocationPhone layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.

        detectChangeFirebaseToClearMap();
        getDeviceLocation();

        downloadLocation();
        downloadMeetingPoint();
        }



    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            updateLocation(ownId,String.valueOf(mLastKnownLocation.getLatitude()),String.valueOf(mLastKnownLocation.getLongitude()));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            Toast.makeText(getApplicationContext(),"LocationPhone Permission is not granted",Toast.LENGTH_LONG).show();
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.

            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            Toast.makeText(getApplicationContext(),"LocationPhone Permission is granted",Toast.LENGTH_LONG).show();

            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
                updateMeetingPoint(ownId,true,String.valueOf(markerLatLng.latitude),String.valueOf(markerLatLng.longitude));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void updateLocation(String deviceId,String latitude,String longtitude){

        if(!TextUtils.isEmpty((deviceId))&&!TextUtils.isEmpty((latitude))&&!TextUtils.isEmpty((longtitude))){
            //generating unique id number
            //String id= databaseArtist.push().getKey();
            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("location");
            LocationPhone locationPhone = new LocationPhone(deviceId,latitude, longtitude);
            dR.child(deviceId).setValue(locationPhone);
            Toast.makeText(this,"locationPhone Updated",Toast.LENGTH_LONG).show();

        }else {
            Toast.makeText(this,"LocationPhone failed to Update",Toast.LENGTH_LONG).show();
        }
    }
    private void updateMeetingPoint(String deviceId,Boolean set,String latitude,String longtitude){

        if(!TextUtils.isEmpty((deviceId))&&!TextUtils.isEmpty((latitude))&&!TextUtils.isEmpty((longtitude))){
            //generating unique id number
            //String id= databaseArtist.push().getKey();
            DatabaseReference dR1 = FirebaseDatabase.getInstance().getReference("meetingPoint");
            LocationMeetingPoint locationMeetingPoint = new LocationMeetingPoint(deviceId,set,latitude, longtitude);
            dR1.child("meetUp").setValue(locationMeetingPoint);
            Toast.makeText(this,"location Meeting Point Updated",Toast.LENGTH_LONG).show();

        }else {
            Toast.makeText(this,"Location Meeting Point failed to Update",Toast.LENGTH_LONG).show();
        }
    }
    private void downloadLocation(){
        //attaching value event listener
        locationDatabase = FirebaseDatabase.getInstance().getReference("location").child(friendId);
        locationDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    locationList.clear();}
                catch (Exception e){
                    System.out.println("Error clear artist list");
                }


                    //LocationPhone location=artistSnapshot.getValue(LocationPhone.class);

                    //locationList.add(location);
                    LocationPhone post = dataSnapshot.getValue(LocationPhone.class);
                    System.out.println(post);




                    if(post!=null){

                        /*mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(post.getLatitude()), Double.parseDouble(post.getLongtitude())))
                                .title("Location of "+post.deviceId));
                    }
                    else{
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(10, 10))
                                .title("Hello world"));
                                */
                        friendTitle="Location of "+post.deviceId;
                        latitudeFriend=Double.parseDouble(post.getLatitude());
                        longtitudeFriend=Double.parseDouble(post.getLongtitude());
                    }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void downloadMeetingPoint(){
        //attaching value event listener
        locationDatabase = FirebaseDatabase.getInstance().getReference("meetingPoint").child("meetUp");
        locationDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    locationList.clear();}
                catch (Exception e){
                    System.out.println("Error clear artist list");
                }


                //LocationPhone location=artistSnapshot.getValue(LocationPhone.class);

                //locationList.add(location);
                LocationMeetingPoint post = dataSnapshot.getValue(LocationMeetingPoint.class);
                System.out.println(post);




                if(post!=null){
                    if(post.getSet()){


                        /*mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(post.getLatitude()), Double.parseDouble(post.getLongtitude())))
                                .title("Meet Up set by "+post.getdeviceId()));
                                */
                        meetUpTitle="Meet Up set by "+post.getdeviceId();
                        latitudeMeetUp=Double.parseDouble(post.getLatitude());
                        longtitudeMeetUp=Double.parseDouble(post.getLongtitude());
                        fab.show();
                        meetingPointSet=true;

                    }else{
                        fab.hide();
                        meetingPointSet=false;

                    }


                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void detectChangeFirebaseToClearMap(){
        //attaching value event listener
        locationDatabase = FirebaseDatabase.getInstance().getReference();
        locationDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{


                    mMap.clear();
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitudeFriend,longtitudeFriend))
                            .title(friendTitle).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    if(meetingPointSet==true){
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitudeMeetUp,longtitudeMeetUp))
                                .title(meetUpTitle));}
                    }

                catch (Exception e){
                    System.out.println("Error clear artist list");
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
