//edu_bupt_nettest_Latency.c
//created by x7, Mar 20, 2013
//modified by x7, Jul 9, 2013, for multi server

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/time.h>
#include  <sys/select.h>
#include <unistd.h>
#include <signal.h>
#include <pthread.h>
#include <math.h>

#include <android/log.h>
#include <jni.h>

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "JNI Latency"

#define LOGD(...)__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#ifdef BUFFSIZE
#undef BUFFSIZE
#endif
#define BUFFSIZE 1024
int Latency_min; //latency变量，默认为0，超时为-1，正常为正值
int Latency_average;
int Latency_state; //latency状态变量,默认为0，超时为-1
int Latency_sock;
int Latency_Threshold = 5; //latency默认Threshold，超过Threshold latency为-1，单位为s

char* Latency_str;
char* Latency_str2;
char* Latency_str3;
char* Latency_url1 = "http://buptant.cn/UNOTest/Latency.txt";
char* Latency_url2 = "http://buptant.cn/UNOTest/Latency.txt";
char* Latency_url3 = "http://buptant.cn/UNOTest/Latency.txt";

int Latency1 = 0;
int Latency2 = 0;
int Latency3 = 0;
int success_time = 0;

void *latency_thread_func() {
	Latency_main();
}

jint Java_edu_bupt_nettest_Latency_getLatencyFromJNI(JNIEnv* env, jobject thiz) {

	int temp;
	pthread_t thread[3];
	memset(&thread, 0, sizeof(thread));
	Latency_average = 0;
	success_time = 0;
	int k;
	for (k = 0; k < 3; k++) {
		if ((temp = pthread_create(&thread[k], NULL, latency_thread_func, NULL)) != 0) {
			LOGD("create thread error\n");
		} else {
			LOGD("thread%d created\n", k + 1);
		}
	}

	void* status;
	int j;
	for (j = 0; j < 3; j++) {
		pthread_join(thread[j], &status);
	}

	LOGD("Latency Tests Succeed");

//	if (Latency_main(Latency_url) > 0) {
//		LOGD("main return > 0");
//		return Latency;
//	} else {
//		LOGD("main return <= 0");
//		return Latency;
//	}

	if(Latency1 >0 && Latency1 < 65535)
		{
		Latency_average = Latency_average + Latency1;
		success_time ++;
		}
	if(Latency2 >0 && Latency2 < 65535)
			{
			Latency_average = Latency_average + Latency2;
			success_time ++;
			}
	if(Latency3 >0 && Latency3 < 65535)
				{
				Latency_average = Latency_average + Latency3;
				success_time ++;
				}
	Latency_average =  Latency_average/success_time;
	LOGD("Latency_min == %d", Latency_average);

	return Latency_average;
}

jint Java_edu_bupt_nettest_Latency_getLatencyJitter(JNIEnv* env, jobject thiz){
	int pow = 0;
	if(Latency1 >0 && Latency1 < 65535)
			{
			pow += (Latency1-Latency_average)*(Latency1-Latency_average);
			}
	if(Latency2 >0 && Latency2 < 65535)
				{
				pow += (Latency2-Latency_average)*(Latency2-Latency_average);
				}
	if(Latency3 >0 && Latency3 < 65535)
				{
				pow += (Latency3-Latency_average)*(Latency3-Latency_average);
				}
//	LOGD("Latency_jitter == %d", sqrt(pow)/success_time);
	return sqrt(pow)/success_time;
}

jint Java_edu_bupt_nettest_Latency_getState(JNIEnv* env, jobject thiz) {
	return Latency_state;
	LOGD("-----state: %d", Latency_state);
}

jint Java_edu_bupt_nettest_Latency_setLatencyTime(JNIEnv* env, jobject thiz,
		jint threshold) {
	if (threshold >= 1) {
		Latency_Threshold = threshold;
		return 1;
	} else
		return 0;
	LOGD("-----Threshold: %d", Latency_Threshold);
}

jint Java_edu_bupt_nettest_Latency_setLatencyServer(JNIEnv* env, jobject thiz,
		jstring serverAddress) {
	Latency_str = (char*) (*env)->GetStringUTFChars(env, serverAddress, NULL);
	if (Latency_str != NULL) {
		Latency_url1 = Latency_str;
		return 1;
	} else
		return 0;
	LOGD("-----str: %s", Latency_str);
}

