name=nate

server:
	java -cp target/Lab3-ZeroMQ-1.0-SNAPSHOT.jar -Djava.rmi.server.useCodebaseOnly=false -Djava.security.policy=policy -Dexec.mainClass=engine.ServiceEngine engine.ServiceEngine

serverMvn:
	mvn exec:java -Djava.rmi.server.useCodebaseOnly=false -Djava.security.policy=policy -Dexec.mainClass=engine.ServiceEngine

client:
	java -cp target/Lab3-ZeroMQ-1.0-SNAPSHOT.jar -Djava.rmi.server.useCodebaseOnly=false -Djava.security.policy=policy -Dexec.mainClass=client.ChatClient client.ChatClient ${name}

clientMvn:
	mvn exec:java -Djava.rmi.server.useCodebaseOnly=false -Djava.security.policy=policy -Dexec.mainClass=client.ChatClient -Dexec.args=${name}
