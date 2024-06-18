package io.mosip.image.compressor.sdk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.env.Environment;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.utils.Util;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

/**
 * Abstract base class for Biometric SDK services.
 * 
 * This class defines common methods and functionalities for processing
 * biometric data used by its concrete implementations. It provides methods for:
 * - Accessing environment variables and configuration flags. - Extracting
 * biometric segments from a BiometricRecord object based on specified
 * modalities. - Retrieving biometric data from a Biometric Identification
 * Record (BIR) object. - Processing and extracting relevant information from
 * different biometric modalities (currently only Face is supported).
 * 
 * Subclasses can extend this class to implement specific functionalities
 * related to the Biometric SDK.
 */
public abstract class SDKService {
	private Logger logger = LoggerFactory.getLogger(SDKService.class);
	private Map<String, String> flags;
	private Environment env;

	/**
	 * Constructs an instance of SDKService with the specified environment and
	 * flags.
	 *
	 * @param env   The environment configuration for SDK operations.
	 * @param flags The flags configuration for SDK operations.
	 */
	protected SDKService(Environment env, Map<String, String> flags) {
		setEnv(env);
		setFlags(flags);
	}

	/**
	 * Retrieves the flags configuration currently set in this SDKService instance.
	 *
	 * @return The map of flags configured for SDK operations.
	 */
	protected Map<String, String> getFlags() {
		return flags;
	}

	/**
	 * Sets the flags configuration for this SDKService instance.
	 *
	 * @param flags The map of flags to be set for SDK operations.
	 */
	protected void setFlags(Map<String, String> flags) {
		this.flags = flags;
	}

	/**
	 * Retrieves the environment configuration currently set in this SDKService
	 * instance.
	 *
	 * @return The environment configuration for SDK operations.
	 */
	protected Environment getEnv() {
		return env;
	}

	/**
	 * Sets the environment configuration for this SDKService instance.
	 *
	 * @param env The environment configuration to be set for SDK operations.
	 */
	protected void setEnv(Environment env) {
		this.env = env;
	}

	/**
	 * Extracts a map of BiometricType to corresponding BIR (Biometric
	 * Identification Record) segments from a BiometricRecord object.
	 * 
	 * This method iterates through the segments in the BiometricRecord and filters
	 * them based on the provided modalitiesToMatch list. If no modalities are
	 * specified, all segments are included.
	 * 
	 * @param bioRecord         The BiometricRecord object containing biometric data
	 *                          segments.
	 * @param modalitiesToMatch A list of BiometricType values specifying the
	 *                          modalities to extract (optional).
	 * @return A map where keys are BiometricType and values are lists of
	 *         corresponding BIR segments.
	 */
	protected Map<BiometricType, List<BIR>> getBioSegmentMap(BiometricRecord bioRecord,
			List<BiometricType> modalitiesToMatch) {
		Boolean noFilter = false;

		/**
		 * if the modalities to match is not passed, assume that all modalities have to
		 * be matched.
		 */
		if (modalitiesToMatch == null || modalitiesToMatch.isEmpty())
			noFilter = true;

		Map<BiometricType, List<BIR>> bioSegmentMap = new HashMap<>();
		for (BIR segment : bioRecord.getSegments()) {
			BiometricType bioType = segment.getBdbInfo().getType().get(0);

			/**
			 * ignore modalities that are not to be matched
			 */
			if (Boolean.FALSE.equals(noFilter) && !modalitiesToMatch.contains(bioType))
				continue;

			bioSegmentMap.computeIfAbsent(bioType, k -> new ArrayList<>());
			bioSegmentMap.get(bioType).add(segment);
		}

		return bioSegmentMap;
	}

	/**
	 * Extracts the raw biometric data from a BIR (Biometric Identification Record)
	 * object.
	 * 
	 * This method validates the provided BIR object and its parameters before
	 * attempting to extract the data. If validation fails, an SDKException is
	 * thrown. Otherwise, the appropriate processing method is called based on the
	 * BiometricType.
	 * 
	 * @param bir The BIR object containing biometric data.
	 * @return A byte array containing the raw biometric data.
	 * @throws SDKException If the BIR object is invalid or data extraction fails.
	 */
	protected byte[] getBirData(BIR bir) {
		BiometricType biometricType = bir.getBdbInfo().getType().get(0);
		PurposeType purposeType = bir.getBdbInfo().getPurpose();
		List<String> bioSubTypeList = bir.getBdbInfo().getSubtype();

		String bioSubType = null;
		if (bioSubTypeList != null && !bioSubTypeList.isEmpty()) {
			bioSubType = bioSubTypeList.get(0).trim();
			if (bioSubTypeList.size() >= 2)
				bioSubType += " " + bioSubTypeList.get(1).trim();
		}

		if (isValidBIRParams(bir, biometricType, bioSubType)) {
			return getBDBData(purposeType, biometricType, bioSubType, bir.getBdb());
		}
		throw new SDKException(ResponseStatus.UNKNOWN_ERROR + "", "null");
	}

