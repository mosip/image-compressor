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
 * Deprecated implementation class for the Image Compressor SDK.
 * <p>
 * This class provides biometric operations that include initializing the SDK,
 * quality checks, matching biometric samples, extracting templates, and segmenting samples.
 * </p>
 * <p>
 * This class is deprecated and will be removed in a future release.
 * Please migrate to {@link ImageCompressorSDKV2} for updated functionality.
 * </p>
 * <p>
 * It uses Spring's {@code @Component} for automatic component scanning and {@code @EnableAutoConfiguration}
 * for enabling Spring Boot auto-configuration.
 * </p>
 * <p>
 * The methods in this class handle various response scenarios and use {@code Response} objects
 * to encapsulate the response status, message, and data.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * ImageCompressorSDK sdk = new ImageCompressorSDK();
 * SDKInfo sdkInfo = sdk.init(initParams);
 * Response<BiometricRecord> response = sdk.extractTemplate(sample, modalitiesToExtract, flags);
 * }</pre>
 * </p>
 *
 * @author Janardhan B S
 * @since 1.2.0.1
 * @deprecated Since 1.2.0.1, for removal in a future release. Use {@link ImageCompressorSDKV2} instead.
 * @see IBioApi
 * @see ImageCompressionService
 * @see SDKInfoService
 */
@Component
@EnableAutoConfiguration
@Deprecated(since="1.2.0.1", forRemoval=true)
@SuppressWarnings("java:S1313") // Suppress the hardcoded IP address warning from Sonar for "1.2.0.1"
public class ImageCompressorSDK implements IBioApi 
{
	/** The environment. */
	@SuppressWarnings({ "java:S6813" })
	@Autowired		
	private Environment env; 

	private static final String API_VERSION = "0.9";

	private static final String ERROR_NOT_IMPLEMENTED = "Sorry! Method functionality not implemented...";

	 /**
     * Initializes the SDK with the provided initialization parameters.
     *
     * @param initParams The initialization parameters for the SDK.
     * @return Information about the initialized SDK.
     */
	@Override
	public SDKInfo init(Map<String, String> initParams) {
		SDKInfoService service = new SDKInfoService(env, API_VERSION, "sample1", "sample2", "sample3");	
		return service.getSDKInfo();
	}

	 /**
     * Performs quality check on the provided biometric sample.
     *
     * @param sample           The biometric record sample to check.
     * @param modalitiesToCheck The list of biometric types to check.
     * @param flags            Additional configuration flags.
     * @return Response containing the quality check result.
     */
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

	 /**
     * Matches the provided biometric sample against a gallery of biometric records.
     *
     * @param sample            The biometric record sample to match.
     * @param gallery           The array of biometric records in the gallery.
     * @param modalitiesToMatch The list of biometric types to match.
     * @param flags             Additional configuration flags.
     * @return Response containing the match decision array.
     */
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

	 /**
     * Extracts biometric template from the provided biometric sample.
     *
     * @param sample           The biometric record sample to extract template from.
     * @param modalitiesToExtract The list of biometric types to extract.
     * @param flags            Additional configuration flags.
     * @return Response containing the extracted biometric record.
     */
	@Override
	public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		ImageCompressionService service = new ImageCompressionService(env, sample, modalitiesToExtract, flags);
		return service.getExtractTemplateInfo();
	}

	 /**
     * Segments the provided biometric sample into segments based on modalities.
     *
     * @param sample              The biometric record sample to segment.
     * @param modalitiesToSegment The list of biometric types to segment.
     * @param flags               Additional configuration flags.
     * @return Response containing the segmented biometric record.
     */
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

	/**
     * @deprecated (since 1.2.0.1, use {@code ImageCompressorSDKV2#convertFormatV2})
     */
	@Override
	@Deprecated(since = "1.2.0.1", forRemoval = true)
	@SuppressWarnings("java:S1313") // Suppress the hardcoded IP address warning from Sonar for "1.2.0.1"
	public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams,
			List<BiometricType> modalitiesToConvert) {
		return null;
	}
	
	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}
}