# Kustom Mobile SDK React Native

[![NPM][npm-badge]][npm-url]
[![React Native][dependency-badge]][dependency-url]
[![Platform][platform-badge]][platform-url]
[![License][license-badge]][license-url]
![status: beta](https://img.shields.io/badge/status-beta-orange)


This library wraps Kustom Mobile SDK and exposes its functionality to integrate with Kustom Checkout as a React Native component.
 

This repository also includes a test application that you can use to see how it works.

## Requirements
* iOS 10 or later.
* Android 4.4 or later.


## Getting started

### Add Dependency

#### NPM

```shell
npm install react-native-kustom-inapp-sdk --save
```

#### Yarn

```shell
yarn add react-native-kustom-inapp-sdk
```

### Warning regarding Android integration

Both the iOS and Android integrations depend on the native SDK.

We've experienced issues with React Native 59 and above where 3rd party Gradle repositories won't
be recognized in the Android project's `build.gradle`. To address this, you'll need to add a
reference to the repository in your own app's `build.gradle`.

You can do it by adding the lines between the comments below:

```groovy
allprojects {
    repositories {
        ...
        // Add the lines below
        maven {
            url 'https://x.klarnacdn.net/mobile-sdk/'
        }
        maven {
            url 'https://x.kustomcdn.co/mobile-sdk/android/'
        }
    }
}
```

## License

This project is licensed under
[Apache License, Version 2.0](https://github.com/Kustom-Checkout/react-native-kustom-inapp-sdk/blob/master/LICENSE).

<!-- Markdown images & links -->
[npm-badge]: https://img.shields.io/npm/v/react-native-kustom-inapp-sdk?style=flat-square
[npm-url]: https://www.npmjs.com/package/react-native-kustom-inapp-sdk
[dependency-badge]: https://img.shields.io/npm/dependency-version/react-native-kustom-inapp-sdk/peer/react-native?style=flat-square
[dependency-url]: https://www.npmjs.com/package/react-native-klarna-inapp-sdk?activeTab=dependencies
[platform-badge]: https://img.shields.io/badge/platform-React%20Native-lightgrey?style=flat-square
[platform-url]: https://reactnative.dev
[license-badge]: https://img.shields.io/github/license/Kustom-Checkout/react-native-kustom-inapp-sdk?style=flat-square
[license-url]: https://github.com/klarna/react-native-klarna-inapp-sdk/blob/master/LICENSE
[beta-url]: https://img.shields.io/badge/status-beta-orange