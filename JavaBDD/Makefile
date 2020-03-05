#
# Makefile to create Java JNI library.
# On Windows, uses Cygwin b20.1 tools with Mingw runtime. 
# 
# Things you may need to change, or redefine on the command line:
#   BUDDY_SRC    -- location of BuDDy source code
#   CUDD_SRC     -- location of CUDD source code
#   CAL_SRC      -- location of CAL source code
#   JDK_ROOT     -- location where you installed JDK
#

BUDDY_SRC = buddy/src
CUDD_SRC = cudd-2.4.1
CAL_SRC = cal-2.1
VER = 2.0

ifeq (${OS},Windows_NT)
  JDK_ROOT = $(firstword $(wildcard c:/j2sdk*))
  CLASSPATH = .\;jdd.jar
  CC = gcc
  CFLAGS = -Wall -O2 -mno-cygwin $(EXTRA_CFLAGS)
  CUDD_CFLAGS = $(CFLAGS) -DHAVE_SYS_RESOURCE_H=0
  CAL_CFLAGS = $(CFLAGS) -DCLOCK_RESOLUTION=60 -DRLIMIT_DATA_DEFAULT=16777216 -DNDEBUG=1 -DSTDC_HEADERS=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_SYS_FILE_H=1 -DHAVE_SYS_STAT_H=1 -DHAVE_UNISTD_H=1 -DHAVE_ERRNO_H=1 -DHAVE_ASSERT_H=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_PWD_H=1 -DHAVE_SYS_TYPES_H=1 -DHAVE_SYS_TIMES_H=1 -DHAVE_SYS_TIME_H=1 -DHAVE_SYS_RESOURCE_H=0 -DHAVE_STDARG_H=1 -DSIZEOF_VOID_P=4 -DSIZEOF_INT=4 -DHAVE_IEEE_754=1 -DPAGE_SIZE=4096 -DLG_PAGE_SIZE=12 -DRETSIGTYPE=void -DHAVE_STRCOLL=1 -DHAVE_SYSCONF=1 -DHAVE_GETHOSTNAME=1 -DHAVE_STRCSPN=1 -DHAVE_STRERROR=1 -DHAVE_STRSPN=1 -DHAVE_STRSTR=1 -DHAVE_GETENV=1 -DHAVE_STRCHR=1 -DHAVE_GETRLIMIT=1 -DHAVE_GETRUSAGE=1 -DHAVE_VALLOC=0
  OBJECT_OUTPUT_OPTION = -o$(space)
  LINK = dllwrap
  LINKFLAGS = -s --target=i386-mingw32 --add-underscore --driver-name gcc -mno-cygwin
  DLL_OUTPUT_OPTION = -o$(space)
  INCLUDES = -I. -I$(JDK_ROOT)/include \
             -I$(BUDDY_SRC) -I$(BUDDY_SRC)/.. \
             -I$(CUDD_SRC)/cudd -I$(CUDD_SRC)/epd -I$(CUDD_SRC)/mtr \
             -I$(CUDD_SRC)/st -I$(CUDD_SRC)/util \
             -I$(CAL_SRC) \
             -I$(JDK_ROOT)/include/win32
  BUDDY_DLL_NAME = buddy.dll
  CUDD_DLL_NAME = cudd.dll
  CAL_DLL_NAME = cal.dll
  ifeq (${CC},icl)    # Intel Windows compiler
    CFLAGS = -DSPECIALIZE_RELPROD -DSPECIALIZE_OR -DSPECIALIZE_AND -DSMALL_NODES /O2 /Ob2 /QxT $(EXTRA_CFLAGS)
    CUDD_CFLAGS = $(CFLAGS) -DHAVE_SYS_RESOURCE_H=0
    CAL_CFLAGS = $(CFLAGS) -DCLOCK_RESOLUTION=60 -DRLIMIT_DATA_DEFAULT=16777216 -DNDEBUG=1 -DSTDC_HEADERS=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_SYS_FILE_H=1 -DHAVE_SYS_STAT_H=1 -DHAVE_UNISTD_H=0 -DHAVE_ERRNO_H=1 -DHAVE_ASSERT_H=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_PWD_H=1 -DHAVE_SYS_TYPES_H=1 -DHAVE_SYS_TIMES_H=1 -DHAVE_SYS_TIME_H=0 -DHAVE_SYS_RESOURCE_H=0 -DHAVE_STDARG_H=1 -DSIZEOF_VOID_P=4 -DSIZEOF_INT=4 -DHAVE_IEEE_754=1 -DPAGE_SIZE=4096 -DLG_PAGE_SIZE=12 -DRETSIGTYPE=void -DHAVE_STRCOLL=1 -DHAVE_SYSCONF=1 -DHAVE_GETHOSTNAME=1 -DHAVE_STRCSPN=1 -DHAVE_STRERROR=1 -DHAVE_STRSPN=1 -DHAVE_STRSTR=1 -DHAVE_GETENV=1 -DHAVE_STRCHR=1 -DHAVE_GETRLIMIT=1 -DHAVE_GETRUSAGE=1 -DHAVE_VALLOC=0
    OBJECT_OUTPUT_OPTION = -Fo
    LINK = xilink
    LINKFLAGS = /dll /libpath:$(JDK_ROOT)/lib user32.lib gdi32.lib
    DLL_OUTPUT_OPTION = /out:
  endif
  ifeq (${CC},cl)     # Microsoft Visual C++ compiler
    CFLAGS = -O2 $(EXTRA_CFLAGS)
    CUDD_CFLAGS = $(CFLAGS) -DHAVE_SYS_RESOURCE_H=0
    CAL_CFLAGS = $(CFLAGS) -DCLOCK_RESOLUTION=60 -DRLIMIT_DATA_DEFAULT=16777216 -DNDEBUG=1 -DSTDC_HEADERS=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_SYS_FILE_H=1 -DHAVE_SYS_STAT_H=1 -DHAVE_UNISTD_H=0 -DHAVE_ERRNO_H=1 -DHAVE_ASSERT_H=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_PWD_H=1 -DHAVE_SYS_TYPES_H=1 -DHAVE_SYS_TIMES_H=1 -DHAVE_SYS_TIME_H=0 -DHAVE_SYS_RESOURCE_H=0 -DHAVE_STDARG_H=1 -DSIZEOF_VOID_P=4 -DSIZEOF_INT=4 -DHAVE_IEEE_754=1 -DPAGE_SIZE=4096 -DLG_PAGE_SIZE=12 -DRETSIGTYPE=void -DHAVE_STRCOLL=1 -DHAVE_SYSCONF=1 -DHAVE_GETHOSTNAME=1 -DHAVE_STRCSPN=1 -DHAVE_STRERROR=1 -DHAVE_STRSPN=1 -DHAVE_STRSTR=1 -DHAVE_GETENV=1 -DHAVE_STRCHR=1 -DHAVE_GETRLIMIT=1 -DHAVE_GETRUSAGE=1 -DHAVE_VALLOC=0
    OBJECT_OUTPUT_OPTION = -Fo
    LINK = cl
    LINKFLAGS = -MLd -LDd -Zi /link /libpath:$(JDK_ROOT)/lib user32.lib gdi32.lib
    DLL_OUTPUT_OPTION = -Fe
  endif
