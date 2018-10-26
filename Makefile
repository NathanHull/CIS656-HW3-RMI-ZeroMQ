name=nate

server:
	mvn exec:java -Djava.rmi.server.useCodebaseOnly=false -Djava.security.policy=policy -Dexec.mainClass=engine.ServiceEngine

client:
	mvn exec:java -Djava.rmi.server.useCodebaseOnly=false -Djava.security.policy=policy -Dexec.mainClass=client.ChatClient -Dexec.args=${name}
