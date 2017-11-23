#include <string.h>
#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

/******* http客户端程序 httpclient.c ************/
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <errno.h>
#include <unistd.h>
#include <netinet/in.h>
#include <limits.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <ctype.h>

#include <pthread.h>
#include <sys/time.h>

#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <string.h>

#define LOG_TAG "Download-JNI"

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)

int thirddownload_startTraffic = 0;
int thirddownload_endTraffic = 0;
int thirddownload_ave_download_netspeed = 0;
int thirddownload_time_duration = 5;
int thirddownload_threadnum = 2;
int thirddownload_cal_duration = 0;
int thirddownload_cal_time_interval = 250000;
//////////////////////////////////////////////////////label ren
long thirddownload_firstbyte_time=0;
long thirddownload_connect_time=0;
long thirddownload_download_time=0;
long thirddownload_parsetime=0;
long thirddownload_opentime=0;
char *IPAddr;


int thirddownload_oneThreadIsFinish = 0; //0未完成  1完成
int thirddownload_recv_started_flag = 0;

char* thirddownload_str = "www.baidu.com";

struct timeval thirddownload_startTime,thirddownload_endTime;


JNIEXPORT jint Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadstartFromJNI( JNIEnv* env,jobject thiz )
{
	thirddownload_startTest();
	return 1;
}

JNIEXPORT void Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadstopFromJNI( JNIEnv* env,jobject thiz )
{
	getThirdDownloadTotalTraffic();
}

JNIEXPORT int Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadgetAveSpeed( JNIEnv* env,jobject thiz )
{
	return thirddownload_ave_download_netspeed;
}

JNIEXPORT void Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadsetServer( JNIEnv* env, jobject thiz, jstring serverAddress)
{
	thirddownload_str = (char*)(*env)->GetStringUTFChars(env,serverAddress,NULL);
	LOGD("-----%s",thirddownload_str);
}

JNIEXPORT void Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadsetDuration( JNIEnv* env, jobject thiz, jint duration)
{
	thirddownload_time_duration = duration;
	LOGD("-----%d",thirddownload_time_duration);
}

JNIEXPORT void Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadsetThreadNum( JNIEnv* env, jobject thiz, jint num)
{
	thirddownload_threadnum = num;
	LOGD("threadnum-%d",thirddownload_threadnum);
}

JNIEXPORT int Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadgetTestState( JNIEnv* env, jobject thiz)
{
	if(thirddownload_oneThreadIsFinish==0){
       return 0;
	}else{
       return 1;
	}
}


///////////////////////////////////////////////////////////////////
JNIEXPORT jlong Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadFirstByteTimeFromJNI( JNIEnv* env,jobject thiz )
{
	return thirddownload_firstbyte_time;
}
JNIEXPORT jlong Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadConnectTimeFromJNI( JNIEnv* env,jobject thiz )
{
	return thirddownload_connect_time;
}
JNIEXPORT jlong Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadDownloadTimeFromJNI( JNIEnv* env,jobject thiz )
{
	return thirddownload_download_time;
}


JNIEXPORT jstring Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadIPAddrFromJNI( JNIEnv* env,jobject thiz )
{
	return (*env)->NewStringUTF(env, IPAddr);
}

JNIEXPORT jlong Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadParseTimeFromJNI( JNIEnv* env,jobject thiz )
{
	return thirddownload_parsetime;
}
JNIEXPORT jlong Java_edu_bupt_nettest_ThirdDownload_ThirdDownloadOpenTimeFromJNI( JNIEnv* env,jobject thiz )
{
	return thirddownload_opentime;
}
/////////////////////////////////////
/********************************************
功能：搜索字符串右边起的第一个匹配字符
********************************************/
char * thirddownload_Rstrchr(char * s, char x)  {
  int i = strlen(s);
  if(!(*s))  return 0;
  while(s[i-1]) if(strchr(s + (i - 1), x))  return (s + (i - 1));  else i--;
  return 0;
}

