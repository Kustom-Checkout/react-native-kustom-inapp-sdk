import React, { Component, type RefObject } from 'react';
import type {
  NativeMethods,
  NativeSyntheticEvent,
  ViewStyle,
} from 'react-native';
import type { KustomProductEvent } from './types/common/KustomProductEvent';
import type { KustomMobileSDKError } from './types/common/KustomMobileSDKError';
import RNKustomCheckoutView, {
  Commands as RNKustomCheckoutViewCommands,
  type RNKustomCheckoutViewProps,
} from './specs/KustomCheckoutViewNativeComponent';

export interface KustomCheckoutViewProps {
  style?: ViewStyle;
  readonly returnUrl: string;
  readonly onEvent?: (kustomProductEvent: KustomProductEvent) => void;
  readonly onError?: (error: KustomMobileSDKError) => void;
}

interface KustomCheckoutViewState {
  nativeViewHeight: number;
}

export class KustomCheckoutView extends Component<
  KustomCheckoutViewProps,
  KustomCheckoutViewState
> {
  checkoutViewRef: RefObject<
    Component<RNKustomCheckoutViewProps> & Readonly<NativeMethods>
  >;
  private snippet: string | null = null;
  private isCheckoutViewReady = false;

  constructor(props: KustomCheckoutViewProps) {
    super(props);
    this.state = {
      nativeViewHeight: 0,
    };
    this.checkoutViewRef = React.createRef();
    this.isCheckoutViewReady = false;
  }

  componentWillUnmount() {
    this.isCheckoutViewReady = false;
  }

  render() {
    return (
      <RNKustomCheckoutView
        ref={this.checkoutViewRef}
        /* eslint-disable-next-line react-native/no-inline-styles */
        style={{
          width: '100%',
          height: this.state.nativeViewHeight,
          flexShrink: 1,
        }}
        returnUrl={this.props.returnUrl || ''}
        onEvent={(
          event: NativeSyntheticEvent<
            Readonly<{
              readonly productEvent: Readonly<{
                readonly action: string;
                readonly params: string;
              }>;
            }>
          >
        ) => {
          let params = {};
          try {
            params = JSON.parse(event.nativeEvent.productEvent.params);
          } catch (e) {
            console.error('Failed to parse productEvent.params', e);
          }
          const productEvent: KustomProductEvent = {
            action: event.nativeEvent.productEvent.action,
            params: params,
          };
          this.props.onEvent?.(productEvent);
        }}
        onError={(
          event: NativeSyntheticEvent<
            Readonly<{
              readonly error: Readonly<{
                readonly isFatal: boolean;
                readonly message: string;
                readonly name: string;
              }>;
            }>
          >
        ) => {
          const mobileSdkError: KustomMobileSDKError = {
            isFatal: event.nativeEvent.error.isFatal,
            message: event.nativeEvent.error.message,
            name: event.nativeEvent.error.name,
          };
          this.props.onError?.(mobileSdkError);
        }}
        onResized={(
          event: NativeSyntheticEvent<
            Readonly<{
              readonly height: string;
            }>
          >
        ) => {
          const newHeight = Number(event.nativeEvent.height);
          if (newHeight !== this.state.nativeViewHeight) {
            console.log(`onResized: new height is ${newHeight}`);
            this.setState({ nativeViewHeight: newHeight });
          }
        }}
        onCheckoutViewReady={() => {
          this.isCheckoutViewReady = true;
          console.log('Native checkout view is ready.');
          if (this.snippet) {
            console.log('Setting the snippet...');
            this.setSnippet(this.snippet);
            this.snippet = null;
          }
        }}
      />
    );
  }

  setSnippet = (snippet: string) => {
    this.snippet = snippet;
    const view = this.checkoutViewRef.current;
    if (view != null && this.isCheckoutViewReady) {
      RNKustomCheckoutViewCommands.setSnippet(view, snippet);
    } else {
      console.log(
        'setSnippet: checkout view is not ready yet. Will set snippet once ready.'
      );
    }
  };

  suspend = () => {
    const view = this.checkoutViewRef.current;
    if (view != null && this.isCheckoutViewReady) {
      RNKustomCheckoutViewCommands.suspend(view);
    } else {
      console.log('suspend: checkout view is not ready.');
    }
  };

  resume = () => {
    const view = this.checkoutViewRef.current;
    if (view != null && this.isCheckoutViewReady) {
      RNKustomCheckoutViewCommands.resume(view);
    } else {
      console.log('resume: checkout view is not ready.');
    }
  };
}

export default KustomCheckoutView;
