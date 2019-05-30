#include <jni.h>
#include <fstream>
#include <android/log.h>
#include <alibabacloud/oss/OssClient.h>


using namespace AlibabaCloud::OSS;

#define  LOG_TAG "@@@BangNote"
#define LOGW(...) ((int)__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))

int uploadFile (const char * pUserName, const char * pFileName);
int listObjectName(const char * pUserName);
int downLoadFile(const char * pUserName, const char * pFileName);

std::string AccessKeyId     = "LTAIu16qahlahcz9";
std::string AccessKeySecret = "qVtgmjKL2pOOkB7MAAy8w0BKOaPX9N";
std::string Endpoint        = "oss-cn-shanghai.aliyuncs.com";
std::string BucketName      = "bangnote";

extern "C" JNIEXPORT jstring JNICALL
Java_com_wyhwl_bangnote_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    //uploadFile ("jin_bangfei", "/sdcard/bangnote/.data/txt_2019-05-21-19-22-28.bnt");
    //listObjectName ("jin_bangfei");
    downLoadFile ("jin_bangfei", "txt_2019-05-21-19-22-28.bnt");
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
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
    OssClient client(Endpoint, AccessKeyId, AccessKeySecret, conf);

    std::shared_ptr<std::iostream> content = std::make_shared<std::fstream>(pFileName, std::ios::in | std::ios::binary);
    PutObjectRequest request(BucketName, ObjectName, content);

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
    OssClient client(Endpoint, AccessKeyId, AccessKeySecret, conf);

    ListObjectsRequest * request = new ListObjectsRequest(BucketName);
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
    OssClient client(Endpoint, AccessKeyId, AccessKeySecret, conf);


    GetObjectRequest request(BucketName, ObjectName);
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
