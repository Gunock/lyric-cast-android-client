# lyric-cast-android-client

## Versioning
Version numbers are generated using this formula:
`major.minor.patch-(optional)alpha/beta number`
* major - bumped with every significant version
* minor - bumped with every new feature(s).
* patch - fix updates.
* alpha/beta number - no commenct.

Examples:
* 1.0.0
* 0.1.0-a3
* 0.1.1-b1

## Project structure
This project consists of modules:
* app - LyricCast app:
    * application - application related classes.
    * di - dependency injection related classes.
    * domain - domain specific classes.
    * shared - extensions, google cast, etc.
    * ui - view segregated by features (feature based structure)
* common - it's in the name.
* dataModel - repositories, data structures
* dataTransfer - format converters (app-json, app-xml, etc)
