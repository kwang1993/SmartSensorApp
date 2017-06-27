package com.smartsensor.www;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.RED;
import static android.graphics.Color.blue;

/**
 * Created by joeal_000 on 4/26/2017.
 */

public class PlotActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int MAX_ENTRIES = 72;    //30 mins
    private int MIN = Integer.MIN_VALUE;
    private int MAX = Integer.MAX_VALUE;
    //private String[] labels = new String[MAX_ENTRIES];
    //private float[] values = new float[MAX_ENTRIES];
    private LineSet lineSet;
    private String dataType = "Temperature";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_main);

        //spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> tadapter = ArrayAdapter.createFromResource(this,
                R.array.list_of_data, android.R.layout.simple_spinner_item);
        tadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(tadapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                getPastData(parent.getItemAtPosition(pos).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //fab
        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        getPastData(dataType);
    }

    public void onClicked(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void getPastData(String s){
        if (s != null && !s.equals(""))
            dataType = s;
        //make a firebase instance
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        //final ArrayList<Data> datalist = new ArrayList<Data>();
        DatabaseReference myRef = db.getReferenceFromUrl("https://smartsensor-842a9.firebaseio.com/SensorNode/S127");
        Query lastQuery = myRef.orderByKey().limitToLast(MAX_ENTRIES);
        // Read from the database
        lastQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                String[] labels = new String[MAX_ENTRIES];
                float[] values = new float[MAX_ENTRIES];
                Data dmax = new Data(MIN, MIN, MIN, MIN, MIN, MIN,
                        new Location(), MIN, MIN, MIN, MIN, MIN, MIN, MIN);
                Data dmin = new Data(MAX, MAX, MAX, MAX, MAX, MAX,
                        new Location(), MAX, MAX, MAX, MAX, MAX, MAX, MAX);
                double minVal = 0;
                double maxVal = 100;
                //datalist.clear();
                LineChartView lineChartView = (LineChartView) findViewById(R.id.linechartview);
                TextView xaxis = (TextView) findViewById(R.id.xlabel);
                TextView yaxis = (TextView) findViewById(R.id.ylabel);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    //long date = child.getValue(Long.class);
                    double PM = child.child("PM").getValue(int.class);
                    double PM1 = child.child("PM1").getValue(int.class);
                    double PM10 = child.child("PM10").getValue(int.class);
                    double battery_voltage = child.child("battery voltage").getValue(double.class);
                    double formaldehyde = child.child("formaldehyde").getValue(int.class);
                    double humidity = child.child("humidity").getValue(double.class);
                    Location location = child.child("location").getValue(Location.class);
                    double pressure = child.child("pressure").getValue(int.class);
                    double rbv = child.child("raw battery voltage").getValue(double.class);
                    int state = child.child("state").getValue(int.class);
                    double temperature = child.child("temperature").getValue(double.class);
                    double temperature_BMP = child.child("temperature_BMP").getValue(double.class);
                    double temperature_DS3231 = child.child("temperature_DS3231").getValue(double.class);
                    double uv = child.child("ultraviolet").getValue(int.class);
                    String date2 = child.getKey();

                    Data d = new Data(PM, PM1, PM10, battery_voltage, formaldehyde, humidity,
                            location, pressure, rbv, state, temperature, temperature_BMP, temperature_DS3231,
                            uv);
                    d.setDate(date2);
                    setMax(dmax, d);
                    setMin(dmin, d);
                    //labels[i] = String.valueOf(i);
                    labels[i] = " ";
                    xaxis.setText("Time (last 30 minutes)");
                    switch (dataType){
                        case "Temperature":
                            values[i] = (float) temperature;
                            minVal = dmin.getTemperature();
                            maxVal = dmax.getTemperature();
                            yaxis.setText("     Temperature (°C)     ");
                            break;
                        case "Temp BMP":
                            values[i] = (float) temperature_BMP;
                            minVal = dmin.getTemperature_BMP();
                            maxVal = dmax.getTemperature_BMP();
                            yaxis.setText("     Temperature (°C)     ");
                            break;
                        case "Temp Node":
                            values[i] = (float) temperature_DS3231;
                            minVal = dmin.getTemperature_DS3231();
                            maxVal = dmax.getTemperature_DS3231();
                            yaxis.setText("     Temperature (°C)     ");
                            break;
                        case "Relative Humidity":
                            values[i] = (float) humidity;
                            minVal = dmin.getHumidity();
                            maxVal = dmax.getHumidity();
                            yaxis.setText("  Relative Humidity (%) ");
                            break;
                        case "PM1":
                            values[i] = (float) PM1;
                            minVal = dmin.getPM1();
                            maxVal = dmax.getPM1();
                            yaxis.setText("        PM 1 (µg/m³)       ");
                            break;
                        case "PM2.5":
                            values[i] = (float) PM;
                            minVal = dmin.getPM();
                            maxVal = dmax.getPM();
                            yaxis.setText("        PM 2.5 (µg/m³)     ");
                            break;
                        case "PM10":
                            values[i] = (float) PM10;
                            minVal = dmin.getPM10();
                            maxVal = dmax.getPM10();
                            yaxis.setText("        PM 10 (µg/m³)      ");
                            break;
                        case "UltraViolet":
                            values[i] = (float) uv;
                            minVal = dmin.getUltraviolet();
                            maxVal = dmax.getUltraviolet();
                            yaxis.setText("      Ultraviolet (mV)      ");
                            break;
                        case "Formaldehyde":
                            values[i] = (float) formaldehyde;
                            minVal = dmin.getFormaldehyde();
                            maxVal = dmax.getFormaldehyde();
                            yaxis.setText("Formaldehyde (µg/m³)");
                            break;
                        case "Battery Voltage":
                            values[i] = (float) battery_voltage;
                            minVal = dmin.getBattery_voltage();
                            maxVal = dmax.getBattery_voltage();
                            yaxis.setText("        Voltage (mV)         ");
                            break;
                        case "Raw Battery Voltage":
                            values[i] = (float) rbv;
                            minVal = dmin.getRaw_battery_voltage();
                            maxVal = dmax.getRaw_battery_voltage();
                            yaxis.setText("        Voltage (mV)         ");
                            break;
                        case "State":
                            values[i] = (float) state;
                            minVal = dmin.getState();
                            maxVal = dmax.getState();
                            break;
                        case "Air Pressure":
                            values[i] = (float) pressure;
                            minVal = dmin.getPressure();
                            maxVal = dmax.getPressure();
                            yaxis.setText("    Air Pressure (hPa)   ");
                            break;
                        default:
                            values[i] = (float) temperature;
                            minVal = dmin.getTemperature();
                            maxVal = dmax.getTemperature();
                            break;
                    }
                    //datalist.add(d);
                    i++;
                }
                lineSet = new LineSet(labels, values);
                lineSet.beginAt(0);
                lineSet.setColor(Color.rgb(132, 175, 232));
                //lineSet.setDotsColor(Color.rgb(66, 143, 244));
                lineSet.setFill(Color.rgb(215, 233, 239));
                lineSet.setThickness(4);
                lineSet.setSmooth(true);

                lineChartView.dismiss();
                Paint paint = new Paint();


//                if (dataType.equals("Temperature") || dataType.equals("Temp BMP") || dataType.equals("Temp Node") || dataType.equals("Relative Humidity") ||
//                        dataType.equals("Air Pressure")) {
//
//                    lineChartView.setAxisBorderValues(((int) minVal) - 1, ((int) maxVal) + 1);
//                    lineChartView.setStep(1);
//                    //lineChartView.setGrid((((int) maxVal) - ((int) minVal) + 2)*5, 0, paint);
//                }
//
//                else if (dataType.equals("Formaldehyde")) {
//                    lineChartView.setAxisBorderValues(((int) minVal) - 1, ((int) maxVal) + 1);
//                    lineChartView.setStep(1);
//                    //lineChartView.setGrid(((int) maxVal) - ((int) minVal) + 2, 0, paint);
//                }
//                else {
                    if (maxVal - minVal >= 30) {
                        int step = 5;
                        int lines = ((int)maxVal - (int)minVal)/step + 1;
                        lineChartView.setAxisBorderValues((int) minVal, (int) minVal + step * lines, step);
                        //lineChartView.setGrid(lines, 0, paint);
                    }
                    else{
                        lineChartView.setAxisBorderValues((int) minVal , ((int) maxVal) + 2, 1);
                        //lineChartView.setGrid(((int) maxVal) - ((int) minVal) + 2, 0, paint);
                    }
//                }

                lineChartView.setAxisColor(BLUE);
                lineChartView.setAxisThickness(4);
                lineChartView.addData(lineSet);
                lineChartView.show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //Log.e("Failed to read value.", error.toException().toString());
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setMax(Data dmax, Data d){
        if (d.getUltraviolet() > dmax.getUltraviolet())
            dmax.setUltraviolet(d.getUltraviolet());
        if (d.getHumidity() > dmax.getHumidity())
            dmax.setHumidity(d.getHumidity());
        if (d.getTemperature() > dmax.getTemperature())
            dmax.setTemperature(d.getTemperature());
        if (d.getBattery_voltage() > dmax.getBattery_voltage())
            dmax.setBattery_voltage(d.getBattery_voltage());
        if (d.getFormaldehyde() > dmax.getFormaldehyde())
            dmax.setFormaldehyde(d.getFormaldehyde());
        if (d.getPM() > dmax.getPM())
            dmax.setPM(d.getPM());
        if (d.getPM1() > dmax.getPM1())
            dmax.setPM1(d.getPM1());
        if (d.getPM10() > dmax.getPM10())
            dmax.setPM10(d.getPM10());
        if (d.getPressure() > dmax.getPressure())
            dmax.setPressure(d.getPressure());
        if (d.getRaw_battery_voltage() > dmax.getRaw_battery_voltage())
            dmax.setRaw_battery_voltage(d.getRaw_battery_voltage());
        if (d.getTemperature_BMP() > dmax.getTemperature_BMP())
            dmax.setTemperature_BMP(d.getTemperature_BMP());
        if (d.getTemperature_DS3231() > dmax.getTemperature_DS3231())
            dmax.setTemperature_DS3231(d.getTemperature_DS3231());
    }

    private void setMin(Data dmin, Data d){
        if (d.getUltraviolet() < dmin.getUltraviolet())
            dmin.setUltraviolet(d.getUltraviolet());
        if (d.getHumidity() < dmin.getHumidity())
            dmin.setHumidity(d.getHumidity());
        if (d.getTemperature() < dmin.getTemperature())
            dmin.setTemperature(d.getTemperature());
        if (d.getBattery_voltage() < dmin.getBattery_voltage())
            dmin.setBattery_voltage(d.getBattery_voltage());
        if (d.getFormaldehyde() < dmin.getFormaldehyde())
            dmin.setFormaldehyde(d.getFormaldehyde());
        if (d.getPM() < dmin.getPM())
            dmin.setPM(d.getPM());
        if (d.getPM1() < dmin.getPM1())
            dmin.setPM1(d.getPM1());
        if (d.getPM10() < dmin.getPM10())
            dmin.setPM10(d.getPM10());
        if (d.getPressure() < dmin.getPressure())
            dmin.setPressure(d.getPressure());
        if (d.getRaw_battery_voltage() < dmin.getRaw_battery_voltage())
            dmin.setRaw_battery_voltage(d.getRaw_battery_voltage());
        if (d.getTemperature_BMP() < dmin.getTemperature_BMP())
            dmin.setTemperature_BMP(d.getTemperature_BMP());
        if (d.getTemperature_DS3231() < dmin.getTemperature_DS3231())
            dmin.setTemperature_DS3231(d.getTemperature_DS3231());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(id);
        return true;
    }

    private void displaySelectedScreen(int id){
        //Fragment fragment = null;
        Intent intent;

        switch(id){
            case R.id.nav_overview:
                //fragment = new OverviewFrag();
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_history:
                //fragment = new HistoryActivity();
                intent = new Intent(this, HistoryActivityReal.class);
                startActivity(intent);
                break;
            case R.id.nav_plots:
                intent = new Intent(this, PlotActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
        }
    }
}
