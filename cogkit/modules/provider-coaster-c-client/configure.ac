AC_PREREQ([2.68])
AC_INIT(coaster-c-client, 1.0)
AC_CONFIG_AUX_DIR(config)
AC_CONFIG_SRCDIR(src/CoasterClient.cpp)
#AC_CONFIG_HEADERS([config.h])
AC_CONFIG_MACRO_DIR([m4])

AM_INIT_AUTOMAKE

LT_INIT
AC_LIBTOOL_CXX


# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_CPP
AC_PROG_INSTALL
AC_PROG_LN_S
AC_PROG_MAKE_SET
AC_PROG_RANLIB
# AC_PROG_CXX sets these to -g -O2 without asking questions, and there are answers
CXXFLAGS=

# Checks for header files.
AC_CHECK_HEADERS([arpa/inet.h fcntl.h netdb.h netinet/in.h stdlib.h string.h sys/socket.h sys/time.h unistd.h getopt.h])

# Libs
AC_CHECK_LIB(pthread, pthread_create)

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_TYPE_SIZE_T

# Checks for library functions.
AC_FUNC_MALLOC
AC_CHECK_FUNCS([gettimeofday memset select socket strerror strrchr])

# debug compilation support

AC_MSG_CHECKING([whether to build with debug information])
AC_ARG_ENABLE([debug],
    [AS_HELP_STRING([--enable-debug],
        [enable debugging symbols and disable compiler optimizations (def=no)])],
    [debugit="$enableval"],
    [debugit=no])
AC_MSG_RESULT([$debugit])

AM_CXXFLAGS="-Wall"
if test x"$debugit" = x"yes"; then
    AC_DEFINE([CONF_DEBUG],[],[Debug Mode])
    AM_CXXFLAGS+=" -g3 -O0"
else
    AC_DEFINE([CONF_NODEBUG],[],[No-debug Mode])
    AM_CXXFLAGS+= "-O2"
fi

USE_TCL=0
AC_ARG_WITH([tcl], 
    [AS_HELP_STRING([--with-tcl],
        [location of Tcl])], 
    [
        USE_TCL=$withval
        CPPFLAGS="${CPPFLAGS} -I ${USE_TCL}/include"
        AM_CXXFLAGS="${AM_CXXFLAGS} -I ${USE_TCL}/include"
        AC_CHECK_HEADERS([tcl.h])
    ])

USE_TCL_VERSION=0
AC_ARG_WITH([tcl-version], 
    [AS_HELP_STRING([--with-tcl-version],
        [Tcl version: 8.5 or 8.6])], 
    [
        [[ ${USE_TCL} == 0 ]] && \
           AC_MSG_ERROR([Use of Tcl requires --with-tcl])
        USE_TCL_VERSION=$withval
        LDFLAGS="$LDFLAGS -L$USE_TCL/lib"
        AM_LDFLAGS="${AM_LDFLAGS} -L$USE_TCL/lib -Wl,-rpath $USE_TCL/lib" 
        # -l tcl$USE_TCL_VERSION
        AC_CHECK_LIB([tcl$USE_TCL_VERSION],[Tcl_PkgProvide])
        AC_CHECK_FUNC([Tcl_PkgProvide],,[AC_MSG_ERROR([Could not link to Tcl!])])
    ])

USE_SWIG=0
AC_ARG_ENABLE([swig], 
    [AS_HELP_STRING([--enable-swig], 
        [enable use of SWIG and Tcl support])],
    [
        [[ $USE_TCL_VERSION == 0 ]] && \
           AC_MSG_ERROR([Use of SWIG requires --with-tcl-version])
        USE_SWIG=1
        AC_MSG_RESULT([SWIG enabled.])
    ])
AM_CONDITIONAL([SWIG], [test $USE_SWIG = 1])

AC_SUBST([AM_CXXFLAGS])

AC_CONFIG_FILES([Makefile])
AC_CONFIG_FILES([src/Makefile])
AC_CONFIG_FILES([tcl/Makefile])

AC_OUTPUT
