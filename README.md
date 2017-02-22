Vaadin Tutorial application
==============

**This repository contains the source code of the application built in [Vaadin Framework tutorial](http://vaadin.com/tutorial).**

If you are new to Maven, read [these instructions](https://vaadin.com/blog/-/blogs/the-maven-essentials-for-the-impatient-developer) to get your project properly imported into Eclipse or your IDE of choice.

If you want to start the tutorial from a specific step, use the following links to checkout the starting point:

 1. Overview (no source code available in this step).
 2. Creating a project using an archetype (no source code available before this step).
 3. [Adding a demo "backend"](https://github.com/vaadin/tutorial/tree/v8-step2)
 4. [Listing entities in a Grid](https://github.com/vaadin/tutorial/tree/v8-step3)
 5. [Creating live filtering for entities](https://github.com/vaadin/tutorial/tree/v8-step4)
 6. [Creating a form to edit Customer objects](https://github.com/vaadin/tutorial/tree/v8-step5)
 7. [Connecting the form to the application](https://github.com/vaadin/tutorial/tree/v8-step6)

Below are some instructions how to work with basic Vaadin application.

Workflow
========

To compile the entire project, run "mvn install".

To run the application, run "mvn jetty:run" and open http://localhost:8080/ .

Debugging client side code
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application

To produce a deployable production mode WAR:
- change productionMode to true in the servlet class configuration (nested in the UI class)
- run "mvn clean package"
- test the war file with "mvn jetty:run-war"

Developing a theme using the runtime compiler
-------------------------

When developing the theme, Vaadin can be configured to compile the SASS based
theme at runtime in the server. This way you can just modify the scss files in
your IDE and reload the browser to see changes.

To use on the runtime compilation, open pom.xml and comment out the compile-theme
goal from vaadin-maven-plugin configuration. To remove a possibly existing
pre-compiled theme, run "mvn clean package" once.

When using the runtime compiler, running the application in the "run" mode
(rather than in "debug" mode) can speed up consecutive theme compilations
significantly.

It is highly recommended to disable runtime compilation for production WAR files.

Using Vaadin pre-releases
-------------------------

If Vaadin pre-releases are not enabled by default, use the Maven parameter
"-P vaadin-prerelease" or change the activation default value of the profile in pom.xml .
