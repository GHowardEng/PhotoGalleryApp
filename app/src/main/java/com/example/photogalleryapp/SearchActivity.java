package com.example.photogalleryapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {

    private EditText fromDate;
    private EditText toDate;
    private EditText caption;
    private EditText latitude;
    private EditText longitude;
    private EditText distance;
    private Calendar fromCalendar;
    private Calendar toCalendar;
    private DatePickerDialog.OnDateSetListener fromListener;
    private DatePickerDialog.OnDateSetListener toListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Render activity, retrieve editText objects
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        fromDate = (EditText) findViewById(R.id.search_fromDate);
        toDate   = (EditText) findViewById(R.id.search_toDate);
        caption  = (EditText) findViewById(R.id.captionText);
        latitude = (EditText) findViewById(R.id.editLat);
        longitude= (EditText) findViewById(R.id.editLong);
        distance = (EditText) findViewById(R.id.editDist);
    }

    public void cancel(final View v) {
        // Exit activity on cancel
        finish();
    }

    public void search(final View v) {
        // Pass back filter info from editText boxes, exit activity
        Intent i = new Intent();
        i.putExtra("STARTDATE", fromDate.getText().toString());
        i.putExtra("ENDDATE", toDate.getText().toString());
        i.putExtra("CAPTION", caption.getText().toString());
        i.putExtra("LAT", latitude.getText().toString());
        i.putExtra("LONG", longitude.getText().toString());
        i.putExtra("DIST", distance.getText().toString());
        setResult(RESULT_OK, i);
        finish();
    }
}

