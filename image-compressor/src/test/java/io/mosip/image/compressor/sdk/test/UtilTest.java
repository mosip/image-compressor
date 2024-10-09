package io.mosip.image.compressor.sdk.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.utils.Util;

public class UtilTest {

    private static final byte[] DATA = "testdata".getBytes();
    private static final byte[] DATA2 = "differentdata".getBytes();
    private static final String METADATA = "metadata";

    @Test
    void testCompareHash_sameData() {
        assertTrue(Util.compareHash(DATA, DATA), "Hashes should match for the same data");
    }

    @Test
    void testCompareHash_differentData() {
        assertFalse(Util.compareHash(DATA, DATA2), "Hashes should not match for different data");
    }

    @Test
    void testComputeFingerPrint_withMetadata() {
        String result = Util.computeFingerPrint(DATA, METADATA);
        assertNotNull(result);
    }

    @Test
    void testComputeFingerPrint_noMetadata() {
        String result = Util.computeFingerPrint(DATA, null);
        assertNotNull(result);
    }

    @Test
    void testEncodeToURLSafeBase64_byteArray() {
        String encoded = Util.encodeToURLSafeBase64(DATA);
        assertEquals(Base64.getUrlEncoder().withoutPadding().encodeToString(DATA), encoded);
    }

    @Test
    void testEncodeToURLSafeBase64_nullByteArray() {
        assertNull(Util.encodeToURLSafeBase64((byte[]) null));
    }

    @Test
    void testEncodeToURLSafeBase64_string() {
        String encoded = Util.encodeToURLSafeBase64("testdata");
        assertEquals(Base64.getUrlEncoder().withoutPadding().encodeToString("testdata".getBytes()), encoded);
    }

    @Test
    void testEncodeToURLSafeBase64_nullString() {
        assertNull(Util.encodeToURLSafeBase64((String) null));
    }

    @Test
    void testDecodeURLSafeBase64() {
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(DATA);
        byte[] decoded = Util.decodeURLSafeBase64(encoded);
        assertArrayEquals(DATA, decoded);
    }

    @Test
    void testDecodeURLSafeBase64_nullData() {
        Exception exception = assertThrows(SDKException.class, () -> {
            Util.decodeURLSafeBase64(null);
        });
        assertTrue(exception.getMessage().contains("decodeURLSafeBase64::data{null}"));
    }

    @Test
    void testIsNullEmpty_byteArray() {
        assertTrue(Util.isNullEmpty((byte[]) null), "Null byte array should be considered empty");
        assertTrue(Util.isNullEmpty(new byte[0]), "Empty byte array should be considered empty");
        assertFalse(Util.isNullEmpty(DATA), "Non-empty byte array should not be considered empty");
    }

    @Test
    void testIsNullEmpty_string() {
        assertTrue(Util.isNullEmpty((String) null), "Null string should be considered empty");
        assertTrue(Util.isNullEmpty(""), "Empty string should be considered empty");
        assertTrue(Util.isNullEmpty("   "), "String with only spaces should be considered empty");
        assertFalse(Util.isNullEmpty("data"), "Non-empty string should not be considered empty");
    }
}