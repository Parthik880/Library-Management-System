$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
$savedUrl = $env:LIBRARY_DB_URL
try {
    mvn -f librarian/pom.xml clean compile
    if ($LASTEXITCODE -ne 0) { throw 'Librarian compilation failed.' }
    mvn -f student/pom.xml clean compile
    if ($LASTEXITCODE -ne 0) { throw 'Student compilation failed.' }
    mvn -f librarian/pom.xml dependency:build-classpath '-Dmdep.outputFile=target/check-classpath.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Dependency lookup failed.' }
    $dependencies = (Get-Content librarian/target/check-classpath.txt -Raw).Trim()
    $separator = [IO.Path]::PathSeparator
    $classpath = "$projectRoot/librarian/target/classes$separator$projectRoot/student/target/classes$separator$dependencies"
    $schemaName = 'library_check_' + [guid]::NewGuid().ToString('N')
    $baseUrl = $savedUrl
    if (-not $baseUrl) { $baseUrl = 'jdbc:postgresql://localhost:5432/library_db' }
    if ($baseUrl -match 'currentSchema=') { throw 'Run checks without a currentSchema URL parameter.' }
    $join = '?'
    if ($baseUrl.Contains('?')) { $join = '&' }
    $env:LIBRARY_DB_URL = "$baseUrl" + "$join" + "currentSchema=$schemaName"
    javac -encoding UTF-8 -cp $classpath -d librarian/target/checks tests/LibraryCheck.java tests/UiCheck.java
    if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed.' }
    java -cp "$projectRoot/librarian/target/checks$separator$classpath" LibraryCheck $schemaName database/library.sql
    if ($LASTEXITCODE -ne 0) { throw 'Integration checks failed.' }
    java -cp "$projectRoot/librarian/target/checks$separator$classpath" UiCheck $schemaName database/library.sql
    if ($LASTEXITCODE -ne 0) { throw 'Swing UI checks failed.' }
} finally {
    $env:LIBRARY_DB_URL = $savedUrl
    Pop-Location
}