/**************************************************************
功能：从字符串src中分析出网站地址和端口，并得到用户要下载的文件
***************************************************************/
void thirddownload_GetHost(char * src, char * web, char * file, int * port)  {
  char * pA;
  char * pB;
  memset(web, 0, sizeof(web));
  memset(file, 0, sizeof(file));
  *port = 0;
  if(!(*src))  return;
  pA = src;
  if(!strncmp(pA, "http://", strlen("http://")))  pA = src+strlen("http://");
  else if(!strncmp(pA, "https://", strlen("https://")))  pA = src+strlen("https://");
  pB = strchr(pA, '/');
  if(pB)  {
    memcpy(web, pA, strlen(pA) - strlen(pB));
    if(pB+1)  {
      memcpy(file, pB + 1, strlen(pB) - 1);
      file[strlen(pB) - 1] = 0;
    }
  }
  else  memcpy(web, pA, strlen(pA));
  if(pB)  web[strlen(pA) - strlen(pB)] = 0;
  else  web[strlen(pA)] = 0;
  pA = strchr(web, ':');
  if(pA)  *port = atoi(pA + 1);
  else *port = 80;
}

static struct itimerval oldtv;

void thirddownload_set_timer()
{
    struct itimerval itv;
    itv.it_interval.tv_sec = 0;
    itv.it_interval.tv_usec = thirddownload_cal_time_interval;
    itv.it_value.tv_sec = 0;
    itv.it_value.tv_usec = 250000;
    setitimer(ITIMER_REAL, &itv, &oldtv);
}

void thirddownload_signal_handler(int m)
{
	thirddownload_cal_duration +=1;
	int kill = thirddownload_cal_time_interval*thirddownload_cal_duration/1000000;
	int current_thirddownload_Traffic = getThirdDownloadTotalTraffic()-thirddownload_startTraffic;
	if(thirddownload_oneThreadIsFinish==1||kill>thirddownload_time_duration||current_thirddownload_Traffic>400000){
		thirddownload_oneThreadIsFinish=1;
    	alarm(0);
    	LOGD("thirddownload_timeout");
    }
}


