# AGENTS.md

```
image-compressor/                 # Maven module — all cmds here
├── WHAT  plugin JAR → biosdk-services · not HTTP service
│   ├── GAV  io.mosip.image.compressor:image-compressor  (ver→pom)
│   └── entry  ImageCompressorSDKV2 · biosdk_class / biosdk_bioapi_impl
│
├── PINS  Boot 4.1.1 · Java 21 · no kernel-bom · JaCoCo≥90%
│   └── MOSIP/opencv/jai/CVE pins → pom properties (no hardcode)
│
├── RUN
│   ├── run-local.bat|sh  init|test|all
│   ├── mvn clean install "-Dgpg.skip=true"
│   ├── mvn test "-Dtest=Class[#method]"
│   └── mvn verify "-Dgpg.skip=true"  → target/site/jacoco/
│
├── SRC …/sdk/
│   ├── impl/   ImageCompressorSDKV2★ extractTemplate only; else UNKNOWN_ERROR
│   │           ImageCompressorSDK  (deprecated V1)
│   ├── service/ SDKService · ImageCompressionService · SDKInfoService(FACE+EXTRACT)
│   ├── constant/ exceptions/  (jacoco excl) · utils/ Base64
│
├── FLOW extractTemplate
│   validate FACE=8 → FaceDecoder→JP2 → OpenCV resize/compress → FaceEncoder ISO
│   → BDB; clear quality; RAW; VERIFY
│
├── CFG  Env→flags→defaults  fx|fy=0.25  ratio=50  (~125×160 from 498×640)
│
├── TEST  JUnit5+Mockito · subclass for protected · mock(Environment)→setEnv
│
└── PUB  GPG+Central · .github/keys · NEVER local deploy w/o GPG
```
