package io.mosip.image.compressor.sdk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.model.SDKInfo;

public class SDKInfoService extends SDKService {
	private String apiVersion;
	private String sample1;
	private String sample2;
	private String sample3;

	public SDKInfoService(Environment env, String apiVersion, String sample1, String sample2, String sample3) {
		super(env, null);
		this.apiVersion = apiVersion;
		this.sample1 = sample1;
		this.sample2 = sample2;
		this.sample3 = sample3;
	}

	public SDKInfo getSDKInfo() {
		SDKInfo sdkInfo = new SDKInfo(this.apiVersion, this.sample1, this.sample2, this.sample3);
		List<BiometricType> supportedModalities = new ArrayList<>();
		supportedModalities.add(BiometricType.FACE);
		sdkInfo.setSupportedModalities(supportedModalities);
		Map<BiometricFunction, List<BiometricType>> supportedMethods = new HashMap<>(); 
		supportedMethods.put(BiometricFunction.EXTRACT, supportedModalities);
		sdkInfo.setSupportedMethods(supportedMethods);
		return sdkInfo;
	}
}