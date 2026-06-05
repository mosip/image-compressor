# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Build and Test Commands

All commands run from the `image-compressor/` subdirectory (the Maven module root):

```bash
# Build (skip GPG signing for local development)
mvn clean install -Dgpg.skip=true

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ImageCompressorSDKV2Test

# Run a specific test method
mvn test -Dtest=ImageCompressorSDKV2Test#testInit

# Skip tests during build
mvn clean install -Dgpg.skip=true -DskipTests

# Generate coverage report (target/site/jacoco/)
mvn verify -Dgpg.skip=true
```

The compiler requires `--enable-preview` (Java 21 preview features). The Surefire plugin passes this automatically; no manual flag needed for `mvn test`.

## Architecture

This is a single Maven module (`io.mosip.image.compressor:image-compressor`) that implements MOSIP's biometric SDK interface. It is not a standalone service — it is deployed as a plugin JAR consumed by [biosdk-services](https://github.com/mosip/biosdk-services), which loads the class via `biosdk_class` / `biosdk_bioapi_impl` configuration properties.

### Layer structure

```
impl/          IBioApiV2 entry points (Spring @Component beans)
service/       Business logic; no Spring injection here
constant/      ResponseStatus enum and SdkConstant config keys
utils/         Base64 URL-safe encode/decode helpers
```

### Key classes

- **`ImageCompressorSDKV2`** (`impl/`) — the active SDK entry point, implements `IBioApiV2`. Only `extractTemplate` does real work; all other methods (`checkQuality`, `match`, `segment`, `convertFormatV2`) return `UNKNOWN_ERROR`.
- **`ImageCompressorSDK`** (`impl/`) — deprecated V1, implements `IBioApi`. Scheduled for removal.
- **`SDKService`** (`service/`) — abstract base providing `getBirData`, `getBioSegmentMap`, `getFaceBdb`, and env/flags accessors. All service classes extend this.
- **`ImageCompressionService`** (`service/`) — contains the actual image processing pipeline: decodes ISO 19794-5:2011 face BIR → extracts raw JP2000 bytes → resizes/compresses via OpenCV → re-encodes to ISO 19794-5:2011 via `FaceEncoder.convertFaceImageToISO`.
- **`SDKInfoService`** (`service/`) — builds the `SDKInfo` metadata object (supported modality: `FACE`; supported function: `EXTRACT`).

### Processing pipeline (extractTemplate)

1. Validate input `BiometricRecord` segments (only `FORMAT_TYPE_FACE = 8` is accepted).
2. Call `getBirData(segment)` → `getFaceBdb` → `FaceDecoder.getFaceBDIR` to extract the raw JP2000 image bytes from the ISO container.
3. Call `resizeAndCompress(jp2000Bytes)` using OpenCV (`Imgproc.resize` + `Imgcodecs.imencode(".jp2")`). OpenCV native library is loaded once via `nu.pattern.OpenCV.loadLocally()` in a `static {}` block.
4. Call `doFaceConversion("REGISTRATION", compressedBytes)` to wrap the result in a new ISO 19794-5:2011 container via `FaceEncoder.convertFaceImageToISO`.
5. Store result back into the segment's BDB, clear quality, set `ProcessedLevelType.RAW`, set `PurposeType.VERIFY`.

### Configuration

Settings are read from Spring `Environment` first, then from the `flags` map passed into `extractTemplate`. Defaults are applied if neither source provides a value.

| Property key | Default | Description |
|---|---|---|
| `mosip.bio.image.compressor.resize.factor.fx` | `0.25` | Horizontal resize factor |
| `mosip.bio.image.compressor.resize.factor.fy` | `0.25` | Vertical resize factor |
| `mosip.bio.image.compressor.compression.ratio` | `50` | JPEG2000 compression ratio (1–1000) |

Default values produce ~125×160 output from a standard 498×640 face image.

### biosdk-services integration config

```properties
biosdk_class=io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2
biosdk_bioapi_impl=io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2
mosip.role.biosdk.getservicestatus=REGISTRATION_PROCESSOR
```

## Testing Patterns

Tests use JUnit 5 + Mockito. Because `ImageCompressionService` and `SDKService` have protected methods, tests subclass the service (e.g., `ImageCompressionServiceTest extends ImageCompressionService`) to gain access rather than using reflection. Mock the Spring `Environment` with `mock(Environment.class)` and inject it via `sdk.setEnv(env)`.

## Publishing

Releases are signed with GPG and published to Maven Central via the Sonatype Central Publishing plugin. The GPG keys live in `.github/keys/`. CI workflows (`tag.yml`, `release-changes.yml`) delegate to `mosip/kattu` reusable workflows. Never run `mvn deploy` locally without the correct GPG setup.