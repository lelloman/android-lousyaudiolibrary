
#include <math.h>

#include <android/log.h>
#include <malloc.h>
#include <time.h>
#include "test.h"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "FFT_JNI", __VA_ARGS__)
#include "fft.h"


int checkOk(int* data1, int* data2, int size){
    for(int i=0;i<size;i++){
        if(data1[i] != data2[i]){
            return 0;
        }
    }
    return 1;
}
/*
 * Class:     com_lelloman_lousyaudiolibrary_algorithm_Fft
 * Method:    testArrayCopySingleThread
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_testArrayCopySingleThread
        (JNIEnv *env, jobject thiz, jint size, jint iterations){
    int* data1 = malloc(sizeof(int) * size);
    int* data2 = malloc(sizeof(int) * size);

    clock_t start = clock();
    for(int i=0;i<iterations;i++){
        arrayCopySingleThread(data1, data2, size);
    }
    clock_t end = clock();
    int ok = checkOk(data1, data2, size);
    LOGE("array copy single ok %d time %d",ok, end-start);
}

/*
 * Class:     com_lelloman_lousyaudiolibrary_algorithm_Fft
 * Method:    testArrayCopyMultiThread
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_testArrayCopyMultiThread
        (JNIEnv *env, jobject thiz, jint size, jint iterations){

    int* data1 = malloc(sizeof(int) * size);
    int* data2 = malloc(sizeof(int) * size);

    clock_t start = clock();
    for(int i=0;i<iterations;i++){
        arrayCopyMultiThread(data1, data2, size);
    }
    clock_t end = clock();

    int ok = checkOk(data1, data2, size);
    LOGE("array copy  multi ok %d time %d",ok, end-start);
}


JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_dummy
  (JNIEnv * env, jobject thiz, jobject byteBuffer, jint size){
    double* data = (double*) (*env)->GetDirectBufferAddress(env,byteBuffer);
    for(int i=0;i<size;i++){
        data[i] = 3.14;
    }
}

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_forward(JNIEnv *env, jobject thiz, jobject byteBuffer, jint size){
    double* data = (double*) (*env)->GetDirectBufferAddress(env, byteBuffer);

    reverseBit(data, size);
    fft(data, size,1);
}

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_inverse(JNIEnv *env, jobject thiz, jobject byteBuffer, jint size, jboolean scale){
    double* data = (double*) (*env)->GetDirectBufferAddress(env, byteBuffer);


    reverseBit(data, size);
    fft(data, size,-1);

    if(scale) {
        double factor = size/4;
        for (int i = 0; i < size; i++) {
            data[i] /= factor;
        }
    }
}

void reverseBit(double* arr,int size){
    int j=0;
    int n2 = size / 2;
    int n4 = size / 4;
    for (int i=0;i<n2;i+=2) {
        if (j > i) {
            swap(arr,j,i);
            swap(arr,j+1,i+1);
            if((j/2)<(n4)){
                swap(arr,(size-(i+2)),(size-(j+2)));
                swap(arr,(size-(i+2))+1,(size-(j+2))+1);
            }
        }
        int m=n2;
        while (m >= 2 && j >= m) {
            j -= m;
            m = m/2;
        }
        j += m;
    }
}

void swap(double* arr,int a, int b){
    double holder = arr[a];
    arr[a] = arr[b];
    arr[b] = holder;
}

void fft(double* pcm,jint size, int sinal){
    int mmax=2;
    int n = size;
    double PI2 = 6.283185307179586;
    while (n > mmax){
        double wr=1.0;
        double wi=0.0;
        int istep = mmax<<  1;
        double theta = sinal * (PI2/mmax);
        double wtemp=sin(0.5*theta);
        double wpr = -2.0*wtemp*wtemp;
        double wpi=sin(theta);

        for (int m=1;m<mmax;m+=2) {
            for (int i= m;i<=n;i+=istep) {
                int j=i+mmax;
                int j1 = j-1;
                int i1 = i-1;

                double tempr=wr*pcm[j1]-wi*pcm[j];
                double tempi=wr*pcm[j]+wi*pcm[j1];

                pcm[j1]=pcm[i1]-tempr;
                pcm[j]=pcm[i]-tempi;
                pcm[i1] += tempr;
                pcm[i] += tempi;
            }
            wr=(wtemp=wr)*wpr-wi*wpi+wr;
            wi=wi*wpr+wtemp*wpi+wi;
        }
        mmax=istep;
    }
}