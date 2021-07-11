#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
const int __mx_builtin_gc_pool_size=100*1024;
unsigned int* __mx_builtin_gc_sps;
unsigned int __mx_builtin_gc_r_sp() {
    unsigned int x;
    asm volatile("mv %0, sp" : "=r"(x));
    return x;
}
int __mx_builtin_gc_a[1003],__mx_builtin_gc_sz[1003],__mx_builtin_gc_en=0;
int __mx_builtin_gc_find(int x){
    int l=0,r=__mx_builtin_gc_en-1,ans=-1;
    while (l<=r){
        int m=(l+r)>>1;
        if (__mx_builtin_gc_a[m]<=x){
            l=m+1;
            ans=m;
        }else{
            r=m-1;
        }
    }
    if (ans>=0 && (__mx_builtin_gc_a[ans]==x || __mx_builtin_gc_a[ans]+4==x)) return ans;
    else return -1;
    /*int l=0,r=__mx_builtin_gc_en-1;
    while (l<=r){
        int m=(l+r)>>1;
        if (__mx_builtin_gc_a[m]==x) return m;
        if (__mx_builtin_gc_a[m]<x){
            l=m+1;
        }else{
            r=m-1;
        }
    }
    l=0;r=__mx_builtin_gc_en-1;x-=4;
    while (l<=r){
        int m=(l+r)>>1;
        if (__mx_builtin_gc_a[m]==x) return m;
        if (__mx_builtin_gc_a[m]<x){
            l=m+1;
        }else{
            r=m-1;
        }
    }
    return -1;*/
}
int __mx_builtin_gc_malloced=0;
int __mx_builtin_gc_abs(int a){return a>=0?a:-a;}
void __mx_builtin_gc_before();
void __mx_builtin_gc(){
    /*unsigned int* spt=(unsigned int*)__mx_builtin_gc_r_sp();
    for (unsigned int* p=spt;p<__mx_builtin_gc_sps;p++){
        int t=__mx_builtin_gc_find(*p);
        if (t>=0 && __mx_builtin_gc_sz[t]>0){
            __mx_builtin_gc_sz[t]=-__mx_builtin_gc_sz[t];
        }
    }
    bool chk=true;
    while (chk){
        chk=false;
        for (int i=0;i<__mx_builtin_gc_en;i++) if (__mx_builtin_gc_a[i]>0 && __mx_builtin_gc_sz[i]<0 && (-__mx_builtin_gc_sz[i])%4==0){
            for (unsigned int* p=(unsigned int*)__mx_builtin_gc_a[i];p<(unsigned int*)(__mx_builtin_gc_a[i]+(-__mx_builtin_gc_sz[i]));p++){
                int t=__mx_builtin_gc_find(*p);
                if (t>=0 && __mx_builtin_gc_sz[t]>0){
                    __mx_builtin_gc_sz[t]=-__mx_builtin_gc_sz[t];
                    chk=true;
                }
            }
            __mx_builtin_gc_a[i]=-__mx_builtin_gc_a[i];
        }
    }
    int new_en=0;
    for (int i=0;i<__mx_builtin_gc_en;i++) if (__mx_builtin_gc_sz[i]<0){
        __mx_builtin_gc_a[new_en]=__mx_builtin_gc_abs(__mx_builtin_gc_a[i]);__mx_builtin_gc_sz[new_en]=__mx_builtin_gc_abs(__mx_builtin_gc_sz[i]);
        new_en++;
    }else{
        free((void*)__mx_builtin_gc_a[i]);
    }
    __mx_builtin_gc_en=new_en;*/
    unsigned int* spt=(unsigned int*)__mx_builtin_gc_r_sp();
    int d[1003],he=1,en=0;
    for (unsigned int* p=spt;p<__mx_builtin_gc_sps;p++){
        int t=__mx_builtin_gc_find(*p);
        if (t>=0 && __mx_builtin_gc_sz[t]>0){
            __mx_builtin_gc_sz[t]=-__mx_builtin_gc_sz[t];
            d[++en]=t;
        }
    }
    while (he<=en){
        int i=d[he++];
        if (__mx_builtin_gc_a[i]>0 && __mx_builtin_gc_sz[i]<0 && (-__mx_builtin_gc_sz[i])%4==0){
            for (unsigned int* p=(unsigned int*)__mx_builtin_gc_a[i];p<(unsigned int*)(__mx_builtin_gc_a[i]+(-__mx_builtin_gc_sz[i]));p++){
                int t=__mx_builtin_gc_find(*p);
                if (t>=0 && __mx_builtin_gc_sz[t]>0){
                    __mx_builtin_gc_sz[t]=-__mx_builtin_gc_sz[t];
                    d[++en]=t;
                }
            }
            __mx_builtin_gc_a[i]=-__mx_builtin_gc_a[i];
        }
    }
    int new_en=0;__mx_builtin_gc_malloced=0;
    for (int i=0;i<__mx_builtin_gc_en;i++) if (__mx_builtin_gc_sz[i]<0){
        __mx_builtin_gc_a[new_en]=__mx_builtin_gc_abs(__mx_builtin_gc_a[i]);__mx_builtin_gc_sz[new_en]=__mx_builtin_gc_abs(__mx_builtin_gc_sz[i]);
        __mx_builtin_gc_malloced+=__mx_builtin_gc_sz[new_en];
        new_en++;
    }else{
        free((void*)__mx_builtin_gc_a[i]);
    }
    __mx_builtin_gc_en=new_en;
}
char* __mx_builtin_malloc(int size){
    if (__mx_builtin_gc_malloced+size>__mx_builtin_gc_pool_size || __mx_builtin_gc_en>=1000){
        __mx_builtin_gc_before();
    }
    if (__mx_builtin_gc_en>=1000) return NULL;
    __mx_builtin_gc_malloced+=size;
    __mx_builtin_gc_a[__mx_builtin_gc_en]=(int)malloc(size);__mx_builtin_gc_sz[__mx_builtin_gc_en]=size;
    return (char*)__mx_builtin_gc_a[__mx_builtin_gc_en++];
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
    char* a=__mx_builtin_malloc(sizeof(char)*2333);
    scanf("%s",a);
    return a;
}
int __mx_builtin_getInt(){
    int a;
    scanf("%d",&a);
    return a;
}
char* __mx_builtin_toString(int i){
    char* a=__mx_builtin_malloc(sizeof(char)*23);
    sprintf(a,"%d",i);
    return a;
}
int __mx_builtin_str_length(char* str){
    return strlen(str);
}
char* __mx_builtin_str_substring(char* str,int left,int right){
    char* a=__mx_builtin_malloc(sizeof(char)*(right-left+1));
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
__attribute__((optimize("O1"))) char* __mx_builtin_str_add(char* l,char* r){
    char* a=__mx_builtin_malloc(sizeof(char)*2333);
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
