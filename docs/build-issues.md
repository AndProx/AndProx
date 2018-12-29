# Common build issues

## Cannot find :GraphView or :usb-serial-for-android

```
Could not determine the dependencies of task ':app:lintVitalRelease'.
> Could not resolve all task dependencies for configuration ':app:releaseRuntimeClasspath'.
   > Could not resolve project :usb-serial-for-android.
     Required by:
         project :app
         project :app > project :natives
      > Unable to find a matching configuration of project :usb-serial-for-android: None of the consumable configurations have attributes.
   > Could not resolve project :GraphView.
     Required by:
         project :app
      > Unable to find a matching configuration of project :GraphView: None of the consumable configurations have attributes.
```

These errors are caused by not having the appropriate `git submodules`. `git`'s default clone
behaviour, and GitHub's "download ZIP" function do not include submodules.

See [getting the code](./hacking.md#getting-the-code).

## Problem configuring project :natives

```
org.gradle.initialization.ReportedException: org.gradle.internal.exceptions.LocationAwareException:
A problem occurred configuring project ':natives'.
[...]
Caused by: java.lang.NullPointerException
at com.google.common.base.Preconditions.checkNotNull(Preconditions.java:782)
at com.android.build.gradle.internal.ndk.NdkHandler.getPlatformVersion(NdkHandler.java:158)
at com.android.build.gradle.internal.ndk.NdkHandler.supports64Bits(NdkHandler.java:331)
at com.android.build.gradle.internal.ndk.NdkHandler.getSupportedAbis(NdkHandler.java:403)
[...]

-- OR --

FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring project ':natives'.
> NDK not configured.
  Download it with SDK manager.
```

You don't have the Android NDK installed. [Please install all required Android SDK
components](./hacking.md#developing--testing-andprox).

## clang++: error: no such file or directory: libgnustl_static.a

```
[143/143] Linking CXX shared library AndProx/natives/build/intermediates/cmake/debug/obj/x86_64/libnatives.so
FAILED: : && Android/sdk/ndk-bundle/toolchains/llvm/prebuilt/.../bin/clang++  [...]
clang++: error: no such file or directory: 'Android/sdk/ndk-bundle/sources/cxx-stl/gnu-libstdc++/4.9/libs/x86_64/libgnustl_static.a'
```

This is caused by an [issue with Android Studio](https://groups.google.com/d/msg/android-ndk/3iKT-kLEGpY/a7WEY_EmAwAJ)

In Android Studio, select `Build` menu -> `Refresh Linked C++ Projects`.

## GraphView: secret key missing

**Note:** This should no longer be an issue in AndProx 2.0.4 and later.

```
Could not evaluate onlyIf predicate for task ':GraphView:signArchives'.
> Unable to retrieve secret key from key ring file '/Users/jonas/.gnupg/secring.gpg' as it does not exist
```

In some environments, GraphView's signing target is triggered, and only the developer has the
private key.

_Linux:_ Run `./third_party/disable_graphview_signing.sh`, which will patch GraphView to disable
signing.

_Manual:_ delete `./third_party/GraphView/maven_push.gradle`, and the reference to
`maven_push.gradle` in `./third_party/GraphView/build.gradle`.
