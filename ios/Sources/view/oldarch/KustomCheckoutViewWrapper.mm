#if !RCT_NEW_ARCH_ENABLED

#import <React/RCTLog.h>
#import "../KustomCheckoutViewWrapper.h"
#import "../../common/RNMobileSDKUtils.h"
#import <KustomMobileSDK/KustomMobileSDK.h>
#import <KustomMobileSDK/KustomMobileSDK-Swift.h>

@interface KustomCheckoutViewWrapper () <KustomEventHandler, KustomSizingDelegate>

@property (nonatomic, strong) KustomCheckoutView* kustomCheckoutView;
@property (nonatomic, assign) BOOL isCheckoutViewReadyEventSent;

@end

@implementation KustomCheckoutViewWrapper

#pragma mark - Initialization

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        self.isCheckoutViewReadyEventSent = NO;
    }
    return self;
}

#pragma mark - React Native Property Setters

- (void) setReturnUrl:(NSString *)returnUrl {
    _returnUrl = returnUrl;
    if (returnUrl.length > 0) {
        self.kustomCheckoutView.returnURL = [NSURL URLWithString:self.returnUrl];
    }
    [self evaluateProps];
}

- (void) evaluateProps {
    [self initializeActualCheckoutView];
}

#pragma mark - KustomCheckoutView Setup

- (void) initializeActualCheckoutView {
    self.isCheckoutViewReadyEventSent = NO;
    self.kustomCheckoutView = [[KustomCheckoutView alloc] initWithReturnURL:[NSURL URLWithString:self.returnUrl] eventHandler:self environment:nil region: nil];
   
    
    self.kustomCheckoutView.sizingDelegate = self;
    
    self.kustomCheckoutView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self addSubview:self.kustomCheckoutView];

    [NSLayoutConstraint activateConstraints:[[NSArray alloc] initWithObjects:
                                             [self.kustomCheckoutView.topAnchor constraintEqualToAnchor:self.topAnchor],
                                             [self.kustomCheckoutView.bottomAnchor constraintEqualToAnchor:self.bottomAnchor],
                                             [self.kustomCheckoutView.leadingAnchor constraintEqualToAnchor:self.leadingAnchor],
                                             [self.kustomCheckoutView.trailingAnchor constraintEqualToAnchor:self.trailingAnchor], nil
                                            ]];
}

#pragma mark - Layout

- (void)layoutSubviews {
    [super layoutSubviews];
    self.kustomCheckoutView.frame = self.bounds;
    [self.kustomCheckoutView layoutSubviews];
}

#pragma mark - UIView Lifecycle

- (void)didMoveToWindow {
    [super didMoveToWindow];
    if (!self.window) {
        self.isCheckoutViewReadyEventSent = NO;
    }
}

#pragma mark - React Native Lifecycle

- (void)didSetProps:(NSArray<NSString *> *)changedProps {
    [super didSetProps:changedProps];
    [self sendCheckoutViewReadyEvent];
}

- (void)sendCheckoutViewReadyEvent {
    if (self.kustomCheckoutView && self.onCheckoutViewReady && !self.isCheckoutViewReadyEventSent) {
        self.isCheckoutViewReadyEventSent = YES;
        self.onCheckoutViewReady(@{});
    }
}

#pragma mark - Methods Exposed to React Native

- (void)setSnippet:(NSString *)snippet {
    [self.kustomCheckoutView setSnippet:snippet];
}

- (void)suspend {
    [self.kustomCheckoutView suspend];
}

- (void)resume {
    [self.kustomCheckoutView resume];
}

#pragma mark - KustomEventHandler

- (void)kustomComponent:(id<KustomComponent>)kustomComponent dispatchedEvent:(KustomProductEvent *)event {
    if (!self.onEvent) {
        RCTLog(@"Missing 'onEvent' callback prop.");
        return;
    }
    
    NSString *serializedParams = [SerializationUtil serializeDictionaryToJsonString:[event getParams]];
    
    self.onEvent(@{
        @"productEvent": @{
            @"action": event.action,
            @"params": serializedParams,
        }
    });
}

- (void)kustomComponent:(id<KustomComponent>)kustomComponent encounteredError:(KustomMobileSDKError *)error {
    if (!self.onError) {
        RCTLog(@"Missing 'onError' callback prop.");
        return;
    }
    
    self.onError(@{
        @"error": @{
            @"name": error.name,
            @"message": error.message,
            @"isFatal": [NSNumber numberWithBool:error.isFatal],
        }
    });
}

#pragma mark - KustomSizingDelegate

- (void)kustomComponent:(id<KustomComponent>)kustomComponent resizedToHeight:(CGFloat)height {
    if (!self.onResized) {
        RCTLog(@"Missing 'onResized' callback prop.");
        return;
    }
    
    self.onResized(@{
        @"height": [[NSNumber numberWithFloat:height] stringValue]
    });
}

@end

#endif
