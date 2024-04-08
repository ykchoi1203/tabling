## Tabling API     

### 프로젝트 설명
- 매장 예약 서비스

### 개발 환경
- 운영체제 : Windows
- IDE : IntelliJ
- JAVA Version : JDK 17
- SpringBoot Version : 2.5.6
- 데이터 베이스 : MariaDB
- 빌드 툴 : Gradle
- 관리 툴 : GitHub

## Dependencies
- Spring Data JPA
- MariaDB connector
- Spring Security
- jwt
- swagger
- Lombok
- Spring Web


## 기술 스택
- 백엔드
    - SpringBoot, Spring Data JPA
- 데이터베이스
    - MariaDB

## ERD

![Image](https://github.com/ykchoi1203/tabling/assets/30820741/01e95bc4-b97f-4cf4-a92d-c2ebd2e53e00)

## 시나리오
- 매장의 점장은 예약 서비스 앱에 상점을 등록한다.(매장 명, 상점위치, 상점 설명)
- 매장을 등록하기 위해서는 파트너 회원 가입이 되어야 한다.(따로, 승인 조건은 없으며 가입 후 바로 이용 가능)
- 매장 이용자는 앱을 통해서 매장을 검색하고 상세 정보를 확인한다.
- 매장의 상세 정보를 보고, 예약을 진행한다.(예약을 진행하기 위해서는 회원 가입이 필수적으로 이루어 져야 한다.)
- 서비스를 통해서 예약한 이후에, 예약 10분전에 도착하여 키오스크를 통해서 방문 확인을 진행한다.
- 예약 및 사용 이후에 리뷰를 작성할 수 있다.
- 리뷰의 경우, 수정은 리뷰 작성자만, 삭제 권한은 리뷰를 작성한 사람과 매장의 관리자(점장등)만 삭제할 수 있습니다.

## Postman
![Tabling Postman](https://github.com/ykchoi1203/tabling/assets/30820741/135ebf3c-cef1-4453-9cac-3b75d7ac4941)
