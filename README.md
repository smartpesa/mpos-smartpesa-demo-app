# SmartPesa for Android #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* Deployment instructions

### Running Unit Test ###

1. Open Android SDK Manager (*Tools* Menu | Android) and make sure you have the latest *Android Support Repository* installed.
2. Test source code are located in *src/androidTest/java*
3. Create test configuration with a custom runner: `android.support.test.runner.AndroidJUnitRunner`
    * Open *Run* menu | *Edit Configurations*
    * Add a new *Android Tests* configuration
    * Give it a *Name* (for example: *FullAndroidTests*)
    * Choose module: app
    * Click on *Test*: *Class:*, and select `AndroidTestSuite`.
    * Add a *Specific instrumentation runner*: `android.support.test.runner.AndroidJUnitRunner`
    * Click on *Target Device*: Show chooser dialog
    * Click *OK*
4. Repeat step 3, for test class `InstrumentTestSuite` and `UnitTestSuite`
5. Connect a device or start an emulator
6. Run the newly created configuration

Note: `AndroidTestSuite` will run the whole unit test suite, while `InstrumentTestSuite` and `UnitTestSuite` will only run `Activity` and `Class` related tests respectively.

### Contribution guidelines ###

* Development is conducted in development branch, while master branch is only used for releases. Further information about branching can be found [here](http://nvie.com/posts/a-successful-git-branching-model/).
* Follow SmartPesa's Android code style. Contact @malvinstn for the file.

### Useful guidelines ###

* [Developing for Android](https://medium.com/google-developers/developing-for-android-introduction-5345b451567c#.2jz4x8bmp)
* [Android Design Guidelines](http://developer.android.com/design/index.html)
* [Android Programming Best Practices](https://developer.android.com/guide/practices/index.html)
* [Android Performance Best Practices](http://developer.android.com/training/best-performance.html)
* [Android Code Style Guidelines](https://source.android.com/source/code-style.html)

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact