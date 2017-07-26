#import "NaverPlugin.h"

@interface NaverPlugin ()

@property(strong, nonatomic) NSString *loginCallbackId;
@end

@implementation NaverPlugin

- (void)pluginInitialize {
    NSLog(@"Start Naver plugin");

    // Delegate 설정
    [NaverThirdPartyLoginConnection getSharedInstance].delegate = self;

    // 네이버 앱과, 인앱 브라우저 인증을 둘다 사용하도록 설정
    // [[NaverThirdPartyLoginConnection getSharedInstance] setIsNaverAppOauthEnable:YES];
    [[NaverThirdPartyLoginConnection getSharedInstance] setIsInAppOauthEnable:YES];

    // 세로 화면 고정 설정
    [[NaverThirdPartyLoginConnection getSharedInstance] setOnlyPortraitSupportInIphone:YES];

    // 네이버 플러그인 데이터 설정
    [[NaverThirdPartyLoginConnection getSharedInstance] setServiceUrlScheme:@"kServiceAppUrlScheme"];
    [[NaverThirdPartyLoginConnection getSharedInstance] setConsumerKey:@"U3_LDixvaYKB83GacpFb"];
    [[NaverThirdPartyLoginConnection getSharedInstance] setConsumerSecret:@"kxPBNNUVu_"];
    [[NaverThirdPartyLoginConnection getSharedInstance] setAppName:@"cordova plugin naver test"];

}

#pragma mark - Cordova commands

/**
 * 네이버 로그인을 요청합니다
 *
 * @param command
 */
- (void)login:(CDVInvokedUrlCommand *)command {

    // 로그인 콜백 아이디 설정
    self.loginCallbackId = command.callbackId;

    // 로그인 요청
    NaverThirdPartyLoginConnection *login = [NaverThirdPartyLoginConnection getSharedInstance];
    [login requestThirdPartyLogin];
}

/**
 * 토큰을 지워 로그아웃 처리 합니다.
 *
 * @param command
 */
- (void)logout:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
    [[NaverThirdPartyLoginConnection getSharedInstance] resetToken];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 * 토큰을 지우고, 계정 연동을 해제합니다.
 *
 * @param command
 */
- (void)logoutAndDeleteToken:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        // 콜백 아이디 설정
        self.loginCallbackId = command.callbackId;

        // 로그아웃 요청
        NaverThirdPartyLoginConnection *loginConnection = [NaverThirdPartyLoginConnection getSharedInstance];
        [loginConnection requestDeleteToken];
    }];
}

/**
 * 클라이언트에 저장된 갱신 토큰(refresh token)을 이용해 접근 토큰(access token)을 갱신하고 갱신된 접근 토큰을 반환합니다.
 *
 * @param command
 */
- (void)refreshAccessToken:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        // 콜백 아이디 설정
        self.loginCallbackId = command.callbackId;

        // AccessToken 재 발급 요청
        NaverThirdPartyLoginConnection *loginConnection = [NaverThirdPartyLoginConnection getSharedInstance];
        [loginConnection requestAccessTokenWithRefreshToken];
    }];
}

/**
 * 네이버 아이디로 로그인 인스턴스의 현재 상태를 반환합니다.
 *
 * @param command
 */
- (void)getState:(CDVInvokedUrlCommand *)command {
    // TODO 구현 필요, 안드로이드 라이브러리에는 있으나 아이폰 라이브러리에는 존재하지 않는 메소드
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"구현되지 않았습니다"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)requestApi:(CDVInvokedUrlCommand *)command {
    // TODO 구현 필요, 안드로이드 라이브러리에는 있으나 아이폰 라이브러리에는 존재하지 않는 메소드
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"구현되지 않았습니다"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)requestMe:(CDVInvokedUrlCommand *)command {
    NSString *accessToken = [[NaverThirdPartyLoginConnection getSharedInstance] accessToken];

    NSString *urlString = @"https://openapi.naver.com/v1/nid/me"; // 사용자 프로필 호출 API URL
    NSMutableURLRequest *urlRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:urlString]];
    NSString *authValue = [NSString stringWithFormat:@"Bearer %@", accessToken];
    NSString *contentType = @"text/json;charset=utf-8";
    [urlRequest setValue:authValue forHTTPHeaderField:@"Authorization"];
    [urlRequest setValue:contentType forHTTPHeaderField:@"Content-Type"];

    [[[NSURLSession sharedSession] dataTaskWithRequest:urlRequest completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        NSError *serializationError;
        NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data
                                                             options:nil
                                                               error:&serializationError];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:json];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }] resume];
}


- (void)oauth20ConnectionDidOpenInAppBrowserForOAuth:(NSURLRequest *)request {
    NSLog(@"oauth20ConnectionDidOpenInAppBrowserForOAuth");
    NSString *accessToken = [[NaverThirdPartyLoginConnection getSharedInstance] accessToken];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:accessToken];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.loginCallbackId];

    self.loginCallbackId = nil;
}

- (void)oauth20ConnectionDidFinishRequestACTokenWithAuthCode {
    NSLog(@"oauth20ConnectionDidFinishRequestACTokenWithAuthCode");
    NSString *accessToken = [[NaverThirdPartyLoginConnection getSharedInstance] accessToken];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:accessToken];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.loginCallbackId];

    self.loginCallbackId = nil;
}

- (void)oauth20ConnectionDidFinishRequestACTokenWithRefreshToken {
    NSLog(@"oauth20ConnectionDidFinishRequestACTokenWithRefreshToken");
    NSString *accessToken = [[NaverThirdPartyLoginConnection getSharedInstance] accessToken];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:accessToken];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.loginCallbackId];

    self.loginCallbackId = nil;
}

- (void)oauth20ConnectionDidFinishDeleteToken {
    NSLog(@"oauth20ConnectionDidFinishDeleteToken");
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.loginCallbackId];

    self.loginCallbackId = nil;
}

- (void)oauth20Connection:(NaverThirdPartyLoginConnection *)oauthConnection didFailWithError:(NSError *)error {
    NSLog(@"oauth20Connection");
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.description];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.loginCallbackId];

    self.loginCallbackId = nil;
}


@end
