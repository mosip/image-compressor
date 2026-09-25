# Image Compressor SDK

Module README — full documentation is in the **[root README](../README.md)**.

| | |
|---|---|
| **Artifact** | `io.mosip.image.compressor:image-compressor` (version in `pom.xml`) |
| **Entry** | `io.mosip.image.compressor.sdk.impl.ImageCompressorSDKV2` |
| **API** | `IBioApiV2` — only `extractTemplate` is implemented |
| **Work** | ISO 19794-5 face → JP2000 resize/compress → ISO |
| **Boot / JDK** | Spring Boot **4.1.1** · Java **21** · no `kernel-bom` |
| **Helpers** | `run-local.bat` / `run-local.sh` → `init` \| `test` \| `all` |

```bat
run-local.bat init
run-local.bat test
run-local.bat all
```

```bash
./run-local.sh init
./run-local.sh test
./run-local.sh all
```

```bash
mvn clean install "-Dgpg.skip=true"
mvn verify "-Dgpg.skip=true"   # JaCoCo ≥ 90% → target/site/jacoco/
```
