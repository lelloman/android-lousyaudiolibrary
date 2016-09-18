#include <android/log.h>
#include "math.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "JNI_DEBUGGING", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,   "JNI_DEBUGGING", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)
#include "fft.h"
#include "phasevocoder.h"

static const double PI = 3.141592653589793;
static const double PI2 = 6.283185307179586;

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_phasevocoder_NativePhaseVocoder_next__Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2II
        (JNIEnv * env, jobject thiz, jobject bufferNio, jobject specNio1, jobject specNio2, jobject phiNio, jobject sigoutNio, jobject outNio, jobject windowNio, jint specSize, jint offset){

    double* buffer = (double*) (*env)->GetDirectBufferAddress(env,bufferNio);
    double* spec1 = (double*) (*env)->GetDirectBufferAddress(env,specNio1);
    double* spec2 = (double*) (*env)->GetDirectBufferAddress(env,specNio2);
    double* window = (double*) (*env)->GetDirectBufferAddress(env,windowNio);
    double* phi = (double*) (*env)->GetDirectBufferAddress(env,phiNio);
    double* sigout = (double*) (*env)->GetDirectBufferAddress(env,sigoutNio);
    double* out = (double*) (*env)->GetDirectBufferAddress(env,outNio);

    int halfN = specSize / 4;
    int N = halfN * 2;
    int size = N;
    int N2 = N*2;
    int H = offset;
    int NmH = N-H;


    for (int i = 0; i < NmH; i++)
        sigout[i] = sigout[i + H];
    for (int i = NmH; i < N; i++)
        sigout[i] = 0;

    for(int i=0;i<halfN;i++){
        spec1[i] = window[i] * buffer[i + offset];
    }
    for(int i=halfN;i<N;i++){
        spec1[i] = 0;
    }
    reverseBit(spec1, size);
    fft(spec1, size,1);


    for(int i=0;i<halfN;i++){
        spec2[i] = window[i] * buffer[i + offset];
    }
    for(int i=halfN;i<N;i++){
        spec2[i] = 0;
    }

    reverseBit(spec2, size);
    fft(spec2, size,1);

    // makePhi
    for(int i=0;i<N;i++){
        int i2 = i*2;
        int i21 = i2 + 1;

        double a1 = spec1[i2];
        double b1 = spec1[i21];
        double a2 = spec2[i2];
        double b2 = spec2[i21];

        double p = phi[i] + (atan2(b2, a2) - atan2(b1, a1));
        while (p < -PI) p += PI2;
        while (p > PI) p -= PI2;
        phi[i] = p;
    }

    // makeOut lol
    for (int i = 0; i < N; i++) {
        int i2 = i * 2;
        int i21 = i2 + 1;

        double p = phi[i];
        out[i2] = cos(p);
        out[i21] = sin(p);
    }

    for (int i = 0; i < N2; i++) {
        double v = spec2[i];
        if(v < 0){
            v *= -1;
        }
        spec2[i] = v * out[i];
    }

    reverseBit(spec2, N);
    fft(spec2, N, -1);

    double factor = size/4;
    for (int i = 0; i < size; i++) {
        spec2[i] /= factor;
    }

    for (int i = 0; i < N; i++)
        sigout[i] += window[i] * spec2[i];
}