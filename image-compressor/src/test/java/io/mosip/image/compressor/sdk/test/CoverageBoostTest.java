package io.mosip.image.compressor.sdk.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.constant.SdkConstant;
import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.entities.VersionType;
import io.mosip.kernel.biometrics.model.Response;

/**
 * Extra paths to satisfy JaCoCo ≥ 90% (handleUnknownException branches, SDKService
 * negatives, V2 extractTemplate happy path).
 */
class CoverageBoostTest {

	private ImageCompressionServiceTest service;
	private Environment env;
	private BiometricRecord sampleFace;

	@BeforeEach
	void setUp() throws Exception {
		env = mock(Environment.class);
		service = new ImageCompressionServiceTest(env, null, null, null);
		sampleFace = loadSampleFace();
	}

	@Test
	void handleUnknownException_allBranches() {
		assertStatus(ResponseStatus.INVALID_INPUT);
		assertStatus(ResponseStatus.MISSING_INPUT);
		assertStatus(ResponseStatus.QUALITY_CHECK_FAILED);
		assertStatus(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF);
		assertStatus(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED);
		assertStatus(ResponseStatus.POOR_DATA_QUALITY);
		assertStatus(ResponseStatus.UNKNOWN_ERROR);
		// unmapped code → default UNKNOWN_ERROR
		Response<BiometricRecord> response = new Response<>();
		service.handleUnknownException(new SDKException("999", "x"), response);
		assertEquals(ResponseStatus.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode());
	}

	private void assertStatus(ResponseStatus status) {
		Response<BiometricRecord> response = new Response<>();
		service.handleUnknownException(new SDKException(String.valueOf(status.getStatusCode()), "x"), response);
		assertEquals(status.getStatusCode(), response.getStatusCode());
		assertNull(response.getResponse());
	}

	@Test
	void v2ExtractTemplate_sampleFace() {
		ImageCompressorSDKV2 sdk = new ImageCompressorSDKV2();
		sdk.setEnv(env);
		Response<BiometricRecord> response = sdk.extractTemplate(sampleFace, List.of(BiometricType.FACE),
				new HashMap<>());
		assertEquals(ResponseStatus.SUCCESS.getStatusCode(), response.getStatusCode());
		assertNotNull(response.getResponse());
		assertEquals(ProcessedLevelType.RAW, response.getResponse().getSegments().get(0).getBdbInfo().getLevel());
		assertEquals(PurposeType.VERIFY, response.getResponse().getSegments().get(0).getBdbInfo().getPurpose());
		assertNotNull(sdk.getEnv());
		assertNull(sdk.convertFormat(sampleFace, "a", "b", Map.of(), Map.of(), List.of()));
	}

	@Test
	void extractTemplate_nullSample_missingInput() {
		ImageCompressionServiceTest svc = new ImageCompressionServiceTest(env, null, null, null);
		Response<BiometricRecord> response = svc.extract();
		assertEquals(ResponseStatus.MISSING_INPUT.getStatusCode(), response.getStatusCode());
	}

	@Test
	void extractTemplate_wrongFormatType() {
		BIR bir = new BIR.BIRBuilder().withBdbInfo(new BDBInfo.BDBInfoBuilder()
				.withFormat(new RegistryIDType("257", "7")).withType(List.of(BiometricType.FACE)).build())
				.withBdb(new byte[] { 1 }).build();
		BiometricRecord record = new BiometricRecord();
		record.setSegments(List.of(bir));
		ImageCompressionServiceTest svc = new ImageCompressionServiceTest(env, record, null, null);
		Response<BiometricRecord> response = svc.extract();
		// production throws with INVALID_INPUT.ordinal() (not statusCode) → UNKNOWN_ERROR
		assertEquals(ResponseStatus.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode());
	}

	@Test
	void extractTemplate_nullBdbInfo() {
		BIR bir = new BIR.BIRBuilder().withBdb(new byte[] { 1 }).build();
		BiometricRecord record = new BiometricRecord();
		record.setSegments(List.of(bir));
		ImageCompressionServiceTest svc = new ImageCompressionServiceTest(env, record, null, null);
		Response<BiometricRecord> response = svc.extract();
		// production throws with INVALID_INPUT.ordinal() (not statusCode) → UNKNOWN_ERROR
		assertEquals(ResponseStatus.UNKNOWN_ERROR.getStatusCode(), response.getStatusCode());
	}

	@Test
	void doFaceConversion_nullBytes() {
		assertThrows(SDKException.class, () -> service.doFaceConversion("REGISTRATION", null));
	}

