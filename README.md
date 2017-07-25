# cordova-plugin-naver
> 네이버 SDK를 Cordova에서 사용할 수 있도록 만든 플러그인 입니다

## 설치 하기전에 앞서서
해당 플러그인을 사용하기 앞서 [네이버 개발자 센터](https://developers.naver.com/)에서 앱을 등록하여

OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME 값을 알아놓아야 합니다

최신 버전은 다음 페이지에서 확인 할 수 있습니다 - https://www.npmjs.com/package/cordova-plugin-naver


## 설치 방법
```bash
$ cordova plugin add cordova-plugin-naver --save --variable OAUTH_CLIENT_ID="OAUTH_CLIENT_ID" --variable OAUTH_CLIENT_SECRET="OAUTH_CLIENT_SECRET" --variable OAUTH_CLIENT_NAME="OAUTH_CLIENT_NAME"
```

만약  OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME 등의 값이 해당 플러그인 추가 후 변경된다면 플러그인 삭제후 다시 추가하는 것을 추천 드립니다.

## API

### 로그인
`Naver.login(Function success, Function failure)`

로그인을 성공하면 다음과 같은 결과가 반환됩니다 :
 
    {
        accessToken: "<long string>",
        refreshToken: "<long string>",
        expiresAt: 1500980193,
        tokenType: "bearer"
    }
    
### 내 정보 요청
**네이버 아이디로 로그인을 한 상태에서 요청할 수 있습니다**

`Naver.requestMe(Function success, Function failure)`

내 정보 요청에 성공할 경우 다음과 같은 결과가 반환됩니다 :

    {
        resultCode: string,
        message: string,
        response: {
            email: string,
            nickname: string,
            profileImage: string,
            age: string,
            gender: string,
            id: string,
            name: string,
            birthday: string
        }
    }
    
 
### 로그인 상태
`Naver.getState(Function success, Function failure)`

로그인 상태요청에 성공하게 되면 다음의 상태 중 하나가 넘어오게 됩니다 (NEED_INIT, NEED_LOGIN, OK)

자세한 내용은 [네이버 개발자 센터 안드로이드 문서](https://developers.naver.com/docs/login/android/)에서 확인해 주세요
 - 12.5. OAuthLoginState 항목을 확인해 주세요 

### API 요청
`Naver.requestApi(String url, Function success, Function failure)`

url의 경우에는 네이버에서 지원하는 [API 명세](https://developers.naver.com/docs/login/profile/)를 확인해서 사용할 수 있습니다.

GET 메서드로 url에 해당하는 API를 호출하며 성공 했을 경우에 결과(content body)를 문자열로 반환합니다.

### 엑세스 토큰 업데이트
`Naver.refreshAccessToken(Function success, Function failure)`

클라이언트에 저장된 갱신 토큰(refresh token)을 이용해 접근 토큰(access token)을 갱신하고 갱신된 접근 토큰을 반환합니다.
 
### 로그아웃
`Naver.logout(Function success, Function failure)`

토큰을 지워 로그아웃 처리 합니다.

로그아웃에 성공할 경우 "OK"가 반환됩니다

### 연동해제
`Naver.logoutAndDeleteToken(Function success, Function failure)`

토큰을 지우고, 계정 연동을 해제합니다.

연동 해제에 성공할 경우 "OK"가 반환됩니다

### API 에러 처리
모든 API 의 기본적인 에러 반환 형태는 다음과 같습니다 :

    {
        "code": "error code string"
        "description": "error description"
    }
    
위의 데이터처럼 넘어오지 않을 경우에는 에러 문자열로 넘어오게 됩니다