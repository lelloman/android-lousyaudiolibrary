//
// Created by lelloman on 15-9-16.
//

#include "main.h"

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_dummy
  (JNIEnv * env, jobject thiz, jobject byteBuffer, jint size){

  double* data = (double*) (*env)->GetDirectBufferAddress(env,byteBuffer);
    for(int i=0;i<size;i++){

        data[i] = 3.14;
    }
  }