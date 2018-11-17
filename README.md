# CIS656-HW3-RMI-ZeroMQ
Client/server chat application with ZeroMQ and JavaRMI communication

## Design
Building on the previous client/server setup, but P2P client messages are now handled with ZeroMQ, a high-performance asynchronous messaging library to simplify communication in distributed systems by managing socket pairs for you. Standard REQ/REP communication is used between clients, and broadcasts are handled through a SUB/PUB handled by the server. Registration is still handled via Java RMi.
