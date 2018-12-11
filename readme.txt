Super22 creates a simple Desktop application from Java Web Applicaton.

The input is a war file. The build with the Ant file "build-super22.xml and a given 
property file will create a executable jar file.
When the jar file is run it will extract a version of jetty-runner and the war 
file and start jetty-runner with this war file as argument.

So build the jar file like so
ant -f build-super22.xml -propertyfile super22-props.properties 

In this case the contents of the property file are:
>>>>>
targetjar=some-application.jar
warfile=some-webapp.war
runner=jetty-runner-9.4.9.v20180320.jar
port=7777
<<<<<<

The build will create the jar file some-application.jar. 
When this jar file is run, the web application will be startet, listening
on 127.0.0.1:7777. 
The system web browser will be startet and open the page http://localhost:7777.


The Netbeans stuff is only used for editing.
