package com.stuff.squishy.daytrippa;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

// classes needed to initialize map
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

// classes needed to add the location component
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

// classes needed to add a marker
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineTranslate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.util.Log;

// classes needed to launch navigation UI
import android.view.View;
import android.widget.Button;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Point destinationPoint, originPoint, appLinkPoint;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button;
    private RadioGroup radioGroup;
    private RadioButton rb_driving;
    private RadioButton rb_walking;
    private RadioButton rb_cycling;
    private EditText editText_search;
    private ImageButton menu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ConstraintLayout shareView;
    private RecyclerView app_Recycler;
    // variables needed for firebase
    private Connections conn;
    private FirebaseAuth fb_auth;
    private FirebaseDatabase fb_database;
    private DatabaseReference ref;
    // variables needed for geocoding
    private CarmenFeature originFeature;
    private CarmenFeature destinationFeature;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private boolean imperial = false;
    private String unitsCriteria = DirectionsCriteria.METRIC, profileCriteria = DirectionsCriteria.PROFILE_DRIVING;

    private FeatureCollection dashedLineDirectionsFeatureCollection;
    AppAdaptor mAdapter;
    List<ApplicationInfo> apps;
    PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.


        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        radioGroup = findViewById(R.id.radioGroup);
        rb_driving = findViewById(R.id.radio_driving);
        rb_walking = findViewById(R.id.radio_walking);
        rb_cycling = findViewById(R.id.radio_cycling);
        editText_search = findViewById(R.id.txt_search);
        drawerLayout = findViewById(R.id.drawer_main);
        navigationView = findViewById(R.id.home_Nav);
        menu = findViewById(R.id.img_menu);
        button = findViewById(R.id.startButton);
        shareView = findViewById(R.id.share_layout);

        conn = Connections.getInstance();
        fb_auth = conn.getFb_authInstance();
        fb_database = conn.getFb_database();
        ref = fb_database.getReference(fb_auth.getCurrentUser().getUid());
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Address orig = getAddress(originPoint);
                Address dest = getAddress(destinationPoint);
                Model_RouteHistory model_routeHistory = new Model_RouteHistory();
                model_routeHistory.setOrigin(orig.getAddressLine(0));
                model_routeHistory.setDestination(dest.getAddressLine(0));
                model_routeHistory.setDistance(currentRoute.distance());
                model_routeHistory.setDuration(currentRoute.duration());

                ref.child("Routes").child("(" + orig.getAddressLine(0) + ") - (" + dest.getAddressLine(0) + ")").setValue(model_routeHistory);
                boolean simulateRoute = false;
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(simulateRoute)
                        .build();
                // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(MainActivity.this, options);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //if statements determine which mode of transport the user has selected and getRoute accordingly
                if (checkedId == R.id.radio_driving) {
                    profileCriteria = DirectionsCriteria.PROFILE_DRIVING;
                } else if (checkedId == R.id.radio_walking) {
                    profileCriteria = DirectionsCriteria.PROFILE_WALKING;
                } else {
                    profileCriteria = DirectionsCriteria.PROFILE_CYCLING;
                }

                if (destinationPoint != null && originPoint != null) {
                    getRoute(originPoint, destinationPoint);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.Travel: {
                        Connections.setShareON(false);
                        intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Share: {
                        Connections.setShareON(true);
                        intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Profile: {
                        intent = new Intent(MainActivity.this, Profile.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Settings: {
                        intent = new Intent(MainActivity.this, Settings.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Logout: {
                        //Logout
                        fb_auth.signOut();
                        intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
                return false;
            }
        });

        menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                drawerLayout.openDrawer(Gravity.LEFT);
                return false;
            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                imperial = dataSnapshot.child("Imperial").getValue(boolean.class);

                if (!imperial) {
                    unitsCriteria = DirectionsCriteria.METRIC;
                } else {
                    unitsCriteria = DirectionsCriteria.IMPERIAL;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null)
        {
            double lng, lat;
            List<String> segments = appLinkData.getPathSegments();
            lat = Double.parseDouble(segments.get(0));
            lng = Double.parseDouble(segments.get(1));
            appLinkPoint = Point.fromLngLat(lng, lat);

        }

    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap)
    {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded()
        {
            @Override
            public void onStyleLoaded(@NonNull Style style)
            {
                enableLocationComponent(style);
                addDestinationIconSymbolLayer(style);
                mapboxMap.addOnMapClickListener(MainActivity.this);


                initSearchFab();
                setUpSource(style);
                setupLayer(style);

                if(conn.isShareON())
                {
                    shareView.setVisibility(View.VISIBLE);
                    try
                    {
                        apps = getInstalledApps();
                        app_Recycler = (RecyclerView) findViewById(R.id.apps_layout);
                        LinearLayoutManager horizontalLayoutManager
                                = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                        app_Recycler.setLayoutManager(horizontalLayoutManager);
                        mAdapter = new AppAdaptor(MainActivity.this, apps, pm, originPoint);
                        app_Recycler.setAdapter(mAdapter);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, "Please select a trip", Toast.LENGTH_SHORT).show();
                        Log.v("Error from app_Recycler", e.getMessage());
                    }

                }

                if(appLinkPoint != null)
                {
                    getRoute(originPoint, appLinkPoint);
                    destinationPoint = appLinkPoint;
                }
            }
        });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle)
    {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point)
    {
        destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);

        return true;
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .voiceUnits(unitsCriteria)
                .profile(profileCriteria)
                .build()
                .getRoute(new Callback<DirectionsResponse>()
                {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null)
                        {
                            navigationMapRoute.removeRoute();
                        }
                        else
                        {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }

                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });

        button.setEnabled(true);
        button.setVisibility(View.VISIBLE);
        button.setBackgroundResource(R.drawable.selected_state);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                        locationComponent.getLastKnownLocation().getLatitude());
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initSearchFab()
    {
        findViewById(R.id.txt_search).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                //.addInjectedFeature(home)
                                //.addInjectedFeature(work)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    private void setUpSource(@NonNull Style loadedMapStyle)
    {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[]{0f, -8f})
        ));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    destinationPoint = selectedCarmenFeature.center();
                    if(destinationPoint != null && originPoint != null)
                    {
                        getRoute(originPoint, destinationPoint);
                    }
            // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);
                }
            }
        }
    }

    private Address getAddress(Point point)
    {
        final double lat = point.latitude();
        final double lng = point.longitude();
        List<Address> addresses;
        Address address = new Address(Locale.getDefault());
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try
        {
            addresses = geocoder.getFromLocation(lat,lng, 1);
            if (addresses != null && addresses.size() >= 0)
            {
                address = addresses.get(0);
                return address;
            }
        }
        catch (IOException e)
        {
            Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
        }


        return address;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain)
    {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted)
    {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

//    private ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
//        ArrayList<PInfo> res = new ArrayList<PInfo>();
//        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
//        for(int i=0;i<packs.size();i++)
//        {
//            PackageInfo p = packs.get(i);
//            if ((!getSysPackages) && (p.versionName == null)) {
//                continue ;
//            }
//            PInfo newInfo = new PInfo();
//            newInfo.setAppname(p.applicationInfo.loadLabel(getPackageManager()).toString());
//            newInfo.setPname(p.packageName);
//            newInfo.setVersionName(p.versionName);
//            newInfo.setVersionCode(p.versionCode);
//            newInfo.setIcon(p.applicationInfo.loadIcon(getPackageManager()));
//            res.add(newInfo);
//        }
//        return res;
//    }

    private List<ApplicationInfo> getInstalledApps()
    {
        pm = getPackageManager();
        List<ApplicationInfo> SysApps = pm.getInstalledApplications(0);
        List<ApplicationInfo> apps = new ArrayList<>();
        for(ApplicationInfo app : SysApps)
        {
            if(!isSystemPackage(app))
            {
                apps.add(app);
            }
        }


        return apps;
    }

    private boolean isSystemPackage(ApplicationInfo appInfo)
    {
        return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}