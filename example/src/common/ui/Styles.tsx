import { StyleSheet } from 'react-native';

export function backgroundStyle(style: any, isDarkMode: boolean) {
  return [style, background(isDarkMode)];
}

export function background(isDarkMode: boolean) {
  return {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };
}

export const Colors = {
  white: '#FFF',
  lighter: '#F3F3F3',
  light: '#DAE1E7',
  lightGray: '#d2d2d2',
  dark: '#444',
  darker: '#222',
  black: '#000',
  pink: '#ffc0cb',
};

const styles = StyleSheet.create({
  scrollView: {
    flex: 1,
    flexGrow: 1,
  },
  column: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
  },
  columnHeader: {
    flexShrink: 0,
  },
  columnItemFill: {
    flexGrow: 1,
  },
  tokenInput: {
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    borderColor: 'gray',
    height: 40,
    borderWidth: 1,
    padding: 10,
    margin: 20,
  },
  buttonsContainer: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    margin: 10,
  },
  button: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 7,
    paddingHorizontal: 10,
    borderRadius: 4,
    elevation: 3,
    backgroundColor: Colors.pink,
  },
  buttonText: {
    textAlign: 'center',
    color: Colors.white,
  },
  navMenuItem: {
    fontSize: 20,
    textAlign: 'center',
    margin: 20,
  },
});

export default styles;
