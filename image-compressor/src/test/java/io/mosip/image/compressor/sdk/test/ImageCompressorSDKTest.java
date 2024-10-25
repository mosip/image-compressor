package io.mosip.image.compressor.sdk.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import io.mosip.image.compressor.sdk.impl.ImageCompressorSDK;
import io.mosip.image.compressor.sdk.service.ImageCompressionService;
import io.mosip.image.compressor.sdk.service.SDKInfoService;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;

class ImageCompressorSDKTest {
    private ImageCompressorSDK sdk;
    private Environment env;

    @BeforeEach
    void setUp() {
        env = mock(Environment.class);
        sdk = new ImageCompressorSDK();
        sdk.setEnv (env); // Inject mock environment
    }

    @Test
    void testInit() {
        Map<String, String> initParams = new HashMap<>();
        SDKInfoService service = mock(SDKInfoService.class);
        SDKInfo mockInfo = new SDKInfo("0.9", "1.2.0,1", "Mosip", "Biometrics");
        when(service.getSDKInfo()).thenReturn(mockInfo);

        SDKInfo result = sdk.init(initParams);
        assertNotNull(result);
    }

    @Test
    void testCheckQuality_NotImplemented() {
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalitiesToCheck = Collections.emptyList();
        Map<String, String> flags = Collections.emptyMap();

        Response<QualityCheck> response = sdk.checkQuality(sample, modalitiesToCheck, flags);
        assertEquals("500", response.getStatusCode() + "");
		assertThat(response.getStatusMessage(), containsString("UNKNOWN_ERROR"));
    }

    @Test
    void testMatch_NotImplemented() {
        BiometricRecord sample = new BiometricRecord();
        BiometricRecord[] gallery = new BiometricRecord[0];
        List<BiometricType> modalitiesToMatch = Collections.emptyList();
        Map<String, String> flags = Collections.emptyMap();

        Response<MatchDecision[]> response = sdk.match(sample, gallery, modalitiesToMatch, flags);
        assertEquals("500", response.getStatusCode() + "");
		assertThat(response.getStatusMessage(), containsString("UNKNOWN_ERROR"));
    }

    @Test
    void testExtractTemplate() {
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalitiesToExtract = Collections.emptyList();
        Map<String, String> flags = Collections.emptyMap();
        ImageCompressionService service = mock(ImageCompressionService.class);
        Response<BiometricRecord> mockResponse = new Response<>();
        when(service.getExtractTemplateInfo()).thenReturn(mockResponse);

        Response<BiometricRecord> response = sdk.extractTemplate(sample, modalitiesToExtract, flags);
        assertNotNull(response);
    }

    @Test
    void testSegment_NotImplemented() {
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalitiesToSegment = Collections.emptyList();
        Map<String, String> flags = Collections.emptyMap();

        Response<BiometricRecord> response = sdk.segment(sample, modalitiesToSegment, flags);
        assertEquals("500", response.getStatusCode() + "");
		assertThat(response.getStatusMessage(), containsString("UNKNOWN_ERROR"));
    }

    @Test
    void testConvertFormat_Deprecated() {
        // Arrange
        BiometricRecord sample = new BiometricRecord();
        String sourceFormat = "format1";
        String targetFormat = "format2";
        Map<String, String> sourceParams = Map.of();
        Map<String, String> targetParams = Map.of();
        List<BiometricType> modalitiesToConvert = List.of(BiometricType.FINGER);

        // Act
        BiometricRecord result = sdk.convertFormat(sample, sourceFormat, targetFormat, sourceParams, targetParams, modalitiesToConvert);

        // Assert
        assertNull(result, "Deprecated convertFormat should return null");
    }
}
