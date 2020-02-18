package com.example.photogalleryapp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.example.photogalleryapp.Utility.SearchUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchUtilityTest {

    @Test
    public void search_working() throws Exception{
        SearchUtility su = new SearchUtility();
        ArrayList<String> photoGallery;
        ArrayList<String> photoCaptions;
        ArrayList<ArrayList<String>> gallery = new ArrayList<ArrayList<String>>();
        ArrayList<String[]> testDetails = new ArrayList<String[]>();
        //String[] testData = new String[6];
        double[] searchLocDouble = {49, -123};
        Date minDate = new SimpleDateFormat("yyyyMMdd").parse("20200101");
        Date maxDate = new SimpleDateFormat("yyyyMMdd").parse("20210101");
        String caption = "";
        double dist = 150;
        int i = 0;

        // Generate dummy test data
        for (i =0; i<3; i++){
            String[] temp = new String[6];
            temp[0] = "sd/file" + Integer.toString(i) + ".jpg";
            temp[1] = "sd/file" + Integer.toString(i) + ".txt";
            temp[2] = "cap" + Integer.toString(i);
            temp[3] = "2020010" + (Integer.toString(i+1)) + "_162324";
            temp[4] = "49";
            temp[5] = "-123";
            testDetails.add(temp);
        }

        // Run search function, test all are present
        gallery = SearchUtility.searchFunc(minDate, maxDate, caption, dist, searchLocDouble, testDetails);
        photoGallery = gallery.get(0);
        photoCaptions = gallery.get(1);
        assertEquals(3, photoGallery.size());
        assertEquals(3, photoCaptions.size());

        // Filter with old date, test none returned
        minDate = new SimpleDateFormat("yyyyMMdd").parse("20100101");
        maxDate = new SimpleDateFormat("yyyyMMdd").parse("20140101");
        gallery = SearchUtility.searchFunc(minDate, maxDate, caption, dist, searchLocDouble, testDetails);
        photoGallery = gallery.get(0);
        photoCaptions = gallery.get(1);
        assertEquals(0, photoGallery.size());
        assertEquals(0, photoCaptions.size());

        // Contstrain date, check one returned
        minDate = new SimpleDateFormat("yyyyMMdd").parse("20200101");
        maxDate = new SimpleDateFormat("yyyyMMdd").parse("20200102");
        gallery = SearchUtility.searchFunc(minDate, maxDate, caption, dist, searchLocDouble, testDetails);
        photoGallery = gallery.get(0);
        photoCaptions = gallery.get(1);
        assertEquals(1, photoGallery.size());
        assertEquals(1, photoCaptions.size());

        // Set to distant location, check for none
        searchLocDouble[0] = 0;
        gallery = SearchUtility.searchFunc(minDate, maxDate, caption, dist, searchLocDouble, testDetails);
        photoGallery = gallery.get(0);
        photoCaptions = gallery.get(1);
        assertEquals(0, photoGallery.size());
        assertEquals(0, photoCaptions.size());

        // Reset location search for all files
        searchLocDouble[0] = 49;
        dist = 20;
        minDate = new SimpleDateFormat("yyyyMMdd").parse("20200101");
        maxDate = new SimpleDateFormat("yyyyMMdd").parse("20200106");
        gallery = SearchUtility.searchFunc(minDate, maxDate, caption, dist, searchLocDouble, testDetails);
        photoGallery = gallery.get(0);
        photoCaptions = gallery.get(1);
        assertEquals(3, photoGallery.size());
        assertEquals(3, photoCaptions.size());

        // Set caption and check for single result
        caption = "cap2";
        gallery = SearchUtility.searchFunc(minDate, maxDate, caption, dist, searchLocDouble, testDetails);
        photoGallery = gallery.get(0);
        photoCaptions = gallery.get(1);
        assertEquals(1, photoGallery.size());
        assertEquals(1, photoCaptions.size());
    }
}
