//edu_bupt_nettest_Upload.c
//created by x7, Mar 27, 2013
//modified by x7, Mar 29, 2013
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <time.h>
#include <pthread.h>

#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "JNI_Upload"

#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#ifdef BUFFSIZE
#undef BUFFSIZE
#endif
#define BUFFSIZE 1024
#define SEND_BUFF_SIZE 1024
//#define SEND_REPEAT 300
//#define PTHREAD_NUM 2

//global vars
//char upload_url[BUFFSIZE] = "\0";
char upload_url0[BUFFSIZE] = "\0";
char upload_url1[BUFFSIZE] = "\0";
//char upload_url2[BUFFSIZE] = "\0";
int upload_port;
int upload_sock = 0;
int upload_kill_flag = 0;

//for multi-server test
int upload_thread_count = 0;


int upload_interval_us;
int upload_crt_speed = 0;
int upload_avg_speed = 0;
int upload_max_speed = 0;
struct timeval upload_time_start;
struct timeval upload_time_crt, upload_time_lst;
int upload_traf_start;
int upload_traf_lst, upload_traf_crt;

int tx_dropped = 0; //������
double tx_dropped_ratio = 0.0; //������
int start_tx_dropped = 0;
int end_tx_dropped = 0;
int start_tx_packet = 0;
int end_tx_packet = 0;

int upload_pthread_num = 3;
int upload_multiserver_number = 1;
int send_repeat = 239;

int Upload_time_interval = 250000;
int upload_time_out = 10;
int Upload_timer_count = 0;
int Upload_traffic_duration = 0;

//split url to host addr and file name
int upload_reslvaddr(char* host, char* destfile, char* url) {
	int i = 0;
	char* p = url; // getting host from URL
	p += sizeof("http://") - 1;
	for (; *p != '/' && *p != '\0'; p++, i++) {
		host[i] = *p;
	}
	host[i] = '\0';
//	LOGD(host);
	strcpy(destfile, p); //file dir and name
//	LOGD(destfile);
}

//created by x7, Apr 1, 2013
void upload_set_timer() {
	struct itimerval itv;
	itv.it_interval.tv_sec = 0;
	itv.it_interval.tv_usec = Upload_time_interval;
	itv.it_value.tv_sec = 0;
	itv.it_value.tv_usec = 250000;
	LOGD("upload_time_out %d, Upload_time_interval %d", upload_time_out,Upload_time_interval);
	setitimer(ITIMER_REAL, &itv, NULL);
	//error here, fixed by zzz, May 28
}

void upload_uninit_time() {
	struct itimerval value;
	value.it_value.tv_sec = 0;
	value.it_value.tv_usec = 0;
	value.it_interval = value.it_value;
	setitimer(ITIMER_REAL, &value, NULL);
}

//array for upload
//created by x7, Mar 29, 2013
int upload_gen_noise(char* n) {
	srand((unsigned long) time(NULL));
	int i;
	for (i = 0; i < SEND_BUFF_SIZE; i++) {
		n[i] = rand() % 256;
	}
	return 0;
}

//created by x7, Mar 29, 2013
int upload_gen_http_req(char** r, char* dst, char* hst) {
	char head[BUFFSIZE];
	memset(head, 0, BUFFSIZE);
	strcat(head, "POST ");
	strcat(head, dst);
	strcat(head, " HTTP/1.1\r\n");
	strcat(head, "Host: ");
	strcat(head, hst);
	strcat(head, "\r\n");
	strcat(head, "Accept: */*\r\n");
	strcat(head, "Accept-Language: zh-cn\r\n");
	//strcat(head, "User-Agent: Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n");
	strcat(head, "User-Agent: ANT-TEST\r\n");
	strcat(head,
			"Content-Type: multipart/form-data;boundary=---------------------------ant\r\n");
	strcat(head, "Connection: Close\r\n");

	//content
	char content[BUFFSIZE];
	memset(content, 0, BUFFSIZE);
	strcat(content, "-----------------------------ant\r\n");
	strcat(content,
			"Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"test.dat\"\r\n");
	strcat(content, "Content-Type: text/plain\r\n");
	strcat(content, "\r\n");
	char* content_end = "\r\n-----------------------------ant--\r\n";

	//content length
	char tmp[BUFFSIZE];
	sprintf(tmp, "Content-Length: %d\r\n\r\n",
			strlen(content) + strlen(content_end)
					+ SEND_BUFF_SIZE * send_repeat);
	strcat(head, tmp);
	//total buf
	*r = (char*) malloc(BUFFSIZE);
	memset(*r, 0, strlen(head) + strlen(content));
	strcat(*r, head);
	strcat(*r, content);
	return 0;
}

