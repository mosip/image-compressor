package io.mosip.image.compressor.sdk.service;

import java.io.IOException;
import java.util.Base64;
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

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.BIRInfo.BIRInfoBuilder;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceEncoder;
import io.mosip.image.compressor.sdk.constant.ResponseStatus;
import io.mosip.image.compressor.sdk.constant.SdkConstant;
import io.mosip.image.compressor.sdk.exceptions.SDKException;

public class ImageCompressionService extends SDKService {
	private Logger LOGGER = LoggerFactory.getLogger(ImageCompressionService.class);
	
	static {
		// load OpenCV library
		nu.pattern.OpenCV.loadShared();
		/**
         * In Java >= 12 it is no longer possible to use addLibraryPath, which modifies the
         * ClassLoader's static usr_paths field. There does not seem to be any way around this
         * so we fall back to loadLocally() and return.
         */
		//nu.pattern.OpenCV.loadLocally();
		//System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
	}
	
	private BiometricRecord sample;
	private List<BiometricType> modalitiesToExtract;

	private ProcessedLevelType[] types = new ProcessedLevelType[] { ProcessedLevelType.INTERMEDIATE,
			ProcessedLevelType.PROCESSED };

	public static final long FORMAT_TYPE_FACE = 8;

	public ImageCompressionService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToExtract = modalitiesToExtract;
	}

	public Response<BiometricRecord> getExtractTemplateInfo() {
		ResponseStatus responseStatus = null;
		Response<BiometricRecord> response = new Response<>();
		try {
			if (sample == null || sample.getSegments() == null || sample.getSegments().isEmpty()) {
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}

			for (BIR segment : sample.getSegments()) {
				// not validating ISO
				byte[] faceBdb = getBirData(segment);

				if (segment.getBirInfo() == null)
					segment.setBirInfo(new BIRInfo(new BIRInfoBuilder().withPayload(segment.getBdb())));
				else 
					segment.getBirInfo().setPayload(segment.getBdb());
				
				BDBInfo bdbInfo = segment.getBdbInfo();
				if (bdbInfo != null) {
					// Update the level to processed
					bdbInfo.setLevel(getProcessedLevelType());
					if (segment.getBdbInfo().getFormat() != null) {
						String type = segment.getBdbInfo().getFormat().getType();
						// Update the fingerprint image to fingerprint minutiae type
						if (type != null && type.equals(String.valueOf(FORMAT_TYPE_FACE))) {
							// do actual resize and compression .. create the face ISO ISO19794_5_2011
							segment.setBdb(doFaceConversion("REGISTRATION", resizeAndCompress(faceBdb)));
						}
					}
				}
			}
		} catch (SDKException ex) {
			LOGGER.error("extractTemplate -- error", ex);
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
			LOGGER.error("extractTemplate -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
			response.setResponse(null);
			return response;
		}
		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setResponse(sample);
		return response;
	}

	public byte[] resizeAndCompress(byte[] jp2000Bytes) throws IOException {
		// Storing the image in a Matrix object
		// of Mat type
		Mat src = Imgcodecs.imdecode(new MatOfByte(jp2000Bytes), Imgcodecs.IMREAD_UNCHANGED);
		LOGGER.info("Orginal Image Details");
		LOGGER.info(String.format("Width=%d, Height=%d, Total Size=%d ", src.width(), src.height(), (src.width() * src.height())));
		// New matrix to store the final image
		// where the input image is supposed to be written
		Mat dst = new Mat();

		// standard calculation for image size width = 498 and height = 640 is 0.25f
		// Scaling the Image using Resize function
		float fxOrginal = 0.25f;
		float fyOrginal = 0.25f;
		int compression = 50;
		if (this.getEnv() != null)
		{
			fxOrginal = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FX, Float.class, 0.25f);
			fyOrginal = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_RESIZE_FACTOR_FY, Float.class, 0.25f);
			compression = this.getEnv().getProperty(SdkConstant.IMAGE_COMPRESSOR_COMPRESSION_RATIO, Integer.class, 50);
		}

		LOGGER.info("Factor ratio Details" + " - " + fxOrginal + " - " + fyOrginal + " - " + compression);
		LOGGER.info(String.format("orginal fx=%.2f, orginal fy=%.2f, Compression Ratio==%d ", fxOrginal, fyOrginal, compression));
		
		Imgproc.resize(src, dst, new Size(0, 0), fxOrginal, fyOrginal, Imgproc.INTER_AREA);
		LOGGER.info("Resized Image Details");
		LOGGER.info(String.format("Width=%d, Height=%d, Total Size=%d ", dst.width(), dst.height(), (dst.width() * dst.height())));

		MatOfInt map = new MatOfInt(Imgcodecs.IMWRITE_JPEG2000_COMPRESSION_X1000, compression);
		MatOfByte mem = new MatOfByte();
		Imgcodecs.imencode(".jp2", dst, mem, map);
		byte[] data = mem.toArray();
		
		LOGGER.info("Compressed Image Details");
		LOGGER.info(String.format("Image length==%d ", data.length));

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
				requestDto.setImageType(0);//0 = jp2, 1 = wsq
				requestDto.setInputBytes(imageData);

				return FaceEncoder.convertFaceImageToISO(requestDto);// get image quality = 40 by default
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			responseStatus = ResponseStatus.UNKNOWN_ERROR;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
		}
		return null;
	}

	public ProcessedLevelType getProcessedLevelType() {
		return ProcessedLevelType.RAW;
	}
}