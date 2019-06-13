/*******************************************************************************
	File:		CVideoRender.cpp

	Contains:	file info dialog implement code

	Written by:	Fenger King

	Change History (most recent first):
	2013-04-01		Fenger			Create file

*******************************************************************************/
#include "windows.h"
#include "commctrl.h"

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
	GetClientRect (m_hDlg, &rcDlg);
	SetWindowPos (m_hDlg, NULL, (nScreenX - rcDlg.right) / 2, (nScreenY - rcDlg.bottom) / 2 - 30, rcDlg.right, rcDlg.bottom, SWP_NOSIZE);
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
