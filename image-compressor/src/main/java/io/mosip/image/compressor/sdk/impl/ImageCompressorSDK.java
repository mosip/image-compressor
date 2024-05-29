package io.mosip.image.compressor.sdk.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.service.ImageCompressionService;
import io.mosip.image.compressor.sdk.service.SDKInfoService;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApi;

/**
 * The Class ImageCompressorSDK.
 * 
 * @author Janardhan B S
 * 
 * This class is deprecated and will be removed in future versions.
 *
 * @since 1.2.0.1
 * @deprecated since 1.2.0.1, for removal in a future release
 */
@Component
@EnableAutoConfiguration
@Deprecated(since="1.2.0.1", forRemoval=true)
public class ImageCompressorSDK implements IBioApi 
{
	/** The environment. */
	@SuppressWarnings({ "java:S6813" })
	@Autowired		
	private Environment env; 

	private static final String API_VERSION = "0.9";

	private static final String ERROR_NOT_IMPLEMENTED = "Sorry! Method functionality not implemented...";

	@Override
	public SDKInfo init(Map<String, String> initParams) {
		SDKInfoService service = new SDKInfoService(env, API_VERSION, "sample1", "sample2", "sample3");	
		return service.getSDKInfo();
	}

	@Override
	public Response<QualityCheck> checkQuality(BiometricRecord sample, List<BiometricType> modalitiesToCheck,
			Map<String, String> flags) {
		Response<QualityCheck> response = new Response<>();
		response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
		response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(),
				ERROR_NOT_IMPLEMENTED));	
		response.setResponse(null);
		return response;
	}

	@Override
	public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery,
			List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
		Response<MatchDecision[]> response = new Response<>();
		response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
		response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(),
				ERROR_NOT_IMPLEMENTED));
		response.setResponse(null);
		return response;
	}

	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		ImageCompressionService service = new ImageCompressionService(env, sample, modalitiesToExtract, flags);
		return service.getExtractTemplateInfo();
	}

	@Override
	public Response<BiometricRecord> segment(BiometricRecord sample, List<BiometricType> modalitiesToSegment,
			Map<String, String> flags) {
		Response<BiometricRecord> response = new Response<>();
		response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
		response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(),
				ERROR_NOT_IMPLEMENTED));
		response.setResponse(null);
		return response;
	}

	@Override
	public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams,
			List<BiometricType> modalitiesToConvert) {
		return null;
	}
}
