#include <string.h>
#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

/******* http�ͻ��˳��� httpclient.c ************/
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
#include <signal.h>

#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

#define LOG_TAG "Download-JNI"

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)

int startTraffic = 0;
int endTraffic = 0;
int lastTraffic = 0;
int current_download_netspeed = 0;
int ave_download_netspeed = 0;
int max_download_netspeed = 0;
int time_duration = 10;
int threadnum = 6;
int recv_started_flag = 0;
int oneThreadIsFinish = 0; //0δ���  1���
int cal_time_interval = 500000; // 20ms
int cal_zero_num = 0;
int cal_duration = 0;

int currentTraffic = 0;

int rx_dropped = 0; //������
double rx_dropped_ratio = 0.0; //������
int start_rx_dropped = 0;//sv
int end_rx_dropped = 0;
int start_rx_packet = 0;
int end_rx_packet = 0;

int is_get_lastTraffic = 0;
int download_total_rx = 0;
int file_last_total_rx = 0;

int Download_time_interval = 250000;
int Download_timer_count = 0;
int Download_traffic_duration = 0;

char* str = "buptant.cn/UNOTest/speedtest/random350x350.jpg";
char* str0 = "buptant.cn/UNOTest/speedtest/random1500x1500.jpg";
char* str1 = "xugang.host033.youdnser.com/UNOTest/speedtest/random1500x1500.jpg";
//FILE *fp;

struct timeval startTime, endTime;

int file_download_size[6];
int server_one_avespeed = 0;
int server_two_avespeed = 0;
int nowTraffic = 0;
int nowFileTraffic = 0;
//int write_log = 0;

JNIEXPORT jint Java_edu_bupt_nettest_Download_startFromJNI(JNIEnv* env,
		jobject thiz) {
	startTest();
	return 1;
}

JNIEXPORT void Java_edu_bupt_nettest_Download_stopFromJNI(JNIEnv* env,
		jobject thiz) {
	oneThreadIsFinish = 1;
}

JNIEXPORT int Java_edu_bupt_nettest_Download_getCurrentSpeed(JNIEnv* env,
		jobject thiz) {
	return current_download_netspeed;
}

JNIEXPORT int Java_edu_bupt_nettest_Download_getAveSpeed(JNIEnv* env,
		jobject thiz) {
	return ave_download_netspeed;
}

JNIEXPORT int Java_edu_bupt_nettest_Download_getServerOneAveSpeed(JNIEnv* env,
		jobject thiz) {
	return server_one_avespeed;
}

JNIEXPORT int Java_edu_bupt_nettest_Download_getServerTwoAveSpeed(JNIEnv* env,
		jobject thiz) {
	return server_two_avespeed;
}

JNIEXPORT int Java_edu_bupt_nettest_Download_getMaxSpeed(JNIEnv* env,
		jobject thiz) {
	return max_download_netspeed;
}

JNIEXPORT void Java_edu_bupt_nettest_Download_setServer(JNIEnv* env,
		jobject thiz, jstring serverAddress) {
	str = (char*) (*env)->GetStringUTFChars(env, serverAddress, NULL);
	//LOGD("-----%s", str);
}

JNIEXPORT void Java_edu_bupt_nettest_Download_setMultiServer(JNIEnv* env,
		jobject thiz, jstring serverOne, jstring serverTwo) {
	str0 = (char*) (*env)->GetStringUTFChars(env, serverOne, NULL);
	str1 = (char*) (*env)->GetStringUTFChars(env, serverTwo, NULL);
	LOGD("---%s----%s", str0,str1);
}

JNIEXPORT void Java_edu_bupt_nettest_Download_setDuration(JNIEnv* env,
		jobject thiz, jint duration) {
	time_duration = duration;
	LOGD("-----%d", time_duration);
}

JNIEXPORT void Java_edu_bupt_nettest_Download_setThreadNum(JNIEnv* env,
		jobject thiz, jint num) {
	threadnum = num;
	LOGD("threadnum-%d", threadnum);
}

