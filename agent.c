#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>
#include <string.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <ifaddrs.h>
#include <netdb.h>

#define PORT 7000 //server port

struct BEACON
{
  int ID; //randomized when agent starts
  int startUpTime; //initialized when agent starts
  int timeInterval; //harcoded while constructing beacon in seconds
  char IP[4]; //intialized when agent starts
  int cmdPort; //random number between 1024 and 9999 if port open
};

BEACON constructBeacon();
int getCurTime();
char* getCurIP();
void sendUDP(BEACON agent);
char* prepareBeacon(BEACON agent);

int main(int argc, char *argv[])
{
  BEACON agent = constructBeacon();
  
  while(1)
  {
    sendUDP(agent);
    printf("Agent Sent!\n\n");
    sleep(agent.timeInterval);
  }

  return 0;
}

void sendUDP(BEACON agent)
{
  int sockfd;
  char buffer[1024];
  struct sockaddr_in servaddr;

  if((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0 ) 
  { 
    perror("socket creation failed"); 
    exit(EXIT_FAILURE); 
  }

  memset(&servaddr, 0, sizeof(servaddr));
  servaddr.sin_family = AF_INET; 
  servaddr.sin_port = htons(PORT); 
  servaddr.sin_addr.s_addr = htonl(INADDR_ANY);

  connect(sockfd, (struct sockaddr *) &servaddr, sizeof(servaddr));
  
  int bytes_sent  = 0;
  char *temp = prepareBeacon(agent);
  bytes_sent = send(sockfd, (char *)temp, 26, 0);
  if(bytes_sent < 0) 
    printf("Error!\n");
  else
    printf("Total Bytes sent = %d\n", bytes_sent);

  close(sockfd);
}

char* prepareBeacon(BEACON agent)
{
  char* buffer = new char[1024];
  
  char ID[6];
  snprintf(ID, 6, "%d", agent.ID);
  
  char startUpTime[6];
  snprintf(startUpTime, 6, "%d", agent.startUpTime);

  char timeInterval[6];
  snprintf(timeInterval, 6, "%d", agent.timeInterval);

  char cmdPort[6];
  snprintf(cmdPort, 6, "%d", agent.cmdPort);

  snprintf(buffer, 26, "%s,%s,%s,%d.%d.%d.%d,%s", ID, 
    startUpTime, timeInterval, agent.IP[0], agent.IP[1], 
    agent.IP[2], agent.IP[3], cmdPort);
  printf("Final Buffer : %s", buffer);
  return buffer;
}

BEACON constructBeacon()
{
  BEACON agentB;
  agentB.ID = (rand() % 9999) + 1; 
  printf("Agent ID: %d\n", agentB.ID);
  agentB.startUpTime = getCurTime();
  printf("Current Time: %d\n", agentB.startUpTime);
  agentB.timeInterval = 4; //harcoded to 30 seconds
  char* temp = getCurIP();
  sscanf(temp, "%d.%d.%d.%d\n", &agentB.IP[0], &agentB.IP[1], 
          &agentB.IP[2], &agentB.IP[3]);
  agentB.cmdPort = (rand() % 8975) + 1024; //between 1024 and 9999
  return agentB;
}

/**
 * Returns current local time
 * Format of return time is 24 hour clock
 * */
int getCurTime()
{
  time_t t = time(NULL);
  struct tm timeinfo = *localtime(&t);

  int cur_time = (timeinfo.tm_min*100) + (timeinfo.tm_sec);
  return cur_time;
}

/**
 * Returns Public IPv4 address
 */
char* getCurIP()
{
  char hostbuffer[256]; 
  char *IPbuffer; 
  struct hostent *host_entry; 
  int hostname;

  hostname = gethostname(hostbuffer, sizeof(hostbuffer));
  host_entry = gethostbyname(hostbuffer);
  IPbuffer = inet_ntoa(*((struct in_addr*)host_entry->h_addr_list[0]));
  return IPbuffer;
}
