/*******************************************************************************
	File:		CDlgNote.h

	Contains:	Open URL dialog header file

	Written by:	Fenger King

	Change History (most recent first):
	2013-10-21		Fenger			Create file

*******************************************************************************/
#ifndef __CDlgNote_H__
#define __CDlgNote_H__

#define		WM_TIMER_ANALYSE	601

class CDlgNote
{
public:
	CDlgNote (HINSTANCE hInst, HWND hParent);
	virtual ~CDlgNote(void);

	virtual int				CreateDlg (void);
	virtual HWND			GetDlg (void) {return m_hDlg;}

protected:
	virtual INT_PTR			OnReceiveMsg (HWND hDlg, UINT uMsg, WPARAM wParam, LPARAM lParam);
	virtual bool			CenterDlg (void);

	virtual int				initDlg(void);
	virtual int				saveNote(void);

protected:
	HINSTANCE				m_hInst;
	HWND					m_hParent;
	HWND					m_hDlg;
	HBRUSH					m_hBrushBG;

public:
	static INT_PTR CALLBACK baseDlgProc (HWND, UINT, WPARAM, LPARAM);

};
#endif //__CDlgNote_H__

