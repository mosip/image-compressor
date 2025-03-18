package io.mosip.image.compressor.sdk.test;

import static java.lang.Integer.parseInt;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.impl.ImageCompressorSDK;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.entities.VersionType;
import io.mosip.kernel.biometrics.model.Response;
import junit.framework.Assert;

class SampleSDKTest {
	Logger LOGGER = LoggerFactory.getLogger(SampleSDKTest.class);

	private String sampleFace = "";

	@BeforeEach
	void setUp() {
		sampleFace = SampleSDKTest.class.getResource("/sample_files/sample_face.xml").getPath();
	}

	@SuppressWarnings({ "deprecation", "removal" })
	@Test
	void test_face() {
		try {
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord sampleRecord = xmlFileToBiometricRecord(sampleFace);

			ImageCompressorSDK sampleSDK = new ImageCompressorSDK();
			Response<BiometricRecord> response = sampleSDK.extractTemplate(sampleRecord, modalitiesToMatch,
					new HashMap<>());
			if (response != null && response.getResponse() != null) {
				BiometricRecord compressedRecord = response.getResponse();
				LOGGER.info("Response {}", compressedRecord);

				Assert.assertEquals("Should be Raw",
						compressedRecord.getSegments().get(0).getBdbInfo().getLevel().toString(),
						ProcessedLevelType.RAW.toString());

				LOGGER.info("BDB base64 encoded {}",
						Base64.getEncoder().encodeToString(compressedRecord.getSegments().get(0).getBdb()));
			}
		} catch (ParserConfigurationException e) {
			LOGGER.error("test_face", e);
		} catch (IOException e) {
			LOGGER.error("test_face", e);
		} catch (SAXException e) {
			LOGGER.error("test_face", e);
		}
	}

	@Test
	void testFaceBIR() {
		try {
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord sample_record = xmlFileToBiometricRecord(sampleFace);
			BIR birSegment = sample_record.getSegments().getFirst();
			ImageCompressionServiceTest service = new ImageCompressionServiceTest(null, sample_record,
					modalitiesToMatch, null);
			byte[] data = service.getBirData(birSegment);

			// Validate that data is not null
			assertNotNull(data, "The image data should not be null");

			// Optionally, add further checks on the data length, format, or content if
			// known
			assertTrue(data.length > 0, "The image data should not be empty");

			LOGGER.info("testFaceBIR face image base64 encoded {}", Base64.getEncoder().encodeToString(data));
		} catch (Exception e) {
			LOGGER.error("testFace", e);
		}
	}

	@Test
	void testGetBirDataWithNullBioSubTypeList() {
		try {
			BIR bir = createBIRWithNullBioSubTypeList();
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord sample_record = xmlFileToBiometricRecord(sampleFace);
			ImageCompressionServiceTest service = new ImageCompressionServiceTest(null, sample_record,
					modalitiesToMatch, null);

			assertThrows(SDKException.class, () -> service.getBirData(bir),
					"Should throw SDKException for null bioSubTypeList");
		} catch (Exception e) {
			LOGGER.error("testGetBirDataWithNullBioSubTypeList", e);
		}
	}

	@Test
	void testGetBirDataWithEmptyBioSubTypeList() {
		try {
			BIR bir = createBIRWithEmptyBioSubTypeList();
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord sample_record = xmlFileToBiometricRecord(sampleFace);
			ImageCompressionServiceTest service = new ImageCompressionServiceTest(null, sample_record,
					modalitiesToMatch, null);

			assertThrows(Exception.class, () -> service.getBirData(bir),
					"Should throw SDKException for empty bioSubTypeList");
		} catch (Exception e) {
			LOGGER.error("testGetBirDataWithEmptyBioSubTypeList", e);
		}
	}

	@Test
	void testGetBirDataWithInvalidBiometricType() {
		try {
			BIR bir = createBIRWithInvalidBiometricType();
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord sample_record = xmlFileToBiometricRecord(sampleFace);
			ImageCompressionServiceTest service = new ImageCompressionServiceTest(null, sample_record,
					modalitiesToMatch, null);

			assertThrows(Exception.class, () -> service.getBirData(bir),
					"Should throw SDKException for invalid biometricType");
		} catch (Exception e) {
			LOGGER.error("testGetBirDataWithInvalidBiometricType", e);
		}
	}

	@Test
	void testGetBirDataWithMultipleBioSubTypes() {
		try {
			BIR bir = createBIRWithMultipleBioSubTypes();
			List<BiometricType> modalitiesToMatch = new ArrayList<>() {
				{
					add(BiometricType.FACE);
					add(BiometricType.FINGER);
					add(BiometricType.IRIS);
				}
			};
			BiometricRecord sample_record = xmlFileToBiometricRecord(sampleFace);
			BIR birSegment = sample_record.getSegments().getFirst();
			ImageCompressionServiceTest service = new ImageCompressionServiceTest(null, sample_record,
					modalitiesToMatch, null);

			// Act
			byte[] result = service.getBirData(bir);

			// Assert
			assertNotNull(result, "BIR data should not be null when bioSubTypeList has multiple entries");
			assertTrue(result.length > 0, "BIR data should not be empty");
		} catch (Exception e) {
			LOGGER.error("testGetBirDataWithMultipleBioSubTypes", e);
		}
	}

