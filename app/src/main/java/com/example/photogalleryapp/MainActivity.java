package com.example.photogalleryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public int currentPhotoIndex = 0;
    private ArrayList<String> photoGallery;
    private ArrayList<String> photoCaptions;
    private String currentPhotoPath = null;
    private String currentCaptionPath = null;
    private String galleryState = null;
    private String captionSearch = null;
    static Date minDate = new Date(Long.MIN_VALUE);
    static Date maxDate = new Date(Long.MAX_VALUE);	// On startup, show all images
    private Date startDate = minDate;
    private Date endDate = maxDate;
    private FusedLocationProviderClient fusedLocationClient;
    private Location loc = new Location("");
    private Location searchLoc = new Location("");
    private int defaultDist = 80000;    // default distance search value
    private double searchDist = defaultDist;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get saved context
        super.onCreate(savedInstanceState);
        // Render main activity
        setContentView(R.layout.activity_main);
        Button btnLeft = (Button)findViewById(R.id.btnLeft);
        Button btnRight = (Button)findViewById(R.id.btnRight);
        Button btnApply = (Button)findViewById(R.id.applyCaption);
        Button btnFilter = (Button)findViewById(R.id.btnFilter);
        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnApply.setOnClickListener(this);
        btnFilter.setOnClickListener(filterListener);
        // Request permissions for location
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
        // Instantiate Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Override onSuccess to store location in class member loc
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            loc = location;
                        }
                    }
                });

        photoGallery = populateGallery(minDate, maxDate);	// Retrieve photos in date range
        Log.d("onCreate, size", Integer.toString(photoGallery.size()));
        if (photoGallery.size() > 0) {
            currentPhotoPath = photoGallery.get(currentPhotoIndex);
            currentCaptionPath = photoCaptions.get(currentPhotoIndex);
            displayPhoto(currentPhotoPath, currentCaptionPath);
        }
    }


    private View.OnClickListener filterListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(MainActivity.this, SearchActivity.class);
            startActivityForResult(i, SEARCH_ACTIVITY_REQUEST_CODE);
        }
    };


    private ArrayList<String> populateGallery(Date min, Date max) {
        int i = 0;
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogalleryapp/files/Pictures");
        File txt = getExternalFilesDir(Environment.DIRECTORY_DCIM);

        photoGallery = new ArrayList<String>();
        photoCaptions = new ArrayList<String>();

        // Generate array of paths of images to be displayed
        File[] fList = file.listFiles();
        File[] capList = txt.listFiles();
        if (fList != null) {
            for (File f : file.listFiles()) {
                Date date = getDate(f.getPath());
                loc = getLoc(capList[i].getPath());
                double dist = getDist(loc, searchLoc);
                if(date.compareTo(min) >= 0 && date.compareTo(max) <=0 && dist <= searchDist) {
                    if (captionSearch == null) {
                        photoGallery.add(f.getPath());
                        photoCaptions.add(capList[i].getPath());
                    }
                    else if(getCap(capList[i].getPath()).matches("(.*)" + captionSearch + "(.*)")){
                        photoGallery.add(f.getPath());
                        photoCaptions.add(capList[i].getPath());
                    }
                }
                i++;
            }
        }
        return photoGallery;
    }

    private void displayPhoto(String path, String capPath) {
        ImageView iv = (ImageView) findViewById(R.id.ivGallery);
        TextView noResult = (TextView) findViewById((R.id.noResult));
        noResult.setText("");
        // Decode and display image
        iv.setImageBitmap(BitmapFactory.decodeFile(path));

        // Set textView to show timestamp
        TextView dateText = (TextView) findViewById(R.id.dateText);
        dateText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getDate(path)));

        // Set editText box to show caption for image
        EditText captionView = (EditText) findViewById(R.id.editText);
        captionView.setText(getCap(capPath));
    }

    private double getDist(Location loc1, Location loc2){
        // Radius of Earth in km
        int R = 6371;
        // Get lat/long of points, convert to radians
        double lat1 = loc1.getLatitude() * Math.PI/180;
        double lat2 = loc2.getLatitude() * Math.PI/180;
        double long1 = loc1.getLongitude() * Math.PI/180;
        double long2 = loc2.getLongitude() * Math.PI/180;
        // Calculate angular difference
        double dLong= long1 - long2;
        double dLat = lat1 - lat2;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLong/2) * Math.sin(dLong/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
    private Location getLoc(String path){
        String[] arr = null;
        String content = null;
        Location fileLoc = new Location("");
        try {
            FileInputStream fis = new FileInputStream(path);
            byte[] buffer = new byte[10];
            StringBuilder sb = new StringBuilder();
            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            fis.close();
            content = sb.toString();
            arr = content.split("_");
            fileLoc.setLatitude(Double.parseDouble(arr[1]));
            fileLoc.setLongitude(Double.parseDouble(arr[2]));
        }
        catch (Exception e){}

        return fileLoc;
    }
    // Method to retrieve caption from text file
    // May need to be edited when adding GPS functionality
    private String getCap(String path) {
        String[] arr = null;
        String content = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            byte[] buffer = new byte[10];
            StringBuilder sb = new StringBuilder();
            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            fis.close();
            content = sb.toString();
            arr = content.split("_");
        }
        catch (IOException e){}

        return arr[0];
    }
    // Set caption by writing to accompanying text file
    private void setCap(String newCap, String path) throws IOException{
        Location fileLoc = getLoc(path);
        // Open file
        FileWriter writer = new FileWriter(path);
        // Overwrite with new caption
        writer.write(newCap + "_" + fileLoc.getLatitude() + "_" +fileLoc.getLongitude());
        writer.flush();
        writer.close();
    }

    private Date getDate(String path){
        Date date = new Date();
        String[] contents = path.split("_");
        try{date = new SimpleDateFormat("yyyyMMdd_HHmmss").parse(contents[1] + "_" + contents[2]);}
        catch(ParseException e){}

        return date;
    }

    public void onClick( View v) {
        switch (v.getId()) {
            case R.id.btnLeft:
                --currentPhotoIndex;
                break;
            case R.id.btnRight:
                ++currentPhotoIndex;
                break;
            case R.id.applyCaption:
                if(photoGallery.size() > 0) {
                    EditText capText = (EditText) findViewById(R.id.editText);
                    currentCaptionPath = photoCaptions.get(currentPhotoIndex);
                    try {
                        setCap(capText.getText().toString(), currentCaptionPath);
                    } catch (IOException e) {}
                }
                break;
            default:
                break;
        }
        // Error checking photo index
        if (currentPhotoIndex < 0)
            currentPhotoIndex = 0;
        if (currentPhotoIndex >= photoGallery.size())
            currentPhotoIndex = photoGallery.size() - 1;
        if(photoGallery.size() > 0) {
            // Update photo and caption path, display on screen
            currentPhotoPath = photoGallery.get(currentPhotoIndex);
            currentCaptionPath = photoCaptions.get(currentPhotoIndex);
            Log.d("photoleft, size", Integer.toString(photoGallery.size()));
            Log.d("photoleft, index", Integer.toString(currentPhotoIndex));
            displayPhoto(currentPhotoPath, currentCaptionPath);
        }
    }

    public void takePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                // Call createImageFile function to generate path to store image
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.photogalleryapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // startActivityForResult will start a new activity with the assumption of returning back to the current activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // This method is for event handling - specifically the event of returning from the camera activity
    // at this point the picture should be displayed in the image view
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Add new picture to gallery and set date range (may need to be changed for filtering)
            captionSearch = null;
            populateGallery(minDate, maxDate);
            // Set photo index to display newest image
            currentPhotoIndex = photoGallery.size() - 1;
            // Get path of newest image
            currentCaptionPath = photoCaptions.get(currentPhotoIndex);
            currentPhotoPath = photoGallery.get(currentPhotoIndex);
            // Set image view to display newest image
            displayPhoto(currentPhotoPath, currentCaptionPath);
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE){
            populateGallery(minDate, maxDate);
            // Delete empty photo and caption files if camera activity does not complete successfully
            File nullPhoto = new File(photoGallery.get(photoGallery.size()-1));
            nullPhoto.delete();

            nullPhoto = new File(photoCaptions.get(photoGallery.size()-1));
            nullPhoto.delete();

            populateGallery(minDate,maxDate);
        }

        // If returning from search activity, update all filter variable with error checking
        else if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            // Receive search filters from search activity
            if(startDate != null && endDate != null) {
                try {
                    startDate = new SimpleDateFormat("yyyy-MM-dd").parse(data.getStringExtra("STARTDATE"));
                    endDate = new SimpleDateFormat("yyyy-MM-dd").parse(data.getStringExtra("ENDDATE"));
                } catch (ParseException e) {
                }
            }
            else{
                startDate = minDate;
                endDate = maxDate;
            }
            if (data.getStringExtra("LAT").isEmpty() || data.getStringExtra("LONG").isEmpty()){
                searchLoc.setLongitude(0);
                searchLoc.setLatitude(0);
            }
            else {
                searchLoc.setLatitude(Double.parseDouble(data.getStringExtra("LAT")));
                searchLoc.setLongitude(Double.parseDouble(data.getStringExtra("LONG")));
            }
            if(data.getStringExtra("DIST").isEmpty()){
                searchDist = defaultDist;
            }
            else{
                searchDist = Double.parseDouble(data.getStringExtra("DIST"));
            }
            captionSearch = data.getStringExtra("CAPTION");
            TextView noResult = (TextView) findViewById((R.id.noResult));
           populateGallery(startDate,endDate);
           currentPhotoIndex = 0;
           if(photoGallery.size() > 0) {
               noResult.setText("");
               displayPhoto(photoGallery.get(currentPhotoIndex), photoCaptions.get(currentPhotoIndex));
           }
           // No photos found
           else{
               TextView dateText = findViewById(R.id.dateText);
               dateText.setText("");
               ImageView iv = (ImageView) findViewById(R.id.ivGallery);
               iv.setImageBitmap(null);
               EditText captionView = (EditText) findViewById(R.id.editText);
               captionView.setText("Caption");
               noResult.setText("No photos found. Try adjusting search filters.");
           }
        }
    }

    // Method to generate JPEG file to store image and txt file to store caption
    private File createImageFile() throws IOException {
        // Get location of device
        fusedLocationClient.getLastLocation();

        // Generate image name with timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Get file path for image
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", dir );

        // Create directory for caption text file
        File txtdir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        // Create text file to store caption
        File capTxt = File.createTempFile(imageFileName, ".txt", txtdir);
        // Open file for writing
        FileWriter writer = new FileWriter(capTxt);
        writer.write("Caption");
        if (loc != null) {
            writer.write("_" + loc.getLatitude() + "_" + loc.getLongitude());
        }
        writer.flush();
        writer.close();

        currentPhotoPath = image.getAbsolutePath();
        Log.d("createImageFile", currentPhotoPath);
        return image;
    }
}
