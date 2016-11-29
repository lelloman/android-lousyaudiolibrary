#include <android/log.h>
#include <pthread.h>
#include "math.h"
#include <stdbool.h>

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "JNI_DEBUGGING", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,   "JNI_DEBUGGING", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)
#include "fft.h"
#include "phasevocoder.h"

#define PI 3.141592653589793
#define PI2 6.283185307179586
#define N_THREADS 3


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
        spec1[i] = window[i] * buffer[i];
    }
    for(int i=halfN;i<N;i++){
        spec1[i] = 0;
    }
    fft(spec1, size,1);


    for(int i=0;i<halfN;i++){
        spec2[i] = window[i] * buffer[i + offset];
    }
    for(int i=halfN;i<N;i++){
        spec2[i] = 0;
    }

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

    fft(spec2, N, -1);

    scale(spec2, size,size/4);

    for (int i = 0; i < N; i++) {
        sigout[i] += window[i] * spec2[i];
    }
}

typedef struct{
    int id;
    void* data1;
    void* data2;
    void* data3;
    int size1;
    int size2;
    int size3;
    bool wait_for_start;
    bool task_completed;
    pthread_mutex_t* mutex_start;
    pthread_cond_t* cond_start;
    pthread_mutex_t* mutex_end;
    pthread_cond_t* cond_end;
} ThreadArg;

static pthread_t threads[N_THREADS];
static ThreadArg args[N_THREADS];
static bool threads_init = false;

void thread_shift_sig_out(void *threadArg){

    ThreadArg *arg = (ThreadArg *) threadArg;
    double *sigout = arg->data1;
    int NmH = arg->size1;
    int H = arg->size2;
    int N = arg->size3;
    pthread_mutex_t *mutex_start = arg->mutex_start;
    pthread_cond_t *cond_start = arg->cond_start;
    pthread_mutex_t *mutex_end = arg->mutex_end;
    pthread_cond_t *cond_end = arg->cond_end;

    for(;;) {
        pthread_mutex_lock(mutex_start);
        if(arg->wait_for_start)
            pthread_cond_wait(cond_start, mutex_start);
        pthread_mutex_unlock(mutex_start);

        for (int i = 0; i < NmH; i++) {
            sigout[i] = sigout[i + H];
        }
        for (int i = NmH; i < N; i++) {
            sigout[i] = 0;
        }

        LOGE("thread %d finish about to signal", arg->id);
        pthread_mutex_lock(mutex_end);
        arg->task_completed = true;
        arg->wait_for_start = true;
        pthread_cond_signal(cond_end);
        pthread_mutex_unlock(mutex_end);
    }
}

void thread_make_spec_and_fft(void *threadArg){
    ThreadArg *arg = (ThreadArg*) threadArg;

    int halfN = arg->size1;
    int N = arg->size2;
    int offset = arg->size3;
    double* spec = arg->data1;
    double* window = arg->data2;
    double* buffer = arg->data3;
    pthread_mutex_t *mutex = arg->mutex_start;
    pthread_cond_t *cond = arg->cond_start;
    pthread_mutex_t *mutex_end = arg->mutex_end;
    pthread_cond_t *cond_end = arg->cond_end;

    for(;;) {
        pthread_mutex_lock(mutex);
        if(arg->wait_for_start)
            pthread_cond_wait(cond, mutex);
        pthread_mutex_unlock(mutex);

        for (int i = 0; i < halfN; i++) {
            spec[i] = window[i] * buffer[i + offset];
        }
        for (int i = halfN; i < N; i++) {
            spec[i] = 0;
        }
        fft(spec, N, 1);

        LOGE("thread %d finish about to signal", arg->id);
        pthread_mutex_lock(mutex_end);
        arg->task_completed = true;
        arg->wait_for_start = true;
        pthread_cond_signal(cond_end);
        pthread_mutex_unlock(mutex_end);
    }
}

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_phasevocoder_NativePhaseVocoderMultiThread_next__Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2Ljava_nio_ByteBuffer_2II
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
    int N2 = N*2;
    int H = offset;
    int NmH = N-H;


    ThreadArg *argSigout = &args[0];
    argSigout->data1 = sigout;
    argSigout->size1 = NmH;
    argSigout->size2 = H;
    argSigout->size3 = N;

    ThreadArg *argSpec1 = &args[1];
    argSpec1->size1 = halfN;
    argSpec1->size2 = N;
    argSpec1->size3 = 0;
    argSpec1->data1 = spec1;
    argSpec1->data2 = window;
    argSpec1->data3 = buffer;

    ThreadArg *argSpec2 = &args[2];
    argSpec2->size1 = halfN;
    argSpec2->size2 = N;
    argSpec2->size3 = H;
    argSpec2->data1 = spec2;
    argSpec2->data2 = window;
    argSpec2->data3 = buffer;

    if(!threads_init) {
        threads_init = true;
        LOGE("THREAD INIT");
        for(int i=0;i<N_THREADS;i++){
            ThreadArg *arg = &args[i];
            arg->mutex_start = (pthread_mutex_t*) malloc(sizeof(pthread_mutex_t));
            arg->cond_start = (pthread_cond_t*) malloc(sizeof(pthread_cond_t));
            arg->mutex_end = (pthread_mutex_t*) malloc(sizeof(pthread_mutex_t));
            arg->cond_end = (pthread_cond_t*) malloc(sizeof(pthread_cond_t));
            arg->id = i;
            pthread_mutex_init(arg->mutex_start, NULL);
            pthread_cond_init(arg->cond_start, NULL);
            pthread_mutex_init(arg->mutex_end, NULL);
            pthread_cond_init(arg->cond_end, NULL);

            pthread_mutex_lock(arg->mutex_start);
            arg->task_completed = false;
            arg->wait_for_start = true;
        }


        pthread_create(&threads[0], NULL, &thread_shift_sig_out, argSigout);
        pthread_create(&threads[1], NULL, &thread_make_spec_and_fft, argSpec1);
        pthread_create(&threads[2], NULL, &thread_make_spec_and_fft, argSpec2);
    }else{
        for(int i=0;i<N_THREADS;i++){
          pthread_mutex_lock(args[i].mutex_start);
            args[i].task_completed = false;
            args[i].wait_for_start = true;
        }
    }


    for(int i=0;i<N_THREADS;i++){
        ThreadArg *arg = &args[i];
        LOGE("unleash thread %d", i);
        arg->wait_for_start = false;
        pthread_mutex_unlock(arg->mutex_start);
        pthread_cond_signal(arg->cond_start);
    }
    LOGE("unleash finished");
   // usleep(10);
    for(int i=0;i<N_THREADS;i++){
        ThreadArg *arg = &args[i];
        LOGE("wait for thread %d to finish", i);
        pthread_mutex_lock(arg->mutex_end);
        if(!arg->task_completed)
            pthread_cond_wait(arg->cond_end, arg->mutex_end);
        pthread_mutex_unlock(arg->mutex_end);
    }

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

    fft(spec2, N, -1);

    double factor = N/2;
    scale(spec2, N, factor);

    for (int i = 0; i < N; i++)
        sigout[i] += window[i] * spec2[i];
}