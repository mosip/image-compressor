# Image Compressor SDK

## Overview
This provide a Image Compressor SDK implementation of [IBioAPIV2](https://github.com/mosip/bio-utils/blob/master/kernel-biometrics-api/src/main/java/io/mosip/kernel/biometrics/spi/IBioApiV2.java) to compress raw jp2000 image using extraction method.

## Technical features
- Extract JP2000 image from ISO ISO19794_5_2011, compress the image for the given ratio using the config file and create new ISO ISO19794_5_2011 response[Removed the SB, ExtraInfo, Quality to keep the response size less].

