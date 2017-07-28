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

## 안드로이드
안드로이드 플랫폼을 지원하실 경우에 네이버 개발자 센터에서 앱을 등록한 후 

로그인 API 환경에 안드로이드를 추가하고 안드로이드 

**앱 페키지 이름을 아이오닉 프로젝트 페키지 이름과 일치**시켜주면 됩니다.

## 아이폰

### 개발제 센터 설정

아이폰 플랫폼을 지원하실 경우에 네이버 개발자 센터에서 앱을 등록한 후 로그인 API 환경에 iOS를 추가합니다

아이폰의 경우에는 안드로이드와 다르게 URL Scheme을 추가 해주셔야 합니다.

URL Scheme 형식은 자동으로 naver-cordova-plugin 에서 만들게 되며 형식은 다음과 같습니다.

```text
naver$OAUTH_CLIENT_ID
``` 

가장 앞에 **naver**가 있고 바로 뒤에는 **OAUTH_CLIENT_ID**가 붙게 됩니다.

예를 들면 OAUTH_CLIENT_ID가 AB_CDEFGHI이라면 Scheme은 **naverAB_CDEFGHI**가 됩니다.

즉 개발자 센터에서 URL Scheme 항목에 **naverAB_CDEFGHI**을 추가해주시면 됩니다.

### XCode 프로젝트 설정
불행하게도 아이폰앱은 AppDelegate에 코드를 추가 해주셔야 합니다.

```objective-c
#import "NaverThirdPartyLoginConnection.h"
```

```objective-c
#define NAVER_APP_SCHEME [[NSBundle mainBundle] objectForInfoDictionaryKey:@"NaverAppScheme"]
```

```objective-c
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
  return [self handleWithUrl:url];
}

#if __IPHONE_OS_VERSION_MAX_ALLOWED > __IPHONE_8_4
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString *,id> *)options {
  return [self handleWithUrl:url];
}
#endif

- (BOOL)handleWithUrl:(NSURL *)url {
  if ([[url scheme] isEqualToString:NAVER_APP_SCHEME]) {
    if ([[url host] isEqualToString:kCheckResultPage]) {
            
      // 네이버앱으로부터 전달받은 url값을 NaverThirdPartyLoginConnection의 인스턴스에 전달
      NaverThirdPartyLoginConnection *thirdConnection = [NaverThirdPartyLoginConnection getSharedInstance];
      THIRDPARTYLOGIN_RECEIVE_TYPE resultType = [thirdConnection receiveAccessToken:url];
            
      if (SUCCESS == resultType) {
        NSLog(@"Getting auth code from NaverApp success!");
      } else {
        // 앱에서 resultType에 따라 실패 처리한다.
      }
    }
    return YES;
  }
  
  return NO;
}
```

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