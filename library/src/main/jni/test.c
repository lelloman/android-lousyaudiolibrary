
#include <pthread.h>

static const int N = 2;

void arrayCopySingleThread(int* data1, int* data2, int size){
    for(int i=0;i<size;i++){
        data2[i] = data1[i];
    }
}

struct copyArgs {
    int* data1;
    int* data2;
    int size;
    int threadId;
};

void copy(void* arg){
    struct copyArgs* args = arg;

    for(int i=args->threadId;i<args->size;i+= N){
        args->data1[i] = args->data2[i];
    }

    free(arg);
}


void arrayCopyMultiThread(int* data1, int* data2, int size){

    pthread_t my_thread[N];

    for(int i=0;i<N;i++) {
        struct copyArgs* arg = malloc(sizeof(struct copyArgs));
        arg->data1 = data1;
        arg->data2 = data2;
        arg->size = size;
        arg->threadId = i;
        int ret = pthread_create(&my_thread[i], NULL, &copy, arg);
    }
}
