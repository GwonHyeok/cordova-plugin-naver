#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import "AppDelegate.h"
#import "NaverThirdPartyLoginConnection.h"

@interface NaverPlugin : CDVPlugin {
  // Member variables go here.
}

- (void)coolMethod:(CDVInvokedUrlCommand*)command;
@end
