/*******************************************************************************
	File:		CVideoRender.cpp

	Contains:	file info dialog implement code

	Written by:	Fenger King

	Change History (most recent first):
	2013-04-01		Fenger			Create file

*******************************************************************************/
#include "windows.h"
#include "windowsx.h"
#include "commctrl.h"
#include "tchar.h"

#include "stdio.h"

#include "CDlgNote.h"
#include "resource.h"

CDlgNote::CDlgNote(HINSTANCE hInst, HWND hParent)
	: m_hInst (hInst)
	, m_hParent (hParent)
	, m_hDlg (NULL)
	, m_hBrushBG (NULL)
{
}

CDlgNote::~CDlgNote(void)
{
	if (m_hDlg != NULL)
		DestroyWindow (m_hDlg);
}

int CDlgNote::CreateDlg (void)
{
	m_hDlg = CreateDialog(m_hInst, MAKEINTRESOURCE(IDD_NOTE), m_hParent, baseDlgProc);
	if (m_hDlg == NULL)
		return -1;
	SetWindowLong(m_hDlg, GWL_USERDATA, (LONG)this);

	RECT rcDlg;
	GetClientRect(m_hDlg, &rcDlg);
	SetWindowPos(m_hParent, NULL, 0, 0, rcDlg.right, rcDlg.bottom, SWP_NOMOVE);
	CenterDlg();
	initDlg();
	ShowWindow(m_hDlg, SW_SHOW);

	return 0;
}

INT_PTR CDlgNote::OnReceiveMsg (HWND hDlg, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	return FALSE;
}

bool CDlgNote::CenterDlg (void)
{
	if (m_hDlg == NULL)
		return false;
	int		nScreenX = GetSystemMetrics (SM_CXSCREEN);
	int		nScreenY = GetSystemMetrics (SM_CYSCREEN);
	RECT	rcDlg;
	GetClientRect (m_hParent, &rcDlg);
	SetWindowPos (m_hParent, NULL, (nScreenX - rcDlg.right) / 2, (nScreenY - rcDlg.bottom) / 2 - 30, rcDlg.right, rcDlg.bottom, SWP_NOSIZE);
	return true;
}

INT_PTR CALLBACK CDlgNote::baseDlgProc(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
	int				wmId, wmEvent;
	CDlgNote *		pDlgBase = NULL;

	if (hDlg != NULL)
		pDlgBase = (CDlgNote *)GetWindowLong (hDlg, GWL_USERDATA);

	switch (message)
	{
	case WM_INITDIALOG:
		if (lParam != NULL)
		{
			SetWindowLong (hDlg, GWL_USERDATA, lParam);
			pDlgBase = (CDlgNote *)lParam;
			pDlgBase->m_hDlg = hDlg;
		}
		break;

	case WM_COMMAND:
		wmId    = LOWORD(wParam);
		wmEvent = HIWORD(wParam);

		switch (wmId)
		{
		case IDOK:
		case IDCANCEL:
			EndDialog(hDlg, LOWORD(wParam));
			break;

		case IDC_BTN_SAVE:
			pDlgBase->saveNote();
			break;

		default:
			break;
		}
		break;

	case WM_ERASEBKGND:
		if (pDlgBase != NULL && pDlgBase->m_hBrushBG != NULL)
		{
			RECT	rcDlg;
			GetClientRect (hDlg, &rcDlg);
			FillRect ((HDC)wParam, &rcDlg, pDlgBase->m_hBrushBG);
			return S_FALSE;
		}
		return S_OK;

	default:
		break;
	}

	if (pDlgBase != NULL)
		return pDlgBase->OnReceiveMsg (hDlg, message, wParam, lParam);
	else
		return (INT_PTR)FALSE;
}

