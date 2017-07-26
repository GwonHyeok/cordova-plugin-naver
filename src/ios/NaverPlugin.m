#import "NaverPlugin.h"

@implementation NaverPlugin

- (void)pluginInitialize {
    NSLog(@"Start Naver plugin");

    // 네이버 앱과, 인앱 브라우저 인증을 둘다 사용하도록 설정
    [[NaverThirdPartyLoginConnection getSharedInstance] setIsNaverAppOauthEnable:YES];
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

- (void)login:(CDVInvokedUrlCommand *)command {

    // 로그인 요청
    NaverThirdPartyLoginConnection *login = [NaverThirdPartyLoginConnection getSharedInstance];
    [login requestThirdPartyLogin];

    // Else just return OK we are already logged out
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)coolMethod:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult = nil;
    NSString *echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