	/**
	 * Validates the parameters of a BIR object (BiometricType and BioSubType).
	 * 
	 * This method currently only supports facial recognition (BiometricType.FACE).
	 * If a different BiometricType is encountered, it logs an error message and
	 * throws an SDKException. Subclasses can potentially override this method to
	 * support additional modalities.
	 * 
	 * @param segment    The BIR object to be validated.
	 * @param bioType    The BiometricType of the BIR object.
	 * @param bioSubType The BioSubType of the BIR object (optional).
	 * @return boolean (always true for Face modality currently).
	 * @throws SDKException If the BiometricType is not supported.
	 */
	@SuppressWarnings({ "unused" })
	protected boolean isValidBIRParams(BIR segment, BiometricType bioType, String bioSubType) {
		ResponseStatus responseStatus = null;
		if (bioType == BiometricType.FACE)
			return true;
		else {
			logger.error("isValidBIRParams::BiometricType{} BioSubType{}", bioType, bioSubType);
			responseStatus = ResponseStatus.MISSING_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
		}
	}

	/**
	 * Retrieves the biometric data from the given BDB data based on the specified
	 * parameters. If the BDB data is null or empty, throws an SDKException
	 * indicating biometric not found.
	 *
	 * @param purposeType The purpose type for biometric data retrieval.
	 * @param bioType     The type of biometric data (e.g., fingerprint, iris,
	 *                    face).
	 * @param bioSubType  The subtype of biometric data, specific to the bioType.
	 * @param bdbData     The BDB data in URL safe Base64 encoded format.
	 * @return The retrieved biometric data as a byte array.
	 * @throws SDKException If the BDB data is null or empty, indicating biometric
	 *                      not found in CBEFF.
	 */
	protected byte[] getBDBData(PurposeType purposeType, BiometricType bioType, String bioSubType, byte[] bdbData) {
		ResponseStatus responseStatus = null;

		if (bdbData != null && bdbData.length != 0) {
			return getBiometericData(purposeType, bioType, bioSubType, Util.encodeToURLSafeBase64(bdbData));
		}

		responseStatus = ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
	}

	/**
	 * Retrieves the biometric data based on the specified parameters and BDB data.
	 * Currently supports FACE biometric type.
	 *
	 * @param purposeType The purpose type for biometric data retrieval.
	 * @param bioType     The type of biometric data (currently only supports
	 *                    BiometricType.FACE).
	 * @param bioSubType  The subtype of biometric data, specific to the bioType.
	 * @param bdbData     The BDB data in URL safe Base64 encoded format.
	 * @return The retrieved biometric data as a byte array.
	 * @throws SDKException If the biometric type is not supported or if there is an
	 *                      error retrieving the biometric data.
	 */
	protected byte[] getBiometericData(PurposeType purposeType, BiometricType bioType, String bioSubType,
			String bdbData) {
		ResponseStatus responseStatus = null;
		if (bioType == BiometricType.FACE)
			return getFaceBdb(purposeType, bioSubType, bdbData);
		responseStatus = ResponseStatus.INVALID_INPUT;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
	}

	/**
	 * Retrieves the FACE biometric data based on the specified parameters and BDB
	 * data.
	 *
	 * @param purposeType      The purpose type for biometric data retrieval.
	 * @param biometricSubType The subtype of the FACE biometric data.
	 * @param bdbData          The BDB data in URL safe Base64 encoded format.
	 * @return The retrieved FACE biometric data as a byte array.
	 * @throws SDKException If there is an error retrieving the FACE biometric data.
	 */
	@SuppressWarnings({ "unused" })
	protected byte[] getFaceBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
		ResponseStatus responseStatus = null;
		try {
			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Face");
			requestDto.setVersion("ISO19794_5_2011");
			byte[] bioData = Util.decodeURLSafeBase64(bdbData);
			requestDto.setInputBytes(bioData);

			FaceBDIR bdir = FaceDecoder.getFaceBDIR(requestDto);
			return bdir.getImage();
		} catch (Exception ex) {
			ex.printStackTrace();
			responseStatus = ResponseStatus.INVALID_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "",
					responseStatus.getStatusMessage() + " " + ex.getLocalizedMessage());
		}
	}
}