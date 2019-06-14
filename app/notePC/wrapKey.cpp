// wrapKey.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include "pch.h"
#include <iostream>
#include "tchar.h"
#include "windows.h"

void	convertTimeNote(void);
int		readTextLine(char * pData, int nSize, char * pLine, int nLine);
int		copyPicFile(char * pPict, char * pDstp);

int		wrapkey(void);

int main()
{
	convertTimeNote();
	//wrapkey();
	return 0;
}

void convertTimeNote(void)
{
	char	szFile[256];
	strcpy(szFile, "Y:\\bang\\bangNote\\bangNote\\app\\notePC\\timenote\\timenote_all.txt");
	FILE * hFile = fopen(szFile, "rb");
	fseek(hFile, 0LL, SEEK_END);
	int nFileSize = ftell(hFile);
	fseek(hFile, 0, SEEK_SET);
	char * pData = new char[nFileSize + 1];
	int nRead = fread(pData, 1, nFileSize, hFile);
	fclose(hFile);


	char	szLine[8192];
	int		nLine = 0;
	int		nRest = nFileSize;
	char *	pBuff = pData;

	char	szTitle[256];
	char	szDate[32];
	char	szTime[32];
	char	szType[32];
	char	szText[81920];
	char	szPict[128];
	int		nPicts = 0;
	char *	pNext = NULL;
	char *	pEndp = NULL;

	int		nTypes = 0;
	char	szTypes[32][32];


	while (pBuff - pData < nFileSize) {
		nLine = readTextLine(pBuff, nRest, szLine, sizeof(szLine));
		pBuff += nLine;

		if (szLine[0] == '#' && szLine[1] == '[') {
			pNext = strchr(szLine, ']');
			if (pNext == NULL)
				continue;
			*pNext = 0;
			strcpy(szType, szLine + 2);
			strcpy(szTitle, pNext + 1);
			memset(szText, 0, sizeof(szText));
			memset(szPict, 0, sizeof(szPict));

			bool bFound = false;
			for (int i = 0; i < nTypes; i++) {
				if (strcmp(szType, szTypes[i]) == 0) {
					bFound = true;
					break;
				}
			}
			if (!bFound) {
				strcpy(szTypes[nTypes], szType);
				nTypes++;
			}
		}
		else if (szLine[0] == '#' && szLine[1] == '2') {
			pNext = strchr(szLine, ' ');
			*pNext = 0;
			strcpy(szDate, szLine + 1);
			memset(szTime, 0, sizeof(szTime));
			strncpy(szTime, pNext + 1, 5);
			strcat(szTime, ":00");
		}
		else if (strstr(szLine, "/storage/") == szLine) {
			continue;
		}
		else if (strstr(szLine, "*********************************") == szLine) {
		//	if (strlen(szPict) <= 0)
		//		continue;
			char	szDestBuff[81920];
			char *	pDestBuff = szDestBuff;

			memset(szDestBuff, 0, sizeof(szDestBuff));
			strcpy(pDestBuff, ("[noteTitle]\n"));
			strcat(pDestBuff, szTitle); strcat(pDestBuff, ("\n"));

			strcat(pDestBuff, ("[noteDate]\n"));
			strcat(pDestBuff, szDate); strcat(pDestBuff, ("\n"));

			strcat(pDestBuff, ("[noteTime]\n"));
			strcat(pDestBuff, szTime); strcat(pDestBuff, ("\n"));

			strcat(pDestBuff, ("[noteType]\n"));
			strcat(pDestBuff, szType); strcat(pDestBuff, ("\n"));

			strcat(pDestBuff, ("[noteText]\n"));
			strcat(pDestBuff, szText); strcat(pDestBuff, ("\n"));

			int nDestSize = strlen(szDestBuff);
			pDestBuff = szDestBuff;
			while (*pDestBuff != 0) {
				*pDestBuff = *pDestBuff ^ 'b';
				pDestBuff++;
			}

			char szFile[256];
			char szPath[256];
			strcpy(szPath, "Y:\\bang\\bangNote\\bangNote\\app\\notePC\\timenote\\data\\"); //"C:\\work\\bangnote\\data\\"
			int nHour = 0, nMinute = 0;
			sscanf(szTime, "%d:%d", &nHour, &nMinute);
			sprintf(szFile, "%stxt_%s-%02d-%02d-%02d.bnt", szPath, szDate, nHour, nMinute, rand()%100);
				
			FILE * hFile = fopen(szFile, "wb");
			fwrite(szDestBuff, 1, nDestSize, hFile);
			fclose(hFile);
		}
		else {
			char * pLine = szLine;
			pNext = strstr(pLine, "<img");
			if (pNext == NULL) {
				strcat(szText, pLine);
				strcat(szText, "\n");
				continue;
			}
			while (pNext != NULL) {
				if (pNext > pLine) {
					strncat(szText, pLine, pNext - pLine);
					strcat(szText, "\n");
				}

				pEndp = strstr(pLine, "/>");
				char * pFile = strstr(pLine, "/Picture/");
				memset(szPict, 0, sizeof(szPict));
				strncpy(szPict, pFile+9, pEndp - pFile - 10);

				SYSTEMTIME stTime;
				GetLocalTime(&stTime);
				char szDstPic[256];
				sprintf(szDstPic, "pic_%d-%02d-%02d-%02d-%02d-%02d_%02d.bnp", 
					stTime.wYear, stTime.wMonth, stTime.wDay, stTime.wHour, stTime.wMinute, stTime.wSecond, nPicts);
				
				strcat(szText, ("[notePict]\n"));
				strcat(szText, szDstPic); strcat(szText, "\n");

				//copyPicFile(szPict, szDstPic);
				nPicts++;

				if (strlen(pEndp) < 6)
					break;

				pLine = pEndp + 2;
				pNext = strstr(pLine, "<img");
				if (pNext == NULL) {
					strcat(szText, pLine);
					strcat(szText, "\n");
					break;
				}
			}
		}
	}

	delete[]pData;

	strcpy(szFile, "Y:\\bang\\bangNote\\bangNote\\app\\notePC\\timenote\\data\\");
	strcat(szFile, "noteType.txt");
	hFile = fopen(szFile, "wb");
	for (int i = 0; i < nTypes; i++) {
		strcat(szTypes[i], "\r\n");
		int nWrite = fwrite(szTypes[i], 1, strlen (szTypes[i]), hFile);
	}
	fclose(hFile);
}

