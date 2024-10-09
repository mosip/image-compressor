package io.mosip.image.compressor.sdk.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.mosip.image.compressor.sdk.exceptions.SDKException;

public class SDKExceptionTest {

	@Test
	void testSDKExceptionWithMessage() {
		String errorCode = "ERR001";
		String errorMessage = "This is an error message.";

		SDKException exception = new SDKException(errorCode, errorMessage);

		assertNotNull(exception);
		assertEquals(errorMessage, exception.getMessage());
	}

	@Test
	void testSDKExceptionWithCause() {
		String errorCode = "ERR002";
		String errorMessage = "This is another error message.";
		Throwable cause = new RuntimeException("Root cause");

		SDKException exception = new SDKException(errorCode, errorMessage, cause);

		assertNotNull(exception);
		assertThat(exception.getMessage(), containsString(errorMessage));
		assertEquals(cause, exception.getCause());
	}
}