	private BIR createValidBIR() {
		BIR bir = new BIR();
		BDBInfo bdbInfo = new BDBInfo();
		bdbInfo.setType(Collections.singletonList(BiometricType.FACE));
		bdbInfo.setPurpose(PurposeType.VERIFY);
		bdbInfo.setSubtype(Arrays.asList("Left"));
		bir.setBdbInfo(bdbInfo);
		bir.setBdb(new byte[] { 1, 2, 3, 4 }); // Sample BDB data
		return bir;
	}

	private BIR createBIRWithNullBioSubTypeList() {
		BIR bir = createValidBIR();
		bir.getBdbInfo().setSubtype(null);
		return bir;
	}

	private BIR createBIRWithEmptyBioSubTypeList() {
		BIR bir = createValidBIR();
		bir.getBdbInfo().setSubtype(Collections.emptyList());
		return bir;
	}

	private BIR createBIRWithInvalidBiometricType() {
		BIR bir = createValidBIR();
		bir.getBdbInfo().setType(Collections.emptyList()); // No valid biometric type
		return bir;
	}

	private BIR createBIRWithMultipleBioSubTypes() {
		BIR bir = createValidBIR();
		bir.getBdbInfo().setSubtype(Arrays.asList("Left", "Right"));
		return bir;
	}

	@SuppressWarnings({ "java:S1854", "unused" })
	private BiometricRecord xmlFileToBiometricRecord(String path)
			throws ParserConfigurationException, IOException, SAXException {
		BiometricRecord biometricRecord = new BiometricRecord();
		List<BIR> birSegments = new ArrayList<BIR>();
		File fXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		LOGGER.debug("Root element :{}", doc.getDocumentElement().getNodeName());
		Node rootBIRElement = doc.getDocumentElement();
		NodeList childNodes = rootBIRElement.getChildNodes();
		for (int temp = 0; temp < childNodes.getLength(); temp++) {
			Node childNode = childNodes.item(temp);
			if (childNode.getNodeName().equalsIgnoreCase("bir")) {
				BIR.BIRBuilder bd = new BIR.BIRBuilder();

				/* Version */
				Node nVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
				String major_version = ((Element) nVersion).getElementsByTagName("Major").item(0).getTextContent();
				String minor_version = ((Element) nVersion).getElementsByTagName("Minor").item(0).getTextContent();
				VersionType bir_version = new VersionType(parseInt(major_version), parseInt(minor_version));
				bd.withVersion(bir_version);

				/* CBEFF Version */
				Node nCBEFFVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
				String cbeff_major_version = ((Element) nCBEFFVersion).getElementsByTagName("Major").item(0)
						.getTextContent();
				String cbeff_minor_version = ((Element) nCBEFFVersion).getElementsByTagName("Minor").item(0)
						.getTextContent();
				VersionType cbeff_bir_version = new VersionType(parseInt(cbeff_major_version),
						parseInt(cbeff_minor_version));
				bd.withCbeffversion(cbeff_bir_version);

				/* BDB Info */
				Node nBDBInfo = ((Element) childNode).getElementsByTagName("BDBInfo").item(0);
				String bdbInfoType = "";
				String bdbInfoSubtype = "";
				String bdbInfoFormat = "";
				String bdbInfoCreationDate = "";
				NodeList nBDBInfoChilds = nBDBInfo.getChildNodes();
				for (int z = 0; z < nBDBInfoChilds.getLength(); z++) {
					Node nBDBInfoChild = nBDBInfoChilds.item(z);
					if (nBDBInfoChild.getNodeName().equalsIgnoreCase("Type")) {
						bdbInfoType = nBDBInfoChild.getTextContent();
					}
					if (nBDBInfoChild.getNodeName().equalsIgnoreCase("Subtype")) {
						bdbInfoSubtype = nBDBInfoChild.getTextContent();
					}
					if (nBDBInfoChild.getNodeName().equalsIgnoreCase("Format")) {
						bdbInfoFormat = nBDBInfoChild.getTextContent();
					}
					if (nBDBInfoChild.getNodeName().equalsIgnoreCase("CreationDate")) {
						bdbInfoCreationDate = nBDBInfoChild.getTextContent();
					}
				}

				BDBInfo.BDBInfoBuilder bdbInfoBuilder = new BDBInfo.BDBInfoBuilder();
				if (!bdbInfoFormat.isEmpty()) {
					String[] info = bdbInfoFormat.split("\n");
					bdbInfoBuilder.withFormat(new RegistryIDType(info[1].trim(), info[2].trim()));
				}
				bdbInfoBuilder.withType(Arrays.asList(BiometricType.fromValue(bdbInfoType)));
				bdbInfoBuilder.withSubtype(Arrays.asList(bdbInfoSubtype));
				BDBInfo bdbInfo = new BDBInfo(bdbInfoBuilder);
				bd.withBdbInfo(bdbInfo);

				/* BDB */
				String strBDB = ((Element) childNode).getElementsByTagName("BDB").item(0).getTextContent();
				bd.withBdb(Base64.getDecoder().decode(strBDB));

				/* Prepare BIR */
				BIR bir = new BIR(bd);

				/* Add BIR to list of segments */
				birSegments.add(bir);
			}
		}
		biometricRecord.setSegments(birSegments);
		return biometricRecord;
	}
}