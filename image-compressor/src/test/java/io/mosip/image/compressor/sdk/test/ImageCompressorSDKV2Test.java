package io.mosip.image.compressor.sdk.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.constant.SdkConstant;
import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2;
import io.mosip.image.compressor.sdk.service.SDKInfoService;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;

class ImageCompressorSDKV2Test {
	private ImageCompressionServiceTest service;
    private ImageCompressorSDKV2 sdk;
    private Environment env;

    @BeforeEach
    void setUp() {
        env = mock(Environment.class);
        sdk = new ImageCompressorSDKV2();
        sdk.setEnv (env); // Inject mock environment
        
        service = new ImageCompressionServiceTest(env, null, null, null);
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
    void testSegment_NotImplemented() {
        BiometricRecord sample = new BiometricRecord();
        List<BiometricType> modalitiesToSegment = Collections.emptyList();
        Map<String, String> flags = Collections.emptyMap();

        Response<BiometricRecord> response = sdk.segment(sample, modalitiesToSegment, flags);
        assertEquals("500", response.getStatusCode() + "");
		assertThat(response.getStatusMessage(), containsString("UNKNOWN_ERROR"));
    }

    @Test
    void testConvertFormatV2_NotImplemented() {
        BiometricRecord bioRecord = new BiometricRecord();
        String sourceFormat = "format1";
        String targetFormat = "format2";
        Map<String, String> sourceParams = Collections.emptyMap();
        Map<String, String> targetParams = Collections.emptyMap();
        List<BiometricType> modalitiesToConvert = Collections.emptyList();

        Response<BiometricRecord> response = sdk.convertFormatV2(bioRecord, sourceFormat, targetFormat, sourceParams, targetParams, modalitiesToConvert);
        assertEquals("500", response.getStatusCode() + "");
		assertThat(response.getStatusMessage(), containsString("UNKNOWN_ERROR"));
    }
    
    @Test
    void testSetImageCompressorSettings_DefaultValues() {
        float[] fxOriginal = new float[1];
        float[] fyOriginal = new float[1];
        int[] compressionRatio = new int[1];

        service.setImageCompressorSettings(fxOriginal, fyOriginal, compressionRatio);

        assertEquals(0.25f, fxOriginal[0], "fxOriginal should be set to default value");
        assertEquals(0.25f, fyOriginal[0], "fyOriginal should be set to default value");
        assertEquals(50, compressionRatio[0], "compressionRatio should be set to default value");
    }

    @Test
    void testSetImageCompressorSettings_EnvValues() {
        float[] fxOriginal = new float[1];
        float[] fyOriginal = new float[1];
        int[] compressionRatio = new int[1];

        // Setup mock environment
        when(env.getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, Float.class, 0.25f)).thenReturn(0.5f);
        when(env.getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY, Float.class, 0.25f)).thenReturn(0.75f);
        when(env.getProperty(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO, Integer.class, 50)).thenReturn(75);

        service.setImageCompressorSettings(fxOriginal, fyOriginal, compressionRatio);

