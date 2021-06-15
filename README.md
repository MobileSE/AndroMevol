# AndroMevol
Automatically extract Android SDK apis and fields from android.jar

=================

# Quick Start
AndroMevol now takes 5 different parameters to extract apis and fields and write to text files
* androidJarPath: The android.jar file that is used to extract.
* androidJars: A path points to a directory containing a collection of Android jars, with each one represents each Android platform version.
* apiLevel: A integer specify which level android.jar are you trying to extract.
* methodOutPath: A text file path storing the extracted methods.
* fieldOutPath: A text file path storing the extracted fields.

# Example

```
java -jar AndroMevol.jar ~/Android/sdk/platforms/android-23/android.jar android-platforms 23 ~/outs/android-23/android-apis.txt ~/out/android-23/android-fields.txt
```
