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
}