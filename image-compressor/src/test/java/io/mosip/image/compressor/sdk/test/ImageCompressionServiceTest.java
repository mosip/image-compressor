package io.mosip.image.compressor.sdk.test;

import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;

import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.image.compressor.sdk.service.ImageCompressionService;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;

class ImageCompressionServiceTest extends ImageCompressionService {
	public ImageCompressionServiceTest(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		super(env, sample, modalitiesToExtract, flags);
	}

	@Override
	protected void setFlags(Map<String, String> flags) {
		super.setFlags(flags);
	}

	@Override
	protected void setEnv(Environment env) {
		super.setEnv(env);
	}

	@Override
	protected void setImageCompressorSettings(float[] fxOrginal, float[] fyOrginal, int[] compressionRatio) {
		super.setImageCompressorSettings(fxOrginal, fyOrginal, compressionRatio);
	}

	@Override
	protected Map<BiometricType, List<BIR>> getBioSegmentMap(BiometricRecord bioRecord,
			List<BiometricType> modalitiesToMatch) {
		return super.getBioSegmentMap(bioRecord, modalitiesToMatch);
	}

	@Override 
	protected void handleUnknownException(SDKException ex, Response<BiometricRecord> response) {
		super.handleUnknownException(ex, response);
	}

	@Override
	protected byte[] resizeAndCompress(byte[] jp2000Bytes) {
		return super.resizeAndCompress(jp2000Bytes);
	}

	@Override
	protected byte[] getBirData(BIR bir) {
		return super.getBirData(bir);
	}

	@Override
	protected byte[] doFaceConversion(String purpose, byte[] imageData) {
		return super.doFaceConversion(purpose, imageData);
	}

	@Override
	protected boolean isValidBIRParams(BIR segment, BiometricType bioType, String bioSubType) {
		return super.isValidBIRParams(segment, bioType, bioSubType);
	}

	@Override
	protected byte[] getBDBData(io.mosip.kernel.biometrics.constant.PurposeType purposeType, BiometricType bioType,
			String bioSubType, byte[] bdbData) {
		return super.getBDBData(purposeType, bioType, bioSubType, bdbData);
	}

	@Override
	protected byte[] getBiometericData(io.mosip.kernel.biometrics.constant.PurposeType purposeType,
			BiometricType bioType, String bioSubType, String bdbData) {
		return super.getBiometericData(purposeType, bioType, bioSubType, bdbData);
	}

	@Override
	protected byte[] getFaceBdb(io.mosip.kernel.biometrics.constant.PurposeType purposeType, String biometricSubType,
			String bdbData) {
		return super.getFaceBdb(purposeType, biometricSubType, bdbData);
	}

	@Override
	protected io.mosip.kernel.biometrics.constant.ProcessedLevelType getProcessedLevelType() {
		return super.getProcessedLevelType();
	}

	@Override
	protected io.mosip.kernel.biometrics.constant.PurposeType getPurposeType() {
		return super.getPurposeType();
	}

	public Response<BiometricRecord> extract() {
		return getExtractTemplateInfo();
	}
}