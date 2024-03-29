[![MIT License][image-1]][1]
[![DeepSource][image-2]][2]
[![Qodana][image-3]][3]
[![Build and Test][image-4]][4]

# Javions

**Javions** is an open-source _ADS-B messages demodulator_. It was coded as a first-year project in computer science at **EPFL**. The instruction set along with the main course resources is available [here][5]. After the conclusion of the 2022-2023 academic year, it will instead be available [here][6].
The instruction set provided by our instructor, [Michel Schinz][7], was grounded on the book [The 1090 Megahertz Riddle][8] authored by [Junzi Sun][9].

![Preview of the GUI][image-5]
<center>Figure 1: Final graphic interface of Javions</center>

### Main contributors
> - [@franklintra][10]
> - [@chukla][11]


## Getting started
This project is designed to function with JavaFX version 20.0.1 and Java 17, ensuring that the preview feature is enabled. (`--enable-preview`)

If you don't have Java 17 installed on your computer, please visit [Adoptium][12], and download the installer corresponding to your operating system (macOS, Linux ou Windows).
For further help installing JavaFX, please follow this [guide][13]. 

## Usage Guidelines
### Viewing Pre-Demodulated and Stored Messages in GUI
If the main class is executed with a single additional argument - the file path for the saved messages, the program will activate the GUI, decoding the saved messages in real time in accordance with the timeStampNs.
Example:
```Bash
java --enable-preview -cp out/production/Javions/ --module-path ${JFX_PATH?} --add-modules javafx.controls ch.epfl.javions.gui.Main resources/messages_20230318_0915.bin
```


### Real-Time Radio Message Decoding

By running the main class without any additional arguments, the GUI (Graphic User Interface) will launch, enabling real-time decoding of radio messages that are received via the System.In input port. 

To retrieve messages from the radio, you will first need to install and configure the [driver][14]. On MacOS, for example, you could use the `brew install airspy` command. 

Once you've completed this step, simply connect your radio. To start the program and begin decoding incoming messages, run the following command:

```Bash
airspy_rx -r - -f 1090 -t 5 -g 17 | cat samples_20230304_1442.bin | java --enable-preview -cp out/production/Javions/ --module-path ${JFX_PATH?} --add-modules javafx.controls ch.epfl.javions.gui.Main
```

> This command will initiate the program and demodulate the messages as they are received.

## Test Procedures
The project was thoroughly tested throughout its development using [JUnit5][15]. All tests can be executed using Maven with the following command:
```Bash
mvn clean test
```
> This command will run all configured tests (should take about 2 to 3 seconds depending on your hardware). The tests are most thorough for the first part of the project (week 1 to 6 aka messages demodulation).

## Additional informations
We will try to create and support two images of this app. A native image for all major platforms and a jar containing all necessary libraries. This is a work in progress and you will be notified as soon as it is available.


# Contribution

You are very welcome to contribute to the project. If you wish to do so, please fork the project, make your changes, and then make a pull request which will be examined by [@franklintra][16] as soon as possible.
Welcome changes are any useful changes which can make the user experience nicer and / or bug fixes.

### Licence
This project is distributed under the MIT licence.


# Main class
## Logic
![Main class logic diagram][image-6]
<center>The logic diagram for the main class of the program. </center>

For better performance, and non-blocking demodulation, the demodulation of the messages is not done on the main JavaFX thread. Instead it is done on a separate thread and the decoded messages are given to a queue which is then polled by the JavaFX thread.
- [ ] Further improvements: Make the TileManager asynchronous by returning empty Images as long as the tile hasn't been loaded from either the memory, drive or server. This could be implemented as a bonus for week 12 of the project.

[1]:	https://choosealicense.com/licenses/mit/
[2]:	https://deepsource.io/gh/franklintra/Javions/?ref=repository-badge
[3]:	https://github.com/franklintra/Javions/actions/workflows/Qodana_quality_tests.yml
[4]:	https://github.com/franklintra/Javions/actions/workflows/build-and-test.yml
[5]:	https://cs108.epfl.ch
[6]:	https://cs108.epfl.ch/archive/23
[7]:	https://people.epfl.ch/michel.schinz
[8]:	https://mode-s.org/decode
[9]:	https://junzis.com/
[10]:	https://www.github.com/franklintra
[11]:	https://www.github.com/chukla
[12]:	https://adoptium.net/
[13]:	https://cs108.epfl.ch/g/openjfx.html
[14]:	https://github.com/airspy/airspyone_host
[15]:	https://junit.org
[16]:	https://www.github.com/franklintra

[image-1]:	https://img.shields.io/badge/License-MIT-green.svg
[image-2]:	https://deepsource.io/gh/franklintra/Javions.svg/?label=resolved+issues&show_trend=false&token=CmvAJnWex2qCynvmZiepgXiK
[image-3]:	https://github.com/franklintra/Javions/actions/workflows/Qodana_quality_tests.yml/badge.svg
[image-4]:	https://github.com/franklintra/Javions/actions/workflows/build-and-test.yml/badge.svg
[image-5]:	https://cs108.epfl.ch/p/i/javions-final;64.png
[image-6]:	https://showme.redstarplugin.com/s/ejxsYwHw