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

if test x"$debugit" = x"yes"; then
    AC_DEFINE([CONF_DEBUG],[],[Debug Mode])
    AM_CXXFLAGS="-g3 -O0"
else
    AC_DEFINE([CONF_NODEBUG],[],[No-debug Mode])
    AM_CXXFLAGS="-O2"
fi

AC_SUBST([AM_CXXFLAGS])

AC_CONFIG_FILES([Makefile])
AC_CONFIG_FILES([src/Makefile])

AC_OUTPUT
