#include <android/log.h>

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "JNI_DEBUGGING", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,   "JNI_DEBUGGING", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)
#include "fft.h"
#include "phasevocoder.h"

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_phasevocoder_NativePhaseVocoder_makeSpec
        (JNIEnv * env, jobject thiz, jobject bufferNio, jobject specNio, jobject windowNio, jint specSize, jint offset){

    double* buffer = (double*) (*env)->GetDirectBufferAddress(env,bufferNio);
    double* spec = (double*) (*env)->GetDirectBufferAddress(env,specNio);
    double* window = (double*) (*env)->GetDirectBufferAddress(env,windowNio);

    int halfN = specSize / 4;
    int N = halfN * 2;
    int size = N;

    for(int i=0;i<halfN;i++){
        spec[i] = window[i] * buffer[i + offset];
    }
    for(int i=halfN;i<N;i++){
        spec[i] = 0;
    }

    reverseBit(spec, size);
    fft(spec, size,1);
}