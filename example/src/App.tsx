import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import HomeScreen from './home/HomeScreen';
import KustomCheckoutScreen from './checkout/KustomCheckoutScreen';

const Stack = createNativeStackNavigator<AppStackParamList>();

const AppStack = () => {
  return (
    <Stack.Navigator initialRouteName="Home">
      <Stack.Screen
        name="Home"
        component={HomeScreen}
        options={{ title: 'Kustom Mobile SDK Example' }}
      />
      <Stack.Screen name="KustomCheckout" component={KustomCheckoutScreen} />
    </Stack.Navigator>
  );
};

function App() {
  return (
    <NavigationContainer>
      <AppStack />
    </NavigationContainer>
  );
}

type AppStackParamList = {
  Home: undefined;
  KustomCheckout: undefined;
};

export default App;
export type { AppStackParamList };