//created by x7, Apr 2, 2013
int upload_calculate_avg() {
	//caculate total time
	LOGD("cal");
	gettimeofday(&upload_time_crt, NULL);
	upload_traf_crt = upload_get_total_traffic();
	int timeuse = 1e6 * (upload_time_crt.tv_sec - upload_time_start.tv_sec)
			+ upload_time_crt.tv_usec - upload_time_start.tv_usec;
	upload_avg_speed = (upload_traf_crt - upload_traf_start) / (timeuse / 1000);
	if (upload_avg_speed > upload_max_speed) {
		upload_max_speed = upload_avg_speed;
	}

	//log
//	char log[BUFFSIZE];
//	sprintf(log,
//			"upload_avg_speed == %d, upload_traf_crt == %d, upload_traf_start == %d, timeuse == %d, upload_kill_flag == %d",
//			upload_avg_speed, upload_traf_crt, upload_traf_start, timeuse,
//			upload_kill_flag);
	LOGD("upload_avg_speed == %d, upload_traf_crt == %d, upload_traf_start == %d, timeuse == %d, upload_kill_flag == %d",
			upload_avg_speed, upload_traf_crt, upload_traf_start, timeuse,
			upload_kill_flag);
	return 0;
}

//created by x7, Mar 29, 2013
int upload_exec() {
//	LOGD("this is a child thread\n");
	upload_kill_flag = 0;
	int i = 0;
	//create noise
	char *noise_buf;
	noise_buf = (char*)malloc(SEND_BUFF_SIZE);
	upload_gen_noise(noise_buf);
	int threadid = upload_thread_count++;
//	LOGD("%d", threadid);

	//resolve address to host and destination file
	char host[BUFFSIZE], destfile[BUFFSIZE];
	int upload_sock = 0;
	struct hostent * site = NULL;
	struct sockaddr_in me;

	char* url = upload_url0;
	LOGD("upload_url0: %s", upload_url0);
	LOGD("upload_url1: %s", upload_url1);

	// for multi-server test
	// by x7, May 17, 2013
	if (strlen(upload_url1) != 0) {
		if (threadid >= upload_pthread_num) {
			url = upload_url1;
//		} else if (threadid == 2) {
//			url = upload_url2;
		}
	}
	LOGD("threadid: %d, url:%s", threadid, url);
	upload_reslvaddr(host, destfile, url);

	while(site == NULL)//get address
	{
//		LOGD("----------------------------------gethostbyname--thread %d--", threadid);
		site = gethostbyname(host);
	}
	upload_sock = socket(AF_INET, SOCK_STREAM, 0); //TCP
//	memset(&me, 0, sizeof(me));
	bzero(&me, sizeof(me));
//	LOGD("----------------------------------bug here?--thread %d--", threadid);
//	LOGD("site->h_addr_list[0] == %s\nsite->h_length == %d", site->h_addr_list[0], site->h_length);
	memcpy(&me.sin_addr, site->h_addr_list[0], site->h_length);
	me.sin_family = AF_INET;
	me.sin_port = htons(upload_port);

	//TCP timeout
	struct timeval timeo = { 5, 0 };
	setsockopt(upload_sock, SOL_SOCKET, SO_SNDTIMEO, (char *) &timeo,
			sizeof(struct timeval));
	setsockopt(upload_sock, SOL_SOCKET, SO_RCVTIMEO, (char *) &timeo,
			sizeof(struct timeval));

	connect(upload_sock, (struct sockaddr *) &me, sizeof(struct sockaddr));

	//connect before send request
	if (upload_sock < 1) {
		LOGD("connect error!n");
		return -1;
	}
	LOGD("\nconnected\n");

	//create http request
	//head
	char* http_req;
	upload_gen_http_req(&http_req, destfile, host);
	//check
	//for (i = 0; i < strlen(http_req); i++)
	//{
	//	putchar(*(http_req + i));
	//}

	//start sending req head
	if (send(upload_sock, http_req, strlen(http_req), 0) < 0) {
		LOGD("send error!n");
		return -1;
	}
//	LOGD("\nhead sent\n");
//	LOGD("free http_req");
	free(http_req);

	//count time
	struct timeval start, end;
	gettimeofday(&start, NULL);

	//start sending
	for (i = 0; i < send_repeat && upload_kill_flag == 0; i++) {
		if (send(upload_sock, noise_buf, SEND_BUFF_SIZE, 0) < 0) {
			LOGD("send error!n");

			//error here, fixed by zzz, May 28
			//close(upload_sock);
			//return -1;
			upload_kill_flag = 1;
		}
		//LOGD("send");
	}

	if (upload_kill_flag == 1) {
		LOGD("killed");
		close(upload_sock);
		upload_uninit_time();
		upload_calculate_avg();
	} else {
		LOGD("finished\n");
		//alarm(0);
		/*//caculate total time
		 gettimeofday( &upload_time_crt, NULL );
		 upload_traf_crt = upload_get_total_traffic();
		 int timeuse = 1e6 * ( upload_time_crt.tv_sec - upload_time_start.tv_sec ) + upload_time_crt.tv_usec - upload_time_start.tv_usec;
		 upload_avg_speed = (upload_traf_crt - upload_traf_start) / (timeuse/1000);

		 //log
		 char log[BUFFSIZE];
		 sprintf(log, "upload_avg_speed == %d, upload_traf_crt == %d, upload_traf_start == %d, timeuse == %d, upload_kill_flag == %d", upload_avg_speed, upload_traf_crt, upload_traf_start, timeuse, upload_kill_flag);
		 LOGD(log);
		 */
		//send req end
		char* content_end = "\r\n-----------------------------ant--\r\n";
		if (send(upload_sock, content_end, strlen(content_end), 0) < 0) {
			LOGD("send error!n");
			return -1;
		}
		LOGD("end sent\n");

		//recv
		char recvbuf[BUFFSIZE];
		while (recv(upload_sock, recvbuf, BUFFSIZE, MSG_WAITALL) > 0) {
			LOGD("\nread : \n\n");
//			LOGD(recvbuf);
		}

		upload_uninit_time();
		upload_kill_flag = 1;

		close(upload_sock);
		LOGD("*********************************calculate avg");
		upload_calculate_avg();
	}
	LOGD("free noise_buf");
	free(noise_buf);
	alarm(0);
	return 0;

}

