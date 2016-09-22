#include "volumereader.h"

#include <android/log.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "FFT_JNI", __VA_ARGS__)

static const double SHORT_MAX = 32768;

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_reader_volume_NativeVolumeReader_nextSample
        (JNIEnv * env, jobject thiz, jobject attrsDoubleNio, jobject attrsIntNio, jobject dataNio, jint dataSize, jobject sampleNio, jint sampleSize){

    double* doubleAttrs = (*env)->GetDirectBufferAddress(env, attrsDoubleNio);
    int* intAttrs = (*env)->GetDirectBufferAddress(env, attrsIntNio);
    double* data = (*env)->GetDirectBufferAddress(env, dataNio);
    unsigned char* samples = (*env)->GetDirectBufferAddress(env, sampleNio);

    int pcmCursor = intAttrs[0];
    int volumeCursor = intAttrs[1];
    double max = doubleAttrs[0];
    double pcmFramesPerVolumeFrame = doubleAttrs[1];

    for(int i=0;i<sampleSize;i += 2){
        unsigned char char1 = samples[i];
        unsigned char char2 = samples[i+1];

        signed short sample = (char2 << 8) + char1;
        //LOGE("sample = %d", sample);
        if(sample < 0){
            sample *= -1;
        }

        if(pcmCursor >= pcmFramesPerVolumeFrame){
            if(volumeCursor >= dataSize) break;

            double output = max / SHORT_MAX;
            pcmCursor = 0;
            data[volumeCursor++] = output;
            LOGE("output = %.2f", output);
            max = 0;
        }else{
            if(sample > max){
                max = sample;
                //LOGE("sample = %d, max = %f", sample, max);
            }
            pcmCursor++;
        }
    }
    intAttrs[0] = pcmCursor;
    intAttrs[1] = volumeCursor;
    doubleAttrs[0] = max;
    doubleAttrs[1] = pcmFramesPerVolumeFrame;
}

