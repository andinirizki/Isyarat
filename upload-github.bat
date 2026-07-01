@echo off
cd /d "%~dp0"

echo ================================
echo Upload Project Android ke GitHub
echo ================================

git add .

git commit -m "Update project otomatis"

git push

echo.
echo Selesai upload ke GitHub.
pause