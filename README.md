# AndroMevol
Automatically extract Android SDK apis and fields from android.jar

=================

# Quick Start
AndroMevol now takes 2 parameters to extract apis and fields and write to csv files
* androidJars: A path points to a directory containing a collection of Android jars, with each one represents each Android platform version.
* framework: A path points to a dex or jar files extracted from different devices.
In addition, directories res and frameworks locate under the same path as AndroidMevol.jar
# Example

```
java -jar AndroMevol.jar ./android-platforms ./frameworks/huawei/framework-29/framework.jar
```