int	copyPicFile(char * pPict, char * pDstp)
{
	char	szFile[256];
	strcpy(szFile, "Y:\\bang\\bangNote\\bangNote\\app\\notePC\\timenote\\Picture\\");
	strcat(szFile, pPict);

	FILE * hFile = fopen(szFile, "rb");
	fseek(hFile, 0LL, SEEK_END);
	int nFileSize = ftell(hFile);
	fseek(hFile, 0, SEEK_SET);
	char * pData = new char[nFileSize + 1];
	int nRead = fread(pData, 1, nFileSize, hFile);
	fclose(hFile);

	strcpy(szFile, "Y:\\bang\\bangNote\\bangNote\\app\\notePC\\timenote\\data\\");
	strcat(szFile, pDstp);

	hFile = fopen(szFile, "wb");
	for (int i = 0; i < nFileSize; i++)
		pData[i] = pData[i] ^ 'b';
	
	int nWrite = fwrite(pData, 1, nFileSize, hFile);
	fclose(hFile);

	return 0;
}

int	readTextLine(char * pData, int nSize, char * pLine, int nLine)
{
	if (pData == NULL)
		return 0;

	char * pBuff = pData;
	while (pBuff - pData < nSize)
	{
		if (*pBuff == '\r' || *pBuff == '\n')
		{
			pBuff++;
			if (*(pBuff) == '\r' || *(pBuff) == '\n')
				pBuff++;
			break;
		}
		pBuff++;
	}

	int nLineLen = pBuff - pData;
	if (nLine > nLineLen)
	{
		int nRNLen = 0;
		pBuff--;
		while (pBuff > pData && (*pBuff == '\r' || *pBuff == '\n'))
		{
			nRNLen++;
			pBuff--;
		}

		memset(pLine, 0, nLine);
		strncpy(pLine, pData, nLineLen - nRNLen);
	}
	return nLineLen;
}

int wrapkey(void)
{
	char	szFolder[256];
	char	szFilter[1024];
	char	szFile[256];
	strcpy(szFolder, "Y:\\bang\\bangNote\\bangNote\\app\\notePC\\timenote\\bangnote\\");
	strcpy(szFilter, szFolder);
	strcat(szFilter, ("*.*"));

	unsigned char * pData = new unsigned char[1024 * 1024 * 2];
	int				nRead = 0;

	WIN32_FIND_DATA  data;
	HANDLE  hFind = FindFirstFile(szFilter, &data);
	if (hFind == INVALID_HANDLE_VALUE)
		return 0;

	do
	{
		if (!strcmp(data.cFileName, (".")) || !strcmp(data.cFileName, ("..")))
			continue;
		if ((data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) == FILE_ATTRIBUTE_DIRECTORY)
			continue;

		strcpy(szFile, szFolder);
		strcat(szFile, data.cFileName);

		FILE * hFile = fopen(szFile, "rb");
		fseek(hFile, 0LL, SEEK_END);
		int nFileSize = ftell(hFile);
		fseek(hFile, 0, SEEK_SET);
		nRead = fread(pData, 1, nFileSize, hFile);
		fclose(hFile);

		for (int i = 0; i < nRead; i++)
			pData[i] = pData[i] ^ 'b';

		hFile = fopen(szFile, "wb");
		nRead = fwrite(pData, 1, nFileSize, hFile);
		fclose(hFile);

	} while (FindNextFile(hFind, &data));

	FindClose(hFind);
	return 0;
}