#import <React/RCTUIManager.h>
#import <React/RCTLog.h>
#import "KustomCheckoutViewManager.h"
#import "view/KustomCheckoutViewWrapper.h"

@implementation KustomCheckoutViewManager

RCT_EXPORT_MODULE(RNKustomCheckoutView)

#pragma mark - View Properties

RCT_EXPORT_VIEW_PROPERTY(returnUrl, NSString)
RCT_EXPORT_VIEW_PROPERTY(onEvent, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onError, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onResized, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCheckoutViewReady, RCTDirectEventBlock)

#pragma mark - View Creation

- (UIView *)view
{
    KustomCheckoutViewWrapper* wrapper = [KustomCheckoutViewWrapper new];
    wrapper.uiManager = self.bridge.uiManager;
    return wrapper;
}

#pragma mark - Exported Methods to React Native

RCT_EXPORT_METHOD(setSnippet:(nonnull NSNumber *)reactTag snippet:(NSString *)snippet)
{
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        KustomCheckoutViewWrapper *view = [self checkoutViewWrapperForTag:reactTag fromRegistry:viewRegistry];
        if (view) {
            [view setSnippet:snippet];
        }
    }];
}

RCT_EXPORT_METHOD(suspend:(nonnull NSNumber *)reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        KustomCheckoutViewWrapper *view = [self checkoutViewWrapperForTag:reactTag fromRegistry:viewRegistry];
        if (view) {
            [view suspend];
        }
    }];
}

RCT_EXPORT_METHOD(resume:(nonnull NSNumber *)reactTag) {
    [self.bridge.uiManager addUIBlock:^(RCTUIManager *uiManager, NSDictionary<NSNumber *,UIView *> *viewRegistry) {
        KustomCheckoutViewWrapper *view = [self checkoutViewWrapperForTag:reactTag fromRegistry:viewRegistry];
        if (view) {
            [view resume];
        }
    }];
}

#pragma mark - Private Helper Methods

- (KustomCheckoutViewWrapper *)checkoutViewWrapperForTag:(NSNumber *)reactTag fromRegistry:(NSDictionary<NSNumber *, UIView *> *)viewRegistry
{
    UIView *view = viewRegistry[reactTag];
    if (!view || ![view isKindOfClass:[KustomCheckoutViewWrapper class]]) {
        RCTLogError(@"Can't find KustomCheckoutViewWrapper with tag #%@", reactTag);
        return nil;
    }
    return (KustomCheckoutViewWrapper *)view;
}

@end
