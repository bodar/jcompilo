<h1><img src="https://raw.githubusercontent.com/wiki/bodar/jcompilo/logo.png" align="bottom"/> JCompilo</h1>

A pure Java 7 (Version 1) / Java 8 (Version 2) build tool with advanced compiler features including:

  * Extremely fast (faster than all other Java build tools by between 20%-80%)  
  * Zero copy Jar creation (Jars are faster to process than .class files in directories - for unit test cycles too)
  * Compatible with Junit test cycles
  * [Pack200](https://en.wikipedia.org/wiki/Pack200) optimizations (10 x faster downloads)    
    * Parallel downloads    
  * No transitive dependencies, itself
Optional Features:

  * Post-processing of output bytecode
  * Tail-recursion call optimisation (via [TotallyLazy](https://github.com/bodar/totallylazy/blob/master/src/com/googlecode/totallylazy/annotations/tailrec.java) or system property "jcompilo.tailrec=your.annotation.Name")
  * Facilitates dependency resolution via [shavenmaven](http://code.google.com/p/shavenmaven/) which gives us the following features
  * Supports 100% convention (no build file needed) or customisation in Java code

# Standard Convention #

The following shows the default folder structure for a JCompilo project. This will be very familiar to old school open source developers.

  * jcompilo.sh
  * Build.java (optional)
  * build (optional)
    * build.dependencies (optional) [see ShavenMaven](https://github.com/bodar/shavenmaven)
    * runtime.dependencies (optional)
    * optional.dependencies (optional)
  * src
    * META-INF (optional)
      * MANIFEST.MF (optional)
    * com
      * example
        * HelloWorld.java
        * SomeResource.txt
  * test
    * com
      * example
        * HelloWorldTest.java

Optional support for Maven project layout (src/main/java, src/test/java) too.

# Latest Releases and Repo #
http://repo.bodar.com/com/googlecode/jcompilo/jcompilo/

# Contributing

Pull requests welcome.

# TODO / Future exploration #

  * Full Maven/ant compatibility (maven-compiler-plugin, maven-surefire-plugin)
  * TestNG compatibility
  * Run tests while compiling
  * Even more compact console output
  * Remove JUnit/Hamcrest dependency
  * REPL
  * Use REPL as build server from IntelliJ
