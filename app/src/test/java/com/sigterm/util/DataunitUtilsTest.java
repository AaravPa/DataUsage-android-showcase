package com.sigterm.util;

import static org.junit.Assert.assertEquals;

import com.sigterm.entities.enums.QuotaUnit;

import org.junit.Test;

public class DataunitUtilsTest {

    @Test
    public void convertsMegabytesToBytes() {
        assertEquals(5L * 1024L * 1024L,
                DataunitUtils.getBytesFromFormattedData(5.0f, QuotaUnit.MB));
    }

    @Test
    public void convertsGigabytesToBytes() {
        assertEquals(2L * 1024L * 1024L * 1024L,
                DataunitUtils.getBytesFromFormattedData(2.0f, QuotaUnit.GB));
    }

    @Test
    public void convertsBytesToMegabytes() {
        assertEquals(1.5f, DataunitUtils.getMBFromBytes(3L * 1024L * 1024L / 2L), 0.0001f);
    }
}
