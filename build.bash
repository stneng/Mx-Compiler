set ff=UNIX
set -e
mkdir out
find ./src -name *.java | javac -d out -cp /ulib/java/antlr-4.9.1-complete.jar @/dev/stdin