//calculate current speed
void upload_timeout() {
//	LOGD("********************************time out");

	Upload_timer_count++;
	int current_traffic = upload_traf_crt-upload_traf_start;
	int kill = Upload_time_interval*Upload_timer_count/1000000;
	LOGD("Upload_traffic_duration is %d.kill is %d,current traffic is %d",Upload_traffic_duration,kill,current_traffic);
	if(Upload_traffic_duration == 0){
		if(kill>upload_time_out){
			upload_kill_flag = 1;
			Upload_timer_count = 0;
		}
	}else{
		if(kill>upload_time_out||current_traffic>Upload_traffic_duration*1024){
			upload_kill_flag = 1;
			Upload_timer_count = 0;
		}
	}
}

void upload_signal_handler() {
//	if(upload_avg_speed != 0) {
//		LOGD("------------------------------------------- clear upload_avg_speed");
////		upload_avg_speed = 0;
//		return;
//	}

	upload_traf_crt = upload_get_total_traffic();
	gettimeofday(&upload_time_crt, NULL);
	int timeuse = 1e6 * (upload_time_crt.tv_sec - upload_time_lst.tv_sec)
			+ upload_time_crt.tv_usec - upload_time_lst.tv_usec;
	if ((upload_traf_crt - upload_traf_lst) == 0) {
		//LOGD("traf == 0");
		return;
	}
	upload_crt_speed = (upload_traf_crt - upload_traf_lst) / (timeuse / 1000);
	LOGD("------------------------------------------- upload_max_speed - %d", upload_max_speed);
	if (upload_crt_speed > upload_max_speed) {
		upload_max_speed = upload_crt_speed;
	}

	LOGD("------------------------------------------- upload_max_speed - %d", upload_max_speed);
	//time out
//	timeuse = 1e6 * ( upload_time_crt.tv_sec - upload_time_start.tv_sec ) + upload_time_crt.tv_usec - upload_time_start.tv_usec;
//	if(timeuse/1e6 > upload_time_out)
//	{
//		LOGD("timeout, kill");
//		alarm(0);
//		upload_kill_flag = 1;
//		upload_calculate_avg();
//	}
	//log
//	char log[BUFFSIZE];
//	sprintf(log,
//			"upload_crt_speed == %d, upload_traf_crt == %d, upload_traf_lst == %d, timeuse == %d, upload_kill_flag == %d",
//			upload_crt_speed, upload_traf_crt, upload_traf_lst, timeuse,
//			upload_kill_flag);
	LOGD("upload_crt_speed == %d, upload_traf_crt == %d, upload_traf_lst == %d, timeuse == %d, upload_kill_flag == %d ,traffic usage == %d",
			upload_crt_speed, upload_traf_crt, upload_traf_lst, timeuse,
			upload_kill_flag, upload_traf_lst-upload_traf_crt);
	//set lst
	upload_traf_lst = upload_traf_crt;
	upload_time_lst = upload_time_crt;
}

