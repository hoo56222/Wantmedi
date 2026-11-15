# Wantmedi

## 로컬 병원 조회 실습

Java 21과 Docker Desktop을 준비하고, 프로젝트 폴더에서 아래 명령을 같은 PowerShell 세션에서 실행합니다. 입력 프롬프트가 뜨면 데모 PostgreSQL 비밀번호를 입력합니다. 기존 데모 DB 볼륨을 사용한다면 그 DB에 설정한 비밀번호를 입력합니다.

```powershell
$wantmediDemoPassword = Read-Host '데모 PostgreSQL 비밀번호' -AsSecureString
$env:DEMO_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $wantmediDemoPassword).Password

docker compose up -d --wait db
.\gradlew.bat bootRun --args="--spring.profiles.active=demo"
```

Compose가 PostgreSQL 17과 PostGIS를 로컬 `55432` 포트에 띄웁니다. Spring Boot는 `demo` 프로필로 시작할 때 [초기화 SQL](src/main/resources/db/demo/init.sql)을 실행해 테이블과 샘플 병원 한 건을 만듭니다. 재실행 시 같은 ID의 샘플을 중복으로 넣지 않습니다.

### SQL로 직접 확인

서버를 켜 둔 상태에서 새 PowerShell 창을 열고 프로젝트 폴더에서 실행합니다.

```powershell
docker compose exec db psql -U wantmedi_demo -d wantmedi_demo -c "SELECT id, name, address FROM demo_hospital WHERE id = 1;"
```

### Postman에서 확인

Postman 데스크톱 앱에서 **Import**로 [컬렉션](postman/Wantmedi-Demo.postman_collection.json)을 가져온 뒤 **Get sample hospital** 요청의 **Send**를 누릅니다. `GET http://127.0.0.1:8080/api/hospitals/1`에 `200 OK`와 아래 JSON이 반환되고, **Test Results**에서 검사 두 개가 통과합니다.

```json
{"id": 1, "name": "샘플병원", "address": "서울특별시 중구"}
```

URL의 ID를 `999`로 바꾸고 보내면 `404 Not Found`가 반환됩니다.

실습이 끝나면 서버 창에서 `Ctrl+C`를 누르고 `docker compose down`으로 DB를 종료합니다. Compose 볼륨에 데이터가 보관됩니다.

## 실행 환경의 비밀값

공용 [application.properties](src/main/resources/application.properties)에는 환경변수 이름과 로컬 개발용 기본 주소를 둡니다. 일반 프로필에서는 `DB_PASSWORD`를 지정하고, DB 주소나 계정이 다르면 `DB_URL`과 `DB_USERNAME`도 지정합니다. `demo` 프로필은 Compose 실습용 접속 설정을 사용합니다.

로컬에서는 STS의 **Run Configurations → Environment**에 실제 값을 등록합니다. 배포할 때는 Spring Boot 서버 프로세스나 컨테이너의 실행 환경에 같은 이름으로 주입합니다. 개인 `.env` 파일을 사용하는 경우 실행 도구에서 해당 값을 환경변수로 전달해야 합니다.

OAuth나 메일 서비스를 추가할 때도 `${GOOGLE_OAUTH_CLIENT_SECRET}`, `${KAKAO_OAUTH_CLIENT_SECRET}`, `${MAIL_PASSWORD}`처럼 환경변수를 참조하고, Java 서비스는 그 설정을 읽도록 작성합니다. OAuth 시크릿과 서버용 API 키는 백엔드에서 사용합니다. `layout3.html` 같은 템플릿에 브라우저용 SDK 키가 필요한 경우에는 도메인과 API를 제한한 별도 키를 사용합니다.
