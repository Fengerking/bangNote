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
#include <alibabacloud/oss/OssClient.h>

#include "COSSMng.h"

#define  LOG_TAG "@@@BangNote"
#define LOGW(...) ((int)__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))

using namespace AlibabaCloud::OSS;

std::string OSS_AccessKeyId     = "LTAIu16qahlahcz9";
std::string OSS_AccessKeySecret = "qVtgmjKL2pOOkB7MAAy8w0BKOaPX9N";
std::string OSS_Endpoint        = "oss-cn-shanghai.aliyuncs.com";
std::string OSS_BucketName      = "bangnote";



int uploadFile (const char * pUserName, const char * pFileName);
int listObjectName(const char * pUserName);
int downLoadFile(const char * pUserName, const char * pFileName);

COSSMng::COSSMng(void)
{
    m_pjVM = NULL;
    m_pjCls = NULL;
    m_pjObj = NULL;
    m_pFileList = NULL;
}

COSSMng::~COSSMng(void)
{
    if (m_pFileList != NULL)
        delete []m_pFileList;
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

    return 0;
}

int COSSMng::Uninit (JNIEnv* env)
{
    ShutdownSdk();

    if (m_pjObj != NULL)
        env->DeleteGlobalRef(m_pjObj);
    m_pjObj = NULL;
    if (m_pjCls != NULL)
        env->DeleteGlobalRef(m_pjCls);
    m_pjCls = NULL;
}

char * COSSMng::getFileList (JNIEnv* env, char * pUser)
{
    return m_pFileList;
}

int COSSMng::uploadFile (JNIEnv* env, char * pFileName)
{
    return 0;
}

int COSSMng::downloadFile (JNIEnv* env, char * pFileName)
{
    return 0;
}


void ProgressCallback(size_t increment, int64_t transfered, int64_t total, void* userData) {
    LOGW ("Progress: % 8d,  % 8lld  % 8lld ", increment, transfered, total);
}

int uploadFile (const char * pUserName, const char * pFileName) {
    std::string strFileName = pFileName;
    int nPos = strFileName.rfind("/");
    strFileName = strFileName.substr(nPos, strFileName.size());
    std::string ObjectName = "user/";
    ObjectName =  ObjectName + pUserName + strFileName;

    InitializeSdk();

    ClientConfiguration conf;
    OssClient client(OSS_Endpoint, OSS_AccessKeyId, OSS_AccessKeySecret, conf);

    std::shared_ptr<std::iostream> content = std::make_shared<std::fstream>(pFileName, std::ios::in | std::ios::binary);
    PutObjectRequest request(OSS_BucketName, ObjectName, content);

    TransferProgress progressCallback = { ProgressCallback , nullptr };
    request.setTransferProgress(progressCallback);

    auto outcome = client.PutObject(request);

    if (!outcome.isSuccess()) {
        LOGW ("Error code: %s, msg: %s", outcome.error().Code().c_str(), outcome.error().Message().c_str());
        ShutdownSdk();
        return -1;
    }

    ShutdownSdk();
    return 0;
}


int listObjectName(const char * pUserName) {
    std::string ObjectName = "user/";
    ObjectName += pUserName;

    InitializeSdk();

    ClientConfiguration conf;
    OssClient client(OSS_Endpoint, OSS_AccessKeyId, OSS_AccessKeySecret, conf);

    ListObjectsRequest * request = new ListObjectsRequest(OSS_BucketName);
    request->setPrefix(ObjectName);

    auto outcome = client.ListObjects(*request);

    if (!outcome.isSuccess()) {
        LOGW ("Error code: %s, msg: %s", outcome.error().Code().c_str(), outcome.error().Message().c_str());
        ShutdownSdk();
        return -1;
    } else {
        for (const auto& object : outcome.result().ObjectSummarys()) {
            LOGW ("File  Name: %s, % 8lld  %s", object.Key().c_str(), object.Size(), object.LastModified().c_str());
        }
    }

    ShutdownSdk();
    return 0;
}

int downLoadFile(const char * pUserName, const char * pFileName) {

    std::string ObjectName = "user/";
    ObjectName += pUserName;
    ObjectName += "/";
    ObjectName += pFileName;

    std::string outFileName = "/sdcard/bangnote/.data/111.bnt";


    InitializeSdk();

    ClientConfiguration conf;
    OssClient client(OSS_Endpoint, OSS_AccessKeyId, OSS_AccessKeySecret, conf);


    GetObjectRequest request(OSS_BucketName, ObjectName);
    request.setResponseStreamFactory([=]() {return std::make_shared<std::fstream>(outFileName, std::ios_base::out | std::ios_base::in | std::ios_base::trunc| std::ios_base::binary); });

    TransferProgress progressCallback = { ProgressCallback , nullptr };
    request.setTransferProgress(progressCallback);

    auto outcome = client.GetObject(request);

    if (!outcome.isSuccess()) {
        LOGW ("Error code: %s, msg: %s", outcome.error().Code().c_str(), outcome.error().Message().c_str());
        ShutdownSdk();
        return -1;
    }

    ShutdownSdk();
    return 0;
}
