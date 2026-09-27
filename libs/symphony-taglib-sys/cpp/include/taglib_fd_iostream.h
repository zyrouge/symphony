#ifndef SYMPHONY_TAGLIB_FD_IOSTREAM_H
#define SYMPHONY_TAGLIB_FD_IOSTREAM_H

#if defined(__unix__) || defined(__ANDROID__) || defined(__APPLE__)

#include "../../vendor/taglib/bindings/c/tag_c.h"

#ifdef __cplusplus
extern "C"
{
#endif

    TAGLIB_C_EXPORT TagLib_IOStream *taglib_fd_iostream_new(int fd, int read_only);

    TAGLIB_C_EXPORT TagLib_IOStream *taglib_named_fd_iostream_new(int fd,
                                                                  int read_only,
                                                                  const char *name);

#ifdef __cplusplus
}
#endif

#endif // defined(__unix__) || defined(__ANDROID__) || defined(__APPLE__)

#endif /* SYMPHONY_TAGLIB_FD_IOSTREAM_H */
