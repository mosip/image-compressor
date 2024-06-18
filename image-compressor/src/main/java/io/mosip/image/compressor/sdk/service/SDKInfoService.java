package io.mosip.image.compressor.sdk.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.model.SDKInfo;

/**
 * Provides information about the Biometric Software Development Kit (SDK).
 * 
 * This class extends `SDKService` and offers methods to retrieve details about
 * the SDK's capabilities and functionalities.
 */
public class SDKInfoService extends SDKService {
	private String apiVersion;
	private String sample1;
	private String sample2;
	private String sample3;

	/**
	 * Constructs a new `SDKInfoService` instance.
	 * 
	 * @param env        An environment object likely used to access configuration
	 *                   properties.
	 * @param apiVersion The API version of the Biometric SDK.
	 * @param sample1    A string value of unclear purpose (might be sample data
	 *                   reference).
	 * @param sample2    A string value of unclear purpose (might be sample data
	 *                   reference).
	 * @param sample3    A string value of unclear purpose (might be sample data
	 *                   reference).
	 */
	public SDKInfoService(Environment env, String apiVersion, String sample1, String sample2, String sample3) {
		super(env, null);
		this.apiVersion = apiVersion;
		this.sample1 = sample1;
		this.sample2 = sample2;
		this.sample3 = sample3;
	}

	/**
	 * Creates and returns an `SDKInfo` object containing details about the
	 * Biometric SDK.
	 * 
	 * This method constructs an `SDKInfo` object with the provided API version and
	 * sets pre-defined supported modalities (facial recognition) and supported
	 * methods (extraction).
	 * 
	 * @return An `SDKInfo` object containing information about the Biometric SDK.
	 */
	public SDKInfo getSDKInfo() {
		SDKInfo sdkInfo = new SDKInfo(this.apiVersion, this.sample1, this.sample2, this.sample3);
		List<BiometricType> supportedModalities = new ArrayList<>();
		supportedModalities.add(BiometricType.FACE);
		sdkInfo.setSupportedModalities(supportedModalities);
		Map<BiometricFunction, List<BiometricType>> supportedMethods = new EnumMap<>(BiometricFunction.class);
		supportedMethods.put(BiometricFunction.EXTRACT, supportedModalities);
		sdkInfo.setSupportedMethods(supportedMethods);
		return sdkInfo;
	}
}