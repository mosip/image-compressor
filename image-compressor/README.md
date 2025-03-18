# Image Compressor SDK

This provide a Image Compressor SDK implementation of [IBioAPIV2](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java) to compress raw jp2000 image using extraction method.

---

## Table of Contents
	- [Technical Features](#technicalfeatures)
	- [Prerequisites](#prerequisites)
	- [Setting Up Locally](#setting-up-locally)
	- [Running the Application](#running-the-application)
	- [Configurations](#configurations)
	- [APIs Provided](#apis-provided)
	- [License](#license)

---
## Technical Features

- Extract JP2000 image from ISO ISO19794_5_2011, compress the image for the given ratio using the config file and create new ISO ISO19794_5_2011 response[Removed the SB, ExtraInfo, Quality to keep the response size less].

---

## Prerequisites

Ensure you have the following installed before proceeding:

	- Java: Version 11.0.0
	- Maven: For building the project
	- Git: To clone the repository
	- Postman (optional): For testing the APIs

---

## Setting Up Locally

### Steps to Set Up:

1. **Clone the repository**

```bash
	   git clone https://github.com/mosip/image-compressor/tree/release-0.0.9
	   cd image-compressor
```

2. **Build the project**
Use Maven to build the project and resolve dependencies.

```bash
   mvn clean install -Dgpg.skip=true
```

---

## Running the Application

Used as reference implementation for biosdk-services[https://github.com/mosip/biosdk-services].

## Configurations 

In biosdk-services below values are required for mockSDK implementation.

	biosdk_class=io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2
	mosip.role.biosdk.getservicestatus=REGISTRATION_PROCESSOR
	biosdk_bioapi_impl=io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2

	mosip.bio.image.compressor.resize.factor.fx=0.25
	mosip.bio.image.compressor.resize.factor.fy=0.25
	mosip.bio.image.compressor.compression.ratio=50

---

## APIs Provided

Image Compressor SDK follows implementation based on [Mosip Spec][https://docs.mosip.io/1.1.5/biometrics/biometric-sdk]:

---

## License

This project is licensed under the [MOSIP License](LICENSE).  

---