else
  ifeq (${shell uname -s},Darwin)
    JDK_ROOT = /Library/Java/Home
    CLASSPATH = .:jdd.jar
    CC = gcc
    CFLAGS = -DSPECIALIZE_RELPROD -DSPECIALIZE_AND -DSPECIALIZE_OR -DSMALL_NODES -O2 -fomit-frame-pointer $(EXTRA_CFLAGS)
    CUDD_CFLAGS = $(CFLAGS)
    CAL_CFLAGS = -O2 -DCLOCK_RESOLUTION=60 -DRLIMIT_DATA_DEFAULT=16777216 -DNDEBUG=1 -DSTDC_HEADERS=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_SYS_FILE_H=1 -DHAVE_SYS_STAT_H=1 -DHAVE_UNISTD_H=1 -DHAVE_ERRNO_H=1 -DHAVE_ASSERT_H=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_PWD_H=1 -DHAVE_SYS_TYPES_H=1 -DHAVE_SYS_TIMES_H=1 -DHAVE_SYS_TIME_H=1 -DHAVE_SYS_RESOURCE_H=1 -DHAVE_STDARG_H=1 -DHAVE_VARARGS_H=1 -DSIZEOF_VOID_P=4 -DSIZEOF_INT=4 -DHAVE_IEEE_754=1 -DPAGE_SIZE=4096 -DLG_PAGE_SIZE=12 -DRETSIGTYPE=void -DHAVE_STRCOLL=1 -DHAVE_SYSCONF=1 -DHAVE_GETHOSTNAME=1 -DHAVE_STRCSPN=1 -DHAVE_STRERROR=1 -DHAVE_STRSPN=1 -DHAVE_STRSTR=1 -DHAVE_GETENV=1 -DHAVE_STRCHR=1 -DHAVE_GETRLIMIT=1 -DHAVE_GETRUSAGE=1 -DHAVE_VALLOC=1 $(EXTRA_CFLAGS)
    OBJECT_OUTPUT_OPTION = -o$(space)
    LINK = $(CC) 
    LINKFLAGS = -dynamiclib -framework JavaVM  -single_module -undefined suppress -flat_namespace
    DLL_OUTPUT_OPTION = -o$(space)
    INCLUDES = -I/usr/local/include \
               -I. -I$(JDK_ROOT)/include \
               -I/System/Library/Frameworks/JavaVM.framework/Headers \
               -I$(BUDDY_SRC) -I$(BUDDY_SRC)/.. \
               -I$(CUDD_SRC)/cudd -I$(CUDD_SRC)/epd -I$(CUDD_SRC)/mtr \
               -I$(CUDD_SRC)/st -I$(CUDD_SRC)/util \
               -I$(CAL_SRC) \
               -I$(JDK_ROOT)/include/linux
    BUDDY_DLL_NAME = libbuddy.jnilib
    CUDD_DLL_NAME = libcudd.jnilib
    CAL_DLL_NAME = libcal.jnilib
  else
    JDK_ROOT = $(firstword $(wildcard /usr/java/j*dk*))
    CLASSPATH = .:jdd.jar
    CFLAGS = -DSPECIALIZE_RELPROD -DSPECIALIZE_AND -DSPECIALIZE_OR -DSMALL_NODES -O2 -fomit-frame-pointer $(EXTRA_CFLAGS)
    CUDD_CFLAGS = $(CFLAGS)
    CAL_CFLAGS = -O2 -DCLOCK_RESOLUTION=60 -DRLIMIT_DATA_DEFAULT=16777216 -DNDEBUG=1 -DSTDC_HEADERS=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_SYS_FILE_H=1 -DHAVE_SYS_STAT_H=1 -DHAVE_UNISTD_H=1 -DHAVE_ERRNO_H=1 -DHAVE_ASSERT_H=1 -DHAVE_SYS_WAIT_H=1 -DHAVE_PWD_H=1 -DHAVE_SYS_TYPES_H=1 -DHAVE_SYS_TIMES_H=1 -DHAVE_SYS_TIME_H=1 -DHAVE_SYS_RESOURCE_H=1 -DHAVE_STDARG_H=1 -DHAVE_VARARGS_H=1 -DSIZEOF_VOID_P=4 -DSIZEOF_INT=4 -DHAVE_IEEE_754=1 -DPAGE_SIZE=4096 -DLG_PAGE_SIZE=12 -DRETSIGTYPE=void -DHAVE_STRCOLL=1 -DHAVE_SYSCONF=1 -DHAVE_GETHOSTNAME=1 -DHAVE_STRCSPN=1 -DHAVE_STRERROR=1 -DHAVE_STRSPN=1 -DHAVE_STRSTR=1 -DHAVE_GETENV=1 -DHAVE_STRCHR=1 -DHAVE_GETRLIMIT=1 -DHAVE_GETRUSAGE=1 -DHAVE_VALLOC=1 $(EXTRA_CFLAGS)
    OBJECT_OUTPUT_OPTION = -o$(space)
    LINK = $(CC)
    LINKFLAGS = -shared
    DLL_OUTPUT_OPTION = -o$(space)
    INCLUDES = -I. -I$(JDK_ROOT)/include \
               -I$(BUDDY_SRC) -I$(BUDDY_SRC)/.. \
               -I$(CUDD_SRC)/cudd -I$(CUDD_SRC)/epd -I$(CUDD_SRC)/mtr \
               -I$(CUDD_SRC)/st -I$(CUDD_SRC)/util \
               -I$(CAL_SRC) \
               -I$(JDK_ROOT)/include/linux
    BUDDY_DLL_NAME = libbuddy.so
    CUDD_DLL_NAME = libcudd.so
    CAL_DLL_NAME = libcal.so
    ifeq (${CC},icc)    # Intel Linux compiler
      CFLAGS = -DSPECIALIZE_RELPROD -DSPECIALIZE_AND -DSPECIALIZE_OR -DSMALL_NODES -O2 -Ob2 -ip $(EXTRA_CFLAGS)
      CUDD_CFLAGS = $(CFLAGS)
      LINK = xild  # Bug in icc link makes it ignore -static, so use xild
      # Include libirc for _intel_fast_memset
      LINKFLAGS = -static -shared /opt/intel_cc_80/lib/libirc.a
    endif
    ifeq (${CC},pathcc)    # Pathscale compiler
      # For 64-bit, eliminate -m32 and add -fPIC in CFLAGS.
      CFLAGS = -DSPECIALIZE_RELPROD -DSPECIALIZE_AND -DSPECIALIZE_OR -DSMALL_NODES -m32 -O2 -fomit_frame_pointer $(EXTRA_CFLAGS)
      CUDD_CFLAGS = $(CFLAGS)
      LINK = pathcc
      LINKFLAGS = -shared $(CFLAGS)
    endif
  endif
