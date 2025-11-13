import type { ViewProps } from 'react-native/Libraries/Components/View/ViewPropTypes';
import type { DirectEventHandler } from 'react-native/Libraries/Types/CodegenTypes';
import type { HostComponent } from 'react-native';
import React from 'react';
import codegenNativeCommands from 'react-native/Libraries/Utilities/codegenNativeCommands';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export interface RNKustomCheckoutViewProps extends ViewProps {
  readonly returnUrl: string;
  readonly onEvent: DirectEventHandler<
    Readonly<{
      readonly productEvent: Readonly<{
        readonly action: string;
        readonly params: string;
      }>;
    }>
  >;
  readonly onError: DirectEventHandler<
    Readonly<{
      readonly error: Readonly<{
        readonly isFatal: boolean;
        readonly message: string;
        readonly name: string;
      }>;
    }>
  >;
  readonly onResized: DirectEventHandler<
    Readonly<{
      // number not supported for events
      readonly height: string;
    }>
  >;
  readonly onCheckoutViewReady?: DirectEventHandler<{}>;
}

type KustomCheckoutViewNativeComponentType =
  HostComponent<RNKustomCheckoutViewProps>;

interface RNKustomCheckoutViewNativeCommands {
  setSnippet: (
    viewRef: React.ElementRef<KustomCheckoutViewNativeComponentType>,
    snippet: string
  ) => void;
  suspend: (
    viewRef: React.ElementRef<KustomCheckoutViewNativeComponentType>
  ) => void;
  resume: (
    viewRef: React.ElementRef<KustomCheckoutViewNativeComponentType>
  ) => void;
}

export const Commands: RNKustomCheckoutViewNativeCommands =
  codegenNativeCommands<RNKustomCheckoutViewNativeCommands>({
    supportedCommands: ['setSnippet', 'suspend', 'resume'],
  });

export default codegenNativeComponent<RNKustomCheckoutViewProps>(
  'RNKustomCheckoutView'
) as KustomCheckoutViewNativeComponentType;
