package com.example.photogalleryapp;
import org.junit.Test;
import static org.junit.Assert.*;
import com.example.photogalleryapp.Utility.SearchUtility;
public class getDistTest {
    @Test
    public void dist_working() throws Exception {
        // Locations of Vancouver and Toronto, respectively
        double[] loc1 = {49.2, -123};
        double[] loc2 = {43.65, -79,3};

        // Get distance between points, assert equal to known distance
        double dist = SearchUtility.getDist(loc1, loc2);
        assertEquals(3370, dist, 15);

        // Check distance to same point is 0
        dist = SearchUtility.getDist(loc1,loc1);
        assertEquals(0, dist, 0.01);
    }
}
