import { Keyboard, Text, TextInput, useColorScheme, View } from 'react-native';
import {
  KustomCheckoutView,
  type KustomCheckoutViewProps,
} from 'react-native-kustom-inapp-sdk';
import React, { useRef, useState } from 'react';
import styles, { backgroundStyle } from '../common/ui/Styles';
import Button from '../common/ui/view/Button';
import testProps from '../common/util/TestProps';

// root pins react 18 (src/ typecheck baseline), example pins react 19 - JSX element
// check fails across the two. Remove this cast once the RN 0.72->0.85 bump unifies
// react versions across root and example.
const CheckoutView = KustomCheckoutView as unknown as React.ComponentType<
  KustomCheckoutViewProps & React.RefAttributes<KustomCheckoutView>
>;

export default function KustomCheckoutScreen(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const checkoutViewRef = useRef<KustomCheckoutView>(null);
  const [snippet, setSnippet] = useState<string>();
  const [eventState, setEventState] = useState<string>();

  const onEvent = (...params: Array<string | boolean | null>) => {
    console.log('onEvent', params);
    setEventState(params.join(', '));
  };

  const renderSnippetInput = () => {
    return (
      <TextInput
        style={styles.tokenInput}
        defaultValue={snippet}
        placeholder="Checkout snippet here..."
        multiline={true}
        blurOnSubmit={true}
        {...testProps('snippetInput')}
        onChangeText={text => {
          setSnippet(text);
        }}
      />
    );
  };

  const renderSetSnippetButton = () => {
    return (
      <View>
        <Button
          onPress={() => {
            if (snippet !== '' && snippet !== undefined) {
              checkoutViewRef.current?.setSnippet(snippet);
            }
            Keyboard.dismiss();
          }}
          title="Set Snippet"
        />
      </View>
    );
  };

  const renderSuspendButton = () => {
    return (
      <View>
        <Button
          onPress={() => {
            checkoutViewRef.current?.suspend();
          }}
          title="Suspend"
        />
      </View>
    );
  };

  const renderResumeButton = () => {
    return (
      <View>
        <Button
          onPress={() => {
            checkoutViewRef.current?.resume();
          }}
          title="Resume"
        />
      </View>
    );
  };

  return (
    <View style={backgroundStyle(styles.column, isDarkMode)}>
      <View style={styles.columnHeader}>
        {renderSnippetInput()}
        <View style={styles.buttonsContainer}>
          {renderSetSnippetButton()}
          {renderSuspendButton()}
          {renderResumeButton()}
        </View>
        <Text {...testProps('state_events')}>{eventState}</Text>
      </View>
      <CheckoutView
        ref={checkoutViewRef}
        style={styles.columnItemFill}
        returnUrl={'returnUrl://'}
        onEvent={kustomProductEvent => {
          onEvent(JSON.stringify(kustomProductEvent));
        }}
        onError={error => {
          onEvent(JSON.stringify(error));
        }}
      />
    </View>
  );
}
