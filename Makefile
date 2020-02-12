agent: agent.c
	g++ agent.c -pthread -o agent

clean:
	rm -f agent
