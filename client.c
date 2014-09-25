// man bind

#include <sys/socket.h>
#include <sys/un.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define MY_SOCK_PATH "/data/local/tmp/ShellPhoneSocket"
#define LISTEN_BACKLOG 50
#define BUF_SIZE 500
#define handle_error(msg)				\
  do { perror(msg); exit(EXIT_FAILURE); } while (0)

int
main(int argc, char *argv[])
{
  int sfd, cfd;
  struct sockaddr_un my_addr, peer_addr;
  socklen_t peer_addr_size;
  char buf[BUF_SIZE];

  if ( argc < 2 ) {
	fprintf(stderr, "Usage: %s number\n",argv[0]);
	exit(EXIT_FAILURE);
  }
  sfd = socket(AF_LOCAL, SOCK_STREAM, 0);
  if (sfd == -1)
    handle_error("socket");

  memset(&my_addr, 0, sizeof(struct sockaddr));
  /* Clear structure */
  my_addr.sun_family = AF_LOCAL;
  strncpy(my_addr.sun_path, MY_SOCK_PATH,
	  sizeof(my_addr.sun_path) - 1);

  if (connect(sfd,(struct sockaddr *) &my_addr,sizeof(struct sockaddr)) == -1) {
    fprintf(stderr,"socket connect fail\n");
    exit(EXIT_FAILURE);
  }

  int len = strlen(argv[1])+1;
  if (len + 1 > BUF_SIZE) {
    fprintf(stderr,
	    "Ignoring long cmd %s\n", argv[1]);
    exit(EXIT_FAILURE);
  }
  printf("Writing %s\n",argv[1]);
  if (write(sfd, argv[1], len) != len) {
    fprintf(stderr, "partial/failed write\n");
    exit(EXIT_FAILURE);
  }
  printf("Reading ...\n");
  int nread = read(sfd, buf, BUF_SIZE);
  if (nread == -1) {
    perror("read");
    exit(EXIT_FAILURE);
  }
  printf("Received %ld bytes: %s\n", (long) nread, buf);
  return 0;
}
