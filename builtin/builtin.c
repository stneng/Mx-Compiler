#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
char* __mx_builtin_malloc(int size){
    return malloc(size);
}
void __mx_builtin_print(char* str){
    printf("%s",str);
}
void __mx_builtin_println(char* str){
    printf("%s\n",str);
}
void __mx_builtin_printInt(int n){
    printf("%d",n);
}
void __mx_builtin_printlnInt(int n){
    printf("%d\n",n);
}
char* __mx_builtin_getString(){
    char* a=malloc(sizeof(char)*2333);
    scanf("%s",a);
    return a;
}
int __mx_builtin_getInt(){
    int a;
    scanf("%d",&a);
    return a;
}
char* __mx_builtin_toString(int i){
    char* a=malloc(sizeof(char)*23);
    sprintf(a,"%d",i);
    return a;
}
int __mx_builtin_str_length(char* str){
    return strlen(str);
}
char* __mx_builtin_str_substring(char* str,int left,int right){
    char* a=malloc(sizeof(char)*(right-left+1));
    memcpy(a,str+left,sizeof(char)*(right-left));
    a[right-left]='\0';
    return a;
}
int __mx_builtin_str_parseInt(char* str){
    int a;
    sscanf(str,"%d",&a);
    return a;
}
int __mx_builtin_str_ord(char* str,int pos){
    return str[pos];
}
char* __mx_builtin_str_add(char* l,char* r){
    char* a=malloc(sizeof(char)*2333);
    strcpy(a,l);
    strcat(a,r);
    return a;
}
bool __mx_builtin_str_lt(char* l,char* r){
    return strcmp(l,r)<0;
}
bool __mx_builtin_str_gt(char* l,char* r){
    return strcmp(l,r)>0;
}
bool __mx_builtin_str_le(char* l,char* r){
    return strcmp(l,r)<=0;
}
bool __mx_builtin_str_ge(char* l,char* r){
    return strcmp(l,r)>=0;
}
bool __mx_builtin_str_eq(char* l,char* r){
    return strcmp(l,r)==0;
}
bool __mx_builtin_str_ne(char* l,char* r){
    return strcmp(l,r)!=0;
}
