https://github.com/zeromq/jeromq

Installed via Homebrew

5 minute guide:
https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html


To create a Maven project:
	mvn archetype:generate -DgroupId=com.group.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

	mvn package

	java -cp target/my-app-1.0-SNAPSHOT.jar com.group.app.App


To implement ZeroMQ:
Add dependency to Maven pom.xml:
	<dependency>
      <groupId>org.zeromq</groupId>
      <artifactId>jeromq</artifactId>
      <version>0.4.3</version>
    </dependency>


For this project specifically
Run server:
	mvn exec:java -Djava.rmi.server.useCodebaseOnly=false \ -Djava.security.policy=policy \ -Dexec.mainClass=engine.ServiceEngine

Run client:
	mvn exec:java -Djava.rmi.server.useCodebaseOnly=false \ -Djava.security.policy=policy \ -Dexec.mainClass=client.ChatClient -Dexec.args=nate
