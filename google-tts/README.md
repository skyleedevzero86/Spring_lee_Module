# Google Cloud TTS

JDK 25, Spring Boot 4.1.2, Gradle 9.8.2, Vue 3, TypeScript, pnpm 구성으로 만든 프로젝트입니다.<br/>

한국어(`ko-KR`), 일본어(`ja-JP`), 중국어(`cmn-CN`), 영어(`en-US`)를 만들고, 말하기 속도와 음높이를 조절할 수 있습니다. <br/>
화면 문구도 같은 네 언어로 바뀝니다.<br/>>

Google Cloud Text-to-Speech는 유료 API입니다. <br/>
Neural2가 Standard보다 요금이 높습니다.<br/>

## 자격 증명

1. [Cloud Text-to-Speech](https://cloud.google.com/text-to-speech?hl=ko) API를 사용 설정합니다.
2. 서비스 계정 키 JSON을 받습니다.
3. 아래 중 한 곳에 둡니다.

- `backend/src/main/resources/service-account-file.json`
- `backend/src/main/resources/application.yml`의 `tts.google.credentials-file`에 파일 경로 지정

JSON 키는 git에 포함하지 않습니다.

<br/>
생성된 mp3는 로컬 MinIO(`http://127.0.0.1:9000`, 버킷 `tts-audio`)에 저장됩니다. <br/>
접속 정보는 `application.yml`의 `tts.minio`입니다.