JNIEXPORT int Java_edu_bupt_nettest_Download_getTestState(JNIEnv* env,
		jobject thiz) {
	if (oneThreadIsFinish == 0) {
		return 0;
	} else {
		return 1;
	}
}

JNIEXPORT jdouble Java_edu_bupt_nettest_Download_getDropRatio(JNIEnv* env,
		jobject thiz) {
    return rx_dropped_ratio;
}

JNIEXPORT int Java_edu_bupt_nettest_Download_updateTraffic(JNIEnv* env,jobject thiz) {
	calNetSpeed();
}

JNIEXPORT void Java_edu_bupt_nettest_Download_setFrequency(JNIEnv* env,jobject thiz,jint frequency) {
	cal_time_interval = frequency*1000;  //cal_time_interval um
	LOGD("cal_time_ineterval = %d",cal_time_interval);
}
JNIEXPORT void Java_edu_bupt_nettest_Download_setTrafficDuration(JNIEnv* env,jobject thiz,jint trafficduration){
	Download_traffic_duration = trafficduration;  //KB
}

/********************************************
 ���ܣ������ַ��ұ���ĵ�һ��ƥ���ַ�
 ********************************************/
char * Rstrchr(char * s, char x) {
	int i = strlen(s);
	if (!(*s))
		return 0;
	while (s[i - 1])
		if (strchr(s + (i - 1), x))
			return (s + (i - 1));
		else
			i--;
	return 0;
}

/**************************************************************
 ���ܣ����ַ�src�з�������վ��ַ�Ͷ˿ڣ����õ��û�Ҫ���ص��ļ�
 ***************************************************************/
void GetHost(char * src, char * web, char * file, int * port) {
	char * pA;
	char * pB;
	memset(web, 0, sizeof(web));
	memset(file, 0, sizeof(file));
	*port = 0;
	if (!(*src))
		return;
	pA = src;
	if (!strncmp(pA, "http://", strlen("http://")))
		pA = src + strlen("http://");
	else if (!strncmp(pA, "https://", strlen("https://")))
		pA = src + strlen("https://");
	pB = strchr(pA, '/');
	if (pB) {
		memcpy(web, pA, strlen(pA) - strlen(pB));
		if (pB + 1) {
			memcpy(file, pB + 1, strlen(pB) - 1);
			file[strlen(pB) - 1] = 0;
		}
	} else
		memcpy(web, pA, strlen(pA));
	if (pB)
		web[strlen(pA) - strlen(pB)] = 0;
	else
		web[strlen(pA)] = 0;
	pA = strchr(web, ':');
	if (pA)
		*port = atoi(pA + 1);
	else
		*port = 80;
}

