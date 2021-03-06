/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_lelloman_lousyaudiolibrary_algorithm_Fft */

#ifndef _Included_com_lelloman_lousyaudiolibrary_algorithm_Fft
#define _Included_com_lelloman_lousyaudiolibrary_algorithm_Fft
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_lelloman_lousyaudiolibrary_algorithm_Fft
 * Method:    dummy
 * Signature: (Ljava/nio/ByteBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_dummy
  (JNIEnv *, jobject, jobject, jint);

JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_forward(JNIEnv *, jobject, jobject, jint);
JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_forwardMagnitude(JNIEnv *, jobject, jobject, jint);
JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_inverse(JNIEnv *, jobject, jobject, jint, jboolean);

/*
 * Class:     com_lelloman_lousyaudiolibrary_algorithm_Fft
 * Method:    testArrayCopySingleThread
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_testArrayCopySingleThread
        (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_lelloman_lousyaudiolibrary_algorithm_Fft
 * Method:    testArrayCopyMultiThread
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_lelloman_lousyaudiolibrary_algorithm_Fft_testArrayCopyMultiThread
        (JNIEnv *, jobject, jint, jint);

void fft(double*data, int size, int sinal);
void scale(double* data, int size, double factor);

#ifdef __cplusplus
}
#endif
#endif