int thirddownload_trafficLabel = 0;
int thirddownload_download()
{

  int sockfd;
  char buffer[1024];
  struct sockaddr_in server_addr;
  struct hostent *host;
  int portnumber,nbytes;
  char host_addr[256];
  char host_file[1024];
  char local_file[256];
  FILE * fp;
  char request[1024];
  int send, totalsend;
  int i;
  char * pt;
  char *argv[256];
  int firstbyte_flag=0;

  struct timeval dns_parse_start,dns_parse_end,start_time_stamp,firstbyte_time_stamp,firstbyte_time_start_stamp,connect_time_stamp;/////label ren

  argv[0]= thirddownload_str;

  thirddownload_GetHost(argv[0], host_addr, host_file, &portnumber);/*分析网址、端口、文件名等*/

  LOGD("webhost:%s\n", host_addr);
  LOGD("hostfile:%s\n", host_file);
  LOGD("portnumber:%d\n\n", portnumber);

  gettimeofday(&dns_parse_start, NULL);
  if((host=gethostbyname(host_addr))==NULL)/*取得主机IP地址*/ //这是一个DNS过程
  {
    LOGD("Gethostname error, %s\n", strerror(errno));
    //exit(1);
  }else{
	    /* 客户程序开始建立 sockfd描述符 */

	    if((sockfd=socket(AF_INET,SOCK_STREAM,0))==-1)/*建立SOCKET连接*/
	    {
	  	  LOGD("Socket Error:%s\a\n",strerror(errno));
	    }
	    /* 客户程序填充服务端的资料 */
	    bzero(&server_addr,sizeof(server_addr));
	    server_addr.sin_family=AF_INET;
	    server_addr.sin_port=htons(portnumber);
	    server_addr.sin_addr=*((struct in_addr *)host->h_addr);
  }
  gettimeofday(&dns_parse_end, NULL);

  IPAddr = inet_ntoa(*((struct in_addr *)host->h_addr));   //////////////
  gettimeofday(&start_time_stamp, NULL);
  LOGD("start time stamp %ld\n",start_time_stamp.tv_usec);
  /* 客户程序发起连接请求 */
  if(connect(sockfd,(struct sockaddr *)(&server_addr),sizeof(struct sockaddr))==-1)/*连接网站*/ /////////////////////label ren
  {
	  LOGD("Connect Error:%s\a\n",strerror(errno));
  }
  gettimeofday(&connect_time_stamp, NULL);
  LOGD("connect time stamp is %ld\n",connect_time_stamp.tv_usec);

  sprintf(request, "GET /%s HTTP/1.1\r\nAccept: */*\r\nAccept-Language: zh-cn\r\n\
User-Agent: Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n\
Host: %s:%d\r\nConnection: Close\r\n\r\n", host_file, host_addr, portnumber);
  //LOGD("%s", request);/*准备request，将要发送给主机*/

  /*取得真实的文件名*/
  if(host_file && *host_file)  pt = thirddownload_Rstrchr(host_file, '/');
  else pt = 0;
  memset(local_file, 0, sizeof(local_file));
  if(pt && *pt)  {
    if((pt + 1) && *(pt+1))  strcpy(local_file, pt + 1);
    else  memcpy(local_file, host_file, strlen(host_file) - 1);
  }
  else if(host_file && *host_file)  strcpy(local_file, host_file);
  else  strcpy(local_file, "index.html");
  //LOGD("local filename to write:%s\n\n", local_file);

  /*发送http请求request*/
  send = 0;
  totalsend = 0;
  nbytes=strlen(request);

  gettimeofday(&firstbyte_time_start_stamp, NULL);
  while(totalsend < nbytes) { //////////////网络通中首字节标志处 ren
    send = write(sockfd, request + totalsend, nbytes - totalsend);
    if(send==-1)  {
    	LOGD("send=-1");
    	break;
    }
    totalsend+=send;
  }

  if((nbytes=read(sockfd,buffer,1))>0){
	  gettimeofday(&firstbyte_time_stamp, NULL);
	  firstbyte_flag=1;
	  LOGD("first byte time is %ld\n",firstbyte_time_stamp.tv_usec);
  }
  /* 连接成功了，接收http响应，response */
  while((nbytes=read(sockfd,buffer,1000))>0&&thirddownload_oneThreadIsFinish==0){
	  if (thirddownload_recv_started_flag == 0) {
		  gettimeofday(&firstbyte_time_stamp, NULL);
		    thirddownload_recv_started_flag = 1;
	  		thirddownload_startTraffic = getThirdDownloadTotalTraffic();
	  		gettimeofday(&thirddownload_startTime, NULL);
	  		signal(SIGALRM, thirddownload_signal_handler);
	  		thirddownload_set_timer();
	  		LOGD("third download start time stamp is %ld and the traffic is %d\n",thirddownload_startTime.tv_usec,thirddownload_startTraffic);
	  }
  }

  thirddownload_oneThreadIsFinish = 1;
  if(thirddownload_trafficLabel==0){
	  thirddownload_trafficLabel = 1;
	  thirddownload_endTraffic = getThirdDownloadTotalTraffic();
	  gettimeofday(&thirddownload_endTime, NULL);
	  int timeuse = 1000000 * ( thirddownload_endTime.tv_sec - thirddownload_startTime.tv_sec ) + thirddownload_endTime.tv_usec - thirddownload_startTime.tv_usec;  //微秒


	  thirddownload_connect_time = 1000000 * ( connect_time_stamp.tv_sec - start_time_stamp.tv_sec ) + connect_time_stamp.tv_usec - start_time_stamp.tv_usec;
	  if(firstbyte_flag==1)
	  {
		  thirddownload_firstbyte_time = 1000000 * ( firstbyte_time_stamp.tv_sec - firstbyte_time_start_stamp.tv_sec ) + firstbyte_time_stamp.tv_usec - firstbyte_time_start_stamp.tv_usec;
	  }else {
		  thirddownload_firstbyte_time = 1000000 * ( thirddownload_startTime.tv_sec - firstbyte_time_start_stamp.tv_sec ) + thirddownload_startTime.tv_usec - firstbyte_time_start_stamp.tv_usec;
	  }
	  thirddownload_parsetime = 1000000 * ( dns_parse_end.tv_sec - dns_parse_start.tv_sec ) + dns_parse_end.tv_usec - dns_parse_start.tv_usec;
	  thirddownload_opentime = 1000000 * ( thirddownload_endTime.tv_sec - dns_parse_start.tv_sec ) + thirddownload_endTime.tv_usec - dns_parse_start.tv_usec;

//	  thirddownload_connect_time /=1000;
//	  thirddownload_firstbyte_time /=1000;
//	  thirddownload_parsetime /=1000;
//	  thirddownload_opentime /=1000;
	  thirddownload_download_time = timeuse;
	  timeuse /=1000;


	  if(timeuse>0){
	  thirddownload_ave_download_netspeed = (thirddownload_endTraffic-thirddownload_startTraffic)/timeuse;
	  }else if (timeuse == 0){
		  thirddownload_ave_download_netspeed = 1000*((thirddownload_endTraffic-thirddownload_startTraffic)/thirddownload_download_time);
	  }else{
		  thirddownload_ave_download_netspeed = 0;
	  }
	  LOGD("finish-%d-%d-%d",thirddownload_endTraffic-thirddownload_startTraffic,timeuse,thirddownload_ave_download_netspeed);
  }
  LOGD("thread is finish");
  close(sockfd);
  return 1;
}

