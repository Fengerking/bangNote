/*******************************************************************************
	File:		COSSMng.h

	Contains:	the aliyun oss class of all objects.

	Written by:	Bangfei Jin

	Change History (most recent first):
	2019-06-03		Bangfei			Create file

*******************************************************************************/
#ifndef __COSSMng_H__
#define __COSSMng_H__

#include "stdio.h"
#include "string.h"
#include "jni.h"

#include <alibabacloud/oss/OssClient.h>

using namespace AlibabaCloud::OSS;

class COSSMng {
public:
	COSSMng(void);
	virtual ~COSSMng(void);

    int     Init (JavaVM * jvm, JNIEnv* env, jclass clsOSS, jobject objOSS);
    int     Uninit (JNIEnv* env);

	int 	getFileList (JNIEnv* env, char * pUser);
    int 	uploadFile (JNIEnv* env, char * pFileName);
    int 	downloadFile (JNIEnv* env, char * pFileName, char * pFilePath);

	void 	progCallback(size_t increment, int64_t transfered, int64_t total);

protected:
    JavaVM *			m_pjVM;
    jclass     			m_pjCls;
    jobject				m_pjObj;
	JNIEnv*				m_pEnv;

    jmethodID			m_fPostEvent;

	OssClient *			m_pOssClient;
	char 				m_szUserID[256];
};


#endif // __COSSMng_H__