////called by upload_get_total_traffic
//void split(char **arr,char *str,const char *del)
//{
//   char *s = NULL;
//   s = strtok(str,del);
//   while(s!=NULL){
//        *arr++=s;
//     s=strtok(NULL,del);
//   }
//}
//
////called by upload_get_total_traffic
//int strsplinum(char *str,const char *del)
//{
//   char *first = NULL;
//   char *second = NULL;
//   int num = 0;
//   first = strstr(str,del);
//   while(first!=NULL)
//   {
//     second = first+1;
//     num++;
//     first = strstr(second,del);
//   }
//   return num;
//}

/*
 int upload_get_total_traffic(){
 FILE *fp;
 char buffer[5000];
 const char *del = "\n";
 const char *del2 = " ";
 char *bufdeal[20];
 int totalTraffic = 0;
 int partnumber = 0;
 if((fp=fopen("/proc/self/net/dev","r"))==NULL){
 }else{
 // fseek(fp,0L,SEEK_END);
 fseek(fp,200L,SEEK_SET);
 fread(buffer,5000,1,fp);
 fclose(fp);
 partnumber = strsplinum(buffer,del);
 split(bufdeal,buffer,del);
 int i;
 int calculator;
 for(i=1;i<partnumber;i++){
 char *s;
 s = strtok(*(bufdeal+i),del2);
 calculator =1;
 while(s!=NULL)
 {
 if(calculator == 10)
 {
 totalTraffic = totalTraffic + atoi(s);
 }
 s = strtok(NULL,del2);
 calculator = calculator + 1;
 }
 }
 return totalTraffic;
 }
 }*/