long readNumber(char const* filename){
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

long readTotal(char const* suffix) {
    char filename[PATH_MAX] = "/sys/class/net/";
//    LOGD("check double ok!");
    if(access("/sys/class/net/",F_OK)==0){
//        	LOGD("OK");
    }
    DIR *dir = opendir(filename);
//    LOGD("-2");
    if (dir == NULL) {
        LOGD("Can't list %s: %s", filename, strerror(errno));
        return -1;
    }
//    LOGD("-1");
    int len = strlen(filename);
//    LOGD("len-%d",len);
    long total = -1;
    struct dirent *entry;
    while (entry = readdir(dir)) {
        // Skip ., .., and localhost interfaces.
    	//LOGD("0");
        if (entry->d_name[0] != '.' && strncmp(entry->d_name, "lo", 2) != 0) {
            strlcpy(filename + len, entry->d_name, sizeof(filename) - len);
            //LOGD("1");
            strlcat(filename, suffix, sizeof(filename));
            //LOGD("2");
            jlong num = readNumber(filename);
            //LOGD("3");
            if (num >= 0) total = total < 0 ? num : total + num;
        }
        //LOGD("4");
    }
    closedir(dir);
//    LOGD("5 and total is %d",total);
    //LOGD("6");
    return total;
}

int getDownloadTotalTraffic() {
	return readTotal("/statistics/rx_bytes");
}

int getDownloadTotalPacket() {
	return readTotal("/statistics/rx_packets");
}

int getDownloadTotalPacketDropped() {
	return readTotal("/statistics/rx_dropped");
}

int calNetSpeed()
{
//	if(write_log == 0) write_log ++;
////	LOGD("download call net speed");
//	if(write_log == 1) {
////		fp = fopen("/sdcard/errorlog.txt","a+");
////		LOGD("write log == 1");
//	}

	if(recv_started_flag == 1){
		if(is_get_lastTraffic==0){
			is_get_lastTraffic = 1;
			lastTraffic = getDownloadTotalTraffic();
			file_last_total_rx = getFileTotalTraffic();
		}else{

			 nowTraffic = getDownloadTotalTraffic();
//			 LOGD("nowTraffic-%d", nowTraffic);
			 nowFileTraffic = getFileTotalTraffic();
			 //LOGD("nowFileTraffic-%d", getFileTotalTraffic());
			 current_download_netspeed = 1000 * ((nowFileTraffic - file_last_total_rx + 0.0) / (cal_time_interval + 0.0));
//			 current_download_netspeed = 1000 * (nowTraffic - lastTraffic)/ cal_time_interval;
//			 int nowfile_download_netspeed = 1000 * (nowFileTraffic - file_last_total_rx)/ cal_time_interval;
//			 LOGD("FileTraffic cost -%d KB,nowFileTraffic is %d", (nowFileTraffic - file_last_total_rx)/1000,nowFileTraffic);
//			 LOGD("nowFileTraffic-1-%d B", file_download_size[0]);
// 			 if(write_log == 1)fprintf(fp,"%d\t", file_download_size[0]);
////			 LOGD("nowFileTraffic-2-%d B", file_download_size[1]);
//			 if(write_log == 1)fprintf(fp,"%d\t", file_download_size[1]);
////			 LOGD("nowFileTraffic-3-%d B", file_download_size[2]);
// 			 if(write_log == 1)fprintf(fp,"%d\t", file_download_size[2]);
////			 LOGD("nowFileTraffic-4-%d B", file_download_size[3]);
// 			 if(write_log == 1)fprintf(fp,"%d\t", file_download_size[3]);
////			 LOGD("nowFileTraffic-5-%d B", file_download_size[4]);
// 			 if(write_log == 1)fprintf(fp,"%d\t", file_download_size[4]);
////			 LOGD("nowFileTraffic-6-%d B", file_download_size[5]);
//			 if(write_log == 1)fprintf(fp,"%d\t", file_download_size[5]);


//			 if (current_download_netspeed == 0&&(nowTraffic - lastTraffic)!=0) {
//				current_download_netspeed = 1;
////				LOGD("current_download_netspeed----%d",current_download_netspeed);
//			 }
			 if (current_download_netspeed <= 0&&(nowFileTraffic - file_last_total_rx)!=0) {
			 				current_download_netspeed = 1;
//				LOGD("current_download_netspeed----%d",current_download_netspeed);
			 			 }
//			 LOGD("max_download_netspeed before---------%d\n",max_download_netspeed);
//			 if(write_log == 1) fprintf(fp,"max_download_netspeed before:%d KB\t",max_download_netspeed);
             if (current_download_netspeed > max_download_netspeed) {
                max_download_netspeed = current_download_netspeed;
			 }
             LOGD("current_download_netspeed----%d KB/S\n",current_download_netspeed);
//             if(write_log == 1) fprintf(fp,"current_download_netspeed:%d KB\t",current_download_netspeed);  //4g OVERFLOW
             LOGD("max_download_netspeed---------%d KB/S\n",max_download_netspeed);
//             if(write_log == 1) fprintf(fp,"max_download_netspeed:%d KB\n",max_download_netspeed);
			 lastTraffic = nowTraffic;
			 file_last_total_rx = nowFileTraffic;
//			 LOGD("if is finished %d-speed-current speed is %d", oneThreadIsFinish, current_download_netspeed);
		}
	}else
	{
		LOGD("recv_started_flag == 0 ");
//		 if(write_log == 1) fputs("calNetSpeed periodly recv_started_flag == 0\n",fp);
	}
//	write_log--;
//	fclose(fp);
	return 1;
}

int getFileTotalTraffic(){
//	LOGD("aaaaaaa-%d",download_total_rx);
	return download_total_rx;
}

void set_timer()
{
	struct itimerval itv;
    itv.it_interval.tv_sec = 0;
    itv.it_interval.tv_usec = Download_time_interval;
    itv.it_value.tv_sec = 0; //需要修改
    itv.it_value.tv_usec = 250000;
    setitimer(ITIMER_REAL, &itv, NULL);
}

void uninit_time()
{
    struct itimerval value;
    value.it_value.tv_sec = 0;
    value.it_value.tv_usec = 0;
    value.it_interval = value.it_value;
    setitimer(ITIMER_REAL, &value, NULL);
}

void signal_handler(int m)
{
	Download_timer_count++;
	LOGD("Download_timer_count is %d",Download_timer_count);
//	LOGD("1");
	int Download_current_traffic = nowTraffic - startTraffic;
	LOGD("current_traffic is %d",Download_current_traffic);
//	LOGD("2");
	int Download_kill = Download_time_interval*Download_timer_count/1000000;
	LOGD("kill is %d,Download_traffic_duration is %d",Download_kill,Download_traffic_duration);
//	LOGD("3");
	if(Download_traffic_duration == 0){
		if(Download_kill > time_duration){
		    	oneThreadIsFinish = 1;
		    	Download_timer_count = 0;
		    	LOGD("timeout");
		    }
	}else {
		if(Download_kill>time_duration||Download_current_traffic>Download_traffic_duration*1024){
				oneThreadIsFinish = 1;
				Download_timer_count = 0;
				LOGD("timeout");
		}
	}
}

int j = 0;
int trafficLabel = 0;
int trafficusage = 0;
void calresult(){
	uninit_time();
	gettimeofday(&endTime, NULL);
	int timeuse = 1000000 * (endTime.tv_sec - startTime.tv_sec)+ endTime.tv_usec - startTime.tv_usec;  //΢��
	timeuse /= 1000;
	endTraffic = getDownloadTotalTraffic();
//	LOGD("endTraffic-%d",endTraffic);
	ave_download_netspeed = download_total_rx / timeuse;
	if (ave_download_netspeed > max_download_netspeed)
	{
		max_download_netspeed = ave_download_netspeed;
	}
	trafficusage = endTraffic - startTraffic;
	LOGD("traffic usage is %d", trafficusage);
//	LOGD("finish-%d-%d-%d-%d", threadid, endTraffic - startTraffic, timeuse,ave_download_netspeed);
//	LOGD("finish-time%d-ave-%d", timeuse,ave_download_netspeed);
	end_rx_dropped = getDownloadTotalPacketDropped();
	end_rx_packet = getDownloadTotalPacket();
	rx_dropped = end_rx_dropped - start_rx_dropped; //������
	rx_dropped_ratio = (double)rx_dropped/(double)(end_rx_packet-start_rx_packet); //������
	LOGD("start-rx-dropped-%d",start_rx_dropped);
	LOGD("end-rx-dropped-%d",end_rx_dropped);
	LOGD("rx-packet-%d",end_rx_packet-start_rx_packet);
	LOGD("end-rx-ratio-%f",rx_dropped_ratio);

//	LOGD("end-file_download_size-%d",file_download_size[0]);
	server_one_avespeed = (file_download_size[0]+file_download_size[1]+file_download_size[2])/timeuse;
	server_two_avespeed = (file_download_size[3]+file_download_size[4]+file_download_size[5])/timeuse;
	LOGD("server-%d-%d",server_one_avespeed,server_two_avespeed);
}

int download() {
	int threadid = j++;
	LOGD("thread id is %d",threadid);
	int sockfd;
	char buffer[10240];
	struct sockaddr_in server_addr;
	struct hostent *host;
	int portnumber;
	int tbytes=0;
	int rbytes=0;
	char host_addr[256];
	char host_file[1024];
	char local_file[256];
//	FILE * fp;
	char request[1024];
	int send, totalsend;
	int i;
	char * pt;
	char *argv[256];

	argv[0] = str1;
	if(threadnum == 1){
		argv[0] = str0;
	}else if(threadnum == 2){
		if(threadid == 0){
			argv[0] = str0;
		}else if(threadid == 1){
			argv[0] = str1;
		}
	}else if(threadnum == 4){
		if(threadid == 0||threadid==1){
			argv[0] = str0;
		}else if(threadid==2||threadid==3){
			argv[0] = str1;
		}
	}else if(threadnum == 6){
		if (threadid == 0||threadid==1||threadid==2) {
			argv[0] = str0;
		} else{
			argv[0] = str1;
		}
	}
	GetHost(argv[0], host_addr, host_file, &portnumber);

    LOGD("webhost:%s\n", host_addr);
//  LOGD("hostfile:%s\n", host_file);
//  LOGD("portnumber:%d\n\n", portnumber);
	if ((host = gethostbyname(host_addr)) == NULL)/*ȡ������IP��ַ*/
	{
		LOGD("Gethostname error, %s\n", strerror(errno));
		//exit(1);
	} else {
		/* �ͻ�����ʼ���� sockfd������ */
		if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)/*����SOCKET����*/
		{
			LOGD("Socket Error:%s\a\n", strerror(errno));
		}
		/* �ͻ�����������˵����� */
		bzero(&server_addr, sizeof(server_addr));
		server_addr.sin_family = AF_INET;
		server_addr.sin_port = htons(portnumber);
		server_addr.sin_addr = *((struct in_addr *) host->h_addr);
	}
	//TCP timeout
	struct timeval timeo = {5,0};
	setsockopt(sockfd, SOL_SOCKET,SO_SNDTIMEO, (char *)&timeo,sizeof(struct timeval));
	setsockopt(sockfd, SOL_SOCKET,SO_RCVTIMEO, (char *)&timeo,sizeof(struct timeval));

	/* �ͻ��������������� */
	int connectTimes = 0;
	while (connect(sockfd, (struct sockaddr *) (&server_addr),
			sizeof(struct sockaddr)) == -1 && connectTimes < 3)/*������վ*/
	{
		LOGD("Connect Error:%s\a\n", strerror(errno));
		connectTimes += 1;
	}

	sprintf(request,
			"GET /%s HTTP/1.1\r\nAccept: */*\r\nAccept-Language: zh-cn\r\n\
User-Agent: Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n\
Host: %s:%d\r\nConnection: Close\r\n\r\n",
			host_file, host_addr, portnumber);
	//LOGD("%s", request);/*׼��request����Ҫ���͸�����*/

	/*ȡ����ʵ���ļ���*/
	if (host_file && *host_file)
		pt = Rstrchr(host_file, '/');
	else
		pt = 0;

	memset(local_file, 0, sizeof(local_file));
	if (pt && *pt) {
		if ((pt + 1) && *(pt + 1))
			strcpy(local_file, pt + 1);
		else
			memcpy(local_file, host_file, strlen(host_file) - 1);
	} else if (host_file && *host_file)
		strcpy(local_file, host_file);
	else
		strcpy(local_file, "index.html");

	/*����http����request*/
	send = 0;
	totalsend = 0;
	tbytes = strlen(request);
	while (totalsend < tbytes) {
		send = write(sockfd, request + totalsend, tbytes - totalsend);
		if (send == -1) {
			LOGD("send=-1");
		}
		if(totalsend < -5) break;
		totalsend += send;
		LOGD("total send is %d.and threadid is %d",totalsend,threadid);
	}

	while (oneThreadIsFinish == 0) {
		if((rbytes = recv(sockfd, buffer, 1024, MSG_WAITALL)) > 1){
			if (recv_started_flag == 0) {
					recv_started_flag = 1;
					signal(SIGALRM, signal_handler);
					set_timer();
					startTraffic = getDownloadTotalTraffic();
					gettimeofday(&startTime, NULL);
				}
			file_download_size[threadid] = file_download_size[threadid] + rbytes;
			download_total_rx = download_total_rx + rbytes;
		}
		if(rbytes <= 0) break;
	}
	LOGD("rbytes out of while is %d,and threadid is %d",rbytes,threadid);
	//one thread is finished,it needs to kill the test
	if(rbytes == -1){
		oneThreadIsFinish = 1;
	}
	if (trafficLabel == 0 && oneThreadIsFinish == 1) {
		LOGD("threadid %d first", threadid);
		trafficLabel = 1;
		calresult();
	}

	LOGD("finish,threadid == %d",threadid);
	close(sockfd);
	return 1;
}

