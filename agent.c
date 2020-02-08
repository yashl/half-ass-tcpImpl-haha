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

int main(int argc, char *argv[])
{
  srand(time(NULL)); //seeding rand for every binary instance

  BEACON agent = constructBeacon();
  
  sendUDP(agent); //start UDP side

  
  return 0;
}

void sendUDP(BEACON agent)
{
  int sockfd;
  char buffer[1024];
  char *hello = "asdajd";
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

  send(sockfd, (const char *)hello, strlen(hello), 0);
  //send(sockfd, (void *)agent, sizeof(agent), 0);  
  printf("Packet Sent.\n");
  close(sockfd);
}

BEACON constructBeacon()
{
  BEACON agentB;
  agentB.ID = (rand() % 9999) + 1; //ID number is randomized
  printf("%d\n", agentB.ID);
  agentB.startUpTime = getCurTime();
  agentB.timeInterval = 30; //harcoded to 30 seconds
  char* temp = getCurIP();
  sscanf(temp, "%d.%d.%d.%d\n", &agentB.IP[0], &agentB.IP[1], 
          &agentB.IP[2], &agentB.IP[3]);
  agentB.cmdPort = (rand() % 8975) + 1024; //random number between 1024 and 9999
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

  int cur_time = (timeinfo.tm_hour * 100) + timeinfo.tm_min;
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
