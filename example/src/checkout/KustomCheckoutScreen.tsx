import { Keyboard, Text, TextInput, useColorScheme, View } from 'react-native';
import { KustomCheckoutView } from 'react-native-kustom-inapp-sdk';
import React, { useRef, useState } from 'react';
import styles, { backgroundStyle } from '../common/ui/Styles';
import Button from '../common/ui/view/Button';
import testProps from '../common/util/TestProps';

export default function KustomCheckoutScreen(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const checkoutViewRef = useRef<KustomCheckoutView>(null);
  const [snippet, setSnippet] = useState<string>();
  const [eventState, setEventState] = useState<string>();

  const onEvent = (...params: Array<string | boolean | null>) => {
    console.log('onEvent', params);
    setEventState(params.join(', '));
  };

  return (
    <View style={backgroundStyle(styles.column, isDarkMode)}>
      <View style={styles.columnHeader}>
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
        <View style={styles.buttonsContainer}>
          <Button
            onPress={() => {
              if (snippet !== '' && snippet !== undefined) {
                checkoutViewRef.current?.setSnippet(snippet);
              }
              Keyboard.dismiss();
            }}
            title="Set Snippet"
          />
          <Button
            onPress={() => {
              checkoutViewRef.current?.suspend();
            }}
            title="Suspend"
          />
          <Button
            onPress={() => {
              checkoutViewRef.current?.resume();
            }}
            title="Resume"
          />
        </View>
        <Text {...testProps('state_events')}>{eventState}</Text>
      </View>
      <KustomCheckoutView
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
