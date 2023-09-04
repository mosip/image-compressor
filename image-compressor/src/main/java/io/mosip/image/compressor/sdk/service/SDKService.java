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

public abstract class SDKService {
	protected Logger LOGGER = LoggerFactory.getLogger(SDKService.class);
	private Map<String, String> flags;
	private Environment env;

	protected SDKService(Environment env, Map<String, String> flags) {
		setEnv(env);
		setFlags(flags);
	}

	protected Map<String, String> getFlags() {
		return flags;
	}

	protected void setFlags(Map<String, String> flags) {
		this.flags = flags;
	}

	protected Environment getEnv() {
		return env;
	}

	protected void setEnv(Environment env) {
		this.env = env;
	}

	protected Map<BiometricType, List<BIR>> getBioSegmentMap(BiometricRecord record,
			List<BiometricType> modalitiesToMatch) {
		Boolean noFilter = false;

		/**
		 * if the modalities to match is not passed, assume that all modalities have to
		 * be matched.
		 */
		if (modalitiesToMatch == null || modalitiesToMatch.isEmpty())
			noFilter = true;

		Map<BiometricType, List<BIR>> bioSegmentMap = new HashMap<>();
		for (BIR segment : record.getSegments()) {
			BiometricType bioType = segment.getBdbInfo().getType().get(0);

			/**
			 * ignore modalities that are not to be matched
			 */
			if (noFilter == false && !modalitiesToMatch.contains(bioType))
				continue;

			if (!bioSegmentMap.containsKey(bioType)) {
				bioSegmentMap.put(bioType, new ArrayList<BIR>());
			}
			bioSegmentMap.get(bioType).add(segment);
		}

		return bioSegmentMap;
	}

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

		if (isValidBIRParams(bir, biometricType, bioSubType))
		{
			return getBDBData(purposeType, biometricType, bioSubType, bir.getBdb());
		}
		return null;
	}

	protected boolean isValidBIRParams(BIR segment, BiometricType bioType, String bioSubType) {
		ResponseStatus responseStatus = null;
		switch (bioType) {
		case FACE:
			break;
		default:
			LOGGER.error("isValidBIRParams>>BiometricType#" + bioType + ">>BioSubType#" + bioSubType);
			responseStatus = ResponseStatus.MISSING_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
		}
		return true;
	}

	protected byte[] getBDBData(PurposeType purposeType, BiometricType bioType, String bioSubType,
			byte[] bdbData) {
		ResponseStatus responseStatus = null;

		if (bdbData != null && bdbData.length != 0) {
			return getBiometericData(purposeType, bioType, bioSubType, Util.encodeToURLSafeBase64(bdbData));
		}

		responseStatus = ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
	}

	protected byte[] getBiometericData(PurposeType purposeType, BiometricType bioType, String bioSubType,
			String bdbData) {
		ResponseStatus responseStatus = null;
		switch (bioType) {
		case FACE:
			return getFaceBdb(purposeType, bioSubType, bdbData);
		default:
			break;
		}
		responseStatus = ResponseStatus.INVALID_INPUT;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
	}

	protected byte[] getFaceBdb(PurposeType purposeType, String biometricSubType, String bdbData) {
		ResponseStatus responseStatus = null;
		try {
			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Face");
			requestDto.setVersion("ISO19794_5_2011");
			byte[] bioData = null;
			try {
				bioData = Util.decodeURLSafeBase64(bdbData);
				requestDto.setInputBytes(bioData);
			} catch (Exception e) {
				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}
			System.out.println(bioData);
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