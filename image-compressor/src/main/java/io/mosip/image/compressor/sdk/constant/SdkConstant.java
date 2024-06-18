package io.mosip.image.compressor.sdk.constant;

/**
 * Constants used in the Image Compressor SDK configuration.
 * <p>
 * This class provides constant variable names used in the configuration
 * related to image compression and resizing factors.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * double resizeFactorFx = env.getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, Double.class, 0.25);
 * double resizeFactorFy = env.getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY, Double.class, 0.25);
 * int compressionRatio = env.getProperty(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO, Integer.class, 50);
 * }</pre>
 * </p>
 * <p>
 * Note: The constants {@code IMAGE_COMPRESSOR_RESIZE_FACTOR_FX} and {@code IMAGE_COMPRESSOR_RESIZE_FACTOR_FY}
 * represent the resize factors for image compression, and {@code IMAGE_COMPRESSOR_COMPRESSION_RATIO} represents
 * the compression ratio.
 * </p>
 * <p>
 * All constants are defined as {@code public static final} and are not meant to be instantiated.
 * </p>
 * 
 * @author Janardhan B S
 * @since 1.0
 */
public class SdkConstant {
	private SdkConstant() {
		throw new IllegalStateException("SdkConstant class");
	}

	/**
     * Configuration key for the resize factor FX used in image compression.
     * <p>
     * The value range is from 0 to {@code Double.MAX_VALUE}.
     * </p>
     */
	public static final String IMAGE_COMPRESSOR_RESIZE_FACTOR_FX = "mosip.bio.image.compressor.resize.factor.fx";
	 /**
     * Configuration key for the resize factor FY used in image compression.
     * <p>
     * The value range is from 0 to {@code Double.MAX_VALUE}.
     * </p>
     */
	public static final String IMAGE_COMPRESSOR_RESIZE_FACTOR_FY = "mosip.bio.image.compressor.resize.factor.fy";
	/**
     * Configuration key for the compression ratio used in image compression.
     * <p>
     * The value range is from 1 to 1000.
     * </p>
     */
	public static final String IMAGE_COMPRESSOR_COMPRESSION_RATIO = "mosip.bio.image.compressor.compression.ratio";
}