//copied from Android source code
//by x7, Apr 7, 2013
long upload_readNumber(char const* filename) {
	char buf[80];
	int fd = open(filename, O_RDONLY);
	if (fd < 0) {
		if (errno != ENOENT)
			LOGD("Can't open %s: %s", filename, strerror(errno));
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

//copied from Android source code
//by x7, Apr 7, 2013
long upload_readTotal(char const* suffix) {
	char filename[PATH_MAX] = "/sys/class/net/";
	DIR *dir = opendir(filename);
	if (dir == NULL) {
		LOGD("Can't list %s: %s", filename, strerror(errno));
		return -1;
	}

	int len = strlen(filename);
	jlong total = -1;
	struct dirent *entry;
	while (entry = readdir(dir)) {
		// Skip ., .., and localhost interfaces.
		if (entry->d_name[0] != '.' && strncmp(entry->d_name, "lo", 2) != 0) {
			strlcpy(filename + len, entry->d_name, sizeof(filename) - len);
			strlcat(filename, suffix, sizeof(filename));
			jlong num = upload_readNumber(filename);
			if (num >= 0)
				total = total < 0 ? num : total + num;
		}
	}

	closedir(dir);
	return total;
}

int upload_get_total_traffic() {
	return upload_readTotal("/statistics/tx_bytes");
//	char name[50];
//	unsigned long rx_bytes, rx_packets, rx_errs, rx_drops, rx_fifo, rx_frame,
//			tx_bytes, tx_packets, tx_errs, tx_drops, tx_fifo, tx_colls,
//			tx_carrier, rx_multi;
//	char buf[512];
//
//	int totalTraffic = 0;
//	FILE *fp = fopen("/proc/net/dev", "r");
//	if (fp == NULL) {
//		//perror("fopen");
//		//return -1;
//	}
//	if (fgets(buf, sizeof(buf), fp))
//	if (fgets(buf, sizeof(buf), fp))
//	if (fgets(buf, sizeof(buf), fp))
//
//	while (fgets(buf, sizeof(buf), fp) != NULL) {
//		char *ptr;
//		buf[sizeof(buf) - 1] = 0;
//		if ((ptr = strchr(buf, ':')) == NULL
//				|| (*ptr++ = 0, sscanf(buf, "%s", name) != 1)) {
//			LOGD("Wrong format \n");
//			fclose(fp);
//		}
//		int readlength = sscanf(ptr,
//				"%ld%ld%ld%ld%ld%ld%ld%*d%ld%ld%ld%ld%ld%ld%ld", &rx_bytes,
//				&rx_packets, &rx_errs, &rx_drops, &rx_fifo, &rx_frame,
//				&rx_multi, &tx_bytes, &tx_packets, &tx_errs, &tx_drops,
//				&tx_fifo, &tx_colls, &tx_carrier);
//		//LOGD("a-%d\n",readlength);
//		if (readlength == 14) {
//			totalTraffic = totalTraffic + tx_bytes;
//			//LOGD("%ld-%d\n",tx_bytes,totalTraffic);
//		}
//	}
//	fclose(fp);
//	return totalTraffic;
}

int getUploadTotalPacket() {
	return readTotal("/statistics/tx_packets");
}

int getUploadTotalPacketDropped() {
	return readTotal("/statistics/tx_dropped");
}

//created by x7, Mar 29, 2013
void* pthread_func() {
	upload_exec();
}

//created by x7, Jun 24, 2013
int upload_test() {
	pthread_t tid[upload_pthread_num * upload_multiserver_number];
	LOGD("upload_pthread_num: %d", upload_pthread_num);
	LOGD("upload_multiserver_number: %d", upload_multiserver_number);
	void* retval;
	memset(&tid, 0, sizeof(tid));
	int i;
	upload_avg_speed = 0;
	start_tx_dropped = getUploadTotalPacketDropped();
	start_tx_packet = getUploadTotalPacket();
	for (i = 0; i < upload_pthread_num * upload_multiserver_number; i++) {
		if (pthread_create(&tid[i], NULL, pthread_func, NULL) < 0) {
			LOGD("pthread create error");
		}
		usleep(5000);
	}

	//error here, fixed by zzz, May 28
	//initial
	LOGD("init traf and time");
	upload_crt_speed = 0;
	upload_max_speed = 0;
	gettimeofday(&upload_time_start, NULL);
	upload_traf_start = upload_get_total_traffic();
	upload_traf_lst = upload_traf_start;
	upload_time_lst = upload_time_start;
	//timer for calculating crt speed
	signal(SIGALRM, upload_timeout);
	LOGD("**************************************set timer");
	upload_set_timer();

	//wait for threads finished
	for (i = 0; i < upload_pthread_num; i++) {
		LOGD("pthread_join");
		pthread_join(tid[i], &retval);
	}

	end_tx_dropped = getUploadTotalPacketDropped();
	end_tx_packet = getUploadTotalPacket();
	tx_dropped = end_tx_dropped - start_tx_dropped; //������
	tx_dropped_ratio = (double) tx_dropped
			/ (double) (end_tx_packet - start_tx_packet); //������
	LOGD("start-tx-dropped-%d", start_tx_dropped);
	LOGD("end-tx-dropped-%d", end_tx_dropped);
	LOGD("start-tx-packet-%d", start_tx_packet);
	LOGD("end-tx-packet-%d", end_tx_packet);
	LOGD("end-tx-ratio-%f", tx_dropped_ratio);
	LOGD("return");
	return;
}

void Java_edu_bupt_nettest_Upload_refreshTraffic(JNIEnv * env, jobject thiz) {
	//LOGD("Upload_refreshTraffic");
	upload_signal_handler();
}
void Java_edu_bupt_nettest_Upload_forcestop(JNIEnv * env, jobject thiz) {
    LOGD("********************************force stop");
    upload_kill_flag = 1;
//    upload_calculate_avg();
}

//created by x7, Mar 29, 2013
int Java_edu_bupt_nettest_Upload_ctestUpload(JNIEnv * env, jobject thiz,
		jstring testURL, jint timeout_s, jint interval_us) {
	//char* upload_url = "http://hgw060077.chinaw3.com/UNOTest/upload/upload_file.php";
	//char* upload_url = "http://127.0.0.1/test-httppost/post.php";
	//char* upload_url = (char*)(*env)->GetStringUTFChars(env, testURL, 0);
	upload_avg_speed = 0;
	upload_thread_count = 0;
	strcpy(upload_url0, (char*) (*env)->GetStringUTFChars(env, testURL, 0));
	strcpy(upload_url1, "");
//	strcpy(upload_url2, "");
	upload_multiserver_number = 1;

	upload_port = 80;
	upload_time_out = timeout_s;
	LOGD("upload_time_out %d", upload_time_out);
	upload_interval_us = interval_us;
//	LOGD(upload_url0);

	upload_test();
}

//created by x7, Mar 29, 2013
int Java_edu_bupt_nettest_Upload_cgetCurrentSpeed(JNIEnv * env, jobject thiz) {
	return upload_crt_speed;
}

//created by x7, Mar 29, 2013
int Java_edu_bupt_nettest_Upload_cgetAverageSpeed(JNIEnv * env, jobject thiz) {
	return upload_avg_speed;
}

//created by x7, Mar 29, 2013
int Java_edu_bupt_nettest_Upload_cgetMaxSpeed(JNIEnv * env, jobject thiz) {
	return upload_max_speed;
}

//created by x7, Jun 20, 2013
int Java_edu_bupt_nettest_Upload_csetPthreadNum(JNIEnv * env, jobject thiz,
		jint num) {
	upload_pthread_num = num;
	return 0;
}

//created by x7, Jun 20, 2013
int Java_edu_bupt_nettest_Upload_csetNetworkType(JNIEnv * env, jobject thiz,
		jint t) {
	if (t == 0) {
		send_repeat = 239;
	} else if (t == 1) {
		send_repeat = 4260;
	}
	return 0;
}
int Java_edu_bupt_nettest_Upload_csetTrafficDuration(JNIEnv * env, jobject thiz,jint trduration){
	Upload_traffic_duration = trduration;
}
JNIEXPORT jdouble Java_edu_bupt_nettest_Upload_getDropRatio(JNIEnv* env,
		jobject thiz) {
	return tx_dropped_ratio;
}

// for multi-server test
//created by x7, May 17, 2013
//modified by x7, Jun24, 2013
int Java_edu_bupt_nettest_Upload_ctestUploadMultiServer(JNIEnv * env,
		jobject thiz, jstring serverAddress0, jstring serverAddress1,
		jint timeout_s, jint interval_us) {
	//char* upload_url = "http://hgw060077.chinaw3.com/UNOTest/upload/upload_file.php";
	//char* upload_url = "http://127.0.0.1/test-httppost/post.php";
	//char* upload_url = (char*)(*env)->GetStringUTFChars(env, testURL, 0);
	upload_avg_speed = 0;
	upload_thread_count = 0;
	strcpy(upload_url0,
			(char*) (*env)->GetStringUTFChars(env, serverAddress0, 0));
	strcpy(upload_url1,
			(char*) (*env)->GetStringUTFChars(env, serverAddress1, 0));
//	strcpy(upload_url2,
//			(char*) (*env)->GetStringUTFChars(env, serverAddress2, 0));
	upload_multiserver_number = 2;
	upload_port = 80;
	upload_time_out = timeout_s;
	LOGD("upload_time_out %d", upload_time_out);
	upload_interval_us = interval_us;
//	LOGD(upload_url0);
	upload_test();
}
