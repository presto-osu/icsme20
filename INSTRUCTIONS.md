# Prerequisites
The experiments described in the paper are conducted on a machine with Intel(R) Xeon(R) CPU E5-2630, 64GB RAM and Ubuntu 18.04 LTS.
Before running the experiments, the following are needed to be present on the system:
- Java 1.8+
- Android SDK, platform 28
- Python 3 (with modules `uiautomator`, `numpy`, `scipy` and `matplotlib`)


---

# Instrumentation
The source code for static analysis and instrumentation of the apps is in the [code](https://github.com/presto-osu/icsme20/tree/master/code) directory. The python script [`gator`](https://github.com/presto-osu/icsme20/blob/master/code/gator) can be used to build and invoke the instrumentation program. Before this step, please make sure that the system environment variable `ANDROID_HOME` is set to the directory containing the android SDK.

To build the instrimentation code, run the following command:

```
$cd code
$./gator b
```

After the build process is complete, the same script can be used to instrument an app, by the running following command:

```
$./gator i -p <path-to-apk-file> -i <path-to-instrumentation-spec-file>
```

The `<path-to-instrumentation-spec-file>` should be the path to the JSON file containing the specifications (location of the code etc.) of the instrumentation process. The specifications for the 9 suject apps are in the file [`instrument_spec.json`](https://github.com/presto-osu/icsme20/blob/master/code/instrument_spec.json). For example, to instrument the cookbook app, the script should be invoked as:

```
$./gator i -p ../apk/com.sparkpeople.android.cookbook.apk -i instrument_spec.json
```

The instrumented APK file will be in the [sootOutput](https://github.com/presto-osu/icsme20/tree/master/code/sootOutput) directory, which needs to be re-signed to be able to run on an android device or emulator.

# Post-instrumentation processing
To ensure the communication between the instrumented app and Firebase Analytics backend, the app package has to be registered into the Firebase dashboard. After the registration, Firebase provides a JSON file (usually named google-services.json) containing the API keys. Some of these keys should be embedded into the APK file as string resources. The [JSON file](https://github.com/presto-osu/icsme20/blob/master/code/sootOutput/google-services.json) we used is provided as a reference. There is a script to replace the API keys in the instrumented app, which can be invoked as:

```
$python3 add_api_keys.py com.sparkpeople.android.cookbook.apk google-services.json
```

The instrumentation process includes a differentially-private wrapper around Firebase Analytics, which uses its own services to generate and report events. To replace the Firebase services with the wrapper services in the app manifest file, run the following command:

```
$python3 fix_manifest.py com.sparkpeople.android.cookbook.apk
```

After all these modifications is done, the modified APK file needs to be signed before it is installed on a device or emulator. We have provided the keys we used to sign the app and also a script to do it:

```
$./sign.sh com.sparkpeople.android.cookbook.apk
```

The last 3 steps (adding API keys, replacing services and signing the app) can be done running the following command:

```
$./post_instrument.sh com.sparkpeople.android.cookbook.apk google-services.json
```

# Simulating user behavior
The script [`simulate_user_action.py`](https://github.com/presto-osu/icsme20/blob/master/code/sootOutput/simulate_user_action.py) can be used to simulate random events of interest by a user. The test methods for each of the 9 apps are in the script [`test_methods.py`](https://github.com/presto-osu/icsme20/blob/master/code/sootOutput/test_methods.py). The script simulate_user_action.py can be used as follows:

```
$python3 simulate_user_action.py -p ../apk/code/com.sparkpeople.android.cookbook.apk -v avd_28_0 -d emulator 5554 -s 0 -n 100 -w
```

Here are the flags explained:

| Flag | Description |
| -----| ------------|
| -p   | The path to the original APK file. If the app is already instrumented, it will use the modified copy in sootOutput folder, otherwise it will instrument, modify and sign it. |
| -v   | Name of the virtual device. For multiple devices, the names should be unique. |
| -d   | Name of the run-time emulator. The name must be in `emulator-xxxx` format where `xxxx` is the port the emulator would run on. It is the same as the `-port` flag of Android SDK's `emulator` command. |
| -w   | Show the emulator UI. This is an optional flag, running without it will run the emulator without the UI. |
| -h   | Show help. |


# Simulating randomization and plotting
After running the script `simulate_user_action.py`, The content dictionaries and event sets of the simulated users will be stored in the [db](https://github.com/presto-osu/icsme20/tree/master/code/sootOutput/db) directory as sqlite database files. The content dictionary is stored in a table named by the name of the event (i.e. `select_content`), and the set of events will be stored in the table `events`. The script `metadata.py` contains the specific details on each app. The script `read_data.py` reads the database files and creates synthetic user data to simulate 1000, 10000 and 10000 users. The synthetic user data is stored in a directory called `synthetic_user_data` in JSON format. These files can be quite large, so they are not included in the repository, but they can be generated by invoking the script `read_data.py`.

After generating the synthetic user data, the script [`simulate_randomization_and_plot.py`](https://github.com/presto-osu/icsme20/blob/master/code/sootOutput/simulate_randomization_and_plot.py) can be invoked to read the data, create randomized data from is, running the simulation 30 times and plotting the results. The charts used in the paper are in the [figs](https://github.com/presto-osu/icsme20/tree/master/code/sootOutput/figs) directory.
