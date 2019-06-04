/*******************************************************************************
	File:		COSSMng.cpp

	Contains:	aliyun oss implement code

	Written by:	Bangfei Jin

	Change History (most recent first):
	2019-06-03		Bangfei			Create file

*******************************************************************************/
#include <fstream>
#include <string>
#include <android/log.h>

#include "COSSMng.h"

#define  LOG_TAG "@@@noteOSS"
#define LOGW(...) ((int)__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))

#define OSS_MSG_FILE_LIST           0X10000001
#define OSS_MSG_FILE_PROG           0X10000010

using namespace AlibabaCloud::OSS;

std::string OSS_AccessKeyId     = "LTAIu16qahlahcz9";
std::string OSS_AccessKeySecret = "qVtgmjKL2pOOkB7MAAy8w0BKOaPX9N";
std::string OSS_Endpoint        = "oss-cn-shanghai.aliyuncs.com";
std::string OSS_BucketName      = "bangnote";

void ossProgressCallback(size_t increment, int64_t transfered, int64_t total, void* userData) {
    //LOGW ("Progress: % 8d,  % 8lld  % 8lld ", increment, transfered, total);
    COSSMng * pOssMng = (COSSMng *)userData;
    pOssMng->progCallback(increment, transfered, total);
}

COSSMng::COSSMng(void) {
    m_pjVM = NULL;
    m_pjCls = NULL;
    m_pjObj = NULL;
}

COSSMng::~COSSMng(void) {
}

int COSSMng::Init (JavaVM * jvm, JNIEnv* env, jclass clsOSS, jobject objOSS) {
    m_pjVM = jvm;
    m_pjCls = clsOSS;
    m_pjObj = objOSS;

    if (m_pjCls != NULL && m_pjObj != NULL) {
        m_fPostEvent = env->GetStaticMethodID(m_pjCls, "postEventFromNative",
                                              "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    }

    InitializeSdk();
    ClientConfiguration conf;
    m_pOssClient = new OssClient(OSS_Endpoint, OSS_AccessKeyId, OSS_AccessKeySecret, conf);

    memset (m_szUserID, 0, sizeof (m_szUserID));
    return 0;
}

int COSSMng::Uninit (JNIEnv* env) {
    ShutdownSdk();

    if (m_pjObj != NULL)
        env->DeleteGlobalRef(m_pjObj);
    m_pjObj = NULL;
    if (m_pjCls != NULL)
        env->DeleteGlobalRef(m_pjCls);
    m_pjCls = NULL;
    return 0;
}

int COSSMng::getFileList (JNIEnv* env, char * pUser) {
    m_pEnv = env;

    strcpy (m_szUserID, pUser);
    std::string ObjectName = "user/";
    ObjectName += pUser;

    ListObjectsRequest * request = new ListObjectsRequest(OSS_BucketName);
    request->setPrefix(ObjectName);

    auto outcome = m_pOssClient->ListObjects(*request);

    if (!outcome.isSuccess()) {
        LOGW ("Error code: %s, msg: %s", outcome.error().Code().c_str(), outcome.error().Message().c_str());
        return -1;
    }

    std::string strFileInfo;
    for (const auto& object : outcome.result().ObjectSummarys()) {
        LOGW ("File  Name: %s, % 8lld  %s", object.Key().c_str(), object.Size(), object.LastModified().c_str());
        strFileInfo = object.Key() + "|" + object.LastModified();

        if (m_fPostEvent != NULL) {
            jstring strInfo = env->NewStringUTF(strFileInfo.c_str());
            env->CallStaticVoidMethod(m_pjCls, m_fPostEvent, m_pjObj, OSS_MSG_FILE_LIST, (int)object.Size(), 0, strInfo);
        }
    }

    return 0;
}

int COSSMng::uploadFile (JNIEnv* env, char * pFileName) {
    m_pEnv = env;

    std::string strFileName = pFileName;
    int nPos = strFileName.rfind("/");
    strFileName = strFileName.substr(nPos, strFileName.size());
    std::string ObjectName = "user/";
    ObjectName =  ObjectName + m_szUserID + strFileName;

    std::shared_ptr<std::iostream> content = std::make_shared<std::fstream>(pFileName, std::ios::in | std::ios::binary);
    PutObjectRequest request(OSS_BucketName, ObjectName, content);

    TransferProgress progressCallback = { ossProgressCallback , this };
    request.setTransferProgress(progressCallback);

    auto outcome = m_pOssClient->PutObject(request);

    if (!outcome.isSuccess()) {
        LOGW ("Error code: %s, msg: %s", outcome.error().Code().c_str(), outcome.error().Message().c_str());
        return -1;
    }

    return 0;
}

int COSSMng::downloadFile (JNIEnv* env, char * pFileName, char * pFilePath) {
    m_pEnv = env;
    std::string ObjectName = "user/";
    ObjectName += m_szUserID;
    ObjectName += "/";
    ObjectName += pFileName;

    std::string outFileName = pFilePath;
    outFileName += pFileName;

    GetObjectRequest request(OSS_BucketName, ObjectName);
    request.setResponseStreamFactory([=]() {return std::make_shared<std::fstream>(outFileName, std::ios_base::out | std::ios_base::in | std::ios_base::trunc| std::ios_base::binary); });

    TransferProgress progressCallback = {ossProgressCallback , this };
    request.setTransferProgress(progressCallback);

    auto outcome = m_pOssClient->GetObject(request);

    if (!outcome.isSuccess()) {
        LOGW ("Error code: %s, msg: %s", outcome.error().Code().c_str(), outcome.error().Message().c_str());
        return -1;
    }

    return 0;
}

void COSSMng::progCallback(size_t increment, int64_t transfered, int64_t total) {
    if (m_fPostEvent == NULL || m_pjVM == NULL)
        return;

//    JNIEnv * env = NULL;
//    m_pjVM->AttachCurrentThread (&env, NULL);
    m_pEnv->CallStaticVoidMethod(m_pjCls, m_fPostEvent, m_pjObj, OSS_MSG_FILE_PROG, (int)transfered, (int)total, NULL);
//    m_pjVM->DetachCurrentThread();
}

