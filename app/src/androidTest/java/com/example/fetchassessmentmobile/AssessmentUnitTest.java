package com.example.fetchassessmentmobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class AssessmentUnitTest {
    private static String TESTURL = "https://fetch-hiring.s3.amazonaws.com/hiring.json";
    private static DataProcessor dataProcessor;

    @BeforeClass
    public static void Setup() {
        dataProcessor = new DataProcessor(TESTURL);
    }

    @Test (timeout = 1000)
    public void GETJSONTest() {
        assertNotSame(new String[]{}, dataProcessor.getListIds());
        assertNotSame(null, dataProcessor.getListIds());
    }

    @Test
    public void JSONCleanTest() {
        ArrayList<String> formattedStrings = dataProcessor.getFormattedListIdStrings();

        for (String s : formattedStrings) {
            assertFalse(s.contains("  "));
            assertFalse(s.contains("null"));
        }
    }

}
