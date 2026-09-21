# Releasing

How to publish a new version of `react-native-kustom-inapp-sdk` to npm.

Releases are **manual** — CI lints, tests and builds on every push, but nothing publishes.
The whole process runs from your machine.

## One-time setup

### Node and Yarn

Use the Node version in [`.nvmrc`](.nvmrc), and let Corepack pick up the pinned Yarn
version from `packageManager` in `package.json`:

```shell
nvm use
corepack enable
```

### npm authentication

This is the step that trips people up. The repo publishes to the **public npm registry**,
but most Kustom machines have `~/.npmrc` pointing at Artifactory:

```
registry=https://kustom.jfrog.io/artifactory/api/npm/prod-npm/
```

`publishConfig.registry` in `package.json` overrides that at publish time, so the package
still lands on npmjs. But you need a **valid npmjs token**, and `npm whoami` on its own
answers for Artifactory — not for npmjs. Always check with an explicit registry:

```shell
npm whoami --registry https://registry.npmjs.org/
```

If that returns `401 Unauthorized`, log in again:

```shell
npm login --registry https://registry.npmjs.org/
```

You need to be a member of the package's npm owners. Ask an existing maintainer if
`npm login` succeeds but publishing returns `403`.

## Releasing

### 1. Start from a clean, up-to-date `main`

```shell
git checkout main
git pull origin main
git status
```

The working tree must be clean. `lib/` is gitignored — a build won't dirty it.

### 2. Choose the version

Follow semver against **what is currently published**, not against what the last tag says
(those can disagree — see [Gotchas](#gotchas)). Check what's actually on npm:

```shell
npm view react-native-kustom-inapp-sdk versions
npm view react-native-kustom-inapp-sdk dist-tags
```

Remember that packaging changes are API changes for consumers. Dropping a build output,
adding an `exports` map, or moving the type definitions can all break people even when no
line of `src/` changed. Step 5 shows you exactly what moved — do it before you settle on
the number.

Bump the version in `package.json`:

```shell
npm version 1.2.3 --no-git-tag-version
```

`--no-git-tag-version` is deliberate: we tag after a successful publish, so a failed
publish doesn't leave a tag pointing at a release that doesn't exist.

The iOS podspec reads its version straight from `package.json`, so there is nothing to
bump there. Native SDK dependencies are a separate concern — see
[Bumping the native SDKs](#bumping-the-native-sdks).

Commit the bump and get it onto `main` through the normal PR flow.

### 3. Install and run the checks

```shell
yarn install --immutable
yarn lint
yarn typecheck
yarn test
```

These are the same checks CI runs. Native tests (`yarn test:android`) run in CI too, and
are worth running locally if the release touches `android/`.

### 4. Build

```shell
yarn prepack
```

This runs `bob build` and writes `lib/`. Both `npm publish` and `npm pack` trigger
`prepack` automatically, so this step is really about seeing the output before it ships.

### 5. Verify the tarball against the last published version

Do not skip this. It is the cheapest way to catch a packaging change that nobody intended,
and it is how we found that the CommonJS build had been dropped.

Pack what you're about to publish:

```shell
npm pack --pack-destination /tmp/release-check
tar -tzf /tmp/release-check/react-native-kustom-inapp-sdk-*.tgz \
  | grep -v '/$' | sed 's|^package/||' | sort > /tmp/release-check/new-files.txt
```

Download and unpack what's currently on npm:

```shell
mkdir -p /tmp/release-check/published && cd /tmp/release-check/published
npm pack react-native-kustom-inapp-sdk@latest
tar -xzf *.tgz
find package -type f | sed 's|^package/||' | sort > /tmp/release-check/old-files.txt
```

Then compare the file lists and the entry points:

```shell
diff /tmp/release-check/old-files.txt /tmp/release-check/new-files.txt

node -e "const p=require('/tmp/release-check/published/package/package.json'); \
  console.log(JSON.stringify({main:p.main,module:p.module,types:p.types,exports:p.exports},null,2))"
node -p "const p=require('./package.json'); \
  JSON.stringify({main:p.main,module:p.module,types:p.types,exports:p.exports},null,2)"
```

Every removed file and every changed entry point is a potential break for a consumer.
If the diff surprises you, stop and reconsider the version number from step 2.

You can also diff the compiled output directly to confirm a fix really made it in:

```shell
diff /tmp/release-check/published/package/lib/module/KustomCheckoutView.js \
     lib/module/KustomCheckoutView.js
```

### 6. Publish

```shell
npm publish
```

`publishConfig` routes this to npmjs regardless of your default registry. To rehearse
without shipping, add `--dry-run`.

Confirm it landed:

```shell
npm view react-native-kustom-inapp-sdk dist-tags
```

### 7. Tag and push

Tag only after the publish succeeds, and tag **the exact commit you published from**:

```shell
git tag v1.2.3
git push origin v1.2.3
```

The tag is not decorative. The podspec pins its source to it:

```ruby
s.source = { :git => "...", :tag => "v#{s.version}" }
```

A tag that points somewhere other than the published commit means CocoaPods consumers
fetch different native sources than npm consumers get. Keep them in sync.

### 8. Create the GitHub release

```shell
gh release create v1.2.3 --generate-notes
```

Review the generated notes and call out breaking changes explicitly — especially
packaging changes, which the auto-generated list won't describe in consumer terms.

## Bumping the native SDKs

The iOS and Android native SDK versions are pinned **independently, in two files**, and
they drift. Check both when releasing a native update:

| Platform | File | Pin |
| --- | --- | --- |
| iOS | `react-native-kustom-inapp-sdk.podspec` | `s.dependency 'KustomMobileSDK', '<version>'` |
| Android | `android/build.gradle` | `implementation 'com.kustom.mobile.sdk:kustom-mobile-sdk:<version>'` |

## Gotchas

**The version in `package.json` is not proof that it shipped.** 1.0.5 was bumped, merged
and tagged, but never published — npm sat on 1.0.4 for months while the repo looked
released. Always check `npm view` rather than the tag list or `package.json`.

**A tag can outlive the commit it should describe.** If a version is bumped and tagged but
the publish happens later, `main` may have moved on, and the tag no longer matches the
tarball. Tag after publishing, from the published commit.

**`npm whoami` answers for the wrong registry.** Without `--registry`, it reports your
Artifactory identity and tells you nothing about whether you can publish to npmjs.

**`yarn npm publish` is not a drop-in substitute.** Yarn Berry reads credentials from its
own config rather than `~/.npmrc`, so it can fail to authenticate even when `npm publish`
works. Use `npm publish`.

**A stale `node_modules` fails in confusing ways.** If `yarn lint` reports
`command not found: eslint`, the install is out of date — run `yarn install --immutable`.