endif

# The java tools:
JAVAC = $(JDK_ROOT)/bin/javac
JAVA = $(JDK_ROOT)/bin/java
JAVAH = $(JDK_ROOT)/bin/javah
JAVADOC = $(JDK_ROOT)/bin/javadoc
JAR = $(JDK_ROOT)/bin/jar

# The java source code
JAVA_SOURCES = net/sf/javabdd/BDD.java \
	net/sf/javabdd/BDDBitVector.java \
	net/sf/javabdd/BDDDomain.java \
	net/sf/javabdd/BDDException.java \
	net/sf/javabdd/BDDFactory.java \
	net/sf/javabdd/BDDPairing.java \
	net/sf/javabdd/BuDDyFactory.java \
	net/sf/javabdd/CALFactory.java \
	net/sf/javabdd/CUDDFactory.java \
	net/sf/javabdd/FindBestOrder.java \
	net/sf/javabdd/JFactory.java \
	net/sf/javabdd/JDDFactory.java \
	net/sf/javabdd/MicroFactory.java \
	net/sf/javabdd/TestBDDFactory.java \
	net/sf/javabdd/TryVarOrder.java \
	net/sf/javabdd/TypedBDDFactory.java
JAVA_CLASSFILES = net/sf/javabdd/*.class
JAVA_PACKAGES = net.sf.javabdd
BUDDY_CLASSFILE = net/sf/javabdd/BuDDyFactory.class
CUDD_CLASSFILE = net/sf/javabdd/CUDDFactory.class
CAL_CLASSFILE = net/sf/javabdd/CALFactory.class
BUDDY_CLASSNAMES = net.sf.javabdd.BuDDyFactory \
	net.sf.javabdd.BuDDyFactory\$$BuDDyPairing
CUDD_CLASSNAMES = net.sf.javabdd.CUDDFactory \
	net.sf.javabdd.CUDDFactory\$$CUDDBDD \
	net.sf.javabdd.CUDDFactory\$$CUDDBDDPairing
CAL_CLASSNAMES = net.sf.javabdd.CALFactory \
	net.sf.javabdd.CALFactory\$$CALBDD \
	net.sf.javabdd.CALFactory\$$CALBDDPairing
EXAMPLE_SOURCES = NQueens.java RubiksCube.java
EXAMPLE_CLASSFILES = $(EXAMPLE_SOURCES:%.java=%.class)
JAR_NAME = javabdd-$(VER).jar

BUDDY_INCLUDE = buddy_jni.h
BUDDY_SRCS = buddy_jni.c \
	$(BUDDY_SRC)/bddio.c $(BUDDY_SRC)/bddop.c $(BUDDY_SRC)/bvec.c \
	$(BUDDY_SRC)/cache.c $(BUDDY_SRC)/fdd.c $(BUDDY_SRC)/imatrix.c \
	$(BUDDY_SRC)/kernel.c $(BUDDY_SRC)/pairs.c $(BUDDY_SRC)/prime.c \
	$(BUDDY_SRC)/reorder.c $(BUDDY_SRC)/tree.c $(BUDDY_SRC)/trace.c
BUDDY_OBJS = $(BUDDY_SRCS:.c=.o)

CUDD_INCLUDE = cudd_jni.h
CUDD_SRCS = cudd_jni.c \
	  $(CUDD_SRC)/cudd/cuddAPI.c $(CUDD_SRC)/cudd/cuddAddAbs.c $(CUDD_SRC)/cudd/cuddAddApply.c $(CUDD_SRC)/cudd/cuddAddFind.c $(CUDD_SRC)/cudd/cuddAddIte.c \
	  $(CUDD_SRC)/cudd/cuddAddInv.c $(CUDD_SRC)/cudd/cuddAddNeg.c $(CUDD_SRC)/cudd/cuddAddWalsh.c $(CUDD_SRC)/cudd/cuddAndAbs.c \
	  $(CUDD_SRC)/cudd/cuddAnneal.c $(CUDD_SRC)/cudd/cuddApa.c $(CUDD_SRC)/cudd/cuddApprox.c $(CUDD_SRC)/cudd/cuddBddAbs.c $(CUDD_SRC)/cudd/cuddBddCorr.c \
	  $(CUDD_SRC)/cudd/cuddBddIte.c $(CUDD_SRC)/cudd/cuddBridge.c $(CUDD_SRC)/cudd/cuddCache.c $(CUDD_SRC)/cudd/cuddCheck.c $(CUDD_SRC)/cudd/cuddClip.c \
	  $(CUDD_SRC)/cudd/cuddCof.c $(CUDD_SRC)/cudd/cuddCompose.c $(CUDD_SRC)/cudd/cuddDecomp.c $(CUDD_SRC)/cudd/cuddEssent.c \
	  $(CUDD_SRC)/cudd/cuddExact.c $(CUDD_SRC)/cudd/cuddExport.c $(CUDD_SRC)/cudd/cuddGenCof.c $(CUDD_SRC)/cudd/cuddGenetic.c \
	  $(CUDD_SRC)/cudd/cuddGroup.c $(CUDD_SRC)/cudd/cuddHarwell.c $(CUDD_SRC)/cudd/cuddInit.c $(CUDD_SRC)/cudd/cuddInteract.c \
	  $(CUDD_SRC)/cudd/cuddLCache.c $(CUDD_SRC)/cudd/cuddLevelQ.c \
	  $(CUDD_SRC)/cudd/cuddLinear.c $(CUDD_SRC)/cudd/cuddLiteral.c $(CUDD_SRC)/cudd/cuddMatMult.c $(CUDD_SRC)/cudd/cuddPriority.c \
	  $(CUDD_SRC)/cudd/cuddRead.c $(CUDD_SRC)/cudd/cuddRef.c $(CUDD_SRC)/cudd/cuddReorder.c $(CUDD_SRC)/cudd/cuddSat.c $(CUDD_SRC)/cudd/cuddSign.c \
	  $(CUDD_SRC)/cudd/cuddSolve.c $(CUDD_SRC)/cudd/cuddSplit.c $(CUDD_SRC)/cudd/cuddSubsetHB.c $(CUDD_SRC)/cudd/cuddSubsetSP.c $(CUDD_SRC)/cudd/cuddSymmetry.c \
	  $(CUDD_SRC)/cudd/cuddTable.c $(CUDD_SRC)/cudd/cuddUtil.c $(CUDD_SRC)/cudd/cuddWindow.c $(CUDD_SRC)/cudd/cuddZddCount.c $(CUDD_SRC)/cudd/cuddZddFuncs.c \
	  $(CUDD_SRC)/cudd/cuddZddGroup.c $(CUDD_SRC)/cudd/cuddZddIsop.c $(CUDD_SRC)/cudd/cuddZddLin.c $(CUDD_SRC)/cudd/cuddZddMisc.c \
	  $(CUDD_SRC)/cudd/cuddZddPort.c $(CUDD_SRC)/cudd/cuddZddReord.c $(CUDD_SRC)/cudd/cuddZddSetop.c $(CUDD_SRC)/cudd/cuddZddSymm.c \
	  $(CUDD_SRC)/cudd/cuddZddUtil.c \
	  $(CUDD_SRC)/epd/epd.c \
	  $(CUDD_SRC)/mtr/mtrBasic.c $(CUDD_SRC)/mtr/mtrGroup.c \
	  $(CUDD_SRC)/st/st.c \
	  $(CUDD_SRC)/util/cpu_time.c $(CUDD_SRC)/util/datalimit.c $(CUDD_SRC)/util/safe_mem.c
CUDD_OBJS = $(CUDD_SRCS:.c=.o)

CAL_INCLUDE = cal_jni.h
CAL_SRCS = cal_jni.c \
	  $(CAL_SRC)/cal.c $(CAL_SRC)/calApplyReduce.c $(CAL_SRC)/calAssociation.c $(CAL_SRC)/calBddCompose.c $(CAL_SRC)/calBddITE.c \
	  $(CAL_SRC)/calBddManager.c $(CAL_SRC)/calBddOp.c $(CAL_SRC)/calBddSatisfy.c $(CAL_SRC)/calBddSize.c \
	  $(CAL_SRC)/calBddSubstitute.c $(CAL_SRC)/calBddSupport.c $(CAL_SRC)/calBddSwapVars.c $(CAL_SRC)/calBddVarSubstitute.c \
	  $(CAL_SRC)/calBlk.c $(CAL_SRC)/calCacheTableTwo.c $(CAL_SRC)/calDump.c $(CAL_SRC)/calGC.c $(CAL_SRC)/calHashTable.c \
	  $(CAL_SRC)/calHashTableOne.c $(CAL_SRC)/calHashTableThree.c $(CAL_SRC)/calInteract.c $(CAL_SRC)/calMem.c \
	  $(CAL_SRC)/calMemoryManagement.c $(CAL_SRC)/calPipeline.c $(CAL_SRC)/calPrint.c \
	  $(CAL_SRC)/calPrintProfile.c $(CAL_SRC)/calQuant.c $(CAL_SRC)/calReduce.c $(CAL_SRC)/calReorderBF.c \
	  $(CAL_SRC)/calReorderDF.c $(CAL_SRC)/calReorderUtil.c \
	  $(CAL_SRC)/calTerminal.c $(CAL_SRC)/calUtil.c $(CAL_SRC)/sbrk.c
CAL_OBJS = $(CAL_SRCS:.c=.o)

default: jar $(BUDDY_DLL_NAME)

all: jar dlls

dlls: $(BUDDY_DLL_NAME) $(CUDD_DLL_NAME) $(CAL_DLL_NAME)

$(BUDDY_DLL_NAME): $(BUDDY_OBJS)
	$(LINK) $(DLL_OUTPUT_OPTION)$@ $(BUDDY_OBJS) $(LINKFLAGS)
#	$(LINK) $(INCLUDES) $(CFLAGS) $(DLL_OUTPUT_OPTION)$@ $(BUDDY_SRCS) -MLd -LDd -Zi /link /libpath:$(JDK_ROOT)/lib 

$(CUDD_DLL_NAME): $(CUDD_OBJS)
	$(LINK) $(DLL_OUTPUT_OPTION)$@ $(CUDD_OBJS) $(LINKFLAGS)

$(CAL_DLL_NAME): $(CAL_OBJS)
	$(LINK) $(DLL_OUTPUT_OPTION)$@ $(CAL_OBJS) $(LINKFLAGS)

buddy_jni.o: $(BUDDY_INCLUDE)

cudd_jni.o: $(CUDD_INCLUDE)

cal_jni.o: $(CAL_INCLUDE)

$(CAL_OBJS): %.o: %.c
	$(CC) $(CAL_CFLAGS) $(INCLUDES) -c $(OBJECT_OUTPUT_OPTION)$@ $<

$(CUDD_OBJS): %.o: %.c
	$(CC) $(CUDD_CFLAGS) $(INCLUDES) -c $(OBJECT_OUTPUT_OPTION)$@ $<

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) -c $(OBJECT_OUTPUT_OPTION)$@ $<

$(BUDDY_INCLUDE): $(BUDDY_CLASSFILE)
	$(JAVAH) -jni -o $(BUDDY_INCLUDE) $(BUDDY_CLASSNAMES)

$(CUDD_INCLUDE): $(CUDD_CLASSFILE)
	$(JAVAH) -jni -o $(CUDD_INCLUDE) $(CUDD_CLASSNAMES)

$(CAL_INCLUDE): $(CAL_CLASSFILE)
	$(JAVAH) -jni -o $(CAL_INCLUDE) $(CAL_CLASSNAMES)

%.class: %.java
	$(JAVAC) -classpath $(CLASSPATH) $(JAVA_SOURCES)

$(EXAMPLE_CLASSFILES): $(EXAMPLE_SOURCES)
	$(JAVAC) -classpath $(CLASSPATH) $(EXAMPLE_SOURCES)

examples: $(EXAMPLE_CLASSFILES)

javadoc: $(JAVA_SOURCES)
	$(JAVADOC) -d javadoc -breakiterator $(JAVA_PACKAGES)

jar: $(JAR_NAME)

$(JAR_NAME): $(BUDDY_CLASSFILE) $(CUDD_CLASSFILE) $(CAL_CLASSFILE) $(JAVA_SOURCES:%.java=%.class) $(EXAMPLE_CLASSFILES)
	$(JAR) cfm $(JAR_NAME) javabddManifest $(JAVA_CLASSFILES) $(EXAMPLE_CLASSFILES)

pdo:
	icl -Qprof_gen $(INCLUDES) $(CFLAGS) $(DLL_OUTPUT_OPTION)$(BUDDY_DLL_NAME) $(BUDDY_SRCS) -LD /link /libpath:$(JDK_ROOT)/lib 
	$(JAVA) NQueens 12
	icl -Qprof_use $(INCLUDES) $(CFLAGS) $(DLL_OUTPUT_OPTION)$(BUDDY_DLL_NAME) $(BUDDY_SRCS) -LD /link /libpath:$(JDK_ROOT)/lib 
	$(JAVA) NQueens 12

ipo:	buddy_jni.h
	icc -D_REENTRANT -D_GNU_SOURCE -O2 -Ob2 -ipo $(INCLUDES) -shared -static $(DLL_OUTPUT_OPTION)libbuddy.so $(BUDDY_SRCS)

pdogen: buddy_jni.h
	icc -D_REENTRANT -D_GNU_SOURCE -O2 -Ob2 -prof_gen $(INCLUDES) -shared -static $(DLL_OUTPUT_OPTION)libbuddy.so $(BUDDY_SRCS)

pdouse: buddy_jni.h
	icc -D_REENTRANT -D_GNU_SOURCE -O2 -Ob2 -prof_use -ipo $(INCLUDES) -shared -static $(DLL_OUTPUT_OPTION)libbuddy.so $(BUDDY_SRCS)

opt_report:
	icl -fast -Qopt_report_fileOPTREPORT.txt -Qopt_report_phase all -Qopt_report_levelmax -Qvec_report3 -Qpar_report3 $(INCLUDES) $(CFLAGS) /obuddy.dll $(BUDDY_SRCS) -LD /link /libpath:$(JDK_ROOT)/lib 

test:	$(EXAMPLE_CLASSFILES)
	$(JAVA) NQueens 8
	$(JAVA) -Dbdd=cudd NQueens 8
	$(JAVA) -Dbdd=cal NQueens 8
	$(JAVA) -Dbdd=java NQueens 8
	$(JAVA) -Dbdd=jdd -cp $(CLASSPATH) NQueens 8

clean:
	$(RM) -f $(JAVA_CLASSFILES) $(BUDDY_INCLUDE) $(CUDD_INCLUDE) $(CAL_INCLUDE) $(BUDDY_OBJS) $(CAL_OBJS) $(CUDD_OBJS) $(DLL_NAME) $(EXAMPLE_CLASSFILES) $(JAR_NAME)
	$(RM) -rf javadoc

update:
	( export CVS_RSH=ssh ; cvs update -Pd )

commit:
	( export CVS_RSH=ssh ; cvs commit )

empty := 
space := $(empty) $(empty)
