package io.mosip.image.compressor.sdk.test;

import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.mosip.image.compressor.sdk.impl.ImageCompressorSDK;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.entities.VersionType;
import io.mosip.kernel.biometrics.model.Response;

@SuppressWarnings("removal")
public class SampleSDKTest {

    Logger LOGGER = LoggerFactory.getLogger(SampleSDKTest.class);

    private String sampleFace = "";

    @Before
    public void Setup() {
    	sampleFace = SampleSDKTest.class.getResource("/sample_files/sample_face.xml").getPath();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void test_face() {
        try {
            List<BiometricType> modalitiesToMatch = new ArrayList<>(){{
                add(BiometricType.FACE);
                add(BiometricType.FINGER);
                add(BiometricType.IRIS);
            }};
            BiometricRecord sampleRecord = xmlFileToBiometricRecord(sampleFace);

            ImageCompressorSDK sampleSDK = new ImageCompressorSDK();	// NOSONAR
			Response<BiometricRecord> response = sampleSDK.extractTemplate(sampleRecord, modalitiesToMatch, new HashMap<>());// NOSONAR
            if (response != null && response.getResponse() != null)
            {
            	BiometricRecord compressedRecord = response.getResponse();
            	LOGGER.info("Response {}", compressedRecord);

                Assert.assertEquals("Should be Raw", compressedRecord.getSegments().get(0).getBdbInfo().getLevel().toString(), ProcessedLevelType.RAW.toString());
                
                LOGGER.info("BDB base64 encoded {}", Base64.getEncoder().encodeToString(compressedRecord.getSegments().get(0).getBdb()));
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private BiometricRecord xmlFileToBiometricRecord(String path) throws ParserConfigurationException, IOException, SAXException {
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
            if(childNode.getNodeName().equalsIgnoreCase("bir")){
                BIR.BIRBuilder bd = new BIR.BIRBuilder();

                /* Version */
                Node nVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
                String major_version = ((Element) nVersion).getElementsByTagName("Major").item(0).getTextContent();
                String minor_version = ((Element) nVersion).getElementsByTagName("Minor").item(0).getTextContent();
                VersionType bir_version = new VersionType(parseInt(major_version), parseInt(minor_version));
                bd.withVersion(bir_version);

                /* CBEFF Version */
                Node nCBEFFVersion = ((Element) childNode).getElementsByTagName("Version").item(0);
                String cbeff_major_version = ((Element) nCBEFFVersion).getElementsByTagName("Major").item(0).getTextContent();
                String cbeff_minor_version = ((Element) nCBEFFVersion).getElementsByTagName("Minor").item(0).getTextContent();
                VersionType cbeff_bir_version = new VersionType(parseInt(cbeff_major_version), parseInt(cbeff_minor_version));
                bd.withCbeffversion(cbeff_bir_version);

                /* BDB Info */
                Node nBDBInfo = ((Element) childNode).getElementsByTagName("BDBInfo").item(0);
                String bdbInfoType = "";
                String bdbInfoSubtype = "";
                String bdbInfoFormat = "";
                String bdbInfoCreationDate = ""; // NOSONAR
                NodeList nBDBInfoChilds = nBDBInfo.getChildNodes();
                for (int z=0; z < nBDBInfoChilds.getLength(); z++){
                    Node nBDBInfoChild = nBDBInfoChilds.item(z);
                    if(nBDBInfoChild.getNodeName().equalsIgnoreCase("Type")){
                        bdbInfoType = nBDBInfoChild.getTextContent();
                    }
                    if(nBDBInfoChild.getNodeName().equalsIgnoreCase("Subtype")){
                        bdbInfoSubtype = nBDBInfoChild.getTextContent();
                    }
                    if(nBDBInfoChild.getNodeName().equalsIgnoreCase("Format")){
                    	bdbInfoFormat = nBDBInfoChild.getTextContent();
                    }
                    if(nBDBInfoChild.getNodeName().equalsIgnoreCase("CreationDate")){
                    	bdbInfoCreationDate = nBDBInfoChild.getTextContent();// NOSONAR
                    }
                }

                BDBInfo.BDBInfoBuilder bdbInfoBuilder = new BDBInfo.BDBInfoBuilder();
                if (!bdbInfoFormat.isEmpty())
                {
                	String[] info = bdbInfoFormat.split("\n");
                	bdbInfoBuilder.withFormat(new RegistryIDType(info[1].trim(), info[2].trim()));
                }
                bdbInfoBuilder.withType(Arrays.asList(BiometricType.fromValue(bdbInfoType)));
                bdbInfoBuilder.withSubtype(Arrays.asList(bdbInfoSubtype));
                BDBInfo bdbInfo = new BDBInfo(bdbInfoBuilder);
                bd.withBdbInfo(bdbInfo);

                /* BDB */
                String strBDB = ((Element) childNode).getElementsByTagName("BDB").item(0).getTextContent();
                bd.withBdb(Base64.getDecoder().decode (strBDB));

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