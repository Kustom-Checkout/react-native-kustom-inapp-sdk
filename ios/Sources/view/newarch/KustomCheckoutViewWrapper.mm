#if RCT_NEW_ARCH_ENABLED

#import <AVFoundation/AVFoundation.h>
#import <React/RCTLog.h>
#import <react/renderer/components/RNKustomMobileSDK/ComponentDescriptors.h>
#import <react/renderer/components/RNKustomMobileSDK/EventEmitters.h>
#import <react/renderer/components/RNKustomMobileSDK/Props.h>
#import <react/renderer/components/RNKustomMobileSDK/RCTComponentViewHelpers.h>
#import "../KustomCheckoutViewWrapper.h"
#import "../../common/RNMobileSDKUtils.h"
#import "RCTFabricComponentsPlugins.h"
#import <KustomMobileSDK/KustomMobileSDK.h>
#import <KustomMobileSDK/KustomMobileSDK-Swift.h>

using namespace facebook::react;

@interface KustomCheckoutViewWrapper () <KustomEventHandler, KustomSizingDelegate, RCTRNKustomCheckoutViewViewProtocol>

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
        static const auto defaultProps = std::make_shared<const RNKustomCheckoutViewProps>();
        _props = defaultProps;
    }
    
    return self;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<RNKustomCheckoutViewComponentDescriptor>();
}

Class<RCTComponentViewProtocol> RNKustomCheckoutViewCls(void)
{
    return KustomCheckoutViewWrapper.class;
}

- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args {
    RCTRNKustomCheckoutViewHandleCommand(self, commandName, args);
}

#pragma mark - KustomCheckoutView Props Update

- (void)updateProps:(const facebook::react::Props::Shared &)props oldProps:(const facebook::react::Props::Shared &)oldProps {
    const auto &oldViewProps = *std::static_pointer_cast<RNKustomCheckoutViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<RNKustomCheckoutViewProps const>(props);
    
    if (oldViewProps.returnUrl != newViewProps.returnUrl) {
        NSString * newReturnUrl = [[NSString alloc] initWithUTF8String: newViewProps.returnUrl.c_str()];
        if (self.kustomCheckoutView != nil) {
            self.kustomCheckoutView.returnURL = [NSURL URLWithString:newReturnUrl];
        } else {
            [self initializeKustomCheckoutView:newReturnUrl];
        }
    }
    
    [super updateProps:props oldProps:oldProps];
}

- (void)updateEventEmitter:(const facebook::react::EventEmitter::Shared &)eventEmitter
{
    [super updateEventEmitter:eventEmitter];

    if (!self.isCheckoutViewReadyEventSent && self.kustomCheckoutView != nil && _eventEmitter) {
        RCTLogInfo(@"Sending onCheckoutViewReady event.");
        std::dynamic_pointer_cast<const RNKustomCheckoutViewEventEmitter>(_eventEmitter)
            ->onCheckoutViewReady({});
        self.isCheckoutViewReadyEventSent = YES;
    } else {
        RCTLogInfo(@"Could not send onCheckoutViewReady event.");
    }
}

#pragma mark - KustomCheckoutView Setup

- (void)initializeKustomCheckoutView:(NSString*)returnUrl {
    self.isCheckoutViewReadyEventSent = NO;
    self.kustomCheckoutView = [[KustomCheckoutView alloc] initWithReturnURL:[NSURL URLWithString:returnUrl] eventHandler:self environment: nil region: nil];
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

#pragma mark - UIView Lifecycle

- (void)didMoveToWindow {
    [super didMoveToWindow];
    if (!self.window) {
        self.isCheckoutViewReadyEventSent = NO;
    }
}

#pragma mark - KustomEventHandler

- (void)kustomComponent:(id<KustomComponent>)kustomComponent dispatchedEvent:(KustomProductEvent *)event {
    if (_eventEmitter) {
        RCTLogInfo(@"Sending onEvent event");
        NSString *serializedParams = [SerializationUtil serializeDictionaryToJsonString:[event getParams]];
        std::dynamic_pointer_cast<const RNKustomCheckoutViewEventEmitter>(_eventEmitter)
        ->onEvent(RNKustomCheckoutViewEventEmitter::OnEvent{
            .productEvent = {
                .action = std::string([event.action UTF8String]),
                .params = std::string([serializedParams UTF8String]),
            }
        });
    } else {
        RCTLogInfo(@"Could not send onEvent event. _eventEmitter is nil!");
    }
}

- (void)kustomComponent:(id<KustomComponent>)kustomComponent encounteredError:(KustomMobileSDKError *)error {
    if (_eventEmitter) {
        RCTLogInfo(@"Sending onError event");
        std::dynamic_pointer_cast<const RNKustomCheckoutViewEventEmitter>(_eventEmitter)
        ->onError(RNKustomCheckoutViewEventEmitter::OnError{
            .error = {
                .name = std::string([error.name UTF8String]),
                .message = std::string([error.message UTF8String]),
                .isFatal = error.isFatal,
            }
        });
    } else {
        RCTLogInfo(@"Could not send onError event. _eventEmitter is nil!");
    }
}

#pragma mark - KustomSizingDelegate

- (void)kustomComponent:(id<KustomComponent>)kustomComponent resizedToHeight:(CGFloat)height {
    if (_eventEmitter) {
        RCTLogInfo(@"Sending onResized event");
        std::dynamic_pointer_cast<const RNKustomCheckoutViewEventEmitter>(_eventEmitter)
        ->onResized(RNKustomCheckoutViewEventEmitter::OnResized{
            .height = std::string([[[NSNumber numberWithFloat:height] stringValue] UTF8String]),
        });
    } else {
        RCTLogInfo(@"Could not send onResized event. _eventEmitter is nil!");
    }
}

@end

#endif
