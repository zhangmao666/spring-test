@echo off
chcp 65001 >nul 2>&1
set PROJECT_ROOT=d:\study_project\spring-boot-test
set SRC_DIR=%PROJECT_ROOT%\src\main\java\com\example\springboottest

echo === Project Analysis Report ===
echo Project: spring-boot-test
echo Root: %PROJECT_ROOT%
echo.

echo [Modules]
echo Listing modules under: %SRC_DIR%\modules
for /d %%d in (%SRC_DIR%\modules\*) do echo   - %%~nd
echo.

echo [Java File Count]
set /a count=0
for /r "%SRC_DIR%" %%f in (*.java) do set /a count+=1
echo Total Java files: %count%
echo.

echo [Lines of Code]
set /a lines=0
for /r "%SRC_DIR%" %%f in (*.java) do (
    for /f %%l in ('type "%%f" 2^>nul ^| find /v /c ""') do set /a lines+=%%l
)
echo Total lines of Java code: %lines%
echo.

echo [REST Controllers]
echo Searching for @RestController...
findstr /s /n "@RestController" "%SRC_DIR%\*.java" 2>nul
echo.

echo [API Endpoints]
echo Searching for @RequestMapping, @GetMapping, @PostMapping...
findstr /s /n "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" "%SRC_DIR%\*.java" 2>nul
echo.

echo [Configuration Files]
echo Files in resources:
for %%f in (%PROJECT_ROOT%\src\main\resources\*.yml %PROJECT_ROOT%\src\main\resources\*.yaml %PROJECT_ROOT%\src\main\resources\*.properties) do echo   - %%~nxf
echo.

echo [Dependencies]
echo Counting dependencies in pom.xml...
for /f %%c in ('findstr /c:"<dependency>" "%PROJECT_ROOT%\pom.xml" ^| find /v /c ""') do echo Total dependencies: %%c
echo.

echo === End ===
