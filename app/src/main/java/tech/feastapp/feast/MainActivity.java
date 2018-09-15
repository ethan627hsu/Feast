package tech.feastapp.feast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2124;
    FirebaseDatabase database;

    ProgressBar pb;
    private GoogleMap gmap;
    private MapView mapView;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE2 = 12345;

    Toolbar.OnMenuItemClickListener menuListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_refresh) {
                pb.setVisibility(ProgressBar.VISIBLE);
                refresh();
                return true;
            } else if (menuItem.getItemId() == R.id.action_search) {
                AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                        .build();
                setTheme(R.style.AppTheme);

                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

    };
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .build();
            setTheme(R.style.AppThemeEditor);
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .setFilter(typeFilter)
                                .build(MainActivity.this);
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE2);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    };
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        database = FirebaseDatabase.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab);
        Toolbar t = findViewById(R.id.toolbar);
        t.inflateMenu(R.menu.main_menu);
        pb = findViewById(R.id.pb);
        pb.setVisibility(ProgressBar.VISIBLE);
        t.setOnMenuItemClickListener(menuListener);

        fab.setOnClickListener(fabListener);
        MapsInitializer.initialize(this);
        mapView = findViewById(R.id.mapView);
        refresh();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place location = PlaceAutocomplete.getPlace(this, data);
                gmap.moveCamera(CameraUpdateFactory.newLatLng(location.getLatLng()));
            }
        }
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == RESULT_OK) {

                bindAlertBuilder(data);
                builder.show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 8512: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refresh();
                }
                return;
            }
        }
    }

    private void bindAlertBuilder(Intent data) {
        final View editLayout = View.inflate(this, R.layout.item_edit, null);

        final Place location = PlaceAutocomplete.getPlace(this, data);

        builder.setTitle("Amount of People")
                .setMessage("How many people are present at the location?")
                .setView(editLayout)
                .setCancelable(true)
                .setPositiveButton("SUMBIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        EditText et = editLayout.findViewById(R.id.et_amount_of_people);
                        int amountOfPeople = Integer.valueOf(et.getText().toString());
                        FeastLocation feastLocation = new FeastLocation();
                        feastLocation.setPeople(amountOfPeople);
                        feastLocation.setLongitude(location.getLatLng().longitude);
                        feastLocation.setLatitude(location.getLatLng().latitude);
                        feastLocation.setName((String) location.getName());


                        if (amountOfPeople == 0) {
                            database.getReference(String.valueOf((String.valueOf(feastLocation.getLatitude()) + String.valueOf(feastLocation.getLongitude())).hashCode())).removeValue();
                        } else {
                            database.getReference(String.valueOf((String.valueOf(feastLocation.getLatitude()) + String.valueOf(feastLocation.getLongitude())).hashCode())).setValue(feastLocation);
                        }
                        refresh();

                    }
                });
    }

    private void refresh() {
        mapView.onCreate(null);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                gmap = googleMap;
                gmap.setMinZoomPreference(12);

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 8512);
                } else {
                    refreshData();
                }

            }
        });
    }

    @SuppressLint("MissingPermission")
    public void refreshData() {
        gmap.setMyLocationEnabled(true);
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                gmap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            }
        });
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for (DataSnapshot i : snapshots) {

                    FeastLocation feastLocation = i.getValue(FeastLocation.class);

                    gmap.addMarker(new MarkerOptions()
                            .position(new LatLng(feastLocation.getLatitude(), feastLocation.getLongitude())).title(feastLocation.getName() + " - " + String.valueOf(feastLocation.getPeople()) + " People")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .draggable(false).visible(true));
                }
                gmap.setBuildingsEnabled(true);
                pb.setVisibility(ProgressBar.GONE);
                mapView.onResume();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