long thirddownload_readNumber(char const* filename) {
    char buf[80];
    int fd = open(filename, O_RDONLY);
    if (fd < 0) {
        if (errno != ENOENT) LOGD("Can't open %s: %s", filename, strerror(errno));
        return -1;
    }

    int len = read(fd, buf, sizeof(buf) - 1);
    if (len < 0) {
        LOGD("Can't read %s: %s", filename, strerror(errno));
        close(fd);
        return -1;
    }

    close(fd);
    buf[len] = '\0';
    return atoll(buf);
}

long thirddownload_readTotal(char const* suffix) {
    char filename[PATH_MAX] = "/sys/class/net/";
//    if(access("/sys/class/net/",F_OK)==0){
//        	LOGD("OK");
//    }
    DIR *dir = opendir(filename);
    //LOGD("-2");
    if (dir == NULL) {
        LOGD("Can't list %s: %s", filename, strerror(errno));
        return -1;
    }
    //LOGD("-1");
    int len = strlen(filename);
    //LOGD("len-%d",len);
    jlong total = -1;
    struct dirent *entry;
    while (entry = readdir(dir)) {
        // Skip ., .., and localhost interfaces.
    	//LOGD("0");
        if (entry->d_name[0] != '.' && strncmp(entry->d_name, "lo", 2) != 0) {
            strlcpy(filename + len, entry->d_name, sizeof(filename) - len);
            //LOGD("1");
            strlcat(filename, suffix, sizeof(filename));
            //LOGD("2");
            jlong num = thirddownload_readNumber(filename);
//            LOGD("3");
//            LOGD("the num is %d\n",num);
            if (num >= 0) total = total < 0 ? num : total + num;
        }
        //LOGD("4");
    }
    //LOGD("5");
    closedir(dir);
//    LOGD("6  is %d\n",total);
    return total;
}

int getThirdDownloadTotalTraffic() {

	return thirddownload_readTotal("/statistics/rx_bytes");
}


void *thirddownload_thread_func()
{
	thirddownload_download();
}

int thirddownload_startTest()
{
	thirddownload_oneThreadIsFinish = 0; // no thread is finished
	thirddownload_cal_duration = 0;
	thirddownload_trafficLabel=0;

    thirddownload_recv_started_flag = 0;

	pthread_t thread[thirddownload_threadnum-1];
	int temp;
	memset(&thread, 0, sizeof(thread));

	int k;
	for(k = 0;k<thirddownload_threadnum;k++){
		if((temp = pthread_create(&thread[k], NULL, thirddownload_thread_func, NULL)) != 0){
			LOGD("create thread error\n");
		}else{
			LOGD("thread%d created\n",k+1);
		}
	}

	void* status;
	int j;
    for(j=0;j<thirddownload_threadnum;j++){
    	pthread_join(thread[j],&status);
    }

    LOGD("third test is finished!/n");
    return 1;
}