        assertEquals(0.5f, fxOriginal[0], "fxOriginal should be set from env value");
        assertEquals(0.75f, fyOriginal[0], "fyOriginal should be set from env value");
        assertEquals(75, compressionRatio[0], "compressionRatio should be set from env value");
    }

    @Test
    void testSetImageCompressorSettings_FlagValues() {
        float[] fxOriginal = new float[1];
        float[] fyOriginal = new float[1];
        int[] compressionRatio = new int[1];

        // Setup flags
        Map<String, String> flags = new HashMap<>();
        flags.put(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, "0.6");
        flags.put(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY, "0.8");
        flags.put(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO, "90");
        service.setFlags(flags);

        service.setImageCompressorSettings(fxOriginal, fyOriginal, compressionRatio);

        assertEquals(0.6f, fxOriginal[0], "fxOriginal should be set from flag value");
        assertEquals(0.8f, fyOriginal[0], "fyOriginal should be set from flag value");
        assertEquals(90, compressionRatio[0], "compressionRatio should be set from flag value");
    }

    @Test
    void testSetImageCompressorSettings_ExceptionInEnv() {
        float[] fxOriginal = new float[1];
        float[] fyOriginal = new float[1];
        int[] compressionRatio = new int[1];

        // Setup mock to throw exception
        when(env.getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, Float.class, 0.25f))
                .thenThrow(new RuntimeException("Env error"));

        service.setImageCompressorSettings(fxOriginal, fyOriginal, compressionRatio);

        assertEquals(0.25f, fxOriginal[0], "fxOriginal should remain default due to env error");
        assertEquals(0.25f, fyOriginal[0], "fyOriginal should remain default due to env error");
        assertEquals(50, compressionRatio[0], "compressionRatio should remain default due to env error");
    }
    
    @Test
    void testGetBioSegmentMap_NoFilter_AllModalitiesMatched() {
        BiometricRecord mockRecord = mock(BiometricRecord.class);
        BIR mockSegment = mock(BIR.class);
        BiometricType mockType = BiometricType.FINGER;

        BDBInfo mockBdbInfo = mock(BDBInfo.class);
        when(mockBdbInfo.getType()).thenReturn(Collections.singletonList(mockType));
        when(mockSegment.getBdbInfo()).thenReturn(mockBdbInfo);
        when(mockRecord.getSegments()).thenReturn(Collections.singletonList(mockSegment));

        Map<BiometricType, List<BIR>> result = service.getBioSegmentMap(mockRecord, null);

        assertEquals(1, result.size());
        assertEquals(1, result.get(mockType).size());
        assertEquals(mockSegment, result.get(mockType).get(0));
    }
    
    @Test
    void testGetBioSegmentMap_WithSpecificModalities_MatchesOnlySpecificTypes() {
        BiometricRecord mockRecord = mock(BiometricRecord.class);
        BIR mockSegment1 = mock(BIR.class);
        BIR mockSegment2 = mock(BIR.class);
        BiometricType mockType1 = BiometricType.FINGER;
        BiometricType mockType2 = BiometricType.IRIS;

        BDBInfo mockBdbInfo1 = mock(BDBInfo.class);
        when(mockBdbInfo1.getType()).thenReturn(Collections.singletonList(mockType1));
        when(mockSegment1.getBdbInfo()).thenReturn(mockBdbInfo1);

        BDBInfo mockBdbInfo2 = mock(BDBInfo.class);
        when(mockBdbInfo2.getType()).thenReturn(Collections.singletonList(mockType2));
        when(mockSegment2.getBdbInfo()).thenReturn(mockBdbInfo2);

        when(mockRecord.getSegments()).thenReturn(Arrays.asList(mockSegment1, mockSegment2));

        Map<BiometricType, List<BIR>> result = service.getBioSegmentMap(mockRecord, Collections.singletonList(BiometricType.FINGER));

        assertEquals(1, result.size());
        assertEquals(1, result.get(mockType1).size());
        assertEquals(mockSegment1, result.get(mockType1).get(0));
    }
    
    @Test
    void testGetBioSegmentMap_EmptyBioRecord() {
        BiometricRecord mockRecord = mock(BiometricRecord.class);
        when(mockRecord.getSegments()).thenReturn(Collections.emptyList());

        Map<BiometricType, List<BIR>> result = service.getBioSegmentMap(mockRecord, null);

        assertEquals(0, result.size());
    }
    
    @Test
    void testHandleUnknownException_InvalidInput() {
        SDKException ex = new SDKException("404", "Biometrics not found in CBEFF");
        Response<BiometricRecord> response = new Response<>();

        service.handleUnknownException(ex, response);

        assertEquals(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode(), response.getStatusCode());
        assertEquals(String.format(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusMessage(), "sample"), response.getStatusMessage());
        assertNull(response.getResponse());
    }
}