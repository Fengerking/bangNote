#include <jni.h>
#include <string>
#include "COSSMng.h"

extern "C" JNIEXPORT jlong JNICALL
Java_com_wyhwl_bangnote_base_noteAliyunOSS_initOSS (JNIEnv* env, jobject obj, jobject oss) {
    JavaVM * 	jvm = 0;
    jobject 	envobj;

    env->GetJavaVM(&jvm);
    jclass clazz = env->GetObjectClass(obj);
    jclass clsOSS = (jclass)env->NewGlobalRef(clazz);
    jobject objOSS  = env->NewGlobalRef(oss);

    COSSMng * pOss = new COSSMng ();
    pOss->Init(jvm, env, clsOSS, objOSS);
    env->DeleteLocalRef(clazz);

    return (long)pOss;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_wyhwl_bangnote_base_noteAliyunOSS_uninitOSS (JNIEnv* env, jobject obj, jlong oss) {
    COSSMng * pOss = (COSSMng *)oss;
    pOss->Uninit(env);
    delete pOss;
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_wyhwl_bangnote_base_noteAliyunOSS_getFileList (JNIEnv* env, jobject obj, jlong oss, jstring strUser) {
    COSSMng * pOss = (COSSMng *)oss;
    char * pUser = (char *) env->GetStringUTFChars(strUser, NULL);
    return pOss->getFileList(env, pUser);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_wyhwl_bangnote_base_noteAliyunOSS_uploadFile (JNIEnv* env, jobject obj, jlong oss, jstring strFile) {
    COSSMng * pOss = (COSSMng *)oss;
    char * pFileName = (char *) env->GetStringUTFChars(strFile, NULL);
    return pOss->uploadFile(env, pFileName);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_wyhwl_bangnote_base_noteAliyunOSS_downloadFile (JNIEnv* env, jobject obj, jlong oss, jstring strFile, jstring strPath) {
    COSSMng * pOss = (COSSMng *)oss;
    char * pFileName = (char *) env->GetStringUTFChars(strFile, NULL);
    char * pFilePath = (char *) env->GetStringUTFChars(strPath, NULL);
    return pOss->downloadFile(env, pFileName, pFilePath);
}