jint Java_edu_bupt_nettest_Latency_setLatencyServer2(JNIEnv* env, jobject thiz,jstring serverAddress) {
	Latency_str2 = (char*) (*env)->GetStringUTFChars(env, serverAddress, NULL);
	if (Latency_str2 != NULL) {
		Latency_url2 = Latency_str2;
		//Latency_url3 = Latency_str3;
		return 1;
	} else
		return 0;
	LOGD("-----str: %s", Latency_str2);
}
jint Java_edu_bupt_nettest_Latency_setLatencyServer3(JNIEnv* env, jobject thiz,jstring serverAddress) {
	Latency_str3 = (char*) (*env)->GetStringUTFChars(env, serverAddress, NULL);
	if (Latency_str3 != NULL) {
		Latency_url3 = Latency_str3;
		return 1;
	} else
		return 0;
	LOGD("-----str: %s", Latency_str2);
}
int Latency_reslvaddr(char* host, char* destfile, char* url) {
	LOGD("-----reslvaddr");
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

/*
 //设置定时器
 void Latency_set_timer() {
 struct itimerval Latency_itv;
 Latency_itv.it_interval.tv_sec = 0;
 Latency_itv.it_interval.tv_usec = 0;
 //	Latency_itv.it_value.tv_sec = Latency_Threshold;
 Latency_itv.it_value.tv_usec = 10;
 setitimer(ITIMER_REAL, &Latency_itv, NULL);

 }

 void Latency_uninit_timer() {
 struct itimerval Latency_itv;
 Latency_itv.it_interval.tv_sec = 0;
 Latency_itv.it_interval.tv_usec = 0;
 Latency_itv.it_value.tv_sec = 0;
 Latency_itv.it_value.tv_usec = 0;
 setitimer(ITIMER_REAL, &Latency_itv, NULL);

 }

 //超时signal处理
 void Latency_signal_handler(int m) {

 LOGD("-----uninit_timer");
 close(Latency_sock);

 if (Latency <= 0 || Latency > (Latency_Threshold * 1000)) {
 Latency_state = -1;

 }

 LOGD("\ntime out\n");
 }
 */

int lj = 0;

int Latency_main() {
	int Latency = Latency_Threshold * 1000; //latency变量，默认为0，超时为-1，正常为正值
	Latency_state = 0; //latency状态变量,默认为0，超时为-1
	int Latency_sock = 0;
	LOGD("-----main");

	int port = 80;
	char host[BUFFSIZE], destfile[BUFFSIZE];

	char head[BUFFSIZE] = "\0";
	char recvbuf[BUFFSIZE];
	struct hostent * site;
	struct sockaddr_in me;

	int i = 0;
	int threadnum = 0;
	char* url;

	if (lj == 0) {
		LOGD("lj == %d", lj);
		url = Latency_url1;
		threadnum = lj;
		lj++;
	} else if(lj == 1){
		LOGD("lj == %d", lj);
		url = Latency_url2;
		threadnum = lj;
		lj ++;
	}else if(lj == 2){
		LOGD("lj == %d", lj);
				url = Latency_url3;
				threadnum = lj;
				lj = 0;
	}

	//resolve address to host and destination file
	Latency_reslvaddr(host, destfile, url);

	site = gethostbyname(host); //get address
	Latency_sock = socket(AF_INET, SOCK_STREAM, 0); //TCP
	memset(&me, 0, sizeof(struct sockaddr_in));
	memcpy(&me.sin_addr, site->h_addr_list[0], site->h_length);
	me.sin_family = AF_INET;
	me.sin_port = htons(port);

	//TCP timeout
	struct timeval timeo;
	timeo.tv_sec = Latency_Threshold;
	timeo.tv_usec = 0;

	setsockopt(Latency_sock, SOL_SOCKET, SO_SNDTIMEO, (char *) &timeo,
			sizeof(struct timeval));

	connect(Latency_sock, (struct sockaddr *) &me, sizeof(struct sockaddr));

	//connect before send request
	if (Latency_sock < 1) {
		LOGD("\nsock unconnected\n");
		return -1;
	}
	LOGD("\nconnected\n");
	//create http request
	//strnset(head, '\0', BUFFSIZE);
	LOGD("\ncreate http request\n\n");
	strcat(head, "GET ");
	strcat(head, destfile);
	strcat(head, " HTTP/1.1\r\n");
	strcat(head, "Host: ");
	strcat(head, host);
	strcat(head, "\r\n");
	strcat(head, "Accept: */*\r\n");
	strcat(head, "Accept-Language: zh-cn\r\n");
	strcat(head,
			"User-Agent: Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)\r\n");
	strcat(head, "Connection: Close\r\n");
	strcat(head, "\r\n");
	strcat(head, "\0\0");
	//htsend(sock, head);
//	LOGD(head);

	//count time
	struct timeval start, end;

	//start sending
	int onetime = 0;
	int sent = 0;
	int total = strlen(head);

	while (sent < total) {
		gettimeofday(&start, NULL);
		onetime = write(Latency_sock, head + sent, total - sent);
		if (onetime == -1) {
			LOGD("send error!\n");
//			exit(0);
			close(Latency_sock);
			Latency = 65535;
			return Latency;
		}
		LOGD("\nsent\n");
		sent += onetime;
	}

//	signal(SIGALRM, Latency_signal_handler); /* 定时开始 */
//	Latency_set_timer();

	if (Latency_state != -1) {
		if (recv(Latency_sock, recvbuf, 1, MSG_WAITALL) < 0) {
			close(Latency_sock);
			Latency = 65535;
			return Latency;
		}

		LOGD("\nread : \n\n");
		gettimeofday(&end, NULL);
		int timeuse = 1000000 * (end.tv_sec - start.tv_sec) + end.tv_usec
				- start.tv_usec;
		Latency = timeuse / 1000;
		LOGD("Latency: %d\n", Latency);
		close(Latency_sock);
	}

	if (Latency < 0 || Latency > (1000 * Latency_Threshold)) {
		Latency = 1000 * Latency_Threshold;
	}

//	Latency_uninit_timer();
//	return Latency;

	if (threadnum == 0) {
		LOGD("Latency1 == %d ", Latency);
		Latency1 = Latency;
	} else if(threadnum == 1){
		LOGD("Latency2 == %d ", Latency);
		Latency2 = Latency;
	}else if(threadnum == 2){
		LOGD("Latency3 == %d ", Latency);
				Latency3 = Latency;
	}
	return 0;

}
