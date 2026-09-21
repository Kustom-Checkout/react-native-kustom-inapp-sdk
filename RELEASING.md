# Releasing

Manual. CI lints, tests and builds — it does not publish.

## Setup (once)

Publishing needs a **Granular Access Token with "Bypass 2FA" enabled**, from the
**`kustom-app`** account (the only maintainer on the package).

1. npmjs.com → `kustom-app` → Access Tokens → **Generate New** → **Granular**
2. Package `react-native-kustom-inapp-sdk`, **Read and write**, tick **Bypass 2FA**, set an expiry
3. `npm config set "//registry.npmjs.org/:_authToken=npm_xxxx"`
4. Verify: `npm whoami --registry https://registry.npmjs.org/` → `kustom-app`

What does **not** work: `npm login` (web auth issues a token that 403s on publish),
`npm publish --otp=...` (granular tokens don't accept OTP headers), any interactive prompt
in a non-TTY shell.

## Release

```shell
git checkout main && git pull                       # must be clean
npm view react-native-kustom-inapp-sdk version      # what's ACTUALLY live
```

Bump, then merge via PR:

```shell
npm version <x.y.z> --no-git-tag-version
```

Then from `main`:

```shell
yarn install --immutable
yarn lint && yarn typecheck && yarn test
npm publish                                          # prepack runs `bob build` for you
git tag v<x.y.z> && git push origin v<x.y.z>         # AFTER publish, at the published commit
gh release create v<x.y.z> --generate-notes
```

Publish is async — `npm view` can lag a few minutes behind a successful publish.

## Check the tarball first

Packaging changes break consumers even when `src/` is untouched. Do this before choosing
the version number.

```shell
npm pack --pack-destination /tmp/rc
cd /tmp/rc && npm pack react-native-kustom-inapp-sdk@latest
for f in *.tgz; do tar -tzf "$f" | sed 's|^package/||' | sort > "$f.txt"; done
diff *.tgz.txt
```

Any removed file, or a changed `main` / `types` / `exports`, is a break for someone —
bump accordingly. This is how the dropped CommonJS build in 1.0.5 was found.

## Native SDK pins

Two files, pinned independently, they drift:

| Platform | File | Pin |
| --- | --- | --- |
| iOS | `react-native-kustom-inapp-sdk.podspec` | `s.dependency 'KustomMobileSDK'` |
| Android | `android/build.gradle` | `com.kustom.mobile.sdk:kustom-mobile-sdk` |

The podspec's own version comes from `package.json` — nothing to bump there.

## Gotchas

- **The version in `package.json` is not proof it shipped.** 1.0.5 was bumped and tagged in
  June, published in September. Trust `npm view`, not the tag list.
- **Tag after publishing, at the published commit.** The podspec pins
  `:tag => "v#{s.version}"`, so a drifting tag means CocoaPods and npm serve different sources.
- **`npm whoami` without `--registry` answers for Artifactory** and tells you nothing about npmjs.
- **`command not found: eslint`** → stale install, run `yarn install --immutable`.
- **`yarn npm publish`** authenticates from Yarn's own config, not `~/.npmrc`. Use `npm publish`.