int CDlgNote::initDlg(void)
{
	HWND hCmbType = GetDlgItem(m_hDlg, IDC_COMBO_TYPE);
	ComboBox_AddString(hCmbType, _T("日常杂事"));
	ComboBox_AddString(hCmbType, _T("股票心得"));
	ComboBox_AddString(hCmbType, _T("笔记记录"));
	ComboBox_AddString(hCmbType, _T("默认笔记"));
	ComboBox_AddString(hCmbType, _T("播放器"));

	ComboBox_SetText(hCmbType, _T("默认笔记"));

	return 0;
}

int CDlgNote::saveNote(void)
{
	TCHAR	szBuff[8192];
	TCHAR *	pBuff = szBuff;
	TCHAR	szItem[8192];
	memset(szBuff, 0, sizeof(szBuff));

	_tcscpy(pBuff, _T("[noteTitle]\n"));
	memset(szItem, 0, sizeof(szItem));
	GetDlgItemText(m_hDlg, IDC_EDIT_TITLE, szItem, sizeof(szItem));
	_tcscat(pBuff, szItem);
	_tcscat(pBuff, _T("\n"));

	SYSTEMTIME stDate = { 0 };
	SendMessage(GetDlgItem (m_hDlg, IDC_NOTEDATE), DTM_GETSYSTEMTIME, 0, (LPARAM)&stDate);
	_tcscat(pBuff, _T("[noteDate]\n"));
	_stprintf(szItem, _T("%d-%02d-%02d\n"), stDate.wYear, stDate.wMonth, stDate.wDay);
	_tcscat(pBuff, szItem);

	SYSTEMTIME stTime = { 0 };
	SendMessage(GetDlgItem(m_hDlg, IDC_NOTETIME), DTM_GETSYSTEMTIME, 0, (LPARAM)&stTime);
	_tcscat(pBuff, _T("[noteTime]\n"));
	_stprintf(szItem, _T("%02d:%02d:%02d\n"), stTime.wHour, stTime.wMinute, stTime.wSecond);
	_tcscat(pBuff, szItem);

	_tcscat(pBuff, _T("[noteType]\n"));
	memset(szItem, 0, sizeof(szItem));
	GetDlgItemText(m_hDlg, IDC_COMBO_TYPE, szItem, sizeof(szItem));
	_tcscat(pBuff, szItem);
	_tcscat(pBuff, _T("\n"));

	_tcscat(pBuff, _T("[noteText]\n"));
	memset(szItem, 0, sizeof(szItem));
	GetDlgItemText(m_hDlg, IDC_EDIT_CONTENT, szItem, sizeof(szItem));

	TCHAR * pItem = szItem;
	pBuff = szBuff + _tcslen(szBuff);
	while (*pItem != 0) {
		if (*pItem == _T('\r')) {
			pItem++;
			continue;
		}
		*pBuff++ = *pItem++;
	}
	_tcscat(pBuff, _T("\n"));

	char szUTF8[8192];
	memset(szUTF8, 0, sizeof(szUTF8));
	WideCharToMultiByte(CP_UTF8, 0, szBuff, -1, szUTF8, sizeof(szUTF8), NULL, NULL);

	char * pData = szUTF8;
	while (*pData != 0) {
		*pData = *pData ^ 'b';
		pData++;
	}

	//yyyy - MM - dd - HH - mm - ss");
	GetLocalTime(&stTime);
	char szFile[256];
	char szPath[256];
	strcpy(szPath, "Y:\\bang\\bangNote\\data\\"); //"C:\\work\\bangnote\\data\\"
	sprintf(szFile, "%stxt_%d-%02d-%02d-%02d-%02d-%02d.bnt", szPath,
						stTime.wYear, stTime.wMonth, stTime.wDay, stTime.wHour, stTime.wMinute, stTime.wSecond);

	FILE * hFile = fopen(szFile, "wb");
	fwrite(szUTF8, 1, strlen(szUTF8), hFile);
	fclose(hFile);

	MessageBox(m_hParent, _T("已保存！"), _T("保存"), MB_OK);

	return 0;
}

