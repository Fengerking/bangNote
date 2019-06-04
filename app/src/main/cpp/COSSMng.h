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

class COSSMng
{
public:
	COSSMng(void);
	virtual ~COSSMng(void);

    int     Init (JavaVM * jvm, JNIEnv* env, jclass clsOSS, jobject objOSS);
    int     Uninit (JNIEnv* env);

    char * 	getFileList (JNIEnv* env, char * pUser);
    int 	uploadFile (JNIEnv* env, char * pFileName);
    int 	downloadFile (JNIEnv* env, char * pFileName);

protected:
    JavaVM *			m_pjVM;
    jclass     			m_pjCls;
    jobject				m_pjObj;

    jmethodID			m_fPostEvent;

    char *				m_pFileList;

};


#endif // __COSSMng_H__