void *thread_func() {
	download();
}

int startTest() {
	//delete to need
	// lin modify 2014-6-30
	//getcwd(dir_buf,sizeof(dir_buf));

//	fp =fopen("/sdcard/errorlog.txt","a+");
//	LOGD("<----------start download test---------->");
//	if(fp == NULL){
////		LOGD("<----------file create error---------->");
//	}else {
////		LOGD("<----------file create success---------->");
//	}
	char* test_string = "<----------start download test---------->";
	char* test_server1 = str;
	char* test_server2 = str1;
	char* test_info = "thread1\tthread2\tthread3\tthread4\tthread5\tthread6\tmax_download_netspeed\tcurrent_download_netspeed\tmax_download_netspeed\n";
	struct timeval time_start;
		gettimeofday(&time_start, NULL);

//	LOGD("<----------before fprint---------->");
//	fprintf(fp,"%s\n",test_string);
//	fprintf(fp,"threadnum is %d\n",threadnum);
//	fprintf(fp,"%s\t\t%s\n",test_server1,test_server2);
//	fprintf(fp,"%s\n",test_info);
//	fprintf(fp,"time %d:%d\n",time_start.tv_sec,time_start.tv_usec);
	//fputs(test_string,fp);
	//fputs(test_string,fp);
//	fclose(fp);
	j = 0;
	trafficLabel = 0;
	cal_zero_num = 0;
	cal_duration = 0;
	max_download_netspeed = 0;
	is_get_lastTraffic=0;
	ave_download_netspeed = 0;
	recv_started_flag = 0;
	download_total_rx = 0;
	file_last_total_rx = 0;
	current_download_netspeed = 0;

	server_one_avespeed = 0;
	server_two_avespeed = 0;
	//

	oneThreadIsFinish = 0; // no thread is finished
	pthread_t thread[threadnum];
	int temp;
	memset(&thread, 0, sizeof(thread));
	int k;

	int i=0;
	for(i=0;i<threadnum;i++){
		file_download_size[i] = 0;
		LOGD("init file_download-%d",file_download_size[i]);
//		if(i != 6){
//			fprintf(fp,"%d\t",file_download_size[i]);
//		}else{
//			fprintf(fp,"%d\n",file_download_size[i]);
//		}
	}

    start_rx_dropped = getDownloadTotalPacketDropped();
    start_rx_packet = getDownloadTotalPacket();
	for (k = 0; k < threadnum; k++) {
		if ((temp = pthread_create(&thread[k], NULL, thread_func, NULL)) != 0) {
			LOGD("create thread error\n");
		} else {
			LOGD("thread%d created\n", k + 1);
		}
		usleep(5000);
	}

	void* status;
	int j;
	for (j = 0; j < threadnum; j++) {
		pthread_join(thread[j], &status);
		LOGD("thread%d joined\n", j + 1);
	}
	if(trafficLabel == 0){
		calresult();
	}
//	fp =fopen("/sdcard/errorlog.txt","a+");
//	fprintf(fp,"%d\t%d\t%d\n",ave_download_netspeed,max_download_netspeed,download_total_rx);
//	fclose(fp);
	LOGD("traffic label is %d",trafficLabel);
	LOGD("download total file traffic is %d",download_total_rx);
	LOGD("Download Success");
	return 1;
}
