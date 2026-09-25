# image-compressor

[![Maven Package upon a push](https://github.com/mosip/image-compressor/actions/workflows/push-trigger.yml/badge.svg)](https://github.com/mosip/image-compressor/actions/workflows/push-trigger.yml)

JPEG2000 face-image compressor implementing MOSIP **IBioApiV2** (`extractTemplate` only).
Ships as a **plugin JAR** for [biosdk-services](https://github.com/mosip/biosdk-services) — **not** a standalone HTTP service.

| | |
|---|---|
| **Group / Artifact** | `io.mosip.image.compressor:image-compressor` |
| **Version** | see `image-compressor/pom.xml` (`<version>`) |
| **JDK** | 21 (`--enable-preview`) |
| **Spring Boot** | 4.1.1 (**no** `kernel-bom`) |
| **Entry class** | `io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2` |
| **Supported** | Modality `FACE` · Function `EXTRACT` |
| **Coverage gate** | JaCoCo **≥ 90%** (instruction + line) |
| **License** | [MPL 2.0](LICENSE) — see [NOTICE](NOTICE) |

---

## Table of contents

1. [Artifacts](#artifacts)
2. [Prerequisites](#prerequisites)
3. [Local setup](#local-setup)
4. [Maven commands](#maven-commands)
5. [What it does](#what-it-does)
6. [Architecture](#architecture)
7. [IBioApiV2 methods](#ibioapiv2-methods)
8. [Configuration](#configuration)
9. [biosdk-services integration](#biosdk-services-integration)
10. [Dependencies](#dependencies)
11. [Security overrides](#security-overrides)
12. [Testing](#testing)
13. [Project layout](#project-layout)
14. [Publishing](#publishing)
15. [License](#license)

---

## Artifacts

Built under `image-compressor/target/`:

| Artifact | Role |
|----------|------|
| `image-compressor-${version}.jar` | Thin plugin JAR |
| `image-compressor-${version}-jar-with-dependencies.jar` | Fat JAR for biosdk-services `loader_path` |
| `…-sources.jar` / `…-javadoc.jar` | Maven Central attachments |

Maven coordinates (set `<version>` from `pom.xml`):

```xml
<dependency>
  <groupId>io.mosip.image.compressor</groupId>
  <artifactId>image-compressor</artifactId>
  <version><!-- see pom.xml --></version>
</dependency>
```

---

## Prerequisites

- **JDK 21** on `PATH`
- **Maven 3.9+** on `PATH`
- MOSIP artifacts available locally **or** via the Central snapshot repository
  (versions = properties in `image-compressor/pom.xml`):

| GAV | Property | Scope in this module |
|-----|----------|----------------------|
| `io.mosip.kernel:kernel-core` | `${kernel.core.version}` | compile (exceptions base) |
| `io.mosip.kernel:kernel-biometrics-api` | `${kernel.biometrics.api.version}` | **provided** (host SPI) |
| `io.mosip.biometric.util:biometrics-util` | `${mosip.biometrics.util.version}` | compile (Face encode/decode) |

Snapshot repo (already in `pom.xml`):

```
https://central.sonatype.com/repository/maven-snapshots/
```

---

## Local setup

Helpers live in the **Maven module** directory (`image-compressor/`), same style as other MOSIP modules:

- Windows cmd: `run-local.bat`
- Linux / macOS / WSL / Git Bash: `run-local.sh`

Working directory **must** be `image-compressor/`.  
This is a **plugin JAR** — there is **no** `start` / `smoke` / `docker` (nothing to run as a server).

### Helper commands

| Command | What it does |
|---------|----------------|
| `init` | `mvn clean package` (skip tests) |
| `test` | `mvn test` |
| `all` | `init` + `test` |
| _(no args)_ / `help` | Usage |

### Windows (cmd)

```bat
cd image-compressor
run-local.bat init
run-local.bat test
run-local.bat all
```

### Linux / macOS / Git Bash

```bash
cd image-compressor
chmod +x run-local.sh   # once
./run-local.sh init
./run-local.sh test
./run-local.sh all
```

After `init`, look for:

```
target/image-compressor-*-SNAPSHOT.jar
target/image-compressor-*-jar-with-dependencies.jar
```

---

## Maven commands

Run from `image-compressor/` (module root):

```bash
# Full build + install (skip GPG locally)
mvn clean install "-Dgpg.skip=true"

# Package only (skip tests)
mvn clean package "-DskipTests=true" "-Dgpg.skip=true"

# All tests
mvn test "-Dgpg.skip=true"

# One test class / method
mvn test "-Dtest=ImageCompressorSDKV2Test" "-Dgpg.skip=true"
mvn test "-Dtest=ImageCompressorSDKV2Test#testInit" "-Dgpg.skip=true"

# Coverage report + gate (≥ 90%)
mvn verify "-Dgpg.skip=true"
# → target/site/jacoco/index.html
```

Surefire always passes `--enable-preview` and JaCoCo `@{argLine}` (no manual flags needed).

---

## What it does

```
ISO/IEC 19794-5:2011 face BIR
              │
              ▼
   FaceDecoder → raw JPEG2000 bytes
              │
              ▼
   OpenCV resize (fx / fy) + JP2 compress (ratio)
              │
              ▼
   FaceEncoder → new ISO/IEC 19794-5:2011 BIR
     · quality cleared
     · ProcessedLevelType.RAW
     · PurposeType.VERIFY
```

Only face segments with format type **`8`** (`FORMAT_TYPE_FACE`) are accepted.

Default factors (`0.25` / `0.25` / `50`) yield ~**125×160** from a standard **498×640** face image.

---

## Architecture

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Entry | `…sdk.impl` | Spring `@Component` implementing `IBioApiV2` / deprecated `IBioApi` |
| Service | `…sdk.service` | Compression pipeline + SDK info (no Spring injection inside services) |
| Constant | `…sdk.constant` | `SdkConstant` keys, `ResponseStatus` codes |
| Exception | `…sdk.exceptions` | `SDKException` → `BaseUncheckedException` |
| Utils | `…sdk.utils` | URL-safe Base64 helpers |

### Key classes

| Class | Notes |
|-------|-------|
| `ImageCompressorSDKV2` | **Active** entry — only `extractTemplate` does real work |
| `ImageCompressorSDK` | **Deprecated** V1 (`IBioApi`) — scheduled for removal |
| `ImageCompressionService` | Decode → resize/compress → re-encode |
| `SDKService` | Shared BIR / face BDB helpers, env + flags |
| `SDKInfoService` | Advertises `FACE` + `EXTRACT` |

### extractTemplate pipeline

1. Validate segments (face format type `8` only).
2. `getBirData` → `getFaceBdb` → `FaceDecoder.getFaceBDIR` → JP2000 bytes.
3. `resizeAndCompress` — OpenCV `Imgproc.resize` + `Imgcodecs.imencode(".jp2")`  
   (native lib loaded once via `nu.pattern.OpenCV.loadLocally()` in a `static {}` block).
4. `doFaceConversion("REGISTRATION", …)` via `FaceEncoder.convertFaceImageToISO`.
5. Write BDB; clear quality; set `RAW` + `VERIFY`.

---

## IBioApiV2 methods

| Method | Behaviour |
|--------|-----------|
| `init` | Returns `SDKInfo` (FACE / EXTRACT) |
| `extractTemplate` | Compresses face ISO → smaller face ISO |
| `checkQuality` | `UNKNOWN_ERROR` (500) |
| `match` | `UNKNOWN_ERROR` (500) |
| `segment` | `UNKNOWN_ERROR` (500) |
| `convertFormatV2` | `UNKNOWN_ERROR` (500) |
| `convertFormat` | Deprecated; returns `null` |

---

## Configuration

Resolution order: Spring **`Environment`** → `flags` map on `extractTemplate` → **defaults**.

| Property key | Default | Description |
|--------------|---------|-------------|
| `mosip.bio.image.compressor.resize.factor.fx` | `0.25` | Horizontal resize factor |
| `mosip.bio.image.compressor.resize.factor.fy` | `0.25` | Vertical resize factor |
| `mosip.bio.image.compressor.compression.ratio` | `50` | JPEG2000 compression ratio (1–1000) |

Constants live in `SdkConstant`.

---

## biosdk-services integration

```properties
biosdk_class=io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2
biosdk_bioapi_impl=io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2
mosip.role.biosdk.getservicestatus=REGISTRATION_PROCESSOR

# optional (defaults shown)
mosip.bio.image.compressor.resize.factor.fx=0.25
mosip.bio.image.compressor.resize.factor.fy=0.25
mosip.bio.image.compressor.compression.ratio=50
```

Place the **fat JAR** (or thin JAR + transitive deps) on biosdk-services `loader_path`.  
Host supplies `kernel-biometrics-api` (this module marks it `provided`).

---

## Dependencies

Parent: `spring-boot-starter-parent` **4.1.1**. No `kernel-bom`.  
MOSIP / codec versions: see properties in `image-compressor/pom.xml` (they change with releases).

| Coord | Version source | Notes |
|-------|----------------|-------|
| Spring Boot parent | **4.1.1** | Version BOM |
| `kernel-core` | `${kernel.core.version}` | Unused web/security/JPA/quartz/c3p0 excluded |
| `kernel-biometrics-api` | `${kernel.biometrics.api.version}` | `provided` |
| `biometrics-util` | `${mosip.biometrics.util.version}` | FaceDecoder / FaceEncoder |
| `org.openpnp:opencv` | `${openpnp.opencv.version}` | Resize + JP2 encode |
| `jai-imageio-jpeg2000` | `${jai.imageio.jpeg2000.version}` | JPEG2000 codec |
| `commons-lang3` | `${commons-lang3.version}` | Boot property override (CVE pin) |
| `commons-codec` | Boot-managed | Base64 / digest helpers |
| `slf4j-api` | Boot-managed | Logging API |

---

## Security overrides

Pinned in `pom.xml` to satisfy known CVEs (also documented in [NOTICE](NOTICE)):

| Artifact | Pin | CVE |
|----------|-----|-----|
| `protobuf-java` | **4.28.2** | CVE-2024-7254 |
| `commons-lang3` | **3.18.0** | CVE-2025-48924 |
| `json-smart` | **2.5.2** | CVE-2024-57699 |
| `spring-security.*` | **≥ 7.0.6** | CVE-2024-38821, CVE-2026-22732, CVE-2024-38827 |
| `spring-boot-actuator-autoconfigure` | Boot **4.1.1** | CVE-2025-22235 |
| `c3p0` | **0.14.0** (excluded; pin if reintroduced) | CVE-2026-27830 |

After changing the pom, **Reload All Maven Projects** in the IDE so scanners (e.g. Mend) drop stale **1.3.1** models.

---

## Testing

- **JUnit 5** + Mockito (+ JUnit Vintage if needed)
- Protected service methods: tests **subclass** the service (no reflection)
- Inject config: `mock(Environment.class)` → `sdk.setEnv(env)`
- Sample face ISO fixtures under `src/test/resources/sample_files/`

| Check | Detail |
|-------|--------|
| Unit tests | `mvn test` or `run-local.* test` |
| Coverage HTML | `target/site/jacoco/index.html` |
| Gate | Instruction + line **≥ 90%** (excludes `constant/`, `exceptions/`, deprecated V1) |

---

## Project layout

```
image-compressor/                          # Git repo root
├── AGENTS.md                              # Low-token agent tree
├── README.md                              # This file
├── LICENSE / NOTICE / THIRD-PARTY-NOTICES*
├── licenses/                              # Full license texts + elections
└── image-compressor/                      # Maven module root
    ├── pom.xml
    ├── run-local.bat | run-local.sh       # init | test | all
    ├── README.md                          # Short module pointer
    └── src/
        ├── main/java/.../sdk/
        │   ├── impl/          ImageCompressorSDKV2 (active), ImageCompressorSDK (deprecated)
        │   ├── service/       ImageCompressionService, SDKInfoService, SDKService
        │   ├── constant/      SdkConstant, ResponseStatus
        │   ├── exceptions/    SDKException
        │   └── utils/         Util (Base64 URL-safe)
        └── test/java/.../sdk/test/
```

---

## Publishing

| Item | Detail |
|------|--------|
| Signing | GPG (`maven-gpg-plugin`); keys under `.github/keys/` |
| Repo | Sonatype Central Publishing (`central-publishing-maven-plugin`) |
| CI | `tag.yml`, `release-changes.yml` → `mosip/kattu` reusable workflows |
| Local | Do **not** run `mvn deploy` without correct GPG setup — use `"-Dgpg.skip=true"` for local builds |

---

## License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).

- Product + MOSIP dependency notices: [NOTICE](NOTICE)
- Dual-license elections: [licenses/NOTICE](licenses/NOTICE)
- Full third-party license texts: [licenses/](licenses/)
