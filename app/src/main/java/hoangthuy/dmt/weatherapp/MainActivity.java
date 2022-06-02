package hoangthuy.dmt.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

//import com.android.volley.Request.Method.GET;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLongitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this,"please enter city Name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "please provide the permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude, 10);

            for (Address adr : addresses){
                if(adr!= null){
                    String city = adr.getLocality();
                    if (city!=null && !city.equals("")){
                        cityName = city;
                    }else{
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this,"User City Not Found...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/current.json?key=3e7d9d21136a49abbfb155017220704&q="+cityName+ "London&aqi=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, null,url, new Response.Listener<JSONObject>){
            @Override
            public void onResponse(JsonObjectRequest response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
            try {
                String temperarture = response.getJSONObject("current").getString("temp_c");
                temperatureTV.setText(temperarture+"°C");
                int isDay = response.getJSONObject("current"),getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                conditionTV.setText(condition);
                if(isDay==1){
                    Picasso.get().load("data:image/jpeg;base64,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").into(backIV);
                }else{
                    Picasso.get().load("data:image/jpeg;base64,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").into(backIV);
                }



            } catch (Exception e) {
                e.printStackTrace();
            }


            },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter valid city Name..", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}