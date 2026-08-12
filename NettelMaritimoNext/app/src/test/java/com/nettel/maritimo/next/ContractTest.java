package com.nettel.maritimo.next;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ContractTest {
    @Test
    public void productionApiUsesHttpsRestBase() {
        assertTrue(BuildConfig.API_BASE_URL.startsWith("https://"));
        assertTrue(BuildConfig.API_BASE_URL.endsWith("/api/v1/"));
    }
}
