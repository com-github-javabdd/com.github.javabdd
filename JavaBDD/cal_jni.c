#include <jni.h>
#include <stdlib.h>
#include "calInt.h"

#include "cal_jni.h"

/*
** When casting from `int' to a pointer type, you should
** first cast to `intptr_cast_type'.  This is a type
** that is (a) the same size as a pointer, on most platforms,
** to avoid compiler warnings about casts from pointer to int of
** different size; and (b) guaranteed to be at least as big as
** `int'.
*/
#if __STDC_VERSION__ >= 199901
  #include <inttypes.h>
  #if INTPTR_MAX >= INT_MAX
    typedef intptr_t intptr_cast_type;
  #else /* no intptr_t, or intptr_t smaller than `int' */
    typedef intmax_t intptr_cast_type;
  #endif
#else
  #include <stddef.h>
  #include <limits.h>
  #if PTRDIFF_MAX >= INT_MAX
    typedef ptrdiff_t intptr_cast_type;
  #else
    typedef int intptr_cast_type;
  #endif
#endif

static Cal_BddManager manager;
static jlong bdd_one, bdd_zero;

#define INVALID_BDD 0L

static void die(JNIEnv *env, char* msg)
{
    jclass cls;
    cls = (*env)->FindClass(env, "java/lang/InternalError");
    if (cls != NULL) {
        (*env)->ThrowNew(env, cls, msg);
    }
    (*env)->DeleteLocalRef(env, cls);
}

