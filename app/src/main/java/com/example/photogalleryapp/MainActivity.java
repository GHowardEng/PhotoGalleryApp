package com.example.photogalleryapp;

import com.example.photogalleryapp.Utility.SearchUtility;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/*
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
 */
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/*
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
*/

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int RC_SIGN_IN = 2;
    public int currentPhotoIndex = 0;
    private ArrayList<String> photoGallery;
    private ArrayList<String> photoCaptions;
    private String currentPhotoPath = null;
    private String currentCaptionPath = null;
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
    private static final int REQUEST_EXTERNAL_STORAGE = 200;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    String encodedImage;

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
        Button btnShare = (Button)findViewById(R.id.share);
        Button btnUpload = (Button)findViewById(R.id.upload);
        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnApply.setOnClickListener(this);
        btnFilter.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        // Request permissions for peripheral access
        ActivityCompat.requestPermissions(
                MainActivity.this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET},
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

    private ArrayList<String> populateGallery(Date min, Date max) {
        int i = 0;
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogalleryapp/files/Pictures");
        File txt = getExternalFilesDir(Environment.DIRECTORY_DCIM);

        photoGallery = new ArrayList<String>();
        photoCaptions = new ArrayList<String>();
        ArrayList<ArrayList<String>> gallery = new ArrayList<ArrayList<String>>();

        // Generate array of paths of images to be displayed
        File[] fList = file.listFiles();
        File[] capList = txt.listFiles();

        ArrayList<String[]> photoDetails = new ArrayList<String[]>();

        if (fList != null) {
            for (File f : file.listFiles()) {
                String[] data = new String[6];
                data[0] = f.getPath();
                data[1] = capList[i].getPath();
                data[2] = getCap(data[1]);
                data[3] = new SimpleDateFormat("yyyyMMdd_HHmmss").format(getDate(data[0]));
                Location photoLoc = getLoc(data[1]);
                data[4] = Double.toString(photoLoc.getLatitude());
                data[5] = Double.toString(photoLoc.getLongitude());

                photoDetails.add(data);
                i++;
            }
        }
        double[] searchLocDouble = new double[2];
        searchLocDouble[0] = searchLoc.getLatitude();
        searchLocDouble[1] = searchLoc.getLongitude();

        gallery = SearchUtility.searchFunc(min, max, captionSearch, searchDist, searchLocDouble, photoDetails);
        photoGallery = gallery.get(0);
        photoCaptions = gallery.get(1);

        return photoGallery;
    }

    private void displayPhoto(String path, String capPath) {
        DecimalFormat df = new DecimalFormat("#.###");
        ImageView iv = (ImageView) findViewById(R.id.ivGallery);
        TextView noResult = (TextView) findViewById((R.id.noResult));
        noResult.setText("");
        // Decode and display image
        iv.setImageBitmap(BitmapFactory.decodeFile(path));

        // Set textView to show timestamp
        TextView dateText = (TextView) findViewById(R.id.dateText);
        dateText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getDate(path)));

        // Set textView to show location
        TextView locText = (TextView) findViewById(R.id.locationText);
        Location photoLoc = getLoc(capPath);
        String locString = "Lat: " + df.format(photoLoc.getLatitude())
                + " Long: " + df.format(photoLoc.getLongitude());
        locText.setText(locString);

        // Set editText box to show caption for image
        EditText captionView = (EditText) findViewById(R.id.editText);
        captionView.setText(getCap(capPath));
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
            case R.id.btnFilter:
                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(i, SEARCH_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.share:
                String filePath = photoGallery.get(currentPhotoIndex);
                String caption = getCap(photoCaptions.get(currentPhotoIndex));

                // Start server upload
                //upload(filePath);

                // Start sharing intent to send image to social media platform
                Bitmap icon = BitmapFactory.decodeFile(filePath);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, caption);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values);

                // Stream encoded image
                OutputStream outstream;
                try {
                    outstream = getContentResolver().openOutputStream(uri);
                    icon.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                    outstream.close();
                } catch (Exception e) {
                    System.err.println(e.toString());
                }

                // Put encoded image in intent along with caption as the subject
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.putExtra(Intent.EXTRA_SUBJECT, caption);
                startActivity(Intent.createChooser(share, "Share Image"));

                break;

            case(R.id.upload):
                upload(photoGallery.get(currentPhotoIndex));

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

    byte[] ba = null;
    private void upload(String picturePath) {
        // Image location URL
        Log.e("path", "----------------" + picturePath);

        // Image
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        ba = bao.toByteArray();
        //ba1 = Base64.encode(ba);
        encodedImage =Base64.encodeToString(ba,Base64.DEFAULT);

        //Log.e("base64", "-----" + ba1);

        // Upload image to server
        new uploadToServer().execute("DataString");
        // Set to run in background*********


    }
    public class uploadToServer extends AsyncTask<String, String, String> {

        private ProgressDialog pd = new ProgressDialog(MainActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(String... data) {

            //String urlString = "http://192.168.1.68:8081/midp/hits"; // URL of server. May need to be changed based on IP
            String urlString = "http://10.0.2.2:8081/midp/hits";  // Use if running emulator on same machine as server
            String imDat = data[0]; //data to post
            OutputStream out = null;

            try {
                // Setup URL connection
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                // Get output stream
                out = new BufferedOutputStream(urlConnection.getOutputStream());

                // Prepare Tx data as name value pairs
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("image", encodedImage));
                params.add(new BasicNameValuePair("data", imDat));
                //params.add(new BasicNameValuePair("thirdParam", paramValue3));

                // Write data
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                // Write list of name value pairs
                writer.write(getQuery(params));
                //writer.write(encodedImage);
                writer.flush();
                writer.close();
                out.close();

                // Execute connection
                urlConnection.connect();

                // Get input stream to read respone
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // Format response stream to string
                byte[] contents = new byte[1024];
                int bytesRead = 0;
                String strContents = "";
                while((bytesRead = in.read(contents)) != -1) {
                    strContents += new String(contents, 0, bytesRead);
                }

                // Output response
                System.out.println("Response:");
                System.out.println((strContents));

                urlConnection.disconnect();

            } catch (Exception e) {
                System.out.println("Error:");
                System.out.println(e.getMessage());
            }

            return "Success";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
        }
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
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

    // This method is for event handling - specifically the event of returning from other activities
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            populateGallery(minDate, maxDate);
            // Delete empty photo and caption files if camera activity does not complete successfully
            File nullPhoto = new File(photoGallery.get(photoGallery.size() - 1));
            nullPhoto.delete();

            nullPhoto = new File(photoCaptions.get(photoGallery.size() - 1));
            nullPhoto.delete();

            populateGallery(minDate, maxDate);
        }

        // If returning from search activity, update all filter variable with error checking
        else if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Receive search filters from search activity
            if (startDate != null && endDate != null) {
                try {
                    startDate = new SimpleDateFormat("yyyy-MM-dd").parse(data.getStringExtra("STARTDATE"));
                    endDate = new SimpleDateFormat("yyyy-MM-dd").parse(data.getStringExtra("ENDDATE"));
                } catch (ParseException e) {
                }
            } else {
                startDate = minDate;
                endDate = maxDate;
            }
            if (data.getStringExtra("LAT").isEmpty() || data.getStringExtra("LONG").isEmpty()) {
                searchLoc.setLongitude(0);
                searchLoc.setLatitude(0);
            } else {
                searchLoc.setLatitude(Double.parseDouble(data.getStringExtra("LAT")));
                searchLoc.setLongitude(Double.parseDouble(data.getStringExtra("LONG")));
            }
            if (data.getStringExtra("DIST").isEmpty()) {
                searchDist = defaultDist;
            } else {
                searchDist = Double.parseDouble(data.getStringExtra("DIST"));
            }
            captionSearch = data.getStringExtra("CAPTION");
            TextView noResult = (TextView) findViewById((R.id.noResult));
            populateGallery(startDate, endDate);
            currentPhotoIndex = 0;
            if (photoGallery.size() > 0) {
                noResult.setText("");
                displayPhoto(photoGallery.get(currentPhotoIndex), photoCaptions.get(currentPhotoIndex));
            }
            // No photos found
            else {
                //Clean up date field
                TextView dateText = findViewById(R.id.dateText);
                dateText.setText("");

                //Clean up location field
                TextView locText = findViewById(R.id.locationText);
                locText.setText("");

                //clean up image field
                ImageView iv = (ImageView) findViewById(R.id.ivGallery);
                iv.setImageBitmap(null);

                //clean up caption field
                EditText captionView = (EditText) findViewById(R.id.editText);
                captionView.setText("Caption");

                //Set text for no picture
                noResult.setText("No photos found. Try adjusting search filters.");
            }
        }
    }

    // Method to generate JPEG file to store image and txt file to store caption
    private File createImageFile() throws IOException {
        // Get location of device
        loc.getLongitude();
        fusedLocationClient.getLastLocation();
        loc.getLongitude();
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
