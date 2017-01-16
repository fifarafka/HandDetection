#include <com_example_monikawojtasik_setupopencbv_NativeClass.h>
#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <vector>

using namespace std;
using namespace cv;

JNIEXPORT jdouble JNICALL Java_com_example_monikawojtasik_setupopencbv_NativeClass_findInscribedCircleJNI
        (JNIEnv* env, jobject obj, jlong imgAddr,
         jdouble rectTLX, jdouble rectTLY, jdouble rectBRX, jdouble rectBRY,
         jdoubleArray incircleX, jdoubleArray incircleY, jlong contourAddr) {
         jdouble some = 3.14;
         return some;
}

