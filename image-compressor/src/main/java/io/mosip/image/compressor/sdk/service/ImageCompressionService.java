package io.mosip.image.compressor.sdk.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

	public ImageCompressionService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToExtract = modalitiesToExtract;
	}

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
			switch (ResponseStatus.fromStatusCode(Integer.parseInt(ex.getErrorCode()))) {
			case INVALID_INPUT:
				response.setStatusCode(ResponseStatus.INVALID_INPUT.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.INVALID_INPUT.getStatusMessage(), "sample"));
				response.setResponse(null);
				return response;
			case MISSING_INPUT:
				response.setStatusCode(ResponseStatus.MISSING_INPUT.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.MISSING_INPUT.getStatusMessage(), "sample"));
				response.setResponse(null);
				return response;
			case QUALITY_CHECK_FAILED:
				response.setStatusCode(ResponseStatus.QUALITY_CHECK_FAILED.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.QUALITY_CHECK_FAILED.getStatusMessage(), ""));
				response.setResponse(null);
				return response;
			case BIOMETRIC_NOT_FOUND_IN_CBEFF:
				response.setStatusCode(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode());
				response.setStatusMessage(
						String.format(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusMessage(), ""));
				response.setResponse(null);
				return response;
			case MATCHING_OF_BIOMETRIC_DATA_FAILED:
				response.setStatusCode(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusCode());
				response.setStatusMessage(
						String.format(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusMessage(), ""));
				response.setResponse(null);
				return response;
			case POOR_DATA_QUALITY:
				response.setStatusCode(ResponseStatus.POOR_DATA_QUALITY.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.POOR_DATA_QUALITY.getStatusMessage(), ""));
				response.setResponse(null);
				return response;
			default:
				response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
				response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
				response.setResponse(null);
				return response;
			}
		} catch (Exception ex) {
			logger.error("extractTemplate -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
			response.setResponse(null);
			return response;
		}
		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setResponse(sample);

		logger.info("ExtractTemplateInfo :: End Response :: {}", response != null ? response.toString() : null);
		return response;
	}

	public byte[] resizeAndCompress(byte[] jp2000Bytes) {
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
		float fxOrginal = 0.25f;
		float fyOrginal = 0.25f;
		int compression = 50;
		if (this.getEnv() != null) {
			fxOrginal = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, Float.class, 0.25f);
			fyOrginal = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY, Float.class, 0.25f);
			compression = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO, Integer.class, 50);
		}

		logger.info("Factor ratio Details :: orginal fx={}, orginal fy={}, Compression Ratio=={} ", fxOrginal,
				fyOrginal, compression);

		Imgproc.resize(src, dst, new Size(0, 0), fxOrginal, fyOrginal, Imgproc.INTER_AREA);
		logger.info("Resized Image Details :: Width {} Height {} Total Size {}", dst.width(), dst.height(),
				(dst.width() * dst.height()));

		MatOfInt map = new MatOfInt(Imgcodecs.IMWRITE_JPEG2000_COMPRESSION_X1000, compression);
		MatOfByte mem = new MatOfByte();
		Imgcodecs.imencode(".jp2", dst, mem, map);
		byte[] data = mem.toArray();

		logger.info("Compressed Image Details :: Image length {}", data.length);

		return data;
	}

	public byte[] doFaceConversion(String purpose, byte[] imageData) {
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

	public ProcessedLevelType getProcessedLevelType() {
		ProcessedLevelType[] types = new ProcessedLevelType[] { ProcessedLevelType.RAW, ProcessedLevelType.INTERMEDIATE,
				ProcessedLevelType.PROCESSED };

		return types[0];
	}

	public PurposeType getPurposeType() {
		return PurposeType.VERIFY;
	}
}