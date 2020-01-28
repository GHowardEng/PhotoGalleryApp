package com.example.photogalleryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.BitmapFactory;
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

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private int currentPhotoIndex = 0;
    private ArrayList<String> photoGallery;
    private ArrayList<String> photoCaptions;
    private String currentPhotoPath = null;
    private String currentCaptionPath = null;
    private String galleryState = null;
    private Date startDate = null;
    private Date endDate = null;
    private String captionSearch = null;
    static Date minDate = new Date(Long.MIN_VALUE);
    static Date maxDate = new Date(Long.MAX_VALUE);	// On startup, show all images

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

        // Generate array of locations of images to be displayed
        File[] fList = file.listFiles();
        File[] capList = txt.listFiles();
        if (fList != null) {
            for (File f : file.listFiles()) {
                Date date = getDate(f.getPath());
                if(date.compareTo(min) >= 0 && date.compareTo(max) <=0) {
                    photoGallery.add(f.getPath());
                    photoCaptions.add(capList[i].getPath());
                }
                i++;
            }
        }

        // Generate array of locations of accompanying captions
        /*File[] capList = txt.listFiles();
        if (capList != null){
            for (File f : txt.listFiles()){
                photoCaptions.add(f.getPath());
            }
        }*/
        return photoGallery;
    }

    private void displayPhoto(String path, String capPath) {

        ImageView iv = (ImageView) findViewById(R.id.ivGallery);
        TextView noResult = (TextView) findViewById((R.id.noResult));
        noResult.setText("");
        // Decode and display image
        iv.setImageBitmap(BitmapFactory.decodeFile(path));
        galleryState = (currentPhotoIndex+1) + " of " + photoGallery.size();
        TextView stateText = (TextView) findViewById(R.id.state);
        stateText.setText(galleryState);


        // Set editText box to show caption for image
        EditText captionView = (EditText) findViewById(R.id.editText);
        try{captionView.setText(getCap(capPath));}
        catch(IOException e){}
    }

    // Method to retrieve caption from text file
    // May need to be edited when adding GPS functionality
    private String getCap(String path) throws IOException{
        String cap = null;
        FileInputStream fis = new FileInputStream(path);
        byte[] buffer = new byte[10];
        StringBuilder sb = new StringBuilder();
        while (fis.read(buffer) != -1) {
            sb.append(new String(buffer));
            buffer = new byte[10];
        }
        fis.close();
        cap = sb.toString();

        return cap;
    }
    // Set caption by writing to accompanying text file
    private void setCap(String newCap, String path) throws IOException{
        // Open file
        FileWriter writer = new FileWriter(path);
        // Overwrite with new caption
        writer.write(newCap);
        writer.flush();
        writer.close();
    }

    private Date getDate(String path){
        Date date = new Date();
        String[] contents = path.split("_");

        // Code to return as string array
        //String[] date = new String[2];
        //date[0] = contents[1];
        //date[1] = contents[2];

        // Return as date object
        try{date = new SimpleDateFormat("yyyyMMdd_HHmmss").parse(contents[1] + "_" + contents[2]);}
        catch(ParseException e){}
        // Returns date info in string array, first element yyyymmdd, second element hhmmss
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

        else if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            // Receive search filters from search activity
            try {
                startDate = new SimpleDateFormat("yyyy-MM-dd").parse(data.getStringExtra("STARTDATE"));
                endDate   = new SimpleDateFormat("yyyy-MM-dd").parse(data.getStringExtra("ENDDATE"));
            }
            catch (ParseException e){}
            captionSearch = data.getStringExtra("CAPTION");

            TextView noResult = (TextView) findViewById((R.id.noResult));

           // Need to convert strings to date objects
           populateGallery(startDate,endDate);
           currentPhotoIndex = 0;
           if(photoGallery.size() > 0) {
               noResult.setText("");
               displayPhoto(photoGallery.get(currentPhotoIndex), photoCaptions.get(currentPhotoIndex));
           }
           // No photos found
           else{
               ImageView iv = (ImageView) findViewById(R.id.ivGallery);
               iv.setImageBitmap(null);
               galleryState = "0 of 0";
               TextView stateText = (TextView) findViewById(R.id.state);
               stateText.setText(galleryState);
               EditText captionView = (EditText) findViewById(R.id.editText);
               captionView.setText("No Caption");
               noResult.setText("No photos found. Try adjusting search filters.");
           }
        }
    }

    // Method to generate JPEG file to store image and txt file to store caption
    private File createImageFile() throws IOException {
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
        writer.write("No Caption");
        writer.flush();
        writer.close();

        currentPhotoPath = image.getAbsolutePath();
        Log.d("createImageFile", currentPhotoPath);
        return image;
    }
}
