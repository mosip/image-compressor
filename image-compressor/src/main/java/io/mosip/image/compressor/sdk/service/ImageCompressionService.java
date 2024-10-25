package io.mosip.image.compressor.sdk.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.constant.SdkConstant;
import io.mosip.image.compressor.sdk.exceptions.SDKException;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;

/**
 * Service class for image compression operations on biometric data. Handles
 * resizing, compression, and conversion of biometric images.
 */
public class ImageCompressionService extends SDKService {
	private Logger logger = LoggerFactory.getLogger(ImageCompressionService.class);

	static {
		/**
		 * load OpenCV library nu.pattern.OpenCV.loadShared();
		 * System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
		 */
		/**
		 * In Java >= 12 it is no longer possible to use addLibraryPath, which modifies
		 * the ClassLoader's static usr_paths field. There does not seem to be any way
		 * around this so we fall back to loadLocally() and return.
		 */
		nu.pattern.OpenCV.loadLocally();
	}

	private BiometricRecord sample;
	@SuppressWarnings("unused")
	private List<BiometricType> modalitiesToExtract;

	public static final long FORMAT_TYPE_FACE = 8;

	/**
	 * Compression service constructor initializing with environment settings,
	 * biometric sample, modalities to extract, and additional flags.
	 *
	 * @param env                 The environment configuration for the SDK.
	 * @param sample              The biometric record sample to process.
	 * @param modalitiesToExtract The list of biometric types to extract.
	 * @param flags               Additional configuration flags.
	 */
	public ImageCompressionService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToExtract = modalitiesToExtract;
	}

	/**
	 * Retrieves and processes biometric data for template extraction.
	 *
	 * @return Response containing the processed biometric record.
	 */
	@SuppressWarnings({ "java:S3776", "java:S6541" })
	public Response<BiometricRecord> getExtractTemplateInfo() {
		logger.info("ExtractTemplateInfo :: Started Request :: {}", sample != null ? sample.toString() : null);

		ResponseStatus responseStatus = null;
		Response<BiometricRecord> response = new Response<>();
		try {
			if (sample == null || sample.getSegments() == null || sample.getSegments().isEmpty()) {
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}

			for (int index = 0; index < sample.getSegments().size(); index++) {
				BIR segment = sample.getSegments().get(index);

				/*
				 * Below Code can be removed if we require PayLoad information
				 */

				if (segment.getBdbInfo() != null && segment.getBdbInfo().getFormat() != null) {
					String type = segment.getBdbInfo().getFormat().getType();
					// Process only for Face
					if (type != null && type.equals(String.valueOf(FORMAT_TYPE_FACE))) {
						BIR extractBir = new BIR();
						extractBir.setVersion(segment.getVersion());
						extractBir.setCbeffversion(segment.getCbeffversion());
						extractBir.setBirInfo(segment.getBirInfo());
						extractBir.setBdbInfo(segment.getBdbInfo());

						/*
						 * Can do ISO validation here
						 */
						byte[] faceBdb = getBirData(segment);

						/*
						 * do actual resize and compression .. create the face ISO ISO19794_5_2011
						 */
						byte[] data = doFaceConversion("REGISTRATION", resizeAndCompress(faceBdb));
						extractBir.setBdb(data);

						/*
						 * Update the Created Date
						 */
						extractBir.getBdbInfo().setCreationDate(LocalDateTime.now());

						/*
						 * Update the Processed Level Type
						 */
						extractBir.getBdbInfo().setLevel(getProcessedLevelType());

						/*
						 * Update the Purpose Type
						 */
						extractBir.getBdbInfo().setPurpose(getPurposeType());

						/*
						 * Update the Quality to null as we do not have quality tool to set the value
						 */
						extractBir.getBdbInfo().setQuality(null);

						sample.getSegments().set(index, extractBir);
					} else {
						throw new SDKException(ResponseStatus.INVALID_INPUT.ordinal() + "", String
								.format(" FORMAT_TYPE_FACE is wrong ! Excepected Value is 8, Received is %s", type));
					}
				} else {
					throw new SDKException(ResponseStatus.INVALID_INPUT.ordinal() + "",
							"BDBInfo is null or Format Value is null");
				}
			}
		} catch (SDKException ex) {
			logger.error("extractTemplate -- error", ex);
			handleUnknownException(ex, response);
			return response;
		} catch (Exception ex) {
			logger.error("extractTemplate -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
			response.setResponse(null);
			return response;
		}
		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setResponse(sample);

		logger.info("ExtractTemplateInfo :: End Response :: {}", response.toString());
		return response;
	}

	protected void handleUnknownException(SDKException ex, Response<BiometricRecord> response) {
		ResponseStatus status = ResponseStatus.fromStatusCode(Integer.parseInt(ex.getErrorCode()));
		switch (status) {
		case INVALID_INPUT:
			response.setStatusCode(ResponseStatus.INVALID_INPUT.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.INVALID_INPUT.getStatusMessage(), "sample"));
			response.setResponse(null);
			break;
		case MISSING_INPUT:
			response.setStatusCode(ResponseStatus.MISSING_INPUT.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.MISSING_INPUT.getStatusMessage(), "sample"));
			response.setResponse(null);
			break;
		case QUALITY_CHECK_FAILED:
			response.setStatusCode(ResponseStatus.QUALITY_CHECK_FAILED.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.QUALITY_CHECK_FAILED.getStatusMessage(), ""));
			response.setResponse(null);
			break;
		case BIOMETRIC_NOT_FOUND_IN_CBEFF:
			response.setStatusCode(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode());
			response.setStatusMessage(
					String.format(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusMessage(), ""));
			response.setResponse(null);
			break;
		case MATCHING_OF_BIOMETRIC_DATA_FAILED:
			response.setStatusCode(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusCode());
			response.setStatusMessage(
					String.format(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusMessage(), ""));
			response.setResponse(null);
			break;
		case POOR_DATA_QUALITY:
			response.setStatusCode(ResponseStatus.POOR_DATA_QUALITY.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.POOR_DATA_QUALITY.getStatusMessage(), ""));
			response.setResponse(null);
			break;
		default:
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
			response.setResponse(null);
			break;
		}
	}

	/**
	 * Resizes and compresses the provided JPEG2000 image data.
	 *
	 * @param jp2000Bytes The input JPEG2000 image data.
	 * @return Compressed image data as byte array.
	 */
	protected byte[] resizeAndCompress(byte[] jp2000Bytes) {
		// Storing the image in a Matrix object
		// of Mat type
		Mat src = Imgcodecs.imdecode(new MatOfByte(jp2000Bytes), Imgcodecs.IMREAD_UNCHANGED);
		logger.info("Orginal Image Details :: Width {} Height {} Total Size {}", src.width(), src.height(),
				(src.width() * src.height()));
		// New matrix to store the final image
		// where the input image is supposed to be written
		Mat dst = new Mat();

		// standard calculation for image size width = 498 and height = 640 is 0.25f
		// Scaling the Image using Resize function
		float[] fxOrginal = new float[] { 0.25f };
		float[] fyOrginal = new float[] { 0.25f };
		int[] compression = new int[] { 50 };
		setImageCompressorSettings(fxOrginal, fyOrginal, compression);

		logger.info("Factor ratio Details :: orginal fx={}, orginal fy={}, Compression Ratio=={} ", fxOrginal[0],
				fyOrginal[0], compression[0]);

		Imgproc.resize(src, dst, new Size(0, 0), fxOrginal[0], fyOrginal[0], Imgproc.INTER_AREA);
		logger.info("Resized Image Details :: Width {} Height {} Total Size {}", dst.width(), dst.height(),
				(dst.width() * dst.height()));

		MatOfInt map = new MatOfInt(Imgcodecs.IMWRITE_JPEG2000_COMPRESSION_X1000, compression[0]);
		MatOfByte mem = new MatOfByte();
		Imgcodecs.imencode(".jp2", dst, mem, map);
		byte[] data = mem.toArray();

		logger.info("Compressed Image Details :: Image length {}", data.length);

		return data;
	}

	/**
	 * Converts the given image data to Face ISO/IEC 19794-5:2011 format.
	 *
	 * @param purpose   The purpose for the conversion.
	 * @param imageData The image data to convert.
	 * @return Converted image data as byte array.
	 */
	protected byte[] doFaceConversion(String purpose, byte[] imageData) {
		ResponseStatus responseStatus = null;
		try {
			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Face");
			requestDto.setPurpose(purpose);
			requestDto.setVersion("ISO19794_5_2011");

			// Convert JP2000 to Face ISO/IEC 19794-5: 2011
			if (imageData != null) {
				requestDto.setImageType(0);// 0 = jp2, 1 = wsq
				requestDto.setInputBytes(imageData);

				// get image quality = 40 by default
				return FaceEncoder.convertFaceImageToISO(requestDto);
			}
		} catch (Exception ex) {
			logger.error("doFaceConversion::error", ex);
			responseStatus = ResponseStatus.UNKNOWN_ERROR;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
		}
		throw new SDKException(ResponseStatus.UNKNOWN_ERROR + "", "null");
	}

	/**
	 * Retrieves the processed level type for the biometric data.
	 *
	 * @return ProcessedLevelType object representing the processed level.
	 */
	protected ProcessedLevelType getProcessedLevelType() {
		ProcessedLevelType[] types = new ProcessedLevelType[] { ProcessedLevelType.RAW, ProcessedLevelType.INTERMEDIATE,
				ProcessedLevelType.PROCESSED };

		return types[0];
	}

	/**
	 * Retrieves the purpose type for the biometric data.
	 *
	 * @return PurposeType object representing the purpose.
	 */
	protected PurposeType getPurposeType() {
		return PurposeType.VERIFY;
	}

	/**
	 * Sets image compressor settings based on environment variables and
	 * configuration flags.
	 * 
	 * This method prioritizes settings obtained from environment variables over
	 * configuration flags. It attempts to retrieve the following properties from
	 * the environment: - `SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX`: Resize
	 * factor for the X-axis (default: 0.25). -
	 * `SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY`: Resize factor for the Y-axis
	 * (default: 0.25). - `SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO`:
	 * Compression ratio for the image (default: 50). - standard calculation for
	 * image size width = 498 and height = 640 is 0.25f If environment variables are
	 * not available, the method checks the configuration flags (`this.getFlags()`)
	 * for the same keys. It attempts to parse the flag values as floats and integer
	 * for resize factors and compression ratio, respectively.
	 * 
	 * In case of any exceptions during retrieval or parsing, the method logs an
	 * error message but continues execution with the default values.
	 * 
	 * @param fxOrginal        An array to hold the resize factor for the X-axis
	 *                         (modified in-place).
	 * @param fyOrginal        An array to hold the resize factor for the Y-axis
	 *                         (modified in-place).
	 * @param compressionRatio An array to hold the compression ratio (modified
	 *                         in-place).
	 */
	protected void setImageCompressorSettings(float[] fxOrginal, float[] fyOrginal, int[] compressionRatio) {
		fxOrginal[0] = 0.25f;
		fyOrginal[0] = 0.25f;
		compressionRatio[0] = 50;
		if (this.getEnv() != null) {
			try {
				fxOrginal[0] = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, Float.class,
						0.25f);
				fyOrginal[0] = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY, Float.class,
						0.25f);
				compressionRatio[0] = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO,
						Integer.class, 50);
			} catch (Exception ex) {
				logger.error("setImageCompressorSettings::error for env values", ex);
			}
		}
		if (!Objects.isNull(getFlags()) && (getFlags().containsKey(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX)
				&& getFlags().containsKey(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY)
				&& getFlags().containsKey(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO))) {
			try {
				fxOrginal[0] = Float.parseFloat(getFlags().get(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX));
				fyOrginal[0] = Float.parseFloat(getFlags().get(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY));
				compressionRatio[0] = Integer.parseInt(getFlags().get(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO));
			} catch (Exception ex) {
				logger.error("setImageCompressorSettings::error for flag values", ex);
			}
		}
	}
}