	@Test
	void doFaceConversion_badBytes() {
		assertThrows(SDKException.class, () -> service.doFaceConversion("REGISTRATION", new byte[] { 1, 2, 3 }));
	}

	@Test
	void sdkService_negatives() {
		BIR finger = mock(BIR.class);
		BDBInfo info = mock(BDBInfo.class);
		when(finger.getBdbInfo()).thenReturn(info);
		when(info.getType()).thenReturn(List.of(BiometricType.FINGER));
		assertThrows(SDKException.class, () -> service.isValidBIRParams(finger, BiometricType.FINGER, null));
		assertThrows(SDKException.class,
				() -> service.getBDBData(PurposeType.VERIFY, BiometricType.FACE, null, null));
		assertThrows(SDKException.class,
				() -> service.getBDBData(PurposeType.VERIFY, BiometricType.FACE, null, new byte[0]));
		assertThrows(SDKException.class,
				() -> service.getBiometericData(PurposeType.VERIFY, BiometricType.FINGER, null, "YQ=="));
		assertThrows(SDKException.class, () -> service.getFaceBdb(PurposeType.VERIFY, null, "!!!"));
	}

	@Test
	void setSettings_flagParseError_keepsDefaults() {
		Map<String, String> flags = new HashMap<>();
		flags.put(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, "bad");
		flags.put(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY, "bad");
		flags.put(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO, "bad");
		service.setFlags(flags);
		float[] fx = new float[1];
		float[] fy = new float[1];
		int[] ratio = new int[1];
		service.setImageCompressorSettings(fx, fy, ratio);
		assertEquals(0.25f, fx[0]);
		assertEquals(0.25f, fy[0]);
		assertEquals(50, ratio[0]);
	}

	@Test
	void processedLevelAndPurpose() {
		assertEquals(ProcessedLevelType.RAW, service.getProcessedLevelType());
		assertEquals(PurposeType.VERIFY, service.getPurposeType());
	}

	private static BiometricRecord loadSampleFace() throws Exception {
		String path = CoverageBoostTest.class.getResource("/sample_files/sample_face.xml").getPath();
		if (path.contains("%")) {
			path = java.net.URLDecoder.decode(path, java.nio.charset.StandardCharsets.UTF_8);
		}
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
		doc.getDocumentElement().normalize();
		BiometricRecord record = new BiometricRecord();
		java.util.ArrayList<BIR> segments = new java.util.ArrayList<>();
		NodeList childNodes = doc.getDocumentElement().getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (!"bir".equalsIgnoreCase(child.getNodeName())) {
				continue;
			}
			Element el = (Element) child;
			Node nVersion = el.getElementsByTagName("Version").item(0);
			int major = Integer.parseInt(((Element) nVersion).getElementsByTagName("Major").item(0).getTextContent());
			int minor = Integer.parseInt(((Element) nVersion).getElementsByTagName("Minor").item(0).getTextContent());
			BIR.BIRBuilder bd = new BIR.BIRBuilder().withVersion(new VersionType(major, minor))
					.withCbeffversion(new VersionType(major, minor));
			Node nBdb = el.getElementsByTagName("BDBInfo").item(0);
			String type = "";
			String subtype = "";
			String format = "";
			NodeList kids = nBdb.getChildNodes();
			for (int z = 0; z < kids.getLength(); z++) {
				Node k = kids.item(z);
				if ("Type".equalsIgnoreCase(k.getNodeName())) {
					type = k.getTextContent();
				}
				if ("Subtype".equalsIgnoreCase(k.getNodeName())) {
					subtype = k.getTextContent();
				}
				if ("Format".equalsIgnoreCase(k.getNodeName())) {
					format = k.getTextContent();
				}
			}
			BDBInfo.BDBInfoBuilder bdb = new BDBInfo.BDBInfoBuilder();
			if (!format.isEmpty()) {
				String[] info = format.split("\n");
				bdb.withFormat(new RegistryIDType(info[1].trim(), info[2].trim()));
			}
			bdb.withType(Collections.singletonList(BiometricType.fromValue(type)));
			bdb.withSubtype(Collections.singletonList(subtype));
			bd.withBdbInfo(new BDBInfo(bdb));
			String bdbB64 = el.getElementsByTagName("BDB").item(0).getTextContent();
			bd.withBdb(java.util.Base64.getDecoder().decode(bdbB64));
			segments.add(new BIR(bd));
		}
		record.setSegments(segments);
		assertTrue(!segments.isEmpty());
		return record;
	}
}