/**** START OF NATIVE METHOD IMPLEMENTATIONS ****/

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    registerNatives
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_sf_javabdd_CALFactory_registerNatives
  (JNIEnv *env, jclass cl)
{
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    initialize0
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_net_sf_javabdd_CALFactory_initialize0
  (JNIEnv *env, jclass cl, jint numSlots, jint cacheSize)
{
    jfieldID one_fid;
    jfieldID zero_fid;
    
    if (manager != NULL) {
        die(env, "init called twice!");
        return;
    }
    
    manager = Cal_BddManagerInit();
    if (manager == NULL) {
        die(env, "unable to initialize CAL");
        return;
    }

    bdd_one  = (jlong) (intptr_cast_type) Cal_BddOne(manager);
    bdd_zero = (jlong) (intptr_cast_type) Cal_BddZero(manager);
    
    //Cal_BddIdentity((Cal_Bdd)(intptr_cast_type) bdd_one);
    //Cal_BddIdentity((Cal_Bdd)(intptr_cast_type) bdd_zero);
    
    one_fid = (*env)->GetStaticFieldID(env, cl, "one", "J");
    zero_fid = (*env)->GetStaticFieldID(env, cl, "zero", "J");
    
    if (!one_fid || !zero_fid) {
        die(env, "cannot find members: version mismatch?");
        return;
    }
    (*env)->SetStaticLongField(env, cl, one_fid, bdd_one);
    (*env)->SetStaticLongField(env, cl, zero_fid, bdd_zero);
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    isInitialized0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_net_sf_javabdd_CALFactory_isInitialized0
  (JNIEnv *env, jclass cl)
{
    return manager != NULL;
}
  
/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    done0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_sf_javabdd_CALFactory_done0
  (JNIEnv *env, jclass cl)
{
    int bdds;
    Cal_BddManager m;

    //Cal_BddFree(manager, (Cal_Bdd) (intptr_cast_type) bdd_one);
    //Cal_BddFree(manager, (Cal_Bdd) (intptr_cast_type) bdd_zero);
    
    //fprintf(stderr, "Garbage collections: %d  Time spent: %ldms\n",
    //    Cudd_ReadGarbageCollections(manager), Cudd_ReadGarbageCollectionTime(manager));
    
    //bdds = Cudd_CheckZeroRef(manager);
    //if (bdds > 0) fprintf(stderr, "Note: %d BDDs still in memory when terminating\n", bdds);
    m = manager;
    manager = NULL; // race condition with delRef
    Cal_BddManagerQuit(m);
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    varNum0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_javabdd_CALFactory_varNum0
  (JNIEnv *env, jclass cl)
{
    return Cal_BddVars(manager);
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    setVarNum0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sf_javabdd_CALFactory_setVarNum0
  (JNIEnv *env, jclass cl, jint x)
{
    jint old = Cal_BddVars(manager);
    while (Cal_BddVars(manager) < x) {
        Cal_Bdd b = Cal_BddManagerCreateNewVarLast(manager);
        //Cal_BddIdentity(manager, b);
    }
    return old;
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    ithVar0
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_ithVar0
  (JNIEnv *env, jclass cl, jint i)
{
    Cal_Bdd d;
    jlong result;
    d = Cal_BddManagerGetVarWithId(manager, i+1);
    result = (jlong) (intptr_cast_type) d;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    level2Var0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sf_javabdd_CALFactory_level2Var0
  (JNIEnv *env, jclass cl, jint level)
{
    Cal_Bdd fn;
    fn = Cal_BddManagerGetVarWithIndex(manager, level);
    if (!fn){
        /* variable should always be found, since they are created at bdd_start */
        fprintf(stderr, "bdd_get_id_from_level: assumption violated");
        exit(-1);
    }
    return (Cal_BddGetIfId(manager, fn) - 1);
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    var2Level0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_net_sf_javabdd_CALFactory_var2Level0
  (JNIEnv *env, jclass cl, jint v)
{
    Cal_Bdd d = Cal_BddManagerGetVarWithId(manager, v);
    return Cal_BddGetIfIndex(manager, d);
    //return (jint) Cudd_ReadPerm(manager, v);
}

/*
 * Class:     net_sf_javabdd_CALFactory
 * Method:    setVarOrder0
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_net_sf_javabdd_CALFactory_setVarOrder0
  (JNIEnv *env, jclass cl, jintArray arr)
{
  int *a;
  jint size = (*env)->GetArrayLength(env, arr);
  if (size != Cal_BddVars(manager)) {
    jclass cls = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    (*env)->ThrowNew(env, cls, "array size != number of vars");
    (*env)->DeleteLocalRef(env, cls);
    return;
  }
  a = (int*) (*env)->GetPrimitiveArrayCritical(env, arr, NULL);
  if (a == NULL) return;
  //Cudd_ShuffleHeap(manager, a);
    printf("setVarOrder not implemented.\n");
  (*env)->ReleasePrimitiveArrayCritical(env, arr, a, JNI_ABORT);
}

/* class net_sf_javabdd_CALFactory_CALBDD */

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    var0
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_var0
  (JNIEnv *env, jclass cl, jlong b)
{
    Cal_Bdd d;
    d = (Cal_Bdd) (intptr_cast_type) b;
    return Cal_BddGetIfId(manager, d);
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    high0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_high0
  (JNIEnv *env, jclass cl, jlong b)
{
    Cal_Bdd d;
    Cal_Bdd res;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) b;
    
    // TODO: check if d is a constant.
    res = Cal_BddThen(manager, d);
    
    result = (jlong) (intptr_cast_type) res;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    low0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_low0
  (JNIEnv *env, jclass cl, jlong b)
{
    Cal_Bdd d;
    Cal_Bdd res;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) b;
    
    // TODO: check if d is a constant.
    res = Cal_BddElse(manager, d);
    
    result = (jlong) (intptr_cast_type) res;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    not0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_not0
  (JNIEnv *env, jclass cl, jlong b)
{
    Cal_Bdd d;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) b;
    d = Cal_BddNot(manager, d);
    result = (jlong) (intptr_cast_type) d;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    ite0
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_ite0
  (JNIEnv *env, jclass cl, jlong a, jlong b, jlong c)
{
    Cal_Bdd d;
    Cal_Bdd e;
    Cal_Bdd f;
    Cal_Bdd g;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) a;
    e = (Cal_Bdd) (intptr_cast_type) b;
    f = (Cal_Bdd) (intptr_cast_type) c;
    g = Cal_BddITE(manager, d, e, f);
    result = (jlong) (intptr_cast_type) g;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    relprod0
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_relprod0
  (JNIEnv *env, jclass cl, jlong a, jlong b, jlong c)
{
    Cal_Bdd d;
    Cal_Bdd e;
    Cal_Bdd f;
    Cal_Bdd g;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) a;
    e = (Cal_Bdd) (intptr_cast_type) b;
    f = (Cal_Bdd) (intptr_cast_type) c;
    //g = Cudd_bddAndAbstract(manager, d, e, f);
    g = 0;
    printf("relprod not implemented.\n");
    result = (jlong) (intptr_cast_type) g;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    restrict0
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_restrict0
  (JNIEnv *env, jclass cl, jlong a, jlong b)
{
    Cal_Bdd d;
    Cal_Bdd e;
    Cal_Bdd f;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) a;
    e = (Cal_Bdd) (intptr_cast_type) b;
    f = Cal_BddReduce(manager, d, e);
    result = (jlong) (intptr_cast_type) f;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    support0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_support0
  (JNIEnv *env, jclass cl, jlong a)
{
    Cal_Bdd d;
    Cal_Bdd e;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) a;
    e = Cal_BddSatisfySupport(manager, d);
    result = (jlong) (intptr_cast_type) e;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    apply0
 * Signature: (JJI)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_apply0
  (JNIEnv *env, jclass cl, jlong a, jlong b, jint oper)
{
    Cal_Bdd d;
    Cal_Bdd e;
    Cal_Bdd f;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) a;
    e = (Cal_Bdd) (intptr_cast_type) b;
    switch (oper) {
    case 0: /* and */
        f = Cal_BddAnd(manager, d, e);
        break;
    case 1: /* xor */
        f = Cal_BddXor(manager, d, e);
        break;
    case 2: /* or */
        f = Cal_BddOr(manager, d, e);
        break;
    case 3: /* nand */
        f = Cal_BddNand(manager, d, e);
        break;
    case 4: /* nor */
        f = Cal_BddNor(manager, d, e);
        break;
    case 5: /* imp */
        d = Cal_BddNot(manager, d);
        //Cudd_Ref(d);
        f = Cal_BddOr(manager, d, e);
        //Cal_BddFree(manager, d);
        break;
    case 6: /* biimp */
        f = Cal_BddXnor(manager, d, e);
        break;
    case 7: /* diff */
        e = Cal_BddNot(manager, e);
        //Cudd_Ref(e);
        f = Cal_BddAnd(manager, d, e);
        //Cal_BddFree(manager, e);
        break;
    case 8: /* less */
        d = Cal_BddNot(manager, d);
        //Cudd_Ref(d);
        f = Cal_BddAnd(manager, d, e);
        //Cal_BddFree(manager, d);
        break;
    case 9: /* invimp */
        e = Cal_BddNot(manager, e);
        //Cudd_Ref(e);
        f = Cal_BddOr(manager, d, e);
        //Cal_BddFree(manager, e);
        break;
    default:
        die(env, "operation not supported");
        return INVALID_BDD;
    }
    result = (jlong) (intptr_cast_type) f;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    satOne0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_satOne0
  (JNIEnv *env, jclass cl, jlong a)
{
    Cal_Bdd d;
    Cal_Bdd e;
    jlong result;
    d = (Cal_Bdd) (intptr_cast_type) a;
    e = Cal_BddSatisfy(manager, d);
    result = (jlong) (intptr_cast_type) e;
    return result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    nodeCount0
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_nodeCount0
  (JNIEnv *env, jclass cl, jlong a)
{
    Cal_Bdd d;
    d = (Cal_Bdd) (intptr_cast_type) a;
    return Cal_BddSize(manager, d, 1);
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    pathCount0
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_pathCount0
  (JNIEnv *env, jclass cl, jlong a)
{
    Cal_Bdd d;
    d = (Cal_Bdd) (intptr_cast_type) a;
    printf("pathCount not implemented.\n");
    //return Cudd_CountPathsToNonZero(d);
    return 0;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    satCount0
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_satCount0
  (JNIEnv *env, jclass cl, jlong a)
{
    Cal_Bdd d;
    double result = 1.0;
    int k = Cal_BddVars(manager);
    while (--k >= 0) result *= 2.0;
    d = (Cal_Bdd) (intptr_cast_type) a;
    return Cal_BddSatisfyingFraction(manager, d) * result;
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    addRef
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_addRef
  (JNIEnv *env, jclass cl, jlong a)
{
    Cal_Bdd d;
    d = (Cal_Bdd) (intptr_cast_type) a;
    //Cudd_Ref(d);
}

/*
 * Class:     net_sf_javabdd_CALFactory_CALBDD
 * Method:    delRef
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_sf_javabdd_CALFactory_00024CALBDD_delRef
  (JNIEnv *env, jclass cl, jlong a)
{
    Cal_Bdd d;
    if (manager == NULL) return;
    d = (Cal_Bdd) (intptr_cast_type) a;
    if (d != INVALID_BDD)
        Cal_BddFree(manager, d);
}
